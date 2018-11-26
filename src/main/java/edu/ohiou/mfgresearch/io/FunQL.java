package edu.ohiou.mfgresearch.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.commons.io.IOUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.belief.Belief;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.lambda.functions.Cons;
import edu.ohiou.mfgresearch.lambda.functions.Func;
import edu.ohiou.mfgresearch.lambda.functions.Pred;
import edu.ohiou.mfgresearch.plan.IPlan;
import edu.ohiou.mfgresearch.plan.IPlanner;
import edu.ohiou.mfgresearch.plan.PlanUtil;
import edu.ohiou.mfgresearch.service.ServiceFinder;
import edu.ohiou.mfgresearch.service.ServiceRegistry;
import edu.ohiou.mfgresearch.service.ServiceUtil;
import edu.ohiou.mfgresearch.service.base.Actor;
import edu.ohiou.mfgresearch.service.base.Grounding;
import edu.ohiou.mfgresearch.service.base.Input;
import edu.ohiou.mfgresearch.service.base.InputGrounding;
import edu.ohiou.mfgresearch.service.base.Output;
import edu.ohiou.mfgresearch.service.base.OutputGrounding;
import edu.ohiou.mfgresearch.service.base.PrefixNSMapping;
import edu.ohiou.mfgresearch.service.base.Service;
import edu.ohiou.mfgresearch.service.base.ServiceGrounding;
import edu.ohiou.mfgresearch.service.base.ServiceProfile;
import edu.ohiou.mfgresearch.service.invocation.ArgBinding;
import edu.ohiou.mfgresearch.service.invocation.DefaultIndividualSupplier;
import edu.ohiou.mfgresearch.service.invocation.ServiceInvoker;

public class FunQL {
	
	static Logger log = LoggerFactory.getLogger(FunQL.class);
	
	List<IPlan> plans = new LinkedList<IPlan>();
	ServiceRegistry registry = new ServiceRegistry();
	Belief belief = new Belief("RDFXML"); //RDFXML is defaulted as the format but should have provision to set from outside
	private Pred<String> isQueryArg = arg->arg.trim().equals("-query");
	private Pred<String> isServiceArg = arg->arg.trim().equals("-service");
	private Pred<String> isBeliefArg = arg->arg.trim().equals("-belief");
	private Pred<String> isKnowledgeArg = arg->arg.trim().equals("-knowledge");
	public Cons<String> parseQueryToPlan = qs->plans.add(new IPlan(qs.trim()));
	public Cons<String> parseServiceToRegistry = ss->registry.addService(new FileInputStream(ss)); //service string is assumed to be a path, but can also be an url
	public Cons<String> parseOntologyToBelief = bs->belief.addTBox(bs.trim()); //is assumed to be from url but can also be from file, may be handled internally by JENA API
	public Cons<String> parseKnowledgeToABox = kb->belief.addABox(kb.trim());	
	
	//utility functions 
	Func<String, Func<String, List<String>>> matchAll = pattern->{
		return
		content->{
			List<String> matches = new LinkedList<String>();
			Matcher match = Pattern.compile(pattern).matcher(content);
			// Find all matches
		    while (match.find()) {
		      matches.add(match.group());
		    }
		    if(matches.size()==0) throw new Exception("Nothing matches the given pattern!");
		    return matches;
		};
	};
	
	Func<String, Func<String, String>> matchAny = pattern->{
		return
		content->{
			List<String> matches = new LinkedList<String>();
			Matcher match = Pattern.compile(pattern).matcher(content);
			// Find all matches
		    while (match.find()) {
		      matches.add(match.group());
		    }
		    if(matches.size()==0) throw new Exception("Nothing matches the given pattern!");
		    return matches.get(0);
		};
	};
	
	//alternative to main(), create an instance of FunQL with setters
	public FunQL addTBox(String url){
		belief.addTBox(url);
		return this;
	}
	
	/**
	 * Either reads the ABox from the given URL
	 * or just create an empty A-Box with the URL
	 * as base IRI
	 * @param url
	 * @return
	 */
	public FunQL addABox(String url){
		belief.addABox(url);
		return this;
	}

	/**
	 * add an axiom with object property or type assertion
	 * to the A-Box
	 * @param subjectURI
	 * @param predicateURI
	 * @param objectURI
	 * @return
	 * @throws Exception 
	 */
	public FunQL addObjectAxiom(String subjectURI, String predicateURI, String objectURI) throws Exception{
		if(belief.getaBox()==null){
			throw new Exception("No assertion (A-Box) is provided! Please add a A-box first.");
		}
		belief.getaBox().createStatement(ResourceFactory.createResource(subjectURI),
										 ResourceFactory.createProperty(predicateURI),
										 ResourceFactory.createResource(objectURI));
		return this;
	}
	
	/**
	 * add an axiom with data property to the A-Box
	 * @param subjectURI
	 * @param predicateURI
	 * @param value
	 * @param dataType
	 * @return
	 */
	public FunQL addValueAxiom(String subjectURI, String predicateURI, String value, RDFDatatype dataType) throws Exception{
		if(belief.getaBox()==null){
			throw new Exception("No assertion (A-Box) is provided! Please add a A-box first.");
		}
		belief.getaBox().createStatement(ResourceFactory.createResource(subjectURI),
										 ResourceFactory.createProperty(predicateURI),
										 ResourceFactory.createTypedLiteral(value, dataType));
		return this;
	}
	
	/**
	 * Utility method to parse content from given url
	 * @param url
	 * @return
	 * @throws IOException 
	 */
	private String parseQueryFromURL(URL url) throws IOException{
		String contents = "";
		BufferedReader reader = new BufferedReader(
		        new InputStreamReader(url.openStream()));
		String inputLine;
        while ((inputLine = reader.readLine()) != null)
            contents += inputLine; 
        reader.close();
        return contents;
	}
	
	/**
	 * Add a query string (URL, file path or raw string)
	 * this method automatically determines the 
	 * variables and type of query
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public IPlan addPlan(String query) throws Exception {
		if(belief.gettBox()==null){
			throw new Exception("No Ontology (T-Box) is provided! Please add a T-box first.");
		}
		
		//parse the query as URL or raw string
		Func<String, String> parseQuery = 
				q->{
					//parse url into content
					String content = 
					Uni.of(q)
						.map(URL::new)
						.map(url->parseQueryFromURL(url))
						.onFailure(e->log.warn("Couldn't read the given query from the URL due to \n"+e.getMessage()
												+"\n will treat the string as raw query!"))
						.get();
					//if content is null then it is a raw string 
			        return content!=null?content:q;
				};	
				
		//extract the var from the complete Funtion string 
	    Func<String, String> extractBindingVar = fs->{
	    	return
	    	Uni.of(fs)
		   	  .map(matchAny.apply("(\\?[a-zA-Z0-9_]+)(?=\\s<-)"))
		   	  .onFailure(e->log.error("The binding variable could not be identified from the function! (should be in format <varname> <- <function>)"))
		   	  .get();
	    };
	    
	  //extract the function from the complete Funtion string 
	    Func<String, String> extractFunction = fs->{
	    	return
	    	Uni.of(fs)
		   	  .map(matchAny.apply("(<-).+"))
		   	  .onFailure(e->log.error("The binding variable could not be identified from the function! (should be in format <varname> <- <function>)"))
		   	  .map(fs1->fs1.replaceAll("<-", ""))
		   	  .map(fs1->fs1.replaceAll("\\s+", ""))
		   	  .get();
	    };
		
		return
		Uni.of(query)
		   .map(parseQuery)
		 //check if the content of the query has a function string
		   .filter(q->matchAny.apply("(FUNCTION|Function|function)").apply(q).length()>0)
		   .map(q->{
			   String funcString =
					   Uni.of(q)
					   	  .map(matchAny.apply("(FUNCTION|Function|function)\\{(.|\n|\r|\t)*\\}"))
					   	  .get();
			   //extract the binding var name
			   String varName = 
			   Uni.of(funcString)
			   	  .map(extractBindingVar)
			   	  .get();
			   String func = 
			   Uni.of(funcString)
			   	  .map(extractFunction)
			   	  .get();
			   
			   //extract var and function string
			   return addPlan(q, varName, func);
		   })
		   .set(p->p.deconstructQuery(belief.gettBox()))
		   .set(p->plans.add(p))
		   .onFailure(e->{
				//no function symbol. can safely pass to IPlan as raw query string
				Uni.of(parseQuery.apply(query))	
				   .map(IPlan::new)
				   .set(p->p.deconstructQuery(belief.gettBox()))
				   .set(p->plans.add(p))
				   .get(); 
				
			})
		   .get();
	}
	
	/**
	 * Add a query string with one unknown data variable, mapped with a function string
	 * @param query
	 * @param var
	 * @param function
	 * @return
	 * @throws Exception
	 */
	public IPlan addPlan(String query, String var, String function) throws Exception{
		IPlan plan = addPlan(query);
		if(plan==null){
			throw new Exception("No Plan is found! Please add a plan first");
		}
		if(plan.getKnownVar().contains(Var.alloc(var))){
			throw new Exception("the variable " + var + " is not an unknown variable!");
		}
		//clean the function string for whitespace
		function.replaceAll("\\s+", "");
		//validate the function string
		Pattern p = Pattern.compile("\\w+\\.\\w+\\(.+\\)");
		Matcher m = p.matcher(function);
		if(!m.matches()){
			throw new Exception("the function " + function + " is not in right format!");
		}
		//find matches
		p = Pattern.compile("[^,()]+");
		m = p.matcher(function);
		List<String> matches = new LinkedList<>();
		while(m.find()){
			matches.add(m.group());
		}
		String[] actors = null;
		String[] args = new String[matches.size()-1];
		for(int i=0; i<matches.size(); i++){
			if(i==0){
				actors = matches.get(0).split(".");
				if(actors.length!=2){
					throw new Exception("the function " + function + " should be in format <classname>.<methodname>(<arg1>,..)!");
				}
			}
			else{
				args[i-1] = matches.get(i);
			}
		}
		//incomplete! need to add input and output, as well as grounding
		registry.addService(registerService(plan, actors[0], actors[1], var, args));
		return plan;
	}
	
	/**
	 * Currrently only accepts Java functions
	 * @param plan
	 * @param source
	 * @param endPoint
	 * @param var
	 * @param args
	 * @return
	 * @throws Exception
	 */
	private Service registerService(IPlan plan, String source, String endPoint, String var, String[] args) throws Exception{
		
		List<ArgBinding> groundings = new LinkedList<>();
		List<ArgBinding> bindings = new LinkedList<>();
		Map<String, String> dataProeprties = new HashMap<>();
		
		//get all triples with dtype property and the arg is as var in object and then get the 
		//get the type of indi variable of type
		BiFunction<BasicPattern, String, String> getParamType = (p, arg) -> {
			Triple t = plan.getDTypeTriples(p, Var.alloc(arg), false).get(0);
			dataProeprties.put(arg, t.getPredicate().getURI()); // save the dataProperty for later against the var
			return plan.getVarTypes(Var.alloc(t.getSubject())).get(0);
		};
 		
		//instantiate the function
		Method func = ServiceUtil.instantiateJavaService(source, endPoint);
		if(func==null){
			throw new Exception("The function is not invocable!");
		}
		
		//collect all the input argument XSD types
		List<RDFDatatype> argTypes = 
				Omni.of(func.getParameterTypes())
					.map(c->getXSDType(c.getName()))
					.toList();
		if(argTypes.size()!=args.length){
			throw new Exception("The function does not have equal number of arguents as specified!");
		}
		
		//collect all the output argument XSD types
		RDFDatatype oArgType = 
				Uni.of(func.getReturnType())
					.map(c->getXSDType(c.getName()))
					.get();		
		
		//collect prefix map from ontology
		//first from supplied ontology
		Map<String, String> pmap = belief.getPrefixMap();
		List<PrefixNSMapping> prefixNSMapping = new LinkedList<>();
		pmap.forEach((p,n)-> prefixNSMapping.add(Uni.of(PrefixNSMapping::new)
													.set(pn->pn.setPrefix(p))
													.set(pn->pn.setNameSpace(n))
													.get()));		
		
		//collect the input ArgBinding
		IntStream.range(1, args.length)
				.forEach(i->Uni.of(ArgBinding::new)
						.set(b->b.setArgPos(1))
						.set(b->b.setParamType(ResourceFactory.createResource(getParamType.apply(plan.getWhereBasicPattern(), args[i]))))
						.set(b->b.setVar(Var.alloc(args[i])))
						.set(b->b.setVarType(argTypes.get(i)))
						.set(b->groundings.add(b))
				);
		
		//create output ArgBinding
		ArgBinding oGrounding = 
		Uni.of(ArgBinding::new)
			.set(b->b.setArgPos(0))
			.set(b->b.setParamType(ResourceFactory.createResource(getParamType.apply(plan.getConstructBasicPattern(), var))))
			.set(b->b.setVar(Var.alloc(var)))
			.set(b->b.setVarType(oArgType))
			.get();
		
		//first create the input binding list i.e. a input param list
		Omni.of(groundings)
			.select(g->bindings.stream().anyMatch(b->b.getParamType().equals(g.getParamType())), g->bindings.add(g));
		
		//Create list of inputs and input groundings
		List<Input> inputs = new LinkedList<>();
		List<InputGrounding> inputGroundings = new LinkedList<>();
		//for each input parameter
		Omni.of(bindings)
			.set(b->Uni.of(Input::new)//create new Input Parameter
						.set(in->in.setParameter(b.getParamType().asNode().getLocalName()))
						.set(in->in.setParameterType(b.getParamType().asNode().getURI()))
						.set(in->inputs.add(in))
						.set(in->Uni.of(InputGrounding::new)//create InputGrounding
									.set(ig->ig.setParameter(in.getParameter()))
									.set(ig->ig.setGrounding(Omni.of(groundings)
																 .filter(g->g.getParamType().equals(b.getParamType()))
																 .map(g-> Uni.of(Grounding::new)
																		 	 .set(igr->igr.setArg(g.getArgPos()))
																		 	 .set(igr->igr.setDataProperty(dataProeprties.get(g.getVar())))//
																		 	 .set(igr->igr.setDataType(g.getVarType().getURI()))
																		 	 .get())
																 .toList()))
									.set(ig->inputGroundings.add(ig))));
		
		//create output param and output groundings
				
		
		//collects output params
		Output output =
			Uni.of(Output::new)
			   .set(o->o.setParameter(oGrounding.getParamType().asNode().getLocalName()))
			   .set(o->o.setParameterType(oGrounding.getParamType().asNode().getURI()))
			   .get();
		
		Uni.of(oGrounding)
			.map(og->Uni.of(Grounding::new)
						.set(g->g.setArg(0))
						.set(g->g.setDataProperty(dataProeprties.get(oGrounding.getVar())))
						.set(g->g.setDataType(oArgType.getURI())));
		
		OutputGrounding outputGrounding = 
				Uni.of(OutputGrounding::new)
					.set(og->og.setParameter(output.getParameter()))
					.get();
		
		
		//collect servicegrounding
		ServiceGrounding serviceGrounding =
		Uni.of(ServiceGrounding::new)
		   .set(sg->sg.setInputGrounding(inputGroundings))
		   .set(sg->sg.setOutputGrounding(outputGrounding))
		   .get()
		;
		
		//create a new Service
		return
		Uni.of(Service::new)
			.set(s->s.setServiceProfile(Uni.of(ServiceProfile::new)//create a new service profile
										   .set(sp->sp.setServiceName(endPoint))
										   .set(sp->sp.setServiceCategory(source))
										   .set(sp->sp.setActor(Uni.of(Actor::new)//create a new Actor
																   .set(a->a.setActorType("java-method"))
																   .set(a->a.setSource(source))
																   .set(a->a.setEndPoint(endPoint))
																   .get()))
										   .set(sp->sp.setPrefixNSMapping(prefixNSMapping))
										   .set(sp->sp.setOutput(output))
										   .set(sp->sp.setInput(inputs))
										   .get()))
			.set(s->s.setServiceGrounding(serviceGrounding))
		   .get();
	}

	private RDFDatatype getXSDType(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
//	Func<String, Func<String, Func<String, Func<String, FunQL>>>> q = tbox->{
//		return abox->{
//			return body->{
//				return head->{
//					FunQL ql = new FunQL();
//					//add the tbox
//					ql.parseOntologyToBelief.accept(tbox);
//					//add the abox
//					ql.parseKnowledgeToABox.accept(abox);
//					//add the query
//					
//					return ql;
//				};				
//			};
//		};
//	};
	
	public static void main(String[] args) throws Exception {	
		String currArg = "";
		log.debug("debug ..");
		log.trace("trace...");
		log.info("Starting to parse query.....");
		
		//create a new FunQL instance
		FunQL q = new FunQL();
		
		for(int i=0;i<args.length;i++){			
			//store currentArg
			if(args[i].startsWith("-") && !currArg.equals(args[i])){
				currArg = args[i];
				continue;
			}			
			final String value = args[i];
			Uni.of(currArg)
			   .select(q.isQueryArg, a->log.trace("Parsing query from " + a))	
			   .select(q.isQueryArg, a->q.parseQueryToPlan.accept(value)) //if the current token is after -query
			   .select(q.isServiceArg, a->log.trace("Parsing service from " + a))
			   .select(q.isServiceArg, a->q.parseServiceToRegistry.accept(value)) // if the token value is after -service 
			   .select(q.isBeliefArg, a->log.trace("Parsing ontology (belief) from " + a))
			   .select(q.isBeliefArg, a->q.parseOntologyToBelief.accept(value)) //if the current token is after -belief	
			   .select(q.isKnowledgeArg, a->log.trace("Parsing knowledge from " + a))
			   .select(q.isKnowledgeArg, a->q.parseKnowledgeToABox .accept(value)); //if the current token is after -knowledge	
			
		}
		log.info("Parsing completed....");
		
		//print parsed query, service, knowledge and belief
		Omni.of(q.plans)
			.map(p->p.getQuery().toString(Syntax.defaultQuerySyntax))
			.set(qs->log.info("Query parsed :: \n"+qs));
		
		Omni.of(q.registry.getServices())
			.set(p->log.info("Service regisered :"+p));
		
		log.info("Belief found : " + q.belief.gettBox().toString());
		log.info("Knowledge found : " + q.belief.getaBox().getNsPrefixURI(""));			
		q.execute();
		
	}

	public void execute() {
		//analyze query and classify
		Omni.of(plans)
			.set(p->p.deconstructQuery(belief.gettBox()))
			.set(p->log.info("Type of plan is "+p.type.toString()));		
		
		Cons<IPlan> executeA1Plan = p->{
			Query selectQuery = PlanUtil.convert2SelectQuery(p.getQuery());
			Function<Query, Table> queryRes = IPlanner.createQueryExecutor(belief.getaBox());
			//display the result, should come from visualization package
			Function<Table, String> display = tab->{
				log.info(tab.toString());
				return "";
			};
			queryRes.andThen(display).apply(selectQuery);
		};	
		
		Cons<IPlan> executeB1Plan = p->{
			Query selectQuery = PlanUtil.convert2SelectQuery(p.getQuery());
			Function<Query, Table> queryRes = IPlanner.createQueryExecutor(belief.getaBox());	
			Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(p.getConstructBasicPattern());
			Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(belief.getaBox());
			BasicPattern updatedPattern = queryRes.andThen(expander).andThen(updater).apply(selectQuery);
			Uni.of(updatedPattern)
				.select(pat->!pat.isEmpty(), pat-> log.info("Successfully updated A-box with the following pattern: \n"+pat.toString()))
				.select(pat->pat.isEmpty(), pat-> log.info("Update could not be applied!"));
		}; 
		
		Cons<IPlan> executeA2Plan = p->{
			ServiceFinder finder = new ServiceFinder(p, belief, registry);
		};
		
		Cons<IPlan> executeB2Plan = p->{
			Query selectQuery = PlanUtil.convert2SelectQuery(p.getQuery());
			Function<Query, Table> queryRes = IPlanner.createQueryExecutor(belief.getaBox());	
			RDFNode oType = ResourceFactory.createResource(p.getUnknownVarType());
			ArgBinding oBind = new ArgBinding();
			oBind.setArgPos(0);
			oBind.setParamType(oType);
			oBind.setVar(p.getUnknownVar());
			ServiceInvoker invoker = new DefaultIndividualSupplier(oBind, belief.getaBox().getNsPrefixURI(""));
			Function<Table, Table> mapUnknownVar = IPlanner.createServiceResultMapper_deafault(invoker);
			
			Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(p.getConstructBasicPattern());
			Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(belief.getaBox());
			BasicPattern updatedPattern = queryRes.andThen(mapUnknownVar).andThen(expander).andThen(updater).apply(selectQuery);
			Uni.of(updatedPattern)
				.select(pat->!pat.isEmpty(), pat-> log.info("Successfully updated A-box with the following pattern: \n"+pat.toString()))
				.select(pat->pat.isEmpty(), pat-> log.info("Update could not be applied!"));
			
		};
		
		Cons<IPlan> executeB2APlan = p->{
			Query selectQuery = PlanUtil.convert2SelectQuery(p.getQuery());
			Function<Query, Table> queryRes = IPlanner.createQueryExecutor(belief.getaBox());	
			
			ServiceFinder servieFinder = new ServiceFinder(p, belief, registry);
			List<Service> servicesFound = servieFinder.findService();
			List<ServiceInvoker> serviceInvoker = servieFinder.createServiceInvoker(servicesFound.get(0));
			Function<Table, Table> mapUnknownVar = IPlanner.createServiceResultMapper(serviceInvoker);
			
			Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(p.getConstructBasicPattern());
			Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(belief.getaBox());
			BasicPattern updatedPattern = queryRes.andThen(mapUnknownVar).andThen(expander).andThen(updater).apply(selectQuery);
			Uni.of(updatedPattern)
				.select(pat->!pat.isEmpty(), pat-> log.info("Successfully updated A-box with the following pattern: \n"+pat.toString()))
				.select(pat->pat.isEmpty(), pat-> log.info("Update could not be applied!"));			
		};
		
		
		Omni.of(plans)
		//if there is no need to match service then just execute the query and return result
			.select(p->p.type==IPlan.PlanType.A1, executeA1Plan)
			.select(p->p.type==IPlan.PlanType.B1, executeB1Plan)	
			.select(p->p.type==IPlan.PlanType.A2, executeA2Plan)
			.select(p->p.type==IPlan.PlanType.B2, executeB2Plan)
			.select(p->p.type==IPlan.PlanType.B2A, executeB2APlan);
	}

}

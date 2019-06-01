package edu.ohiou.mfgresearch.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
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
import edu.ohiou.mfgresearch.service.base.Grounding_;
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
import edu.ohiou.mfgresearch.service.invocation.JavaServiceInvoker;
import edu.ohiou.mfgresearch.service.invocation.ServiceInvoker;

public class FunQL {
	
	static Logger log = LoggerFactory.getLogger(FunQL.class);
	
	static Properties prop =  Uni.of(Properties::new)
			  .set(p->p.load(Uni.of("resources/META-INF/funql.properties")
					   .map(FileInputStream::new)
					   .get()))
			  .get();
	
	List<IPlan> plans = new LinkedList<IPlan>();
	ServiceRegistry registry = new ServiceRegistry();
	Belief belief = new Belief("RDFXML"); //RDFXML is defaulted as the format but should have provision to set from outside
	private Pred<String> isQueryArg = arg->arg.trim().equals("-query");
	private Pred<String> isServiceArg = arg->arg.trim().equals("-service");
	private Pred<String> isBeliefArg = arg->arg.trim().equals("-belief");
	private Pred<String> isKnowledgeArg = arg->arg.trim().equals("-knowledge");
	public Cons<String> parseQueryToPlan = qs->plans.add(new IPlan(parseQueryFromFile(qs.trim())));
	public Cons<String> parseServiceToRegistry = ss->registry.addService(new FileInputStream(ss)); //service string is assumed to be a path, but can also be an url
	public Cons<String> parseOntologyToBelief = bs->belief.addTBox(bs.trim()); //is assumed to be from url but can also be from file, may be handled internally by JENA API
	public Cons<String> parseKnowledgeToABox = kb->belief.addABox(kb.trim());
	private Function<Table, Table> selectPostProcess = tab->{
		return tab;
	};
	private Function<Table, Table> servicePostProcess = tab->{
		return tab;
	};
	public boolean setLocal = false;
	
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

	private boolean querySuccessful = false;
	public boolean isQuerySuccess(){
		return querySuccessful;
	}
	
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
	 * Either reads the ABox from the given URL
	 * or just create an empty A-Box with the URL
	 * as base IRI
	 * @param url
	 * @return
	 */
	public FunQL addABox(Model model){
		belief.addABox(model);
		return this;
	}
	
	/**
	 * Directly add the belief
	 * @param b
	 * @return
	 */
	public FunQL addBelief(Belief b){
		this.belief = b;
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
	private String parseQueryFromURL(String urlString) throws IOException{
		String contents = "";
		BufferedReader reader = new BufferedReader(
		        new InputStreamReader(new URL(urlString).openStream()));
		String inputLine;
        while ((inputLine = reader.readLine()) != null)
            contents += inputLine + "\n"; 
        reader.close();
        return contents;
	}
	
	/**
	 * Utility method to parse content from given url
	 * @param url
	 * @return
	 * @throws IOException 
	 */
	private String parseQueryFromFile(String path) throws IOException{
		String contents = "";
		BufferedReader reader = new BufferedReader(
		        new InputStreamReader(new File(path).toURI().toURL().openStream()));
		String inputLine;
        while ((inputLine = reader.readLine()) != null)
            contents += inputLine + "\n"; 
        reader.close();
        return contents;
	}
	
	public FunQL addPlan(Query query){
		plans.add(new IPlan(query));
		return this;
	}	
	
	/**
	 * Add a query string (URL, file path or raw string)
	 * without any instance for the function,
	 * function is considered to be static or non-instance web service
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public FunQL addPlan(String query) throws Exception {
		return addPlan(query, null);
	}
	
	/**
	 * Add a query string (URL, file path or raw string)
	 * this method automatically determines the 
	 * variables and type of query
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public FunQL addPlan(String query, Object instance) throws Exception {
		if(belief.gettBox()==null){
			log.warn("No Ontology (T-Box) is provided! Please add a T-box first.");
		}
		
		//parse the query as URL or raw string
		Func<String, String> parseQuery = 
			q->{
				//parse url into content
				String contentURL = 
				Uni.of(q)
					.map(q1->parseQueryFromURL(q1))
					.onFailure(e->{
						log.warn("The query source is not a web address and may be a file path");
					})												
					.get();
				if(contentURL!=null) return contentURL;
				//parse file path into content
				String contentFile = 
				Uni.of(q)
					.map(q1->parseQueryFromFile(q1))
					.onFailure(e->{
						log.warn("Couldn't read the given query from the URL due to \n"+e.getMessage()+"\n will treat the string as raw query!");
					})												
					.get();
				//if content is null then it is a raw string 
		        return contentFile!=null?contentFile:q;
			};	
				
		//extract the var from the complete Function string 
	    Func<String, String> extractBindingVar = fs->{
	    	return
	    	Uni.of(fs)
		   	  .map(matchAny.apply("(\\?[a-zA-Z0-9]+)(?=\\s<-)"))
		   	  .onFailure(e->log.error("The binding variable could not be identified from the function! (should be in format <varname> <- <function>)"))
		   	  .get();
	    };
	    
	  //extract the function from the complete Function string 
	    Func<String, String> extractFunction = fs->{
	    	return
	    	Uni.of(fs)
		   	  .map(matchAny.apply("(<-).+"))
		   	  .onFailure(e->log.error("The binding variable could not be identified from the function! (should be in format <varname> <- <function>)"))
		   	  .map(fs1->fs1.replaceAll("<-", ""))
		   	  .map(fs1->fs1.replaceAll("\\s+", ""))
		   	  .map(fs1->fs1.replaceAll("}", ""))
		   	  .get();
	    };
		
		
		Uni.of(query)
		   .map(parseQuery)
		 //check if the content of the query has a function string
		   .filter(q->matchAny.apply("(FUNCTION|Function|function)\\{(.|\n|\r|\t)*\\}").apply(q).length()>0)
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
			   //extract function command string
			   String func = 
			   Uni.of(funcString)
			   	  .map(extractFunction)
			   	  .get();
			   
			   //extract var and function string
			   return addPlan(q.replaceAll("(FUNCTION|Function|function)\\{(.|\n|\r|\t)*\\}", ""), varName, func, instance);
		   })
		   .onFailure(e->{
				//no function symbol. can safely pass to IPlan as raw query string
				Uni.of(parseQuery.apply(query))	
				   .map(IPlan::new)
				   .set(p->p.deconstructQuery(belief.gettBox())) 
				   .set(p->registerIndiMakerService(p, new LinkedList<Var>(){}))
				   .set(p->plans.add(p));				
			});
		return this;
	}
	
	/**
	 * Add a query string with one unknown data variable, mapped with a function string
	 * @param query
	 * @param var
	 * @param function
	 * @return
	 * @throws Exception
	 */
	public FunQL addPlan(String query, String var, String function, Object instance) throws Exception{
		IPlan plan = new IPlan(query);
		log.info("Given query-->\n"+query);
		plan.deconstructQuery(belief.gettBox());
//		if(plan.getKnownVar().contains(Var.alloc(var))){
//			throw new Exception("the variable " + var + " is not an unknown variable!");
//		}
		//clean the function string for whitespace
		function.replaceAll("\\s+", "");
		//validate the function string
		Pattern p = Pattern.compile("[a-zA-Z0-9_]+:[a-zA-Z0-9_]+\\((\\?[a-zA-Z0-9]+)*(,\\?[a-zA-Z0-9]+)*\\)");
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
			//get the source and endpoint 
			if(i==0){
				//check if prefix is used
				if(matches.get(0).contains(":")){
					//for now consider the prefix is mapped to a java namespace
					actors = matches.get(0).split(":");
					if(actors.length!=2){
						throw new Exception("the function " + function + " should be in format <classname>.<methodname>(<arg1>,..)!");
					}	
					//extract the source 
					actors[0] = plan.getQuery().getPrefix(actors[0]);
					actors[0] = actors[0].substring(actors[0].lastIndexOf("/")+1, actors[0].length());
				}
				//otherwise treat the complete string 
				else{
					actors=new String[2];
					int ix = matches.get(0).lastIndexOf(".");
					actors[0] = matches.get(0).substring(0, ix-1);
					actors[1] = matches.get(0).substring(ix+1, matches.get(0).length());
				}
			}
			else{
				//get all the input arguments 
				args[i-1] = matches.get(i);
			}
		}
		//incomplete! need to add input and output, as well as grounding
		//commented in favor of lean 
		//registry.addService(registerService(plan, actors[0], actors[1], var, args), instance);
		plans.add(registerLeanService(plan, actors[0], actors[1], var, args, instance));
		return this;
	}
	
	private IPlan registerLeanService(IPlan plan, String source, String endPoint, String var, String[] args, Object instance) throws Exception{
		
		List<Var> mappedVar = new LinkedList<Var>();
		
		//instantiate the function
		Method func = ServiceUtil.instantiateJavaService(source, endPoint);
		if(func==null) throw new Exception("The function is not invocable!");
		
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
		if(oArgType==null){
			throw new Exception("the output type could not be deciphered from the service");
		}		
		
		List<ArgBinding> groundings = new LinkedList<>();
		//collect the input ArgBinding
		IntStream.range(0, args.length)
				.forEach(i->Uni.of(ArgBinding::new)
						.set(b->b.setArgPos(i))
						.set(b->b.setVar(Var.alloc(args[i].replace("?", ""))))
						.set(b->b.setVarType(argTypes.get(i)))
						.set(b->groundings.add(b))
						.onFailure(e->e.printStackTrace())
				);
		
		//create output ArgBinding
		ArgBinding oGrounding = 
		Uni.of(ArgBinding::new)
			.set(b->b.setArgPos(0))
			.set(b->b.setVar(Var.alloc(var.replace("?", ""))))
			.set(b->b.setVarType(oArgType))
			.get();
		mappedVar.add(Var.alloc(var.replace("?", "")));		
		
		ServiceInvoker invoker = new JavaServiceInvoker(func, instance);
		Omni.of(groundings).set(g->invoker.setInputArgument(g));
		invoker.setOutputArgument(oGrounding);
		plan.setInvoker(invoker);
		
		plan = registerIndiMakerService(plan, mappedVar);
		
		return plan;
	}
	
	private IPlan registerIndiMakerService(IPlan plan, List<Var> varMapped){
		List<Var> unknownVars = plan.getUnknownVars();
		for(Var uv:unknownVars){
			if(!varMapped.contains(uv)){
				ArgBinding osbind = new ArgBinding();
				osbind.setArgPos(0);
				//get variable type
				osbind.setParamType(plan.detectUnknownVariableType(uv));
				
				osbind.setVar(uv); //?c5
				ServiceInvoker defaultSuppl = new DefaultIndividualSupplier(osbind, belief.getaBox().getNsPrefixURI(""));
				plan.setInvoker(defaultSuppl);
			}
		}
		return plan;
	}
	
	/**
	 * Currently only accepts Java functions
	 * @param plan
	 * @param source when source is supplied the end point is a static method, when not it is a lambda function 
	 * @param endPoint either a implicitly supplied method or an automatically generated service name
	 * @param var
	 * @param args
	 * @return
	 * @throws Exception
	 */
	private Service registerService(IPlan plan, String source, String endPoint, String var, String[] args) throws Exception{
		
		List<ArgBinding> groundings = new LinkedList<>();
		Map<String, String> dataProeprties = new HashMap<>();
		
		//get all triples with dtype property and the arg is as var in object and then get the 
		//get the type of individual variable of type
		BiFunction<BasicPattern, String, String> getParamType = (p, arg) -> {
			List<Triple> t = plan.getDTypeTriples(p, Var.alloc(arg.replace("?", "")), false);
			dataProeprties.put(arg, t.get(0).getPredicate().getURI()); // save the dataProperty for later against the var
			return plan.getVarTypes(Var.alloc(t.get(0).getSubject())).get(0);
		};
 		
		//instantiate the function
		Method func = ServiceUtil.instantiateJavaService(source, endPoint);
		if(func==null) throw new Exception("The function is not invocable!");	
		
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
					.onFailure(e->e.printStackTrace(System.out))
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
		IntStream.range(0, args.length)
				.forEach(i->Uni.of(ArgBinding::new)
						.set(b->b.setArgPos(i))
						.set(b->{
							//get all triples with dtype property and the arg is as var in object and then get the 
							//get the type of individual variable of type
							List<Triple> t = plan.getDTypeTriples(plan.getWhereBasicPattern(), Var.alloc(args[i].replace("?", "")), false);
							dataProeprties.put(args[i], t.get(0).getPredicate().getURI()); // save the dataProperty for later against the var
							b.setParamName(t.get(0).getSubject().toString());
							b.setParamType(ResourceFactory.createResource(plan.getVarTypes(Var.alloc(t.get(0).getSubject())).get(0)).asNode());
						})
						.set(b->b.setVar(Var.alloc(args[i].replace("?", ""))))
						.set(b->b.setVarType(argTypes.get(i)))
						.set(b->groundings.add(b))
						.onFailure(e->e.printStackTrace())
				);
		
		//create output ArgBinding
		ArgBinding oGrounding = 
		Uni.of(ArgBinding::new)
			.set(b->b.setArgPos(0))
			.set(b->{
				//get all triples with dtype property and the arg is as var in object and then get the 
				//get the type of individual variable of type
				List<Triple> t = plan.getDTypeTriples(plan.getConstructBasicPattern(), Var.alloc(var.replace("?", "")), false);
				dataProeprties.put(var, t.get(0).getPredicate().getURI()); // save the dataProperty for later against the var
				b.setParamName(t.get(0).getSubject().toString());
				b.setParamType(NodeFactory.createURI(plan.getVarTypes(Var.alloc(t.get(0).getSubject())).get(0)));
			})
			.set(b->b.setVar(Var.alloc(var.replace("?", ""))))
			.set(b->b.setVarType(oArgType))
			.get();
		
		//first create the input binding list i.e. a input param list
		List<ArgBinding> bindings = groundings;
//				groundings.stream()
//						.collect(Collectors.toMap(ArgBinding::getParamType, p->p, (p,q)->p))
//						.values()
//						.stream()
//						.collect(Collectors.toList());
//		Omni.of(groundings)
//			.select(g->bindings.stream().anyMatch(b->b.getParamType().equals(g.getParamType())), g->bindings.add(g));
		
		
		//Create list of inputs and input groundings
		List<Input> inputs = new LinkedList<>();
		List<InputGrounding> inputGroundings = new LinkedList<>();
		//for each input parameter
		Omni.of(bindings)
			.set(b->Uni.of(Input::new)//create new Input Parameter
						.set(in->in.setParameter(b.getParamName()))
						.set(in->in.setParameterType(b.getParamType().getURI()))
						.set(in->inputs.add(in))
						.set(in->Uni.of(InputGrounding::new)//create InputGrounding
									.set(ig->ig.setParameter(in.getParameter()))
									.set(ig->ig.setGrounding(Omni.of(groundings)
																 .filter(g->g.getParamType().equals(b.getParamType()))
																 .map(g-> Uni.of(Grounding::new)
																		 	 .set(igr->igr.setArg(g.getArgPos()))
																		 	 .set(igr->igr.setDataProperty(dataProeprties.get("?"+g.getVar().getVarName())))//
																		 	 .set(igr->igr.setDataType(g.getVarType().getURI()))
																		 	 .get())
																 .toList()))
									.set(ig->inputGroundings.add(ig))));
		
		//create output param and output groundings
				
		
		//collects output params
		Output output =
			Uni.of(Output::new)
			   .set(o->o.setParameter(oGrounding.getParamName()))
			   .set(o->o.setParameterType(oGrounding.getParamType().getURI()))
			   .get();
		
		Grounding_ outGrounding =
		Uni.of(oGrounding)
			.map(og->Uni.of(Grounding_::new)
						.set(g->g.setArg(0))
						.set(g->g.setDataProperty(dataProeprties.get("?"+og.getVar().getVarName())))
						.set(g->g.setDataType(oArgType.getURI()))).get().get()
						;
		
		OutputGrounding outputGrounding = 
				Uni.of(OutputGrounding::new)
					.set(og->og.setParameter(output.getParameter()))
					.set(og->og.setGrounding(Omni.of(outGrounding).toList()))
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
			.set(s->log.info("Service regisered :"+s.toString()))
		   .get();
	}

	private RDFDatatype getXSDType(String name) {
		switch (name) {
		case "double":
			return XSDDatatype.XSDdouble;
		case "java.lang.Double":
			return XSDDatatype.XSDdouble;
		case "integer":
			return XSDDatatype.XSDinteger;
		case "java.lang.Integer":
			return XSDDatatype.XSDinteger;
		case "java.lang.String":
			return XSDDatatype.XSDstring;
		case "[Ljava.lang.String;":
			return XSDDatatype.XSDstring;
		case "org.apache.jena.graph.Node":
			return XSDDatatype.XSDanyURI;
		default:
			return null;
		}
	}
	
	public static void main(String[] args) throws Exception {	
		String currArg = "";
		
		//create a new FunQL instance
		FunQL fq = new FunQL();
		
		for(int i=0;i<args.length;i++){			
			//store currentArg
			if(args[i].startsWith("-") && !currArg.equals(args[i])){
				currArg = args[i];
				continue;
			}			
			final String value = args[i];
			Uni.of(currArg)
			   .select(fq.isQueryArg, a->log.trace("Parsing query from " + a))	
			   .select(fq.isQueryArg, a->fq.parseQueryToPlan.accept(value)) //if the current token is after -query
			   .select(fq.isServiceArg, a->log.trace("Parsing service from " + a))
			   .select(fq.isServiceArg, a->fq.parseServiceToRegistry.accept(value)) // if the token value is after -service 
			   .select(fq.isBeliefArg, a->log.trace("Parsing ontology (belief) from " + a))
			   .select(fq.isBeliefArg, a->fq.parseOntologyToBelief.accept(value)) //if the current token is after -belief	
			   .select(fq.isKnowledgeArg, a->log.trace("Parsing knowledge from " + a))
			   .select(fq.isKnowledgeArg, a->fq.parseKnowledgeToABox .accept(value)); //if the current token is after -knowledge	
			
		}
		log.info("Parsing completed....");
		
		//print parsed query, service, knowledge and belief
		Omni.of(fq.plans)
			.map(p->p.getQuery().toString(Syntax.defaultQuerySyntax))
			.set(qs->log.info("Query parsed :: \n"+qs));
		
		Omni.of(fq.registry.getServices())
			.set(p->log.info("Service regisered :"+p));
		
		log.info("Belief found : " + fq.belief.gettBox().toString());
		log.info("Knowledge found : " + fq.belief.getaBox().toString());		
		
		//analyze query and classify
		Omni.of(fq.plans)
			.set(p->p.deconstructQuery(fq.belief.gettBox()))
			.set(p->log.info("Type of plan is "+p.type.toString()));		
		
		fq.execute();
		
	}
	
	/**
	 * get the belief
	 * @return
	 */
	public Belief getBelief(){
		return belief;
	}
	
	/**
	 * Return plans
	 * @return
	 */
	public List<IPlan> getPlans(){
		return plans;
	}
	
	/**
	 * Return plan by index (in sequence the plan is added)
	 * @return
	 */
	public IPlan getPlan(int index){
		return plans.get(0);
	}
	
	public PrefixMapping getAllPrefixMapping(){
		 PrefixMapping pm = PrefixMapping.Factory.create();
		 //getall prefix 
		 Map<String, String> nsMap = belief.gettBox().asGraphModel().getNsPrefixMap();
		 nsMap.putAll(belief.getaBox().size()>0?belief.getaBox().getNsPrefixMap():new HashMap<String, String>());
		 nsMap.putAll(belief.getLocalABox().size()>0?belief.getLocalABox().getNsPrefixMap():new HashMap<String, String>());
		 pm.setNsPrefixes(nsMap);
		 return pm;
	}
	
	public void setSelectPostProcess(Function<Table, Table> selectPostProcess){
		this.selectPostProcess = selectPostProcess;
	}

	public void setServicePostProcess(Function<Table, Table> servicePostProcess){
		this.servicePostProcess = servicePostProcess;
	}
	
	public FunQL execute() {
		
		Cons<IPlan> executeA1Plan = p->{
//			Query selectQuery = PlanUtil.convert2SelectQuery(p.getQuery());
			Function<Query, Table> queryRes = p.getBinding()==null?
												IPlanner.createQueryExecutor(belief.getaBox()):
												IPlanner.createQueryExecutorWithBind(belief.getaBox(), p.getBinding());	
			queryRes.andThen(selectPostProcess).apply(p.getQuery());
		};	
		
		Cons<IPlan> executeB1Plan = p->{
//			Query selectQuery = PlanUtil.convert2SelectQuery(p.getQuery());
			Function<Query, BasicPattern> queryRes = p.getBinding()==null?
												IPlanner.createConstructExecutor(belief.getaBox()):
												IPlanner.createConstructExecutorWithBind(belief.getaBox(), p.getBinding());	
//			Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(p.getConstructBasicPattern());
			Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(setLocal?belief.getLocalABox():belief.getaBox());
			BasicPattern updatedPattern = queryRes.andThen(updater).apply(p.getQuery());
			Uni.of(updatedPattern)
				.select(pat->!pat.isEmpty(), pat-> log.info("Successfully updated A-box with the following pattern: \n"+belief.writePattern(pat)))
				.select(pat->pat.isEmpty(), pat-> log.info("Update could not be applied!"));
		}; 
		
		Cons<IPlan> executeA2Plan = p->{
			ServiceFinder finder = new ServiceFinder(p, belief, registry);
		};
		
		Cons<IPlan> executeB2Plan = p->{
			Query selectQuery = PlanUtil.convert2SelectQuery(p.getQuery());
			Function<Query, Table> queryRes = p.getBinding()==null?
												IPlanner.createQueryExecutor(belief.getaBox()):
												IPlanner.createQueryExecutorWithBind(belief.getaBox(), p.getBinding());	
			Node oType = NodeFactory.createURI(p.getUnknownVarType());
			ArgBinding oBind = new ArgBinding();
			oBind.setArgPos(0);
			oBind.setParamType(oType);
			oBind.setVar(p.getUnknownVar());
			ServiceInvoker invoker = new DefaultIndividualSupplier(oBind, belief.getaBox().getNsPrefixURI(""));
			Function<Table, Table> mapUnknownVar = IPlanner.createServiceResultMapper_deafault(invoker);
			
			Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(p.getConstructBasicPattern());
			Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(setLocal?belief.getLocalABox():belief.getaBox());
			BasicPattern updatedPattern = queryRes.andThen(mapUnknownVar).andThen(expander).andThen(updater).apply(selectQuery);
			Uni.of(updatedPattern)
				.select(pat->!pat.isEmpty(), pat-> log.info("Successfully updated A-box with the following pattern: \n"+belief.writePattern(pat)))
				.select(pat->pat.isEmpty(), pat-> log.info("Update could not be applied!"));
			
		};
		
		Cons<IPlan> executeB2APlan = p->{
			Query selectQuery = PlanUtil.convert2SelectQuery(p.getQuery());
			Function<Query, Table> queryRes = p.getBinding()==null?
												IPlanner.createQueryExecutor(belief.getaBox()):
												IPlanner.createQueryExecutorWithBind(belief.getaBox(), p.getBinding());	
			
			Function<Table, Table> mapUnknownVar;
			//for now it is considered that B2A plans need no function as there is no unkown data variable in the construct
//			if(registry.getServices().size()==0){
				Node oType = NodeFactory.createURI(p.getUnknownVarType());
				ArgBinding oBind = new ArgBinding();
				oBind.setArgPos(0);
				oBind.setParamType(oType);
				oBind.setVar(p.getUnknownVar());
				ServiceInvoker invoker = new DefaultIndividualSupplier(oBind, belief.getaBox().getNsPrefixURI(""));
				mapUnknownVar = IPlanner.createServiceResultMapper_deafault(invoker);
//			}
//			else{
//				ServiceFinder servieFinder = new ServiceFinder(p, belief, registry);
//				List<Service> servicesFound = servieFinder.findService();
//				
//				List<ServiceInvoker> serviceInvoker = servieFinder.createServiceInvoker(servicesFound.get(0));
//				mapUnknownVar = IPlanner.createServiceResultMapper(serviceInvoker);
//			}			
			
			Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(p.getConstructBasicPattern());
			Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(setLocal?belief.getLocalABox():belief.getaBox());
			BasicPattern updatedPattern = queryRes.andThen(mapUnknownVar).andThen(expander).andThen(updater).apply(selectQuery);
			Uni.of(updatedPattern)
				.select(pat->!pat.isEmpty(), pat-> log.info("Successfully updated A-box with the following pattern: \n"+belief.writePattern(pat)))
				.select(pat->pat.isEmpty(), pat-> log.info("Update could not be applied!"));			
		};
		
		Cons<IPlan> executeB2CPlan = p->{
			Query selectQuery = PlanUtil.convert2SelectQuery(p.getQuery());
			Function<Query, Table> queryRes = p.getBinding()==null?
													IPlanner.createQueryExecutor(belief.getaBox()):
													IPlanner.createQueryExecutorWithBind(belief.getaBox(), p.getBinding());	;	
			
			ServiceFinder servieFinder = new ServiceFinder(p, belief, registry);
			List<Service> servicesFound = servieFinder.findService();
			
			List<ServiceInvoker> serviceInvoker = servieFinder.createServiceInvoker(servicesFound.get(0));
			Function<Table, Table> mapUnknownVar = IPlanner.createServiceResultMapper(serviceInvoker);
			
			Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(p.getConstructBasicPattern());
			Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(setLocal?belief.getLocalABox():belief.getaBox());
			BasicPattern updatedPattern = queryRes.andThen(mapUnknownVar).andThen(expander).andThen(updater).apply(selectQuery);
			Uni.of(updatedPattern)
				.select(pat->!pat.isEmpty(), pat-> log.info("Successfully updated A-box with the following pattern: \n"+belief.writePattern(pat)))
				.select(pat->pat.isEmpty(), pat-> log.info("Update could not be applied!"));			
		};
		
		Cons<IPlan> executeLeanPlan = p->{
			Query selectQuery = PlanUtil.convert2SelectQuery(p.getQuery());
			//function to execute query
			Function<Query, Table> queryRes = p.getBinding()==null?
													IPlanner.createQueryExecutor(belief.getaBox()):
													IPlanner.createQueryExecutorWithBind(belief.getaBox(), p.getBinding());
			//function to print select result										
			Function<Table, Table> printSelectResult = 
					tab->Uni.of(tab)
							.select(res->res.rows().hasNext(),res->log.info("query returned result!"))
							.select(res->res.rows().hasNext(),res->querySuccessful=true)
							.select(res->!res.rows().hasNext(),res->log.info("query didn't returned any result!"))
							.select(res->res.rows().hasNext(), res-> log.info(belief.writeTable(res)))
							.get();
														
			BasicPattern updatedPattern = null;			
			List<ServiceInvoker> serviceInvoker = p.getInvoker();
			if(serviceInvoker.size()>0){
				Function<Table, Table> mapUnknownVar = IPlanner.createServiceResultMapper(serviceInvoker);			
				Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(p.getConstructBasicPattern());
				Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(setLocal?belief.getLocalABox():belief.getaBox());
				updatedPattern = queryRes.andThen(printSelectResult)
						 				 .andThen(selectPostProcess)
										 .andThen(mapUnknownVar)
										 .andThen(servicePostProcess)
										 .andThen(expander)
										 .andThen(updater)
										 .apply(selectQuery);				
			}
			else{
				//if construct query
				if(p.type.toString().contains("B")){
					Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(setLocal?belief.getLocalABox():belief.getaBox());			
					Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(p.getConstructBasicPattern());
					updatedPattern = queryRes.andThen(printSelectResult)
					 						 .andThen(selectPostProcess)
											 .andThen(servicePostProcess)
							 				 .andThen(expander)
							 				 .andThen(updater)
							 				 .apply(selectQuery);					
				}else{ //for now there is no function for plain old select query
					queryRes.andThen(printSelectResult)
					 		.andThen(selectPostProcess)
					 		.apply(p.getQuery());					
				}
			}
			Uni.of(updatedPattern)
				.select(pat->!pat.isEmpty(), pat-> log.info("Successfully updated A-box with the following pattern: \n"+belief.writePattern(pat)))
				.select(pat->pat.isEmpty(), pat-> log.info("Update could not be applied!"));			
		};
		
		for(IPlan plan:plans){
				Uni.of(plan)
				//if there is no need to match service then just execute the query and return result
	//				.select(p->p.type==IPlan.PlanType.A1, executeA1Plan)
	//				.select(p->p.type==IPlan.PlanType.B1, executeB1Plan)	
	//				.select(p->p.type==IPlan.PlanType.A2, executeA2Plan)
	//				.select(p->p.type==IPlan.PlanType.B2, executeB2Plan)
	//				.select(p->p.type==IPlan.PlanType.B2A, executeB2APlan)
	//				.select(p->p.type==IPlan.PlanType.B2C, executeB2CPlan)
					.set(executeLeanPlan)
					.onFailure(e->e.printStackTrace());
		}
		return this;
	}

}

package edu.ohiou.mfgresearch.io;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.belief.Belief;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.lambda.functions.Func;
import edu.ohiou.mfgresearch.plan.IPlan;
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

public class M {
	
	static Logger log = LoggerFactory.getLogger(M.class);
	Belief belief = new Belief("RDFXML");
	IPlan plan;
	ServiceRegistry registry = new ServiceRegistry();
	
	public static M create(){
		return new M();
	}
	
	public M addTBox(String url){
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
	public M addABox(String url){
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
	public M addObjectAxiom(String subjectURI, String predicateURI, String objectURI) throws Exception{
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
	public M addValueAxiom(String subjectURI, String predicateURI, String value, RDFDatatype dataType) throws Exception{
		if(belief.getaBox()==null){
			throw new Exception("No assertion (A-Box) is provided! Please add a A-box first.");
		}
		belief.getaBox().createStatement(ResourceFactory.createResource(subjectURI),
										 ResourceFactory.createProperty(predicateURI),
										 ResourceFactory.createTypedLiteral(value, dataType));
		return this;
	}
	
	/**
	 * Add a query string
	 * this method automatically determines the 
	 * variables and type of query
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public M addPlan(String query) throws Exception{
		if(belief.gettBox()==null){
			throw new Exception("No Ontology (T-Box) is provided! Please add a T-box first.");
		}
		Uni.of(query)
		   .map(IPlan::new)
		   .set(p->p.deconstructQuery(belief.gettBox()))
		   .onSuccess(p->plan = p);
		return this;
	}
	
	public M addService(String var, String function) throws Exception{
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
		registry.addService(createJavaService(actors[0], actors[1], var, args));
		return this;
	}
	
	private Service createJavaService(String source, String endPoint, String var, String[] args) throws Exception{
		
		Map<String, String> paramMap	= new HashMap<>();
		
		//instantiate the function
		Method func = ServiceUtil.instantiateJavaService(source, endPoint);
		if(func==null){
			throw new Exception("The function is not invocable!");
		}
		
		//collect all the input argument XSD types
		List<String> argTypes = 
				Omni.of(func.getParameterTypes())
					.map(c->c.getName())
					.toList();
		if(argTypes.size()!=args.length){
			throw new Exception("The function does not have equal number of arguents as specified!");
		}
		
		//collect all the input argument XSD types
		String oArgTypes = 
				Uni.of(func.getReturnType())
					.map(c->c.getName())
					.get();		
		
		//collect prefix map from ontology
		//first from supplied ontology
		Map<String, String> pmap = belief.getPrefixMap();
		List<PrefixNSMapping> prefixNSMapping = new LinkedList<>();
		pmap.forEach((p,n)-> prefixNSMapping.add(Uni.of(PrefixNSMapping::new)
													.set(pn->pn.setPrefix(p))
													.set(pn->pn.setNameSpace(n))
													.get()));		
		
		//collect input params
		List<Input> inputs = new LinkedList<>();
		IntStream.range(1, args.length)
			.forEach(i->Uni.of(Input::new)
							.set(in->in.setParameter("in_param"+i))
							.set(in->{
								String pType = getParamType(plan.getWhereBasicPattern(), args[i]);
								paramMap.put(args[i], pType);
								in.setParameterType(pType);
							})
							.set(in->inputs.add(in)));		
		
		//collects output params
		Output output =
		Uni.of(Output::new)
		   .set(o->o.setParameter("out_param1"))
		   .set(o->{
			   String pType = getParamType(plan.getConstructBasicPattern(), var);
			   paramMap.put(var, pType);
			   o.setParameterType(pType);
		   })
		   .get();
		
		List<InputGrounding> InputGroundings = new LinkedList<>();
		List<Grounding> grounding = new LinkedList<>();
		
		IntStream.range(1, args.length)
				.forEach(i->Uni.of(Input::new)
								.set(in->in.setParameter("in_param"+i))
								.set(in->{
									String pType = getParamType(plan.getWhereBasicPattern(), args[i]);
									paramMap.put(args[i], pType);
									in.setParameterType(pType);
								})
								.set(in->inputs.add(in)));	
//		Uni.of(Grounding::new)
//		   .set(ig->ig.setArg(1))
//		   .set(ig->ig.setDataProperty(dataProperty))
//		   .set(ig->ig.setDataType(dataType));
		
		IntStream.range(1, args.length)
				.forEach(i->Uni.of(InputGrounding::new)
								.set(in->in.setParameter("in_param"+i))
								.set(in->in.setGrounding(grounding))
								.set(in->InputGroundings.add(in)));
		
		OutputGrounding outputGrounding = null;
		
		
		//collect servicegrounding
		ServiceGrounding serviceGrounding =
		Uni.of(ServiceGrounding::new)
		   .set(sg->sg.setInputGrounding(InputGroundings))
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

	private String getParamType(BasicPattern p, String arg) {
		//get all triples with dtype property and the arg is as var in object
		Triple t = plan.getDTypeTriples(p, Var.alloc(arg), false).get(0);
		//get the type of indi variable of type 
		return plan.getVarTypes(Var.alloc(t.getSubject())).get(0);
	}
	
}

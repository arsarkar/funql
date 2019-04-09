package edu.ohiou.mfgresearch.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.belief.Belief;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.plan.IPlan;
import edu.ohiou.mfgresearch.plan.PlanUtil;
import edu.ohiou.mfgresearch.plan.IPlan.PlanType;
import edu.ohiou.mfgresearch.service.base.Input;
import edu.ohiou.mfgresearch.service.base.Output;
import edu.ohiou.mfgresearch.service.base.OutputGrounding;
import edu.ohiou.mfgresearch.service.base.Service;
import edu.ohiou.mfgresearch.service.invocation.ArgBinding;
import edu.ohiou.mfgresearch.service.invocation.DefaultIndividualSupplier;
import edu.ohiou.mfgresearch.service.invocation.ServiceInvoker;

public class ServiceFinder {
	
	static Logger log = LoggerFactory.getLogger(ServiceFinder.class);
	
	//constant for namespace
	public static String SERVICECLASS = "http://www.daml.org/services/owl-s/1.2/Service.owl#Service";
	public static String xsdPrefix = "xsd:";
	
	IPlan p;
	Belief b;
	ServiceRegistry reg;
//	Map<String, Individual> anons = new HashMap<String, Individual>();
	List<Service> matchedServices = new LinkedList<Service>(); //matched services, not dynamically changed once instantiated 
	List<Map<String, Var>> inParamBindings = new LinkedList<Map<String, Var>>(); //list of parameter<->Var bindings (same index of matched service)
	List<Map<String, Var>> outParamBindings = new LinkedList<Map<String, Var>>(); //list of output parameter<->Var binding (one expected) (same index of matched service)
	List<ArgBinding> outParamGroundings = new LinkedList<ArgBinding>();//list of output arg grounding (same index of matched service)
	List<List<ArgBinding>> inParamGroundings = new LinkedList<List<ArgBinding>>();//list of parameter<->Var bindings (same index of matched service)
	
	public ServiceFinder(IPlan plan, Belief belief, ServiceRegistry registry) {
		this.p = plan;
		this.b = belief;
		this.reg = registry;
	}
	
	/**
	 * get the input parameter bindings 
	 * @return
	 */
	public Map<String, Var> getInParamBinding(Service s) {
		return inParamBindings.get(matchedServices.indexOf(s));
	}
	
	/**
	 * Finding a service by matching the input and output types
	 * this will create input and output param grounding 
	 * no need to call this function when explicit grounding is provided
	 * @return
	 */
	public List<Service> findService(){
		
		findServiceMatchngOutput();
		findServiceMatchingInput();	
		
		log.info("Services found-->");
		matchedServices.forEach(s->log.info(s.toString()));
		return matchedServices;
	}	
	
	/**
	 * Find a service which matches the output type of the service
	 * @param outArgBinding
	 * @param bind
	 * @return
	 */
	public void findServiceMatchngOutput(){
		Map<String, Var> outParamBinding = new HashMap<String, Var>();
//		Var outputVar = Omni.of(p.getUnknownVars())
//							.find(v->p.getIndiVars().contains(v)) // only one output var is permitted (this means one instance of ServiceInvoker is tied to this output indi variable)
//							.get();
		List<Var> oVars = Omni.of(p.getUnknownVars())
							  .filter(v->p.getIndiVars().contains(v))
							  .toList();
		List<Service> serviceList = Omni.of(reg.getServices()).map(sn->reg.getService(sn)).toList();
		
//		List<Service> servMatchOType = new LinkedList<Service>();
		for(Var ov:oVars){
			List<String> oTypes = p.getVarTypes(ov);
			for(String c:oTypes){
				for(Service s: serviceList){
					Output out = s.getServiceProfile().getOutput();
					IRI servOType = out.getParameterType().contains("http")?
							IRI.create(out.getParameterType()):
								ServiceUtil.mapIRI(s, out.getParameterType());
							if(servOType.equals(IRI.create(c))){
								if(!matchedServices.contains(s)){
									matchedServices.add(s);
									outParamBinding.put(out.getParameter(), ov);
								}
							}
				}
			}			
		}
		outParamBindings.add(outParamBinding);
	}
	
	
	
//	/**
//	 * Filter services from the given output type
//	 * @param iri
//	 * @return
//	 */
//	public List<Service> filterServiceonOutputType(List<Service> services, String iri){
//		return
//		Omni.of(services)
//			.filter(s->{
//				return ServiceUtil.mapIRI(s, s.getServiceProfile().getOutput().getParameterType()).equals(iri);
//			})
//			.toList();
//	}
	
	public void findServiceMatchingInput(){
		
		List<Var> knownVar = Omni.of(p.getKnownVar())
								.filter(v->p.getIndiVars().contains(v))
								.toList();				
		List<Service> serviceList = Omni.of(reg.getServices()).map(sn->reg.getService(sn)).toList();
		List<List<String>> knownVarTypes = new LinkedList<List<String>>(); //get it before hand from IPlan analysis
		knownVar.forEach(v->{
			knownVarTypes.add(p.getVarTypes(v));
		});		
		
		//make all combination  
		List<List<Var>> varComb = PlanUtil.combinations(knownVar);
		
		for(int combi=0; combi< varComb.size(); combi++){
			
			List<Var> comb = varComb.get(combi); 
			
			//for each combination find if any service matches 
			for(int servi=0; servi<serviceList.size(); servi++){
				
				List<Input> inParams = serviceList.get(servi).getServiceProfile().getInput();
				Map<String, Var> inParamBinding = new HashMap<String, Var>(); //pair of param name and var comb
					
				//for each variable try to find if any service parameter matches any of the possible types of known variable
				for(int vari=0; vari<comb.size(); vari++){
					Var v = comb.get(vari);				
					List<String> varTypes = knownVarTypes.get(knownVar.indexOf(v));
					
					//for each vartype match a input parameter type, 
					boolean doBreak = false;
					for(String varType: varTypes){ //need to first check the asserted type and then the inferred type
						for(Input param:inParams){
							IRI ipType = param.getParameterType().contains("http")?IRI.create(param.getParameterType()):ServiceUtil.mapIRI(serviceList.get(servi), param.getParameterType());
							if(IRI.create(varType).equals(ipType)){ //may need to add prefix
								//if matched store the binding
								inParamBinding.put(param.getParameter(), v);
								doBreak = true;
								break;
							}
						}
						if(doBreak) break;
					}
				}
				
				//check if all service parameter is matched
				boolean allMatched = true;
				for(Input param:inParams){
					if(!inParamBinding.containsKey(param.getParameter())){
						allMatched = false;
						break;
					}
				}
				if(allMatched){
					//store the service in the matched service list
					if(!matchedServices.contains(serviceList.get(servi))){
						matchedServices.add(serviceList.get(servi));
						inParamBindings.add(inParamBinding);
					}
					else{
						if(inParamBindings.size()-1>=matchedServices.indexOf(serviceList.get(servi)))
							inParamBindings.set(matchedServices.indexOf(serviceList.get(servi)), inParamBinding);
						else
							inParamBindings.add(inParamBinding);
					}
				}
			}
		}
	}
	

	private RDFNode createDummyLiteral(String dataType) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Create a data type triple with the given variable 
	 * as subject and data property as predicate and 
	 * a new variable presenting a literal as object
	 * 
	 * the new variable is named following the subject Var 
	 * appending a random number between 0 and 99
	 * @param dPropUri
	 * @param param
	 * @param v
	 * @return
	 */
	public Triple createDTypeTriples(String dPropUri, Var v) {
		Random rand = new Random();
		Model m = ModelFactory.createDefaultModel();
		return Triple.create(v.asNode(), 
				  m.createProperty(dPropUri).asNode(), 
				  Var.alloc(v.getName()+rand.nextInt(99)).asNode());
	}
	
	
	/**
	 * case B2A
	 * 	bind service to data grounding, bind default individual for indi var
	 *	Prepare a ServiceInvoker and return for the current services found 
	 * @param service
	 * @return
	 */
	public List<ServiceInvoker> createServiceInvoker(Service service) {
		
		List<ServiceInvoker> invokers = new LinkedList<ServiceInvoker>();
		List<ArgBinding> inParamGrounding = new LinkedList<ArgBinding>(); //list of input grounding (same index of matched service)	
		 //repair Input grounding
		 Map<String, Var> mapping = getInParamBinding(service); //get the param and var mapping
		 for(String param:mapping.keySet()){
			 //find all the datatype triples from where clause which has the parameter in the subject
			 Var v = mapping.get(param);
			 List<Triple> planInPGnds = p.getDTypeTriples(p.getWhereBasicPattern(), v, true);
			 //grounding for the parameter 
			 Omni.of(service.getServiceGrounding().getInputGrounding())
				.filter(ig->{
					return ig.getParameter().equals(param);
					}) //find the grounding for the parameter
				//.set(ig->log.info("grounding for param " + param + " --> " + ig.getGrounding().toString()))
				.set(ig->{
						Omni.of(ig.getGrounding())        //get all the groundings 
							//for each grounding find the triple having the same datatype with the grounding
							//if not present then create new but for both case add the grounding as new argument grounding
							.set(g->{
						 		  	
									//find the triples in the where clause which has the datatype in the grounding as predicate
									String uri = g.getDataProperty().contains("http")?g.getDataProperty():ServiceUtil.mapIRI(service, g.getDataProperty()).getIRIString();
									Triple dTypeT = Omni.of(planInPGnds)
										 		    	.find(t->{
										 		    		return t.getPredicate().getURI().equals(uri);
										 		    	})
										 		    	.get();
									//if not present then create new 
									if(dTypeT==null){
										dTypeT = createDTypeTriples(uri, mapping.get(param));
										p.add2whereBasicPattern(dTypeT);
										log.warn("Datatype triple " + dTypeT.toString() + "is not found in query but added!");
									}
									//add the input grounding in the input grounding
									Var dVar = Var.alloc(dTypeT.getObject());
									RDFDatatype dType = g.getDataType().contains("http")?new XSDDatatype(g.getDataType().substring(g.getDataType().indexOf("#")+1, g.getDataType().length()))
																						:new XSDDatatype(g.getDataType().replaceAll("xsd:", ""));
									ArgBinding iBind = Uni.of(ArgBinding::new)
														  .set(ab->ab.setArgPos(g.getArg())) //set argument position
														  .set(ab->ab.setParamName(ig.getParameter())) //set the parameter
														  .set(ab->ab.setVar(dVar)) //set the variable
														  .set(ab->ab.setVarType(dType)) //set the variable type
														  .onFailure(e->log.error("Error in creating the input param grounding due to "+ e.getMessage()))
														  .get();
									inParamGrounding.add(iBind); // set the XSD type		 		    
						 	  });
				});	
		 }
		 
		 //save the input grounding for the service
		 if(inParamGroundings.size()-1 >= matchedServices.indexOf(service)){
			 inParamGroundings.set(matchedServices.indexOf(service), inParamGrounding);
		 }
		 else{
			 inParamGroundings.add(inParamGrounding);
		 }	
		 
		 log.info("Input grounding for the service " + service.toString());
		 inParamGrounding.forEach(ab->log.info(ab.toString()));
		 
		 //create the output parameter grounding 
		 Map<String, Var> obind = outParamBindings.get(matchedServices.indexOf(service));		 
		 OutputGrounding oGround = service.getServiceGrounding().getOutputGrounding();
		//find all the datatype tripls from where clause which has the indi out var in the subject
		 Var outV = obind.get(oGround.getParameter());
		 String ogDProp = oGround.getGrounding().get(0).getDataProperty();
		 String oURI = ogDProp.contains("http")?ogDProp:ServiceUtil.mapIRI(service, ogDProp).getIRIString();
		 List<Triple> planOutPGnds = p.getDTypeTriples(p.getConstructBasicPattern(), outV, true);
		 Triple oTriple = planOutPGnds.stream()
				 					  .filter(t->t.getPredicate().getURI().equals(oURI))
				 					  .findFirst().get();
		 
		 // if no data type with any of the groundings are not found then add new
		 if(oTriple==null){
			 oTriple = createDTypeTriples(oURI, outV);
			 p.add2constructBasicPattern(oTriple);
			 log.warn("Datatype triple " + oTriple.toString() + "is not found in construct pattern but added!");
		 } 
		 
		 //add output grounding
		 Var ovv = Var.alloc(oTriple.getObject());
		 String ogDType = oGround.getGrounding().get(0).getDataType();
		 RDFDatatype dType = ogDType.contains("http")
				 					?new XSDDatatype(ogDType.substring(ogDType.indexOf("#")+1, ogDType.length()))
				 					:new XSDDatatype(oGround.getGrounding().get(0).getDataType().replaceAll("xsd:", ""));
		 ArgBinding oArgBind = Uni.of(ArgBinding::new)
							  .set(ab->ab.setArgPos(oGround.getGrounding().get(0).getArg())) //set argument position
							  .set(ab->ab.setParamName(oGround.getParameter())) //set the parameter
							  .set(ab->ab.setVar(ovv)) //set the variable
							  .set(ab->ab.setVarType(dType)) //set the variable type
							  .get();
		 //save the output grounding for the service
		 if(outParamGroundings.size()-1 >= matchedServices.indexOf(service)){
			 outParamGroundings.set(matchedServices.indexOf(service), oArgBind);
		 }
		 else{
			 outParamGroundings.add(oArgBind);
		 }
		 
		 log.info("Output grounding for the service " + service.toString());
		 log.info(oArgBind.toString());
		 
		//get the service invoker for the service
		ServiceInvoker invoker = reg.getServiceInvoker(service); 
		 
		//for B2A types plans
		if(p.type==PlanType.B2C){
			//set input groundings 
			inParamGroundings.get(matchedServices.indexOf(service))
							 .forEach(ab->{
				 invoker.setInputArgument(ab);
			});
			//set output grounding
			invoker.setOutputArgument(oArgBind);
			//add the matched service invoker 
			invokers.add(invoker);
			
			//add default individual supplier for the unknown variables
			List<Var> unknownVars = p.getUnknownVars();
			for(Var uv:unknownVars){
				if(!uv.equals(ovv)){
					RDFNode oType = ResourceFactory.createResource(p.getVarTypes(uv).get(0));
					ArgBinding osbind = new ArgBinding();
					osbind.setArgPos(0);
					//osbind.setParamName(oGround.getParameter()); //class5
					osbind.setParamType(oType); //class5 url
					osbind.setVar(uv); //?c5
					ServiceInvoker defaultSuppl = new DefaultIndividualSupplier(osbind, b.getaBox().getNsPrefixURI(""));
					invokers.add(defaultSuppl);
				}
			}			
		}			
		return invokers;
	}
	
	
	
//	public List<Service> filterServiceonInputTypes(List<Service> services, List<Var> vars){
//		
//		return services;
//	}
//	
//	
//	public Map<String, String> matchServiceInputParams(Service s, List<Var> vars){
//		
//		
//		
//		return new HashMap<String, String>();
//	}
	
////	/**
////	 * Creates a graph with precondition type asserted from 
////	 * the query solutions 
////	 * @param querySolution
////	 */
////	public void createPreConditionGraph(Binding querySolution){
////		//create service individual 
////		Individual servInd = createServiceIndividual("PlanService", m);
////	}
//	
//	/**
//	 * create a graph from the service profile and grounding where 
//	 * every parameter is an anonymous individual and every argument is 
//	 * normalized by corresponding data types
//	 * @param s Service
//	 * @param m default ontology used for creating the graph on
//	 * @return
//	 */
//	public List<Graph> createGraphs(Service s, Binding bind) {
//		
//		List<OntModel> models = new ArrayList<OntModel>();
//		models.add(reg.getServiceModel(s.getServiceProfile().getServiceName()));
//		models.add(ModelFactory.createOntologyModel());
//		
//		//get input parameter types for the service
//		Func<Service, Omni<Input>> getInputParams = s0->Omni.of(s.getServiceProfile().getInput());
//		
//		//get output paramter type for the service
//		Func<Service, Output> getOutputType = s0->s0.getServiceProfile().getOutput();
//		
//		//get all input grounings for the given Input
//		Func<Input, Omni<InputGrounding>> getInputGroundings  = ip->Omni.of(s.getServiceGrounding().getInputGrounding())
//			    														.filter(ig->ig.getParameter().equals(ip.getParameter()));
//		
//		//create service individual 
//		Individual servInd = Uni.of(models.get(0).createIndividual(models.get(0).createClass(SERVICECLASS)))
//							   .set(ind->anons.put(s.getServiceProfile().getServiceName(), ind))
//							   .get(); 
//		Individual planInd = Uni.of(models.get(1).createIndividual(models.get(1).createClass(SERVICECLASS)))
//							   .set(ind->anons.put(PLANSERVICE, ind))
//							   .get();
//		
//		//add the input paramters in service graph
//		Omni<Individual> inInds =
//		Uni.of(s)
//		   .fMap(getInputParams) //map to Omni of input parameters
//		   .map(in->addInputParamInd(servInd, in, models.get(0))); //add the input as hasInput property 
//		
//		//add all the input variable types for the plan graph
//		bind.vars().forEachRemaining(v->Uni.of(v)
////										.filter(v0->PlanUtil.isVarGroundedByIndividual(b.gettBox(), p.getQuery(), v0))
//				                        .set(v0->{
//					                        	String ity=
//					                        	PlanUtil.getInputType(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, b.getaBox()), //need an inference model
//					                        						  bind.get(v0).getURI()).getLocalName();
//					                        	addQueryVarInd(planInd, v0.getVarName(), ity, models.get(1));
//				                        		})
//				                        );
//		
//		
//		//now add the data properties for each individuals 
//		Uni.of(s)
//		   .fMap(getInputParams) 
//		   .fMap(getInputGroundings)
//		   .set(ig->addInputArguments(anons.get(ig.getParameter()), ig.getGrounding(), models.get(0)));
//		
//		//add the output paramter 
//		Individual outInd = 
//		Uni.of(s)
//		   .map(getOutputType)
//		   .map(out->addOutputParamInd(servInd, out, models.get(0)))
//		   .get();		
//		
//		return models.stream().map(m->m.getGraph()).collect(Collectors.toList());
//	}
//	
//	/**
//	 * Create an anon indie of the given input parameter type 
//	 * and add to the service indie with hasInput property 
//	 * @param serviceInd
//	 * @param input parameter type
//	 * @param m OntoModel
//	 * @return anon individual created for the input
//	 */
//	public Individual addInputParamInd(Individual serviceInd, Input in, OntModel m){
//		Individual inInd = createParamInd(in.getParameterType(), m);
//		anons.put(in.getParameter(), inInd);
//		serviceInd.addProperty(m.createProperty(HASINPUT), inInd);
//		return inInd;
//	}
//	
//	/**
//	 * Create an anon indie of the given query variable type 
//	 * and add to the service indie with hasInput property 
//	 * @param planInd
//	 * @param varNme query variable name
//	 * @param varType query variable type
//	 * @param m OntoModel
//	 * @return anon individual created for the input
//	 */
//	public Individual addQueryVarInd(Individual planInd, String varName, String varType, OntModel m){
//		Individual inInd = createParamInd(varType, m);
//		anons.put(varName, inInd);
//		planInd.addProperty(m.createProperty(HASINPUT), inInd);
//		return inInd;
//	}
//
//	/**
//	 * Add the groundings as data property to the given input individual
//	 * Only basic primitive types are supported for the time being, 
//	 * in future need to extend to every other XSD data types.
//	 * @param inInd  
//	 * @param grounding Supporrts xsd:double, xsd:string, xsd:int
//	 */
//	public void addInputArguments(Individual inInd, List<Grounding> groundings, OntModel m){
//		Omni.of(groundings)
//			.select(g->g.getDataType().equals("xsd:double"), 
//					g->inInd.addProperty(m.createDatatypeProperty(g.getDataProperty().trim()), m.createTypedLiteral(normDouble, XSDDatatype.XSDdouble)))
//			.select(g->g.getDataType().equals("xsd:int"), 
//					g->inInd.addProperty(m.createDatatypeProperty(g.getDataProperty().trim()), m.createTypedLiteral(normInt, XSDDatatype.XSDint)))
//			.select(g->g.getDataType().equals("xsd:string"), 
//					g->inInd.addProperty(m.createDatatypeProperty(g.getDataProperty().trim()), m.createTypedLiteral(normString, XSDDatatype.XSDstring)))
//		    ;
//	}
//	
//	/**
//	 * Create an anon indie of the given output parameter type 
//	 * and add to the service indie with hasOutput property 
//	 * @param serviceInd service individual
//	 * @param oType output parameter type
//	 * @param m OntoModel
//	 * @return anon individual created for the output
//	 */
//	public Individual addOutputParamInd(Individual serviceInd, Output out, OntModel m){
//		Individual outInd = createParamInd(out.getParameterType(), m);
//		anons.put(out.getParameter(), outInd);
//		serviceInd.addProperty(m.createProperty(HASOUTPUT), outInd);
//		return outInd;
//	}
	
//	/**
//	 * Create an anon indie of type owls:Service 
//	 * @param sn service name
//	 * @param m
//	 * @return
//	 */
//	private Individual createServiceIndividual(String sn, OntModel m){
//		return
//			Uni.of(m.createIndividual(m.createClass(SERVICECLASS)))
//			   .set(ind->anons.put(sn, ind))
//			   .get();
//	}
//	
//	/**
//	 * Create a new anonymous individual of the given type
//	 * @param parameterType
//	 * @param m
//	 * @return
//	 */
//	private Individual createParamInd(String parameterType, OntModel m){
//		return
//		Uni.of(m.createIndividual(m.createClass(parameterType.trim())))
//		   .get();
//	}
	

	
}

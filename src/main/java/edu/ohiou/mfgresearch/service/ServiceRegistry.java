package edu.ohiou.mfgresearch.service;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.jena.graph.Graph;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.lambda.functions.Pred;
import edu.ohiou.mfgresearch.service.base.InputGrounding;
import edu.ohiou.mfgresearch.service.base.Service;
import edu.ohiou.mfgresearch.service.invocation.JavaServiceInvoker;
import edu.ohiou.mfgresearch.service.invocation.ServiceInvoker;

public class ServiceRegistry {	

	static Logger log = LoggerFactory.getLogger(ServiceRegistry.class);
	private Map<String, Service> registry = new HashMap<String, Service>();
	public static final Object JAVA_METHOD = "java-method";
	private Map<String, ServiceInvoker> javaFunctions = new HashMap<String, ServiceInvoker>();
		
	private Pred<Service> isJavaFunction = s->{
		return s.getServiceProfile().getActor().getActorType().equals(JAVA_METHOD);
	};
	
	/**
	 * add service to registry by name if not already added
	 * also instantiate the proxy for the source (e.g. java-method)
	 * @param s
	 */
	public void addService(Service s, Object instance){
		Uni.of(s)
		   .filter(service->!registry.containsKey(service.getServiceProfile().getServiceName()))
		   .set(service->registry.put(service.getServiceProfile().getServiceName(), service))
		   .select(isJavaFunction, service->addJavaFunctionAsService(s, instance));
			
	}
	
	public void addJavaFunctionAsService(Service s, Object instance) {
		Uni.of(s.getServiceProfile().getActor())
		   .map(a->ServiceUtil.instantiateJavaService(a.getSource(), a.getEndPoint()))
		   .set(m->javaFunctions.put(s.getServiceProfile().getServiceName(), new JavaServiceInvoker(m, instance)))
		   .onSuccess(m->log.info("Java method "+m+" is instantiated successsully for service "+ s.toString()));
	}

	/**
	 * Map service from the given service profile in json
	 * this file should be in the Service class hierarchy
	 * @param jsonService
	 */
	public void addService(InputStream jsonService){
		ObjectMapper mapper = new ObjectMapper();
		Uni.of(jsonService)
		   .map(s->mapper.readValue(s, Service.class))
		   .onFailure(e->e.printStackTrace())
		   .onSuccess(s1->addService(s1, null));
	}
	
	public String toString(){
		return registry.toString();
	}
	
	/**
	 * @return list of services in the registry
	 */
	public List<Service> getRegistry(){
		return registry.values().stream().collect(Collectors.toList());
	}
	
	/**
	 * @return list of seervice names in the registry
	 */
	public List<String> getServices(){
		return registry.keySet().stream().collect(Collectors.toList());
	}

	/**
	 * get service for the given service name
	 * @param sn
	 * @return
	 */
	public Service getService(String sn) {
		// TODO Auto-generated method stub
		return registry.get(sn);
	}
	
	public ServiceInvoker getServiceInvoker(Service s){
		return
		Uni.of(s)
		 .filter(isJavaFunction)
		 .map(s1->javaFunctions.get(s1.getServiceProfile().getServiceName()))
		 .get();
	}

	/**
	 * Create a default Ontology for the service graph
	 * with ns mapping already set
	 * @param serviceName
	 * @return
	 */
	public OntModel getServiceModel(String serviceName) {
		Map<String, String> nsMaps=
		registry.get(serviceName)
		        .getServiceProfile()
		        .getPrefixNSMapping()
		        .stream()
		        .collect(Collectors.toMap(px->px.getPrefix(), px->px.getNameSpace()));
		return
		Uni.of(ModelFactory.createOntologyModel())
		   .set(m->m.setNsPrefixes(nsMaps))
		   .get();
	}

}

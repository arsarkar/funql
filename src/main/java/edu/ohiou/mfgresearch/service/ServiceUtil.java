package edu.ohiou.mfgresearch.service;

import java.lang.reflect.Method;
import org.apache.jena.rdf.model.RDFNode;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.service.base.Service;

public class ServiceUtil {
	static Logger log = LoggerFactory.getLogger(ServiceRegistry.class);
//	/**
//	 * Find all the ungrounded services which parameter type and InputGrounding 
//	 * @param iTypeBinds binding of Var and Types where Var are from triples containing ObjectProperty as predicate
//	 * @param groundings argument bindings of Var and Predicate are from triples where the predicate is a data property    
//	 * @param tBox a knowledge base which can serve for further referencing the input type 
//	 * @return List of services matching the search criteria
//	 */
//	public static List<Service> findServiceByInputType(List<String> services, Binding iTypeBinds, Binding groundings, OntModel tBox){		
//		// find the services matching the input types with parameterType
//		Omni.of(services)
//			.select(exactMatchAnyParameter, collectServicehasAtleastOneParamterCommon)
//			;
//		return null;
//	}
	
	/**
	 * Returns a service grounded by the given input and output types
	 * @param serv
	 * @param ity
	 * @param oty
	 * @return
	 */
//	public static NativeService getGroundedNativeService(ProtoServ serv, List<Node> ity, Node oty) {
//		return Uni.of(NativeService::new)
//					.set(s -> s.setService(serv))
//					.set(s->ity.forEach(t->s.setInputParamter(t, 0)))
//					.set(s ->s.setOutputParamter(oty))
//					.get();
//	}s
	
	/**
	 * Returns a supplier of the service result after binding the input type and parameters
	 * @param serv
	 * @param iType
	 * @param input
	 * @return
	 */
//	public static Suppl<Table> bindService(NativeService serv, Binding iType, Binding input) {
//		return ()->{
//			return serv.performService(iType, input);
//		};
//	}
	
	/**
	 * get the IRI of the type of the given parameter type 
	 * @param paramName
	 * @return
	 */
	public static IRI mapIRI(Service s, String node){
		return
		Omni.of(s.getServiceProfile().getPrefixNSMapping())
			.find(ns->ns.getPrefix().equals(node.split(":")[0]))
			.map(ns->ns.getNameSpace()+node.split(":")[1])
			.map(url->IRI.create(url))
			.get();
	}
	
	/**
	 * get the java method for the given source and endpoint
	 * @param source
	 * @param endPoint
	 * @return
	 */
	public static Method instantiateJavaService(String source, String endPoint){
		return
			Uni.of(source)
			   .map(s->Class.forName(s))
			   .fMap(c->Omni.of(c.getMethods()))
			   .find(m->m.getName().equals(endPoint))
			   .onFailure(e->log.info("Failed to instantiate method for source "+ source + " and endpoint "+ endPoint + "\n" + e.getMessage()))
			   .get();
	}

	public static RDFNode createDummyLiteral(String dataType) {
		// TODO Auto-generated method stub
		return null;
	}
	
}

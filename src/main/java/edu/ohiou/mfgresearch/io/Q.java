package edu.ohiou.mfgresearch.io;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import edu.ohiou.mfgresearch.plan.IPlan;

public final class Q {
	
	private IPlan plan;
	
	public Q() {
		plan = new IPlan();
	}
	
	public Q addPrefix(String key, String value){
		
		return this;
	}
	
//	public Q addCondition(String subject, String predicate, String object){
//		Node s = subject.startsWith("?")? Var.alloc(subject):ResourceFactory.createResource(subject).asNode();
//		Node o = object.startsWith("?")? Var.alloc(object):ResourceFactory.createResource(object).asNode();
//		plan.add2whereBasicPattern(new Triple(s, ResourceFactory.createProperty(predicate).asNode(), o));
//		return this;
//	}
//	
//	public Q addConsequent(String subject, String predicate, String object){
//		Node s = subject.startsWith("?")? Var.alloc(subject):ResourceFactory.createResource(subject).asNode();
//		Node o = object.startsWith("?")? Var.alloc(object):ResourceFactory.createResource(object).asNode();
//		plan.add2constructBasicPattern(new Triple(s, ResourceFactory.createProperty(predicate).asNode(), o));
//		return this;
//	}	
	
	public Q createPlan(){
		
		return this;
	}
	
}

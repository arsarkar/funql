/**
 * 
 */
package edu.ohiou.mfgresearch.sparkle;

import static org.junit.Assert.*;

import java.util.stream.IntStream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.impl.GraphMatcher;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import edu.ohiou.mfgresearch.lambda.Uni;
import org.junit.Assert;

/**
 * @author sarkara1
 *
 */
public class RDFGraphEquivalence {

	String ns = "http://example.com/local#";
	OntModel m1, m2, m3, m4, m5;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		m1 =
		Uni.of(ModelFactory.createDefaultModel())
		.map(m->ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, m))
		.set(m->m.setNsPrefix("ns", ns))
		.set(m->{
			OntClass c1 = m.createClass(ns+"C1");
			OntClass c2 = m.createClass(ns+"C2");
			IntStream.range(1,2)
					 .forEach(i->{
						 Individual i1 = m.createIndividual(null, c1);
						 i1.addProperty(m.createDatatypeProperty(ns+"D1"), m.createTypedLiteral(i*10));
						 Individual i2 = m.createIndividual(null, c2);	
						 i2.addProperty(m.createDatatypeProperty(ns+"D2"), m.createTypedLiteral(i*20));
						 ObjectProperty p1 = m.createObjectProperty(ns+"P1");
						 i1.addProperty(p1, i2);
					 });
				
		})
		.set(m->m.write(System.out, RDFFormat.RDFXML.getLang().getLabel()))
		.onFailure(e->e.printStackTrace())
		.get();
		
		m2 =
		Uni.of(ModelFactory.createDefaultModel())
		.map(m->ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, m))
		.set(m->m.setNsPrefix("ns", ns))
		.set(m->{
			OntClass c1 = m.createClass(ns+"C1");
			OntClass c2 = m.createClass(ns+"C2");
			IntStream.range(1, 2)
					 .forEach(i->{
						 Individual i1 = m.createIndividual(null, c1);
						 i1.addProperty(m.createDatatypeProperty(ns+"D1"), m.createTypedLiteral(i*10));
						 Individual i2 = m.createIndividual(null, c2);	
						 i2.addProperty(m.createDatatypeProperty(ns+"D2"), m.createTypedLiteral(i*20));
						 ObjectProperty p1 = m.createObjectProperty(ns+"P1");
						 i1.addProperty(p1, i2);
					 });
				
		})
		.set(m->m.write(System.out, RDFFormat.TURTLE.getLang().getLabel()))
		.onFailure(e->e.printStackTrace())
		.get();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void baseEquivalence() {
		System.out.println("is m1 isomorphic with m2? "+m1.getGraph().isIsomorphicWith(m2.getGraph()));
		System.out.println("is m1 equal with m2? "+GraphMatcher.equals(m1.getGraph(), m2.getGraph()));
		Node[][] nodes = GraphMatcher.match(m1.getGraph(), m2.getGraph());
		for(Node[] nn:nodes){
			for(Node n:nn){
				System.out.print(n.getBlankNodeLabel()+ " ");
			}
			System.out.println();
		}
	}

}

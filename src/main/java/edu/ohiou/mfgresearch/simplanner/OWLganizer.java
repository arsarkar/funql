package edu.ohiou.mfgresearch.simplanner;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.graph.compose.Difference;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import edu.ohiou.mfgresearch.lambda.Uni;

/**
 * An utility class for organizing ontology.
 * So far added:
 * 1) A-Box and T-Box separation
 * 
 * @author sarkara1
 *
 */
public class OWLganizer {
	
	/**
	 * Create empty ontology
	 * @return
	 */
	public static Model createDefaultModel(){
		return ModelFactory.createDefaultModel();
	}
	
	/**
	 * Read ontology from URI (an URL)
	 * @param m
	 * @param URI
	 * @return
	 */
	public static Model readModel(Model m, String URI){
		return m.read(URI);
	}

	/**
	 * Extract the T-Box from the ontology
	 * @param m
	 * @return
	 */
	public static Model getTBox(Model m){
		Model aboxTypes = Uni.of(m)
								   .map(OWLganizer::constructABoxTypes)
								  .get();
		Model aboxProps = Uni.of(m)
								   .map(OWLganizer::constructABoxPropAsst)
								  .get();
		Model aboxLit = Uni.of(m)
								   .map(OWLganizer::constructABoxPropLiteral)
								  .get();
		Model m1 = ModelFactory.createModelForGraph(new Difference(m.getGraph(), aboxTypes.getGraph()));
		Model m2 = ModelFactory.createModelForGraph(new Difference(m1.getGraph(), aboxLit.getGraph()));
		Model m3 = ModelFactory.createModelForGraph(new Difference(m2.getGraph(), aboxProps.getGraph()));
		
		return m3;		
	}
	
	/**
	 * Extract the A-Box from the ontology
	 * @param m
	 * @return
	 */
	public static Model getABox(Model m){
		Model aboxTypes = Uni.of(m)
								   .map(OWLganizer::constructABoxTypes)
								  .get();
		Model aboxProps = Uni.of(m)
								   .map(OWLganizer::constructABoxPropAsst)
								  .get();
		Model aboxLit = Uni.of(m)
								   .map(OWLganizer::constructABoxPropLiteral)
								  .get();
		Model m1 = ModelFactory.createModelForGraph(new Union(aboxTypes.getGraph(), aboxProps.getGraph()));
		Model m2 = ModelFactory.createModelForGraph(new Union(m1.getGraph(), aboxLit.getGraph()));
		
		return m2;		
	}

	public static Model constructABoxTypes(Model m){
		Query cq1 = 
		Uni.of(ConstructBuilder::new)
		.map(b->b.addPrefix("rdf", IMPM.rdf))
		.map(b->b.addPrefix("owl", IMPM.owl))
		.map(b->b.addConstruct("?i", "rdf:type", "?c"))
		.map(b->b.addWhere("?i", "rdf:type", "?c")) 
		.map(b->b.addFilter("NOT EXISTS {?i rdf:type owl:Class}"))
		.map(b->b.build())
		.onFailure(e->e.printStackTrace())
		.get();
		
		QueryExecution qe = QueryExecutionFactory.create(cq1, m);
		return qe.execConstruct();
	}
	
	public static Model constructABoxPropAsst(Model m){
		
		Query cq2 = 
				Uni.of(ConstructBuilder::new)
				.map(b->b.addPrefix("rdf", IMPM.rdf))
				.map(b->b.addPrefix("owl", IMPM.owl))
				.map(b->b.addConstruct("?i1", "?p", "?i2"))
				.map(b->b.addWhere("?i1", "?p", "?i2"))
				.map(b->b.addWhere("?i1", "rdf:type", "?c")) //"?s", "rdf:type", "owl:Class"
				.map(b->b.addWhere("?i2", "rdf:type", "?c")) //"?s", "rdf:type", "owl:Class"
				.map(b->b.addFilter("NOT EXISTS {?i1 rdf:type owl:Class}"))
				.map(b->b.addFilter("NOT EXISTS {?i2 rdf:type owl:Class}"))
				.map(b->b.build())
				.onFailure(e->e.printStackTrace())
				.get();
				
		QueryExecution qe = QueryExecutionFactory.create(cq2, m);
		return qe.execConstruct();
	}
	
	public static Model constructABoxPropLiteral(Model m){
		
		Query cq2 = 
				Uni.of(ConstructBuilder::new)
				.map(b->b.addPrefix("rdf", IMPM.rdf))
				.map(b->b.addPrefix("owl", IMPM.owl))
				.map(b->b.addConstruct("?i1", "?p", "?l"))
				.map(b->b.addWhere("?i1", "?p", "?l"))
				.map(b->b.addWhere("?i1", "rdf:type", "?c")) //"?s", "rdf:type", "owl:Class"
				.map(b->b.addFilter("NOT EXISTS {?i1 rdf:type owl:Class}"))
				.map(b->b.addFilter("isLiteral(?l)"))
				.map(b->b.build())
				.onFailure(e->e.printStackTrace())
				.get();
				
		QueryExecution qe = QueryExecutionFactory.create(cq2, m);
		return qe.execConstruct();
	}

}

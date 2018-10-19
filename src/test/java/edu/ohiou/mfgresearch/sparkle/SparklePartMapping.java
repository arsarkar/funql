package edu.ohiou.mfgresearch.sparkle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.CollectionGraph;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Before;
import org.junit.Test;

import Ontology.partloader.IMPlanXMLLoader;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.lambda.functions.Cons;
import edu.ohiou.mfgresearch.lambda.functions.Func;
import edu.ohiou.mfgresearch.lambda.functions.Suppl;
import edu.ohiou.mfgresearch.simplanner.IMPM;

public class SparklePartMapping {
	
	IMPlanXMLLoader loader;
	private List<Triple> triples;
	private Graph g;
	private OntModel m;

	/**
	 * Following the implementation of default model generation 
	 * https://github.com/apache/jena/blob/master/jena-extras/jena-querybuilder/src/test/java/org/apache/jena/arq/querybuilder/UpdateBuilderExampleTests.java
	 * Does it have ssame effect of ModelFactory.createDefaultModel()?
	 */
	Suppl<Model> defaultModelGen = ()->{
		triples = new ArrayList<Triple>();
		g = new CollectionGraph(triples);
		return ModelFactory.createDefaultModel(); //cannot create model from empty graph
	};
	
	Func<Model, OntModel> defaultOntGen = m->{
		return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, m);
	};
	
	Func<String, OntModel> importModel = url->{
		return (OntModel) ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM).read(url);
	};
	
	Func<OntModel, Func<OntClass, Individual>> 
		individual = m->c->m.createIndividual(IMPM.design_ins+c.getLocalName().toLowerCase()+IMPM.newHash(4), c);
	

	
	@Before
	public void setUp() throws Exception {
		loader = new IMPlanXMLLoader();
		m = defaultOntGen.apply(defaultModelGen.get());
		m.clearNsPrefixMap();
		m.setNsPrefix("cpm", IMPM.cpm);
		m.setNsPrefix("rdf", IMPM.rdf);
		m.setNsPrefix("owl", IMPM.owl);
		m.setNsPrefix("impmu", IMPM.impmu);
		m.setNsPrefix("dc", IMPM.dcelements);
	}

	@Test
	public void SparklePartLoad1() {
		String partName = loader.readPartName();
		
		//imported ontologies
		OntModel cpm=
		Uni.of(()->{
			return 
			importModel.apply("https://raw.githubusercontent.com/arsarkar/SIMPOM/master/cpm/cpm.rdf");
		}).get();	
		
		OntModel purl=
				Uni.of(()->{
					return 
					importModel.apply("http://dublincore.org/2012/06/14/dcelements");
				}).get();
		
		//create new cpm.Part
		Uni.of(m).map(m->m.createClass(IMPM.Part)).get();
		OntClass part = cpm.getOntClass(IMPM.Part);
		Uni.of(m).map(individual)
					  .map(f->f.apply(part))
					  .map(t->t.addProperty(m.createDatatypeProperty(IMPM.label), m.createTypedLiteral(partName)));
		
		//add features
		
		
		//::Notes:
		//rdf:type can also be written as 'a'
		//Ontology Classes needs OntClass Object, not just String
		inspect("?s", "rdf:type", part);

		m.write(System.out, "RDFXML");
		
	}

	private void inspect(Object s, Object p, Object o){
		Uni.of(SelectBuilder::new)
		.map(b->b.addPrefix("rdf", IMPM.rdf))
		.map(b->b.addPrefix("owl", IMPM.owl))
		.map(b->b.addVar("*"))
		.map(b->b.addWhere(s, p, o)) //"?s", "rdf:type", "owl:Class"
		.map(b->{
			Query q = b.build();
			System.out.println(q.toString());
			return QueryExecutionFactory.create(q, m);
		})
		.onFailure(e->e.printStackTrace())
		.onSuccess(f->{
			ResultSet res = f.execSelect();
			if(res.hasNext()){
				while (res.hasNext()) {
					System.out.println(res.next().toString());					
				}
			}
			else{
				System.out.println("No Result!");
			}
		});
	}
	
	private void select(Object s, Object p, Object o){
		
		Stream<Object> vars = 
				Stream.of(s,p,o)
					  .filter(t->t.toString().charAt(0) == '?');
		
		Uni.of(SelectBuilder::new)
				.map(b->b.addPrefix("rdf", IMPM.rdf))
				.map(b->b.addPrefix("owl", IMPM.owl))
				.map(b->{
					vars.map(v->b.addVar(v));
					return b;
				})
				.map(b->b.addWhere(s, p, o)) //"?s", "rdf:type", "owl:Class"
				.map(b->{
					Query q = b.build();
					System.out.println(q.toString());
					return QueryExecutionFactory.create(q, m);
				})
				.onFailure(e->e.printStackTrace())
				.onSuccess(f->{
					ResultSet res = f.execSelect();
					if(res.hasNext()){
						res.forEachRemaining(System.out::println);
					}
					else{
						System.out.println("No Result!");
					}
				});
	}
	
	private Stream<String> findVars(Object s, Object p, Object o){
		return null;
	}
	
}

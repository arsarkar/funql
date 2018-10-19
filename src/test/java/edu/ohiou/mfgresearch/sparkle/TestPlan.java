package edu.ohiou.mfgresearch.sparkle;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;
import org.junit.Before;
import org.junit.Test;

import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.plan.IPlan;
import edu.ohiou.mfgresearch.plan.IPlanner;
import edu.ohiou.mfgresearch.plan.PlanUtil;
import edu.ohiou.mfgresearch.simplanner.IMPM;

public class TestPlan {

	String ns = "http://example.com/local#";
	
	@Before
	public void setUp() throws Exception {
	}

//	@Test
//	public void curryTest(){
//		BiFunction<Integer, Integer, Integer> biAdder = (u, v)->u+v;
//		Function<Integer, Function<Integer, Integer>> curryAdder = new Function<Integer, Function<Integer, Integer>>() {
//			@Override
//			public Function<Integer, Integer> apply(Integer u) {
//				return new Function<Integer, Integer>() {
//					@Override
//					public Integer apply(Integer v) {
//						return u+v;
//					}
//				};
//			}
//		};
//		org.junit.Assert.assertEquals(biAdder.apply(3, 4), curryAdder.apply(3).apply(4));
//	}
	
	@Test
	public void testVarCombination(){
		List<Var> vars = new LinkedList<Var>();
		vars.add(Var.alloc("?v1"));
		vars.add(Var.alloc("?v2"));
		vars.add(Var.alloc("?v3"));
		List<List<Var>> combs = PlanUtil.combinations(vars);
		for(List<Var> comb: combs){
			for(Var v:comb){
				System.out.print(v.getName()+ " ");
			}
			System.out.println("");
		}
	}
	
	
	@Test
	public void testConstructBasicPatternParsing(){
		
		System.out.println("query 1-->");
		Uni.of(()->new IPlan(getQuery(1)))
				.map(p->PlanUtil.getConstructBasicPattern(p.getQuery()))
				.onSuccess(System.out::println);
		System.out.println("query 2-->");
		Uni.of(()->new IPlan(getQuery(2)))
				.map(p->PlanUtil.getConstructBasicPattern(p.getQuery()))
				.onSuccess(System.out::println);
		System.out.println("query 3-->");
		Uni.of(()->new IPlan(getQuery(3)))
				.map(p->PlanUtil.getConstructBasicPattern(p.getQuery()))
				.onSuccess(System.out::println);
	}
	
	@Test
	public void testConstructParsing(){
		
		System.out.println("query 3-->");
		Query cq = getQuery(3);

		System.out.println("Query--->" + cq.toString());
		System.out.println("Query type--->" + cq.getQueryType() + "<=>" + Query.QueryTypeConstruct);
		System.out.println("Query pattern--->" + cq.getQueryPattern());
		System.out.println("Query syntax--->" + cq.getSyntax());
		System.out.println("Query ordered? " + cq.isOrdered());
		System.out.println("Query orderBy--->" + cq.getOrderBy());
		System.out.println("Query variables---> " + cq.getProjectVars());
		
		System.out.println("As select query---> " + OpAsQuery.asQuery(Algebra.compile(cq.getQueryPattern())));
		
	}
	
	@Test
	public void testConstruct2SelectConversion(){
		
		System.out.println("query 1-->");
		Uni.of(()->new IPlan(getQuery(1)))
				.map(p->PlanUtil.convert2SelectQuery(p.getQuery()))
				.onSuccess(System.out::println);
		System.out.println("query 2-->");
		Uni.of(()->new IPlan(getQuery(2)))
				.map(p->PlanUtil.convert2SelectQuery(p.getQuery()))
				.onSuccess(System.out::println);
		System.out.println("query 3-->");
		Uni.of(()->new IPlan(getQuery(3)))
				.map(p->PlanUtil.convert2SelectQuery(p.getQuery()))
				.onSuccess(System.out::println);
		
	}
	
	@Test
	public void testConstructParseConstructVars(){
		
		System.out.println("query 1-->");
		Uni.of(()->new IPlan(getQuery(1)))
				.map(p->PlanUtil.getUnknownVars(p.getQuery()))
				.onSuccess(System.out::println);
		System.out.println("query 2-->");
		Uni.of(()->new IPlan(getQuery(2)))
				.map(p->PlanUtil.getUnknownVars(p.getQuery()))
				.onSuccess(System.out::println);
		System.out.println("query 3-->");
		Uni.of(()->new IPlan(getQuery(3)))
				.map(p->PlanUtil.getUnknownVars(p.getQuery()))
				.onSuccess(System.out::println);		
	}
	
	@Test
	public void testPlanQueryExecution(){
		IPlan plan = new IPlan(getQuery(4));
		System.out.println(plan.getQuery());
		OntModel store = createTripleStore1();
		store.write(System.out, "TURTLE");
		Supplier<Table> qex = IPlanner.createQueryExecutor(store, plan);
		Table tab = qex.get();
		
		//print the table result by looping on each binding
		tab.rows().forEachRemaining(b->{
			System.out.println(b);
			String i1 = b.get(Var.alloc("i1")).getURI();
			OntClass c = store.getIndividual(i1).getOntClass();
			System.out.println("type of i1 = "+ c);
		});
	}	
	
	
	@Test
	public void testTripleStore1(){
		OntModel m = createTripleStore1();
		m.write(System.out, "TURTLE");
	}
	
	/**
	 * Create a triple store with just one arc in the graph
	 *ns:C1 ns:P1 ns:C2
	 *ns:C1 ns:D1 ^^xsd:int
	 *ns:C2 ns:D2 ^^xsd:int 
	 * @return new Model
	 */
	private OntModel createTripleStore1(){
		
		return
		Uni.of(ModelFactory.createDefaultModel())
				.map(m->ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, m))
				.set(m->m.setNsPrefix("ns", ns))
				.set(m->{
					OntClass c1 = m.createClass(ns+"C1");
					OntClass c2 = m.createClass(ns+"C2");
					IntStream.range(0, 5)
							 .forEach(i->{
								 Individual i1 = m.createIndividual(ns+"C1_"+IMPM.newHash(4), c1);
								 i1.addProperty(m.createDatatypeProperty(ns+"D1"), m.createTypedLiteral(i*10));
								 Individual i2 = m.createIndividual(ns+"C2_"+IMPM.newHash(4), c2);	
								 i2.addProperty(m.createDatatypeProperty(ns+"D2"), m.createTypedLiteral(i*20));
								 ObjectProperty p1 = m.createObjectProperty(ns+"P1");
								 i1.addProperty(p1, i2);
							 });
						
				})
				.onFailure(e->e.printStackTrace())
				.set(i->{
					int number = 10;
					number++;
				})
//				.set(System.out::println)
				.get();
	}

	private Query getQuery(int queryIx){		
		switch (queryIx) {
		case 1:
			return
					Uni.of(ConstructBuilder::new)
					 //create a new select query
					.set(b->b.addPrefix("rdf", IMPM.rdf))
					.set(b->b.addPrefix("owl", IMPM.owl))
					.set(b->b.addPrefix("ns", ns))
					.set(b->b.addConstruct("?i1", "ns:P1", "?m"))
					.set(b->b.addConstruct("?m", "rdf:type", "ns:C2"))
					.set(b->b.addWhere("?i1", "rdf:type", "ns:C1"))
					.set(b->b.addWhere("?i1", "ns:D1", "ns:L1"))
					.map(b->b.build())
					.get()
					;
		case 2:
			return
					Uni.of(ConstructBuilder::new)
					 //create a new select query
					.set(b->b.addPrefix("rdf", IMPM.rdf))
					.set(b->b.addPrefix("owl", IMPM.owl))
					.set(b->b.addPrefix("ns", ns))
					.set(b->b.addConstruct("?i1", "ns:P1", "?m"))
					.set(b->b.addConstruct("?i2", "ns:P1", "?m"))
					.set(b->b.addConstruct("?i3", "ns:P1", "?m"))
					.set(b->b.addConstruct("?m", "rdf:type", "ns:C4"))
					.set(b->b.addWhere("?i1", "rdf:type", "ns:C1"))
					.set(b->b.addWhere("?i2", "rdf:type", "ns:C2"))
					.set(b->b.addWhere("?i3", "rdf:type", "ns:C3"))
					.map(b->b.build())
					.get()
					;
		case 3:
			return
					Uni.of(ConstructBuilder::new)
					 //create a new select query
					.set(b->b.addPrefix("rdf", IMPM.rdf))
					.set(b->b.addPrefix("owl", IMPM.owl))
					.set(b->b.addPrefix("ns", ns))
					.set(b->b.addConstruct("?i3", "ns:P3", "?m"))
					.set(b->b.addConstruct("?m", "rdf:type", "ns:C2"))
					.set(b->b.addWhere("?i1", "rdf:type", "ns:C1"))
					.set(b->b.addWhere("?i1", "ns:P1", "?i2"))
					.set(b->b.addWhere("?i2", "ns:D1", "?i3"))
					.set(b->b.addFilter("isLiteral(?i3)"))
					.set(b->b.addOrderBy(Var.alloc("i1")))
					.map(b->b.build())
					.get()
					;
		case 4:
			return
					Uni.of(ConstructBuilder::new)
					 //create a new select query
					.set(b->b.addPrefix("rdf", IMPM.rdf))
					.set(b->b.addPrefix("owl", IMPM.owl))
					.set(b->b.addPrefix("ns", ns))
					.set(b->b.addConstruct("?i1", "ns:P2", "?m"))
					.set(b->b.addConstruct("?i2", "ns:P2", "?m"))
					.set(b->b.addConstruct("?m", "rdf:type", "ns:C3"))
					.set(b->b.addWhere("?i1", "rdf:type", "ns:C1"))
					.set(b->b.addWhere("?i2", "rdf:type", "ns:C2"))
					.set(b->b.addWhere("?i1", "ns:P1", "?i2"))
					.set(b->b.addWhere("?i1", "ns:D1", "?d1"))
					.set(b->b.addWhere("?i2", "ns:D2", "?d2"))
					.map(b->b.build())
					.get()
					;
		default:
			return null;
		}
	}
}

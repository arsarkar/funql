package edu.ohiou.mfgresearch.sparkle;

import static org.junit.Assert.*;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingUtils;
import org.junit.Before;
import org.junit.Test;

import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.plan.IPlan;
import edu.ohiou.mfgresearch.plan.PlanUtil;
import edu.ohiou.mfgresearch.simplanner.IMPM;

public class TestAlgebra {

	Model m;
	
	@Before
	public void setUp() throws Exception {
		m = ModelFactory.createDefaultModel();
		m.clearNsPrefixMap();
		m.setNsPrefix("cpm", IMPM.cpm);
		m.setNsPrefix("rdf", IMPM.rdf);
		m.setNsPrefix("owl", IMPM.owl);
		m.setNsPrefix("impmu", IMPM.impmu);
		m.setNsPrefix("dc", IMPM.dcelements);
	}

	@Test
	public void testBinding() {
	
		Binding bind1 = 
		BindingFactory.binding(Var.alloc("x"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
		Binding bind2 =
		BindingFactory.binding(Var.alloc("y"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
		Binding bind3 =
		BindingFactory.binding(Var.alloc("z"), m.createLiteral("A String Value", true).asNode());
		Binding bind4 = BindingUtils.merge(BindingUtils.merge(bind1, bind2), bind3);
		System.out.println(bind4.toString());
		
	}
	
	@Test
	public void testTable() {
	
		Binding bind1 = 
		BindingFactory.binding(Var.alloc("x"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
		Binding bind2 =
		BindingFactory.binding(Var.alloc("y"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
		Binding bind3 = 
		BindingFactory.binding(Var.alloc("x"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
		Binding bind4 =
		BindingFactory.binding(Var.alloc("y"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
		Binding bind5 = BindingUtils.merge(bind1, bind2);
		Binding bind6 = BindingUtils.merge(bind3, bind4);
		Table tab = TableFactory.create();
		tab.addBinding(bind5);
		tab.addBinding(bind6);
		System.out.println(tab.toString());
	}
	
	@Test
	public void testSubstitute1() {
	
		Binding bind1 = 
		BindingFactory.binding(Var.alloc("x"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
		Binding bind2 =
		BindingFactory.binding(Var.alloc("y"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
//		Binding bind3 = 
//		BindingFactory.binding(Var.alloc("x"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
//		Binding bind4 =
//		BindingFactory.binding(Var.alloc("y"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
		Binding bind5 = BindingUtils.merge(bind1, bind2);
//		Binding bind6 = BindingUtils.merge(bind3, bind4);
//		Table tab = TableFactory.create();
//		tab.addBinding(bind5);
//		tab.addBinding(bind6);
//		System.out.println(tab.toString());
		BasicPattern pattern = new BasicPattern();
		pattern.add(new Triple(Var.alloc("x"), m.createProperty(IMPM.type).asNode(), Var.alloc("y")));
		System.out.println("pattern1 = "+ pattern.toString());
		pattern = Substitute.substitute(pattern, bind5);
		System.out.println("pattern2 = "+ pattern.toString());		
		
	}

	@Test
	public void testSubstitute2() {
	
		Binding bind1 = 
		BindingFactory.binding(Var.alloc("x"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
		Binding bind2 =
		BindingFactory.binding(Var.alloc("y"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
//		Binding bind3 = 
//		BindingFactory.binding(Var.alloc("x"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
//		Binding bind4 =
//		BindingFactory.binding(Var.alloc("y"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
		Binding bind5 = BindingUtils.merge(bind1, bind2);
//		Binding bind6 = BindingUtils.merge(bind3, bind4);
//		Table tab = TableFactory.create();
//		tab.addBinding(bind5);
//		tab.addBinding(bind6);
//		System.out.println(tab.toString());
		BasicPattern pattern = new BasicPattern();
		pattern.add(new Triple(Var.alloc("x"), m.createProperty(IMPM.type).asNode(), Var.alloc("y")));
		System.out.println("pattern1 = "+ pattern.toString());
		pattern = Substitute.substitute(pattern, bind5);
		System.out.println("pattern2 = "+ pattern.toString());		
		
	}
	
	@Test
	public void testSubstitute3() {
	
		Binding bind1 = 
		BindingFactory.binding(Var.alloc("x"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
		Binding bind2 =
		BindingFactory.binding(Var.alloc("y"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
//		Binding bind3 = 
//		BindingFactory.binding(Var.alloc("x"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
//		Binding bind4 =
//		BindingFactory.binding(Var.alloc("y"), m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode());
		Binding bind5 = BindingUtils.merge(bind1, bind2);
//		Binding bind6 = BindingUtils.merge(bind3, bind4);
//		Table tab = TableFactory.create();
//		tab.addBinding(bind5);
//		tab.addBinding(bind6);
//		System.out.println(tab.toString());
		BasicPattern pattern = new BasicPattern();
		pattern.add(new Triple(Var.alloc("x"), m.createProperty(IMPM.type).asNode(), Var.alloc("y")));
		pattern.add(new Triple(Var.alloc("x"), m.createProperty(IMPM.hasFeature).asNode(), m.createResource(IMPM.Feature).asNode()));
		System.out.println("pattern1 = "+ pattern.toString());
		pattern = Substitute.substitute(pattern, bind5);
		System.out.println("pattern2 = "+ pattern.toString());		
		
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
	public void testConstructParseConstrructVars(){
		
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

	private Query getQuery(int queryIx){
		String ns = "http://example.com/local#";
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
		default:
			return null;
		}
	}
	
}

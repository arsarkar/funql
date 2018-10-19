package edu.ohiou.mfgresearch.sparkle;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.CollectionGraph;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpLib;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
import org.apache.jena.sparql.algebra.op.OpDiff;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLabel;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpList;
import org.apache.jena.sparql.algebra.op.OpMinus;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpProcedure;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpPropFunc;
import org.apache.jena.sparql.algebra.op.OpQuad;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpTopN;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.algebra.walker.Walker;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.DC_11;
import org.checkerframework.checker.oigj.qual.O;
import org.junit.Before;
import org.junit.Test;
import org.swrlapi.drools.owl.core.OO;

import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.simplanner.IMPM;


public class SparkleUpdate {

	private static final String NS_prefix = "http://example.org/ns#";
	private List<Triple> triples;
	private Graph g;
	private Model m;
	
	@Before
	public void setUp() throws Exception {
		triples = new ArrayList<Triple>();
		g = new CollectionGraph(triples);
		m = ModelFactory.createModelForGraph(g);
	}

	/**
	 * Example 1: Adding some triples to a graph
	 * 
	 * @see https://www.w3.org/TR/sparql11-update/#example_1
	 */
	@Test
	public void example1() {
		Resource r = ResourceFactory.createResource("http://example/book1");
		Property price = ResourceFactory.createProperty(NS_prefix + "price");
		Literal priceV = ResourceFactory.createPlainLiteral("42");
		m.add(r, price, priceV);
		m.setNsPrefix("dc", DC_11.NS);
		m.setNsPrefix("ns", NS_prefix);
		
		Resource r2 = m.createResource("http://example/book2");
//		r2.addProperty(DC_11.title, "A new book")
//		  .addProperty(DC_11.creator, "A.N.Other");
	
//		UpdateBuilder builder = new UpdateBuilder().addPrefix("dc", DC_11.NS).addInsert(r, DC_11.title, "A novel book")
//				.addInsert(r, DC_11.creator, "B.N.Other");
		
		UpdateBuilder builder = new UpdateBuilder().addPrefix("dc", DC_11.NS).addInsert(r2, DC_11.title, "A new book")
				.addInsert(r2, DC_11.creator, m.createTypedLiteral("A.B.Other"));
//		UpdateRequest req = builder.buildString();
		
		UpdateAction.execute(builder.buildRequest(), m);
		
//		assertTrue(m.contains(r, price, priceV));
//		assertTrue(m.contains(r2, DC_11.title, "A new book"));
//		assertTrue(m.contains(r2, DC_11.creator, "A.N.Other"));
//		assertEquals(3, triples.size());
//		assertEquals(2, m.getNsPrefixMap().size());
//		assertEquals(NS_prefix, m.getNsPrefixMap().get("ns"));
//		assertEquals(DC_11.NS, m.getNsPrefixMap().get("dc"));
		
		m.write(System.out, "RDFXML");

	}
	
	@Test
	public void readQuery1(){
		//translate to algebra 
		Uni.of(SelectBuilder::new)
				 //create a new select query
				.set(b->b.addPrefix("rdf", IMPM.rdf))
				.set(b->b.addPrefix("owl", IMPM.owl))
				.set(b->b.addPrefix("cpm", IMPM.cpm))
				.set(b->b.addPrefix("impmu", IMPM.impmu))
				.set(b->b.addVar("p")) //same as adding project to algebra
				.set(b->b.addWhere("?p", "rdf:type", "cpm:Part"))
				.set(b->b.addWhere("?p", "cpm:hasFeature", "?f"))
				.set(b->b.addWhere("?f", "impmu:hasSpecification", "?s"))
//				.set(b->b.addBind("{Binding=getFeature(?p)}", "f"))
//				.set(b->b.addBind("{Binding=getFeature(?p)}", "s"))
				.map(b->b.build())
				//Query
				.set(q->System.out.println(q.serialize()))
				.map(q->Algebra.compile(q))
				//Operation
				.map(o->{
//					OpVars.fixedVars(o).forEach(v->System.out.println("fv = "+v.getName()));
//					OpVars.mentionedVars(o).forEach(v->System.out.println("mv = "+v.getName()));
//					OpVars.visibleVars(o).forEach(v->System.out.println("vv = "+v.getName()));
					BasicPattern pattern;
					Walker.walk(o, new OpVisitorBase(){

						@Override
						public void visit(OpBGP opBGP) {
							// TODO Auto-generated method stub
							super.visit(opBGP);
							BasicPattern pattern = opBGP.getPattern();
							Graph g = GraphFactory.createJenaDefaultGraph();
							pattern.forEach(p->g.add(p));
							System.out.println(g);
//							System.out.println(g.getCapabilities());
							
						}
						
					});
					return o;
				})
				
				.onFailure(e->e.printStackTrace())
//				.onSuccess(o->SSE.write(o));
				;
		
	}
	
	@Test
	public void readConstruct1(){
		//translate to algebra 
		Uni.of(ConstructBuilder::new)
				 //create a new select query
				.set(b->b.addPrefix("rdf", IMPM.rdf))
				.set(b->b.addPrefix("owl", IMPM.owl))
				.set(b->b.addPrefix("cpm", IMPM.cpm))
				.set(b->b.addPrefix("impmu", IMPM.impmu))
				.set(b->b.addWhere("?p", "rdf:type", "cpm:Part"))
				.set(b->b.addConstruct("?p", "cpm:hasFeature", "?f"))
				.set(b->b.addConstruct("?f", "impmu:hasSpecification", "?s"))
//				.set(b->b.addBind("{Binding=getFeature(?p)}", "f"))
//				.set(b->b.addBind("{Binding=getFeature(?p)}", "s"))
				.map(b->b.build())
				//Query
				.set(q->System.out.println(q.serialize()))
				.map(q->Algebra.compile(q))
				//Operation
				.map(o->{
//					OpVars.fixedVars(o).forEach(v->System.out.println("fv = "+v.getName()));
//					OpVars.mentionedVars(o).forEach(v->System.out.println("mv = "+v.getName()));
					OpVars.visibleVars(o).forEach(v->System.out.println("vv = "+v.getName()));
					
					Walker.walk(o, new OpVisitorBase(){

						@Override
						public void visit(OpBGP opBGP) {
							// TODO Auto-generated method stub
							super.visit(opBGP);
							opBGP.getPattern().forEach(p->System.out.println(p.toString()));
						}
						
					});
					return o;
				})
				.onFailure(e->e.printStackTrace())
				//.onSuccess(o->SSE.write(o));
				;
		
	}
	
	@Test
	public void readGraph(){

		Graph g = GraphFactory.createJenaDefaultGraph();
		//translate to algebra 
		Uni.of(SelectBuilder::new)
				 //create a new select query
				.set(b->b.addPrefix("rdf", IMPM.rdf))
				.set(b->b.addPrefix("owl", IMPM.owl))
				.set(b->b.addPrefix("cpm", IMPM.cpm))
				.set(b->b.addPrefix("impmu", IMPM.impmu))
				.set(b->b.addVar("p")) //same as adding project to algebra
				.set(b->b.addWhere("?p", "rdf:type", "cpm:Part"))
				.set(b->b.addWhere("?p", "cpm:hasFeature", "?f"))
				.set(b->b.addWhere("?f", "impmu:hasSpecification", "?s"))
//				.set(b->b.addBind("{Binding=getFeature(?p)}", "f"))
//				.set(b->b.addBind("{Binding=getFeature(?p)}", "s"))
				.map(b->b.build())
				//Query
				.set(q->System.out.println(q.serialize()))
				.map(q->Algebra.compile(q))
				//Operation
				.map(o->{
//					OpVars.fixedVars(o).forEach(v->System.out.println("fv = "+v.getName()));
//					OpVars.mentionedVars(o).forEach(v->System.out.println("mv = "+v.getName()));
//					OpVars.visibleVars(o).forEach(v->System.out.println("vv = "+v.getName()));
					BasicPattern pattern;
					Walker.walk(o, new OpVisitorBase(){

						@Override
						public void visit(OpBGP opBGP) {
							// TODO Auto-generated method stub
							super.visit(opBGP);
							BasicPattern pattern = opBGP.getPattern();
							pattern.forEach(p->g.add(p));
							System.out.println(g);
						}						
					});
					return o;
				})
				
				.onFailure(e->e.printStackTrace())
//				.onSuccess(o->SSE.write(o));
				;
		
		Graph g1 = GraphFactory.createJenaDefaultGraph();	
		g1.add(new Triple(m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode(),
						  m.createProperty(IMPM._type).asNode(),
						  m.createResource(IMPM.Part).asNode()));
		g1.add(new Triple(m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode(),
						  m.createProperty(IMPM.hasFeature).asNode(),
						  m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode()));
		g1.add(new Triple(m.createResource(IMPM.design_ins+IMPM.newHash(4)).asNode(),
						  m.createProperty(IMPM._type).asNode(),
						  m.createResource(IMPM.Part).asNode()));
		System.out.println("g1 = "+ g1.toString());
		
		
	}

}

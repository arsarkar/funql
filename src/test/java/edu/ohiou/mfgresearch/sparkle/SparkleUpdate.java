package edu.ohiou.mfgresearch.sparkle;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.CollectionGraph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.walker.Walker;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.modify.UsingList;
import org.apache.jena.sparql.modify.request.UpdateAdd;
import org.apache.jena.sparql.modify.request.UpdateClear;
import org.apache.jena.sparql.modify.request.UpdateCopy;
import org.apache.jena.sparql.modify.request.UpdateCreate;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateDeleteWhere;
import org.apache.jena.sparql.modify.request.UpdateDrop;
import org.apache.jena.sparql.modify.request.UpdateLoad;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.sparql.modify.request.UpdateMove;
import org.apache.jena.sparql.modify.request.UpdateVisitor;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.DC_11;
import org.junit.Before;
import org.junit.Test;

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
	public void executeInsert1(){
		UpdateRequest request = UpdateFactory.create();
//		request.add("CREATE GRAPH <http://example/apat1>");
//		request.add("LOAD <file:resources/META-INF/ontology/apat8.owl> INTO <http://example/apat1>");
//		Model m1 = ModelFactory.createDefaultModel();
//		UpdateAction.execute(request, m);
//		m1.write(System.out, "NTRIPLE");
		Model m = ModelFactory.createDefaultModel().read("resources/META-INF/ontology/apat8.owl");
		Dataset ds = DatasetFactory.create(m); 
		UpdateAction.readExecute("resources/META-INF/query/insert-pattern2.q", ds);
//		m.write(System.out, "NTRIPLE");
		SSE.write(ds);
		
	}
	
	@Test
	public void readInsert1(){
		UpdateRequest request = UpdateFactory.create();
		Model m = ModelFactory.createDefaultModel().read("resources/META-INF/ontology/apat8.owl");
		//UpdateAction.readExecute("resources/META-INF/query/insert-pattern2.q", m);
		request = UpdateFactory.read("resources/META-INF/query/insert-pattern2.q");
		Update update = request.getOperations().get(0);
		System.out.println(update.toString());
		UpdateAction.execute(update, m);
		m.write(System.out, "NTRIPLE");
		
		update.visit(new UpdateVisitor() {
			
			@Override
			public void visit(UpdateDataDelete update) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void visit(UpdateDataInsert update) {
				// TODO Auto-generated method stub
				System.out.println(update.toString());
			}
			
			@Override
			public void visit(UpdateMove update) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void visit(UpdateCopy update) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void visit(UpdateAdd update) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void visit(UpdateLoad update) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void visit(UpdateCreate update) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void visit(UpdateClear update) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void visit(UpdateDrop update) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Sink<Quad> createInsertDataSink() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Sink<Quad> createDeleteDataSink() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void visit(UpdateDeleteWhere update) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void visit(UpdateModify update) {
				System.out.println(update.toString());
			}
		});
		
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

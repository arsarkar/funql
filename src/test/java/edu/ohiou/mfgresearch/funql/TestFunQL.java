package edu.ohiou.mfgresearch.funql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
import org.apache.jena.sparql.algebra.op.OpDiff;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExt;
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
import org.junit.Before;
import org.junit.Test;

import edu.ohiou.mfgresearch.functions.DummyFunction;
import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.plan.IPlan;
import edu.ohiou.mfgresearch.simplanner.IMPM;

public class TestFunQL {
	String folder = "C:/Users/sarkara1/git/";

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testA1PlanNoService1() {
		try {
			FunQL.main(new String[] { "-query", folder + "funql/resources/META-INF/query/select-psl-before.q",
					"-belief", "https://raw.githubusercontent.com/arsarkar/SIMPOM/master/psl/psl_ext_2.0.owl",
					"-knowledge", "C:/Users/sarkara1/git/SIMPOM/psl/psl_ind_1.owl" });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testA1PlanNoService2() {
		try {
			FunQL.main(new String[] { "-query",
					"C:/Users/sarkara1/git/sparkle/resources/META-INF/query/select-psl-ax14-1.q", "-belief",
					"https://raw.githubusercontent.com/arsarkar/SIMPOM/master/psl/psl_ext_2.0.owl", "-knowledge",
					"C:/Users/sarkara1/git/SIMPOM/psl/psl_ind_2.owl" });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testA1PlanNoService_cone_volume() {
		try {
			FunQL.main(new String[] { "-query",
					"C:/Users/sarkara1/git/sparkle/resources/META-INF/query/select-volume-cone.q", "-belief",
					"http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl", "-knowledge",
					"C:/Users/sarkara1/git/SIMPOM/geometry/geom-ind1.owl" });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testB1PlanNoService1() {
		try {
			FunQL.main(new String[] { "-query",
					"C:/Users/sarkara1/git/sparkle/resources/META-INF/query/construct-psl-ax14-1.q", "-belief",
					"https://raw.githubusercontent.com/arsarkar/SIMPOM/master/psl/psl_ext_2.0.owl", "-knowledge",
					"C:/Users/sarkara1/git/SIMPOM/psl/psl_ind_2.owl" });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testB2Plan1() {
		try {
			FunQL.main(new String[] { "-query",
					"C:/Users/sarkara1/git/sparkle/resources/META-INF/query/construct-volume-cone1.q", "-service",
					"C:/Users/sarkara1/git/sparkle/resources/META-INF/services/calculateVolumeCone.json", "-belief",
					"http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl", "-knowledge",
					"C:/Users/sarkara1/git/SIMPOM/geometry/geom-ind1.owl" });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testB2APlan1() {
		try {
			FunQL.main(new String[] { "-query", folder + "funql/resources/META-INF/query/construct-volume-cone2.q",
					"-service", folder + "funql/resources/META-INF/services/calculateVolumeCone.json", "-belief",
					"http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl", "-knowledge",
					folder + "SIMPOM/geometry/geom-ind1.owl" });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void makeCubeFromCone() {
		try {
			FunQL.main(new String[] { "-query", folder + "funql/resources/META-INF/query/make-cube-from-cone.q",
					"-service", folder + "funql/resources/META-INF/services/make-cube-from-cone.json", "-belief",
					"http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl", "-knowledge",
					folder + "SIMPOM/geometry/cube-ind1.owl" });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testQueryService() {
		try {
			Uni.of(FunQL::new).get().addTBox("http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl")
					.addABox("C:/Users/sarkara1/git/SIMPOM/geometry/geom-ind1.owl")
					.addPlan("C:/Users/sarkara1/git/funql/resources/META-INF/query/construct-volume-cone3.q").execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testQueryService1() {
		Uni.of("C:/Users/sarkara1/git/SIMPOM/geometry/geom-ind3.owl").map(File::new).map(FileOutputStream::new)
				.map(os -> Uni.of(FunQL::new).get()
						.addTBox("http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl")
						.addABox("https://raw.githubusercontent.com/arsarkar/SIMPOM/master/geometry/geom-ind1.owl")
						.addPlan("C:/Users/sarkara1/git/funql/resources/META-INF/query/construct-volume-cone3.q")
						.execute().getBelief().getaBox().write(os))
				.onFailure(e -> e.printStackTrace());
	}

	@Test
	public void testMakeCube() {
		Uni.of("C:/Users/sarkara1/git/SIMPOM/geometry/geom-ind4.owl").map(File::new).map(FileOutputStream::new)
				.map(os -> Uni.of(FunQL::new).get()
						.addTBox("http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl")
						.addABox("https://raw.githubusercontent.com/arsarkar/SIMPOM/master/geometry/geom-ind1.owl")
						.addPlan("C:/Users/sarkara1/git/funql/resources/META-INF/query/construct-cube-cone.q").execute()
						.getBelief().getaBox().write(os))
				.onFailure(e -> e.printStackTrace());
	}

	@Test
	public void testCalSide() {
		Uni.of(folder + "SIMPOM/geometry/geom-ind-square1.owl").map(File::new).map(FileOutputStream::new)
				.map(os -> Uni.of(FunQL::new).get()
						.addTBox("http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl")
						.addABox(folder + "SIMPOM/geometry/geom-ind-square.owl")
						.addPlan(folder + "funql/resources/META-INF/query/calculate-vertex-square.q").execute()
						.getBelief().getaBox().write(os))
				.onFailure(e -> e.printStackTrace());
	}

	@Test
	public void testSamplePattern1() {
		Uni.of("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/apat2.owl").map(File::new)
				.map(FileOutputStream::new)
				.map(os -> Uni.of(FunQL::new).get()
						.addTBox("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/tpat1.owl")
						.addABox("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/apat1.owl")
						.addPlan("C:/Users/sarkara1/git/funql/resources/META-INF/query/sample-pattern1.q").execute()
						.getBelief().getaBox().write(os))
				.onFailure(e -> e.printStackTrace());
	}

	@Test
	public void testSamplePatternAlt() {
		DummyFunction func = new DummyFunction("Hello");
		Uni.of("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat2.owl").map(File::new)
				.map(FileOutputStream::new).map(
						os -> Uni.of(FunQL::new).get()
								.addTBox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\tpat1.owl")
								.addABox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat1.owl")
								.addPlan(
										Uni.of(ConstructBuilder::new).set(b -> b.addPrefix("rdf", IMPM.rdf))
												.set(b -> b.addPrefix("tpat", "http://www.ohio.edu/ontologies/tpat1#"))
												.set(b -> b.addPrefix("dummy",
														"edu.ohiou.mfgresearch.functions.DummyFunction"))
												.set(b -> b.addWhere("?c1", "rdf:type", "tpat:class1"))
												.set(b -> b.addWhere("?c2", "rdf:type", "tpat:class2"))
												.set(b -> b.addWhere("?c1", "tpat:property1", "?c2"))
												.set(b -> b.addWhere("?c2", "tpat:dprop1", "?in"))
												.set(b -> b.addConstruct("?c4", "rdf:type", "tpat:class4"))
												.set(b -> b.addConstruct("?c2", "tpat:property3", "?c4"))
												.set(b -> b.addConstruct("?c4", "tpat:dprop2", "?out"))
												.map(b -> b.build()).get().serialize(),
										"?out", "dummy:calDummyIn(?in)", func)
								.execute().getBelief().getaBox().write(os))
				.onFailure(e -> e.printStackTrace());
	}

	@Test
	public void testSamplePattern2() {
		Uni.of("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/apat2.owl").map(File::new)
				.map(FileOutputStream::new)
				.map(os -> Uni.of(FunQL::new).get()
						.addTBox("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/tpat1.owl")
						.addABox("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/apat1.owl")
						.addPlan("C:/Users/sarkara1/git/funql/resources/META-INF/query/sample-pattern2.q").execute()
						.getBelief().getaBox().write(os))
				.onFailure(e -> e.printStackTrace());
	}

	@Test
	public void testSamplePattern3() {
		Uni.of("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/apat3.owl").map(File::new)
				.map(FileOutputStream::new)
				.map(os -> Uni.of(FunQL::new).get()
						.addTBox("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/tpat1.owl")
						.addABox("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/apat2.owl")
						.addPlan("C:/Users/sarkara1/git/funql/resources/META-INF/query/sample-pattern3.q").execute()
						.getBelief().getaBox().write(os))
				.onFailure(e -> e.printStackTrace());
	}

	@Test
	public void testSamplePattern2then3then4() {
		Uni.of(folder + "funql/resources/META-INF/ontology/apat4.owl").map(File::new).map(FileOutputStream::new)
				.map(os -> Uni.of(FunQL::new).get().addTBox(folder + "funql/resources/META-INF/ontology/tpat1.owl")
						.addABox(folder + "funql/resources/META-INF/ontology/apat1.owl")
						.addPlan(folder + "funql/resources/META-INF/query/sample-pattern2.q")
						.addPlan(folder + "funql/resources/META-INF/query/sample-pattern3.q")
						.addPlan(folder + "funql/resources/META-INF/query/sample-pattern4.q").execute().getBelief()
						.getaBox().write(os))
				.onFailure(e -> e.printStackTrace());
	}

	@Test
	public void testSamplePattern5() {
		Uni.of("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat5.owl").map(File::new)
				.map(FileOutputStream::new)
				.map(os -> Uni.of(FunQL::new).get()
						.addTBox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\tpat1.owl")
						.addABox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat1.owl")
						.addPlan("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\query\\sample-pattern5.q")
						.execute().getBelief().getaBox().write(os))
				.onFailure(e -> e.printStackTrace());
	}

	@Test
	public void testSamplePattern6() {
		Uni.of("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat7.owl").map(File::new)
				.map(FileOutputStream::new)
				.map(os -> Uni.of(FunQL::new).get()
						.addTBox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\tpat1.owl")
						.addABox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat6.owl")
						.addPlan("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\query\\sample-pattern5.q")
						.execute().getBelief().getaBox().write(os))
				.onFailure(e -> e.printStackTrace());
	}

	@Test
	public void testSamplePattern7() {
		Uni.of("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat7.owl").map(File::new)
				.map(FileOutputStream::new)
				.map(os -> Uni.of(FunQL::new).get()
						.addTBox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\tpat1.owl")
						.addABox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat1.owl")
						.addPlan("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\query\\sample-pattern6.q")
						.execute().getBelief().getaBox().write(os))
				.onFailure(e -> e.printStackTrace());
	}

	@Test
	public void testSamplePattern8() {
		Uni.of("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat9.owl").map(File::new)
				.map(FileOutputStream::new)
				.map(os -> Uni.of(FunQL::new).get()
						.addTBox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\tpat1.owl")
						.addABox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat8.owl")
						.addPlan("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\query\\sample-pattern7.q")
						.execute().getBelief().getaBox().write(os))
				.onFailure(e -> e.printStackTrace());
	}

	@Test
	public void testSelectPattern8() {
		Uni.of(FunQL::new)
				.set(q -> q.addTBox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\tpat1.owl"))
				.set(q -> q.addABox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat8.owl"))
				.set(q -> q.addPlan("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\query\\select-pattern-7.q"))
				.set(q -> q.setSelectPostProcess(tab -> {
					System.out.println("The result of the query is-->");
					System.out.println(tab.toString());
					return tab;
				})).map(q -> q.execute()).onFailure(e -> e.printStackTrace(System.out));
	}

	@Test
	public void testConstructIndiFunc1() {
		Uni.of(FunQL::new)
				.set(q -> q.addTBox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\tpat1.owl"))
				.set(q -> q.addABox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat1.owl"))
				.set(q -> q
						.addPlan("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\query\\construct-pattern1.q"))
				.map(q -> q.execute()).onFailure(e -> e.printStackTrace(System.out));
	}

	@Test
	public void testConstructInsert1() {
		Uni.of(FunQL::new)
				.set(q -> q.addTBox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\tpat1.owl"))
				.set(q -> q.addABox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat8.owl"))
				.set(q -> q.addPlan("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\query\\insert-pattern3.q"))
				.map(q -> q.execute()).onFailure(e -> e.printStackTrace(System.out));
	}

	@Test
	public void runBasicConstruct1() {
		String tpath = getClass().getClassLoader().getResource("META-INF/ontology/tpat1.owl").getFile();
		String apath = getClass().getClassLoader().getResource("META-INF/ontology/apat1.owl").getFile();
		String ppath = getClass().getClassLoader().getResource("META-INF/query/sparql-construct-pattern1.q").getFile();
		Uni.of(FunQL::new).set(q -> q.addTBox(tpath)).set(q -> q.addABox(apath)).set(q -> q.addPlan(ppath))
				.map(q -> q.execute()).onFailure(e -> e.printStackTrace(System.out));
	}

	@Test
	public void runQueryWalker() throws IOException {
		String ppath = getClass().getClassLoader().getResource("META-INF/query/sparql-construct-pattern1.q").getFile();
		String query = parseQueryFromFile(ppath);
		QVisitior visitior = new QVisitior();
		Uni.of(query)
			.map(IPlan::new)
			.map(i->i.getQuery())
			.map(Algebra::compile)
			.set(op->org.apache.jena.sparql.algebra.walker.Walker.walk(op, visitior));
	}

	/**
	 * Utility method to parse content from given url
	 * @param url
	 * @return
	 * @throws IOException 
	 */
	private String parseQueryFromFile(String path) throws IOException{
		String contents = "";
		BufferedReader reader = new BufferedReader(
		        new InputStreamReader(new File(path).toURI().toURL().openStream()));
		String inputLine;
        while ((inputLine = reader.readLine()) != null)
            contents += inputLine + "\n"; 
        reader.close();
        return contents;
	}

	static class QVisitior extends OpVisitorBase {

		public QVisitior() {

		}

		@Override
		public void visit(OpBGP opBGP) {
			// TODO Auto-generated method stub
			super.visit(opBGP);
			System.out.println("BGP = " + opBGP.toString());
		}

		@Override
		public void visit(OpQuadPattern quadPattern) {
			// TODO Auto-generated method stub
			super.visit(quadPattern);
			System.out.println("QuadPattern = " + quadPattern.toString());
		}

		@Override
		public void visit(OpQuadBlock quadBlock) {
			// TODO Auto-generated method stub
			super.visit(quadBlock);
			System.out.println("QuadBlock = " + quadBlock.toString());
		}

		@Override
		public void visit(OpTriple opTriple) {
			// TODO Auto-generated method stub
			super.visit(opTriple);
			System.out.println("Triple = " + opTriple.toString());
		}

		@Override
		public void visit(OpQuad opQuad) {
			// TODO Auto-generated method stub
			super.visit(opQuad);
			System.out.println("Quad = " + opQuad.toString());
		}

		@Override
		public void visit(OpPath opPath) {
			// TODO Auto-generated method stub
			super.visit(opPath);
			System.out.println("Path = " + opPath.toString());
		}

		@Override
		public void visit(OpProcedure opProc) {
			// TODO Auto-generated method stub
			super.visit(opProc);
			System.out.println("Procedure = " + opProc.toString());
		}

		@Override
		public void visit(OpPropFunc opPropFunc) {
			// TODO Auto-generated method stub
			super.visit(opPropFunc);
			System.out.println("PropFunc = " + opPropFunc.toString());
		}

		@Override
		public void visit(OpJoin opJoin) {
			// TODO Auto-generated method stub
			super.visit(opJoin);
			System.out.println("Join = " + opJoin.toString());
		}

		@Override
		public void visit(OpSequence opSequence) {
			// TODO Auto-generated method stub
			super.visit(opSequence);
		}

		@Override
		public void visit(OpDisjunction opDisjunction) {
			// TODO Auto-generated method stub
			super.visit(opDisjunction);
			System.out.println("Disjunction = " + opDisjunction.toString());
		}

		@Override
		public void visit(OpLeftJoin opLeftJoin) {
			// TODO Auto-generated method stub
			super.visit(opLeftJoin);
			System.out.println("LeftJoin = " + opLeftJoin.toString());
		}

		@Override
		public void visit(OpConditional opCond) {
			// TODO Auto-generated method stub
			super.visit(opCond);
			System.out.println("Conditional = " + opCond.toString());
		}

		@Override
		public void visit(OpMinus opMinus) {
			// TODO Auto-generated method stub
			super.visit(opMinus);
			System.out.println("Minus = " + opMinus.toString());
		}

		@Override
		public void visit(OpDiff opDiff) {
			// TODO Auto-generated method stub
			super.visit(opDiff);
			System.out.println("Difference = " + opDiff.toString());
		}

		@Override
		public void visit(OpUnion opUnion) {
			// TODO Auto-generated method stub
			super.visit(opUnion);
			System.out.println("Union = " + opUnion.toString());
		}

		@Override
		public void visit(OpFilter opFilter) {
			// TODO Auto-generated method stub
			super.visit(opFilter);
			System.out.println("Filter = " + opFilter.toString());
		}

		@Override
		public void visit(OpGraph opGraph) {
			// TODO Auto-generated method stub
			super.visit(opGraph);
			System.out.println("Graph = " + opGraph.toString());
		}

		@Override
		public void visit(OpService opService) {
			// TODO Auto-generated method stub
			super.visit(opService);
			System.out.println("Service = " + opService.toString());
		}

		@Override
		public void visit(OpDatasetNames dsNames) {
			// TODO Auto-generated method stub
			super.visit(dsNames);
			System.out.println("DatasetNames = " + dsNames.toString());
		}

		@Override
		public void visit(OpTable opTable) {
			// TODO Auto-generated method stub
			super.visit(opTable);
			System.out.println("Table = " + opTable.toString());
		}

		@Override
		public void visit(OpNull opNull) {
			// TODO Auto-generated method stub
			super.visit(opNull);
			System.out.println("Null = " + opNull.toString());
		}

		@Override
		public void visit(OpLabel opLabel) {
			// TODO Auto-generated method stub
			super.visit(opLabel);
			System.out.println("Label = " + opLabel.toString());
		}

		@Override
		public void visit(OpAssign opAssign) {
			// TODO Auto-generated method stub
			super.visit(opAssign);
			System.out.println("Assign = " + opAssign.toString());
		}

		@Override
		public void visit(OpExtend opExtend) {
			// TODO Auto-generated method stub
			super.visit(opExtend);
			System.out.println("Extend = " + opExtend.toString());
		}

		@Override
		public void visit(OpList opList) {
			// TODO Auto-generated method stub
			super.visit(opList);
			System.out.println("List = " + opList.toString());
		}

		@Override
		public void visit(OpOrder opOrder) {
			// TODO Auto-generated method stub
			super.visit(opOrder);
			System.out.println("Order = " + opOrder.toString());
		}

		@Override
		public void visit(OpProject opProject) {
			// TODO Auto-generated method stub
			super.visit(opProject);
			System.out.println("Project = " + opProject.toString());
		}

		@Override
		public void visit(OpDistinct opDistinct) {
			// TODO Auto-generated method stub
			super.visit(opDistinct);
			System.out.println("Distinct = " + opDistinct.toString());
		}

		@Override
		public void visit(OpReduced opReduced) {
			// TODO Auto-generated method stub
			super.visit(opReduced);
			System.out.println("Reduced = " + opReduced.toString());
		}

		@Override
		public void visit(OpSlice opSlice) {
			// TODO Auto-generated method stub
			super.visit(opSlice);
			System.out.println("Slice = " + opSlice.toString());
		}

		@Override
		public void visit(OpGroup opGroup) {
			// TODO Auto-generated method stub
			super.visit(opGroup);
			System.out.println("Group = " + opGroup.toString());
		}

		@Override
		public void visit(OpTopN opTop) {
			// TODO Auto-generated method stub
			super.visit(opTop);
			System.out.println("TopN = " + opTop.toString());
		}

		@Override
		public void visit(OpExt opExt) {
			// TODO Auto-generated method stub
			super.visit(opExt);
			System.out.println("Ext = " + opExt.toString());
		}

	}

}

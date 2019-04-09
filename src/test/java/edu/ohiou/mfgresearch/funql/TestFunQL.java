package edu.ohiou.mfgresearch.funql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.junit.Before;
import org.junit.Test;

import edu.ohiou.mfgresearch.functions.DummyFunction;
import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.simplanner.IMPM;

public class TestFunQL {
	String folder = "C:/Users/sormaz/Documents/GitHub/";

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testA1PlanNoService1(){
		try {
			FunQL.main(new String[]
					{"-query", folder + "funql/resources/META-INF/query/select-psl-before.q", 
					 "-belief",  "https://raw.githubusercontent.com/arsarkar/SIMPOM/master/psl/psl_ext_2.0.owl", 
					 "-knowledge", "C:/Users/sarkara1/git/SIMPOM/psl/psl_ind_1.owl"});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testA1PlanNoService2(){
		try {
			FunQL.main(new String[]
					{"-query", "C:/Users/sarkara1/git/sparkle/resources/META-INF/query/select-psl-ax14-1.q", 
					 "-belief",  "https://raw.githubusercontent.com/arsarkar/SIMPOM/master/psl/psl_ext_2.0.owl", 
					 "-knowledge", "C:/Users/sarkara1/git/SIMPOM/psl/psl_ind_2.owl"});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testA1PlanNoService_cone_volume(){
		try {
			FunQL.main(new String[]
					{"-query", "C:/Users/sarkara1/git/sparkle/resources/META-INF/query/select-volume-cone.q", 
					 "-belief",  "http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl", 
					 "-knowledge", "C:/Users/sarkara1/git/SIMPOM/geometry/geom-ind1.owl"});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testB1PlanNoService1(){
		try {
			FunQL.main(new String[]
					{"-query", "C:/Users/sarkara1/git/sparkle/resources/META-INF/query/construct-psl-ax14-1.q", 
					 "-belief",  "https://raw.githubusercontent.com/arsarkar/SIMPOM/master/psl/psl_ext_2.0.owl", 
					 "-knowledge", "C:/Users/sarkara1/git/SIMPOM/psl/psl_ind_2.owl"});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	@Test
	public void testB2Plan1() {
		try {
			FunQL.main(new String[]
								{"-query", "C:/Users/sarkara1/git/sparkle/resources/META-INF/query/construct-volume-cone1.q", 
								 "-service", "C:/Users/sarkara1/git/sparkle/resources/META-INF/services/calculateVolumeCone.json" ,
								 "-belief",  "http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl", 
								 "-knowledge", "C:/Users/sarkara1/git/SIMPOM/geometry/geom-ind1.owl"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testB2APlan1() {
		try {
			FunQL.main(new String[]
								{"-query", folder + "funql/resources/META-INF/query/construct-volume-cone2.q", 
								 "-service", folder + "funql/resources/META-INF/services/calculateVolumeCone.json" ,
								 "-belief",  "http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl", 
								 "-knowledge", folder + "SIMPOM/geometry/geom-ind1.owl"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void makeCubeFromCone() {
		try {
			FunQL.main(new String[]
								{"-query", folder + "funql/resources/META-INF/query/make-cube-from-cone.q", 
								 "-service", folder + "funql/resources/META-INF/services/make-cube-from-cone.json" ,
								 "-belief",  "http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl", 
								 "-knowledge", folder + "SIMPOM/geometry/cube-ind1.owl"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testQueryService() {
		try {
			Uni.of(FunQL::new).get()
			 .addTBox("http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl")
			 .addABox("C:/Users/sarkara1/git/SIMPOM/geometry/geom-ind1.owl")
			 .addPlan("C:/Users/sarkara1/git/funql/resources/META-INF/query/construct-volume-cone3.q")
			 .execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testQueryService1() {
		Uni.of("C:/Users/sarkara1/git/SIMPOM/geometry/geom-ind3.owl")
		.map(File::new)
		.map(FileOutputStream::new)
	    .map(os->Uni.of(FunQL::new).get()
				 .addTBox("http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl")
				 .addABox("https://raw.githubusercontent.com/arsarkar/SIMPOM/master/geometry/geom-ind1.owl")
				 .addPlan("C:/Users/sarkara1/git/funql/resources/META-INF/query/construct-volume-cone3.q")
				 .execute()
				 .getBelief()
				 .getaBox().write(os))
	    .onFailure(e->e.printStackTrace());
	}
	
	@Test
	public void testMakeCube() {		
			Uni.of("C:/Users/sarkara1/git/SIMPOM/geometry/geom-ind4.owl")
				.map(File::new)
				.map(FileOutputStream::new)
			    .map(os->Uni.of(FunQL::new).get()
						 .addTBox("http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl")
						 .addABox("https://raw.githubusercontent.com/arsarkar/SIMPOM/master/geometry/geom-ind1.owl")
						 .addPlan("C:/Users/sarkara1/git/funql/resources/META-INF/query/construct-cube-cone.q")
						 .execute()
						 .getBelief()
						 .getaBox().write(os))
			    .onFailure(e->e.printStackTrace());
	}
	
	@Test
	public void testCalSide() {		
			Uni.of(folder +"SIMPOM/geometry/geom-ind-square1.owl")
				.map(File::new)
				.map(FileOutputStream::new)
			    .map(os->Uni.of(FunQL::new).get()
						 .addTBox("http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl")
						 .addABox(folder +"SIMPOM/geometry/geom-ind-square.owl")
						 .addPlan(folder +"funql/resources/META-INF/query/calculate-vertex-square.q")
						 .execute()
						 .getBelief()
						 .getaBox().write(os))
			    .onFailure(e->e.printStackTrace());
	}
	
	@Test
	public void testSamplePattern1() {		
			Uni.of("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/apat2.owl")
				.map(File::new)
				.map(FileOutputStream::new)
			    .map(os->Uni.of(FunQL::new).get()
						 .addTBox("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/tpat1.owl")
						 .addABox("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/apat1.owl")
						 .addPlan("C:/Users/sarkara1/git/funql/resources/META-INF/query/sample-pattern1.q")
						 .execute()
						 .getBelief()
						 .getaBox().write(os))
			    .onFailure(e->e.printStackTrace());
	}
	
	@Test
	public void testSamplePatternAlt() {
			DummyFunction func = new DummyFunction("Hello");
			Uni.of("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat2.owl")
				.map(File::new)
				.map(FileOutputStream::new)
			    .map(os->Uni.of(FunQL::new).get()
						 .addTBox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\tpat1.owl")
						 .addABox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat1.owl")
						 .addPlan(Uni.of(ConstructBuilder::new)
								   .set(b->b.addPrefix("rdf", IMPM.rdf))
								   .set(b->b.addPrefix("tpat", "http://www.ohio.edu/ontologies/tpat1#"))
								   .set(b->b.addPrefix("dummy", "edu.ohiou.mfgresearch.functions.DummyFunction"))
								   .set(b->b.addWhere("?c1", "rdf:type", "tpat:class1"))
								   .set(b->b.addWhere("?c2", "rdf:type", "tpat:class2"))
								   .set(b->b.addWhere("?c1", "tpat:property1", "?c2"))
								   .set(b->b.addWhere("?c2", "tpat:dprop1", "?in"))
								   .set(b->b.addConstruct("?c4", "rdf:type", "tpat:class4"))
								   .set(b->b.addConstruct("?c2", "tpat:property3", "?c4"))
								   .set(b->b.addConstruct("?c4", "tpat:dprop2", "?out"))
								   .map(b->b.build())
								   .get().serialize(),
								   "?out",
								   "dummy:calDummyIn(?in)",
								   func)
						 .execute()
						 .getBelief()
						 .getaBox().write(os))
			    .onFailure(e->e.printStackTrace());
	}
	
	@Test
	public void testSamplePattern2() {		
			Uni.of("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/apat2.owl")
				.map(File::new)
				.map(FileOutputStream::new)
			    .map(os->Uni.of(FunQL::new).get()
						 .addTBox("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/tpat1.owl")
						 .addABox("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/apat1.owl")
						 .addPlan("C:/Users/sarkara1/git/funql/resources/META-INF/query/sample-pattern2.q")
						 .execute()
						 .getBelief()
						 .getaBox().write(os))
			    .onFailure(e->e.printStackTrace());
	}
	
	@Test
	public void testSamplePattern3() {		
			Uni.of("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/apat3.owl")
				.map(File::new)
				.map(FileOutputStream::new)
			    .map(os->Uni.of(FunQL::new).get()
						 .addTBox("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/tpat1.owl")
						 .addABox("C:/Users/sarkara1/git/funql/resources/META-INF/ontology/apat2.owl")
						 .addPlan("C:/Users/sarkara1/git/funql/resources/META-INF/query/sample-pattern3.q")
						 .execute()
						 .getBelief()
						 .getaBox().write(os))
			    .onFailure(e->e.printStackTrace());
	}
	
	@Test
	public void testSamplePattern2then3then4() {		
			Uni.of(folder + "funql/resources/META-INF/ontology/apat4.owl")
				.map(File::new)
				.map(FileOutputStream::new)
			    .map(os->Uni.of(FunQL::new).get()
						 .addTBox(folder + "funql/resources/META-INF/ontology/tpat1.owl")
						 .addABox(folder + "funql/resources/META-INF/ontology/apat1.owl")
						 .addPlan(folder + "funql/resources/META-INF/query/sample-pattern2.q")
						 .addPlan(folder + "funql/resources/META-INF/query/sample-pattern3.q")
						 .addPlan(folder + "funql/resources/META-INF/query/sample-pattern4.q")
						 .execute()
						 .getBelief()
						 .getaBox().write(os))
			    .onFailure(e->e.printStackTrace());
	}
	
	@Test
	public void testSamplePattern5() {		
			Uni.of("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat5.owl")
				.map(File::new)
				.map(FileOutputStream::new)
			    .map(os->Uni.of(FunQL::new).get()
						 .addTBox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\tpat1.owl")
						 .addABox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat1.owl")
						 .addPlan("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\query\\sample-pattern5.q")
						 .execute()
						 .getBelief()
						 .getaBox().write(os))
			    .onFailure(e->e.printStackTrace());
	}
	
	@Test
	public void testSamplePattern6() {		
			Uni.of("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat7.owl")
				.map(File::new)
				.map(FileOutputStream::new)
			    .map(os->Uni.of(FunQL::new).get()
						 .addTBox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\tpat1.owl")
						 .addABox("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\ontology\\apat6.owl")
						 .addPlan("C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\query\\sample-pattern5.q")
						 .execute()
						 .getBelief()
						 .getaBox().write(os))
			    .onFailure(e->e.printStackTrace());
	}
}

package edu.ohiou.mfgresearch.funql;

import org.junit.Before;
import org.junit.Test;


import edu.ohiou.mfgresearch.io.FunQL;

public class TestFunQL {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testA1PlanNoService1(){
		try {
			FunQL.main(new String[]
					{"-query", "C:\\Users\\sarkara1\\git\\sparkle\\resources\\META-INF\\query\\select-psl-before.q", 
					 "-belief",  "https://raw.githubusercontent.com/arsarkar/SIMPOM/master/psl/psl_ext_2.0.owl", 
					 "-knowledge", "C:\\Users\\sarkara1\\git\\SIMPOM\\psl\\psl_ind_1.owl"});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testA1PlanNoService2(){
		try {
			FunQL.main(new String[]
					{"-query", "C:\\Users\\sarkara1\\git\\sparkle\\resources\\META-INF\\query\\select-psl-ax14-1.q", 
					 "-belief",  "https://raw.githubusercontent.com/arsarkar/SIMPOM/master/psl/psl_ext_2.0.owl", 
					 "-knowledge", "C:\\Users\\sarkara1\\git\\SIMPOM\\psl\\psl_ind_2.owl"});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testA1PlanNoService_cone_volume(){
		try {
			FunQL.main(new String[]
					{"-query", "C:\\Users\\sarkara1\\git\\sparkle\\resources\\META-INF\\query\\select-volume-cone.q", 
					 "-belief",  "http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl", 
					 "-knowledge", "C:\\Users\\sarkara1\\git\\SIMPOM\\geometry\\geom-ind1.owl"});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testB1PlanNoService1(){
		try {
			FunQL.main(new String[]
					{"-query", "C:\\Users\\sarkara1\\git\\sparkle\\resources\\META-INF\\query\\construct-psl-ax14-1.q", 
					 "-belief",  "https://raw.githubusercontent.com/arsarkar/SIMPOM/master/psl/psl_ext_2.0.owl", 
					 "-knowledge", "C:\\Users\\sarkara1\\git\\SIMPOM\\psl\\psl_ind_2.owl"});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	@Test
	public void testB2Plan1() {
		try {
			FunQL.main(new String[]
								{"-query", "C:\\Users\\sarkara1\\git\\sparkle\\resources\\META-INF\\query\\construct-volume-cone1.q", 
								 "-service", "C:\\Users\\sarkara1\\git\\sparkle\\resources\\META-INF\\services\\calculateVolumeCone.json" ,
								 "-belief",  "http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl", 
								 "-knowledge", "C:\\Users\\sarkara1\\git\\SIMPOM\\geometry\\geom-ind1.owl"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testB2APlan1() {
		try {
			FunQL.main(new String[]
								{"-query", "C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\query\\construct-volume-cone2.q", 
								 "-service", "C:\\Users\\sarkara1\\git\\funql\\resources\\META-INF\\services\\calculateVolumeCone.json" ,
								 "-belief",  "http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl", 
								 "-knowledge", "C:\\Users\\sarkara1\\git\\SIMPOM\\geometry\\geom-ind1.owl"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}

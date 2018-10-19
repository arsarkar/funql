package edu.ohiou.mfgresearch.sparkle;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;

import Ontology.partloader.IMPlanXMLLoader;
import Ontology.partloader.PartFeatureLoader;
import edu.ohiou.mfgresearch.simplanner.PartRDFGenerator;

public class SIMPlannerTest {

	@Test
	public void PartFeatureLoading1() {
		PartFeatureLoader loader = new  IMPlanXMLLoader();
		PartRDFGenerator gen = new PartRDFGenerator(loader );
		try {
			gen.setUp();
			gen.load();
			OutputStream stream = new FileOutputStream(new File("C:/Users/sarkara1/git/SIMPOM/impm-ind/partrdf/features2.rdf"));
			gen.getModel().write(stream, "RDFXML");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

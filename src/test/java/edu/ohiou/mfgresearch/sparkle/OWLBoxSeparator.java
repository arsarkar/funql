package edu.ohiou.mfgresearch.sparkle;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.jena.graph.compose.Difference;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.simplanner.OWLganizer;

public class OWLBoxSeparator {

//	@Test
//	public void testABoxExtractionBySelect() {
//		Model abox = 
//		Anything.of(OWLganizer::createDefaultModel)
//				.map(m->OWLganizer.readModel(m, "https://raw.githubusercontent.com/InfoneerTXST/Vocabularies/master/MSDL_Ontology.owl"))
//				.map(OWLganizer::extractABox)
//				.map(ModelFactory::createModelForGraph)
//				.get();
//		
////		Anything.of(()->new File("C:/Users/sarkara1/git/Vocabularies/MSDL_ABox.owl"))
////				.map(file->new FileOutputStream(file))
////				.map(fs->abox.write(fs, "RDFXML"));
//	}
	
	@Test
	public void testTBoxExtractionByConstrucct() {
		
		Model tbox = 
		Uni.of(OWLganizer::createDefaultModel)
				.map(m->OWLganizer.readModel(m, "https://raw.githubusercontent.com/InfoneerTXST/Vocabularies/master/MSDL_Ontology.owl"))
				.map(m->OWLganizer.getTBox(m))
				.get();
		
		Uni.of(()->new File("C:/Users/sarkara1/git/Vocabularies/MSDL_TBox.owl"))
				.map(file->new FileOutputStream(file))
				.map(fs->tbox.write(fs, "RDFXML"));
	}
	
	
}

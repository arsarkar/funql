package edu.ohiou.mfgresearch.sparkle;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import edu.ohiou.mfgresearch.lambda.Uni;
import ru.avicomp.ontapi.OntManagers;
import ru.avicomp.ontapi.OntologyManager;
import ru.avicomp.ontapi.OntologyModel;
import uk.ac.manchester.cs.jfact.JFactFactory;


public class TestYourBelief {

	OntModel tbox = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM); 
	OntModel abox = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM); 
	public String ns = "http://www.example.com/case1#";
	
	
	@Before
	public void setUp() throws Exception {
		tbox.read("https://raw.githubusercontent.com/arsarkar/SIMPOM/master/unversioned/infer-feature-type.owl", "RDFXML");
		System.out.println("####OWL T-Box---------------------------------------------------------------->");
		//tbox.write(System.out, "RDFXML");
		abox.read("https://raw.githubusercontent.com/arsarkar/SIMPOM/master/unversioned/infer-feature-ins.owl", "RDFXML");
		System.out.println("####OWL A-Box---------------------------------------------------------------->");
		//abox.write(System.out, "RDFXML");
	}
	
	@Test
	public void testConversionOWLAPI() {
	    // Pass to ONT-API
	    OntologyManager manager = OntManagers.createONT();
	    OntologyModel ontoTbox = manager.addOntology(tbox.getGraph());
	    
	}

	@Test
	public void testInferenceModel() {
		OntologyManager manager = OntManagers.createONT();
	    OntologyModel ontoTbox = manager.addOntology(tbox.getGraph());
		OWLReasoner reasoner =
				Uni.of(JFactFactory::new)
				   .map(fct->fct.createReasoner(ontoTbox, new SimpleConfiguration(500000)))
				   .onFailure(e->e.printStackTrace())
				   .get();	
				reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
	}
	
	@Test
	public void testMergeOntology() {
		OntologyManager manager = OntManagers.createONT();
	    manager.addOntology(tbox.getGraph());
	    manager.addOntology(abox.getGraph());
	    OWLOntologyMerger merger = new OWLOntologyMerger(manager);
	    OWLOntology mOnto = null;
	    try {
			mOnto = merger.createMergedOntology(manager, IRI.create("https://example.com/case1merged"));
				
	    } catch (OWLOntologyCreationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    try {
			mOnto.saveOntology(System.out);
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void loadOntologyWithImportAndReason() throws OWLOntologyCreationException {
		OntologyManager manager = OntManagers.createONT();
	    manager.addOntology(tbox.getGraph());
	    manager.addOntology(abox.getGraph());
	    OWLOntologyMerger merger = new OWLOntologyMerger(manager);
	    OWLOntology mOnto = null;
	    try {
			mOnto = merger.createMergedOntology(manager, IRI.create("https://example.com/case1merged"));
				
	    } catch (OWLOntologyCreationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
//		Uni.of(IRI.create("https://raw.githubusercontent.com/arsarkar/SIMPOM/master/unversioned/infer-feature-type.owl"))
//		   .map(iri->man.getOWLDataFactory().getOWLImportsDeclaration(iri))
//		   .set(imp->man.applyChange(new AddImport(onto, imp)))
//		   .onFailure(e->e.printStackTrace())
//		   .onSuccess(imp->onto.saveOntology(System.out));
		OWLReasoner reasoner = new JFactFactory().createReasoner(mOnto, new SimpleConfiguration(500000));
		
		reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
		NodeSet<OWLClass> types =
		reasoner.getTypes(manager.getOWLDataFactory().getOWLNamedIndividual(IRI.create("http://www.example.com/case1/ind#pocket1")), false);
		types.forEach(c->System.out.print(c.toString()+"  "));
		// To generate an inferred ontology we use implementations of
		// inferred axiom generators
		List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
		gens.add(new InferredClassAssertionAxiomGenerator());
		// Put the inferred axioms into a fresh empty ontology.
		OWLOntology infOnt = null;
		infOnt = manager.createOntology();
		InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner,
				gens);
		iog.fillOntology(manager.getOWLDataFactory(), infOnt);
		try {
			infOnt.saveOntology(System.out);
		} catch (OWLOntologyStorageException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}

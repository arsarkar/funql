package edu.ohiou.mfgresearch.belief;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.pfunction.library.version;
import org.semanticweb.owlapi.model.OWLClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.functions.Func;
import ru.avicomp.ontapi.OntManagers;
import ru.avicomp.ontapi.OntologyManager;
import ru.avicomp.ontapi.OntologyModel;

public class Belief {
	
	static Logger log = LoggerFactory.getLogger(Belief.class);

	List<String> tBoxURLs = new LinkedList<String>(); //stores the URLs supplied 
	List<String> aBoxURLs = new LinkedList<String>(); //stores the URLs supplied
	
	OntologyManager manager = OntManagers.createONT(); //OntoAPI manager, used in collecting and merging ontologies
	private String lang = "RDFXML";  //ontology syntax e.g. "RDFXML"
	OntologyModel tBox = null; //merged ontology 
//	Model tBoxGraph = null;
	Model aBox = null; //Jena RDF model used to store individuals loaded from KB 
	Model localABox = ModelFactory.createDefaultModel(); //Another local RDF model used for storing instances created from the query

	/**
	 * Utility class working as structure for ontology sources
	 * contains an URL and format (e.g. "RDFXML", "TURTLE" etc.)
	 */
//	public class OntoSource {
//		public String uri = "";
//		public String lang = "";
//		public OntoSource(String uri, String lang) {
//			this.uri = uri;
//			this.lang = lang;
//		}
//	}
	
	public Belief(String lang)
	{
		this.lang = lang;
	}
	
	/**
	 * Add a terminology box from the given Ontosource
	 * overwrites!
	 * @param tbox
	 */
	public void addTBox(String tbox){
		if(tbox.length()==0) log.error("Given tbox url is empty");
		tBox=
		Uni.of(ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM))
		 	.set(model->model.read(tbox.trim()))
		 	.map(model->manager.addOntology(model.getGraph()))
			.onFailure(e->log.error("Failed to load T-box from " + tbox + "due to" + e.getMessage()))
		 	.get();
		//log.info("T-Box added : " + gettBox().toString());
//		need to apply the logic for merging multiple ontology, right now only loads one ontology (overwrites existing tbox)
//		if(tBoxGraph!=null) tBox = manager.addOntology(tBoxGraph.getGraph());
	}
	
	public void addTBox(OntModel model){
		if(model!=null) tBox = Uni.of(model).map(m->manager.addOntology(model.getGraph())).get();
		//log.info("T-Box added : " + gettBox().toString());
	}
	
	/**
	 * add an abox but overwrites
	 * @param abox
	 */
	public void addABox(String abox) {
		if(abox.length()==0) log.error("Given abox url is empty");
		if(aBox == null) aBox =  ModelFactory.createDefaultModel();
		Uni.of(ModelFactory.createDefaultModel())
		   .map(model->model.read(abox.trim(), "RDFXML"))
		   .set(m->aBox.add(m))
		   .onFailure(e->{
			   log.error("Failed to load a-box from " + abox + " due to" + e.getMessage());
			   addEmptyABox(abox);
		   })
		   .get();
		//log.info("A-Box added : " + aBox.toString());
//		Uni.of(abox)
//		.filter(s->!s.isEmpty())
//		.map(t->collectGraphs(t))
//		.map(f->f.apply(aBox));
	}
	
	public void addABox(Model m){
		if(aBox == null) aBox =  ModelFactory.createDefaultModel();
		aBox.add(m);
		//log.info("A-Box added : " + aBox.toString());
	}
	
	/**
	 * Create an empty ABox with the supplied URL 
	 * as base IRI
	 * @param aboxIRI
	 */
	public void addEmptyABox(String aboxIRI){
		aBox = Uni.of(ModelFactory.createDefaultModel())
				  .set(m->m.setNsPrefix("", aboxIRI))
				  .get();
	}
	
	/**
	 * Constructor which takes a list of IRIs for t-box and List of IRI to t-Boxes 
	 * @param aBoxes 
	 * @param tBoxes
	 */
	public Belief(List<String> tBoxes, List<String> aBoxes) {
		//instantiate ONTAPI manager
		manager = OntManagers.createONT();
		//load tbox by ONTAPI		
		Omni.of(tBoxes)
			.set(t->addTBox(t));	
		Omni.of(aBoxes)
			.set(a->addABox(a));
	}
	
	/**
	 * Simple constructor taking one abox and tbox source
	 * from outside, localABox is populated by an event when
	 * not supplied
	 * @param abox
	 * @param tbox
	 */
	public Belief(String tbox, String abox){
		addTBox(tbox);
		addABox(abox);
	}
	
//	/**
//	 * Simple constructor taking just t-boxes as there is no A-box to supply 
//	 * from outside, localABox is populated by an event
//	 * @param abox
//	 * @param tbox
//	 */
//	public Belief(String tbox){
//		Uni.of(tbox)
//			.map(Belief::collectGraphs)
//			.map(f->f.apply(tBoxGraph));
//		tBox = manager.addOntology(tBoxGraph.getGraph());
//	}
	
	/**
	 * Naked constructor taking t-box and a-box Models 
	 * from outside, localABox is populated by an event
	 * @param abox
	 * @param tbox
	 */
	public Belief(OntologyModel tbox, Model abox){
		this.aBox = abox;
		this.tBox = tbox;
	}
	
	/**
	 * returns a function, which receives a Model and reads the given OntoSource 
	 * into the model and returns it, new changes are added as delta
	 * @param source
	 * @return
	 */
	public Func<Model, Model> collectGraphs(String source){
		
		return  m->{
					return
					Uni.of(ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM))
						.map(mn->mn.read(source, lang ))
						.map(mn->ModelFactory.createUnion(m, mn))
						.get();
				};
	}
	

	public OntologyModel gettBox() {
		return tBox;
	}

	public Model getaBox() {
		return aBox;
	}

	public String writeaBox(String format){
		StringWriter s = new StringWriter();
		aBox.write(s, format);
		s.flush();
		return s.toString();
	}

	public Model getLocalABox() {
		return localABox;
	}

	public Map<String, String> getPrefixMap(){
		return tBox.getFormat()
				   .asPrefixOWLDocumentFormat()
				   .getPrefixName2PrefixMap();					
	}
	 
	 /**
	  * Write basic pattern
	  * @param pat
	  * @return
	  */
	 public String writePattern(BasicPattern pat) {
		 StringBuilder s = new StringBuilder();
		 PrefixMapping pm = PrefixMapping.Factory.create();
		 //getall prefix 
		 Map<String, String> nsMap = tBox.asGraphModel().getNsPrefixMap();
		 nsMap.putAll(aBox.size()>0?aBox.getNsPrefixMap():new HashMap<String, String>());
		 nsMap.putAll(localABox.size()>0?localABox.getNsPrefixMap():new HashMap<String, String>());
		 pm.setNsPrefixes(nsMap);
		 pat.forEach(t->{
			 s.append(t.getSubject().toString(pm)).append(" ");
			 s.append(t.getPredicate().toString(pm)).append(" ");
			 s.append(t.getObject().toString(pm)).append("\n");
		 });		 
		 return s.toString();
	 }
	 
	 /**
	  * Write basic pattern
	  * @param pat
	  * @return
	  */
	 public String writeBinding(Binding bind) {
		 StringBuilder s = new StringBuilder();
		 PrefixMapping pm = PrefixMapping.Factory.create();
		 //getall prefix 
		 Map<String, String> nsMap = tBox.asGraphModel().getNsPrefixMap();
		 nsMap.putAll(aBox.size()>0?aBox.getNsPrefixMap():new HashMap<String, String>());
		 nsMap.putAll(localABox.size()>0?localABox.getNsPrefixMap():new HashMap<String, String>());
		 pm.setNsPrefixes(nsMap);
		 bind.vars().forEachRemaining(v->{
			 s.append(v.toString()).append("->").append(bind.get(v).toString(pm)).append(", ");
		 });
		 return s.toString().substring(0, s.length()-2);
	 }
	 
	 /**
	  * Write basic pattern
	  * @param pat
	  * @return
	  */
	 public String writeTable(Table tab) {
		 StringBuilder s = new StringBuilder();
		 PrefixMapping pm = PrefixMapping.Factory.create();
		 //getall prefix 
		 Map<String, String> nsMap = tBox.asGraphModel().getNsPrefixMap();
		 nsMap.putAll(aBox.size()>0?aBox.getNsPrefixMap():new HashMap<String, String>());
		 nsMap.putAll(localABox.size()>0?localABox.getNsPrefixMap():new HashMap<String, String>());
		 pm.setNsPrefixes(nsMap);
		 tab.rows().forEachRemaining(b->s.append(writeBinding(b)).append("\n"));
		 return s.toString();
	 }

//	@Override
//	public Belief clone() throws CloneNotSupportedException {
//		return Uni.of(new Belief(this.lang))
//				  .set(b->b.tBoxURLs=this.tBoxURLs)
//				  .set(b->b.tBox=this.gettBox())
//				   .get();
//	}
	 
	 
}
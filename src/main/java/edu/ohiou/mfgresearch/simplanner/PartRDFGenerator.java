package edu.ohiou.mfgresearch.simplanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.CollectionGraph;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import Ontology.partloader.IMPlanXMLLoader;
import Ontology.partloader.PartFeatureLoader;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.lambda.functions.Func;
import edu.ohiou.mfgresearch.lambda.functions.Suppl;

public class PartRDFGenerator {

	PartFeatureLoader loader;
	private List<Triple> triples;
	private Graph g;
	private OntModel m;
	List<Individual> indList = new ArrayList<Individual>();
	List<String> nameList = new ArrayList<String>();
	Function<Individual, String> nameOfInd = i->nameList.get(indList.indexOf(i));
	Function<String, Individual> indOfName = s->indList.get(nameList.indexOf(s));
	Map<Literal, Individual> mapIBE = new HashMap<Literal, Individual>();
	
	public PartRDFGenerator(PartFeatureLoader loader){
		this.loader = loader;
	}
	
	/**
	 * Following the implementation of default model generation 
	 * https://github.com/apache/jena/blob/master/jena-extras/jena-querybuilder/src/test/java/org/apache/jena/arq/querybuilder/UpdateBuilderExampleTests.java
	 * Does it have ssame effect of ModelFactory.createDefaultModel()?
	 */
	Suppl<Model> defaultModelGen = ()->{
		triples = new ArrayList<Triple>();
		g = new CollectionGraph(triples);
		return ModelFactory.createDefaultModel(); //cannot create model from empty graph
	};
	
	Func<Model, OntModel> defaultOntGen = m->{
		return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, m);
	};
	
	Func<String, OntModel> importModel = url->{
		return (OntModel) ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM).read(url);
	};
	
	Func<OntModel, Func<OntClass, Individual>> 
		typedIndividual =m->c->m.createIndividual(IMPM.design_ins+c.getLocalName().toLowerCase()+IMPM.newHash(4), c);	
	
	public void setUp() throws Exception {
		
		loader = new IMPlanXMLLoader();
		m = defaultOntGen.apply(defaultModelGen.get());
		m.clearNsPrefixMap();
		m.setNsPrefix("cpm", IMPM.cpm);
		m.setNsPrefix("rdf", IMPM.rdf);
		m.setNsPrefix("owl", IMPM.owl);
		m.setNsPrefix("impmu", IMPM.impmu);
		m.setNsPrefix("dc", IMPM.dcelements);
	}
	
	public void load(){
		Individual part = loadPartName(loader.readPartName());
		List<Individual> features = loadFeatures(part, loader.readFeatures());
		features = loadFeaturePrecedence(features);
		features = loadSpecification(features);
		
	}
	
	private List<Individual> loadSpecification(List<Individual> features) {
		features.forEach(f->{
			Map<String, String> dimensions = loader.readFeatureDimensions(nameOfInd.apply(f));
			dimensions.forEach((k,v)->{
				System.out.println(k+v);
				Uni.of(()->typedIndividual.apply(m)
											   .apply(m.createClass(IMPM.impml+k))
							)
						.set(si->{
							f.addProperty(m.createObjectProperty(IMPM.hasSpecification), si);
						})
						.set(si->{
							si.addProperty(m.createObjectProperty(IMPM.prescribed_by), makeICE(k, v));
						});
			}); 
			Map<String, String> tolerances = loader.readTolerances(nameOfInd.apply(f));
			tolerances.forEach((k,v)->{
				System.out.println(k+v);
				Uni.of(()->typedIndividual.apply(m)
											   .apply(m.createClass(IMPM.impml+k))
							)
						.set(si->{
							f.addProperty(m.createObjectProperty(IMPM.hasSpecification), si);
						})
						.set(si->{
							si.addProperty(m.createObjectProperty(IMPM.prescribed_by), makeICE(k, v));
						});
			});
		});
		
		return features;
	}

	private Individual makeICE(String k, String v) {
		return
		Uni.of(()->{
					return
					typedIndividual.apply(m)
								   .apply(m.createClass(IMPM.impml+k+"ContentEntity"));
						})
				.set(ice->{
					ice.addProperty(m.createObjectProperty(IMPM.inheres_in), 
								    makeIBE(m.createTypedLiteral(Double.parseDouble(v))));
				})
				.get();
	}

	private Individual makeIBE(Literal v) {
		return mapIBE.get(v)==null?
						 makeNewIBE(v):
							mapIBE.get(v);
	}

	private Individual makeNewIBE(Literal v) {
		
		return 
				Uni.of(()->{
					return
					typedIndividual.apply(m)
								   .apply(m.createClass(IMPM.InformationBearingEntity));
						})
				.set(ibe->{
					ibe.addProperty(m.createProperty(IMPM.has_decimal_value), 
								    v);
					mapIBE.put(v, ibe);
				})
				.get();
	}

	private List<Individual> loadFeaturePrecedence(List<Individual> features){
		features.forEach(f->{
			String nf = loader.readNextFeature(nameOfInd.apply(f));
			String pf = loader.readPreviousFeature(nameOfInd.apply(f));
			System.out.println(nf + "<>" + pf);
			if(nf!=null) f.addProperty(m.createObjectProperty(IMPM.hasNextFeature), indOfName.apply(nf));
			if(pf!=null) indOfName.apply(pf).addProperty(m.createProperty(IMPM.hasNextFeature), f);
		});
		
		return features;
	}
	
	Function<String, Function<Individual, Individual>> makeFeature = f->part->{
		return
		Uni.of(()->typedIndividual.apply(m)
				.apply(m.createClass(IMPM.Feature)))
		.set(i->part.addProperty(m.createObjectProperty(IMPM.hasFeature), i))
		.set(i->i.addProperty(m.createAnnotationProperty(IMPM.label), m.createTypedLiteral(f)))
		.set(i->indList.add(i))
		.set(i->nameList.add(f))
		.onFailure(e->e.printStackTrace())
		.get();
	};

	private List<Individual> loadFeatures(Individual part, List<String> features) {
		return
		features.stream()
				.map(f->{
								
								Individual nf = makeFeature.apply(f).apply(part);
								System.out.println(nf.toString());
								return nf;
							}
						)
				.collect(Collectors.toList());
		
	}

	private Individual loadPartName(String partName) {
		Uni.of(m).map(m->m.createClass(IMPM.Part)).get();
		OntClass part = m.getOntClass(IMPM.Part);
		return
		Uni.of(m).map(typedIndividual)
					  .map(f->f.apply(part))
					  .set(t->t.addProperty(m.createAnnotationProperty(IMPM.label), m.createTypedLiteral(partName)))
					  .set(i->indList.add(i))
					  .set(i->nameList.add(partName))
					  .get();
			
	}
	
	public OntModel getModel(){
		return m;
	}
	
}

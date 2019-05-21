package edu.ohiou.mfgresearch.service.invocation;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.lambda.functions.Suppl;
import edu.ohiou.mfgresearch.simplanner.IMPM;

/**
 * Create an individual using type name and random hash string
 * in the namespace provided
 * @author sarkara1
 *
 */
public class DefaultIndividualSupplier extends AbstractServiceInvoker {

	static Logger log = LoggerFactory.getLogger(DefaultIndividualSupplier.class);
	//private OntModel aBox; //this needs to be replaced by the Belief as the updates need to be queued
	private String typeIRI;
	private String ns;
	
	public DefaultIndividualSupplier(ArgBinding outputBinding, String ns) {
		setOutputArgument(outputBinding);
		//store the type now if it is a concrete URI, otherwise wait for later.
		if(!outArgBinding.paramType.isVariable()){
			this.typeIRI = outArgBinding.paramType.getURI();			
		}
		this.ns = ns;
//		this.aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, aBox);
	}
	
	@Override
	public void setOutputArgument(ArgBinding binding) {
		// TODO Auto-generated method stub
		super.setOutputArgument(binding);
		if(!outArgBinding.paramType.isVariable()){
			this.typeIRI = outArgBinding.paramType.getURI();			
		}
	}



	@Override
	public Suppl<Table> invokeService(Binding input) {
		return ()->{
			Table res = TableFactory.create();
			Uni.of(ModelFactory.createOntologyModel())
			   .map(m->m.createIndividual(createIndividualIRI(), m.createClass(typeIRI)))
			   .set(i->log.info("Individual created --> " + i.toString()))
			   .set(i->res.addBinding(BindingFactory.binding(outArgBinding.var, i.asNode())))
			   .onFailure(e->log.error("Failed to create individual for the type " + typeIRI))
			   .get();
			return res;
		};
	}

	private String createIndividualIRI() {
		int islash = typeIRI.lastIndexOf("/");
		int ihash = typeIRI.lastIndexOf("#");
		String typeName = typeIRI.substring(islash>ihash?islash+1:ihash+1, typeIRI.length());
		if(ns.endsWith("#"))
			return ns + typeName + "_I" + IMPM.newHash(4);
		else if(ns.endsWith("/"))
			return ns + typeName + "_I" + IMPM.newHash(4);
		else
			return ns + "#" + typeName + "_I" + IMPM.newHash(4);
	}
	
	@Override
	public void setInputArgument(ArgBinding binding) {
		log.warn("DefaultIndividualSupplier do not accept any input argument binding");
	}
	
	/**
	 * Returns empty binding
	 */
	@Override
	public Binding createInputBinding(Binding b) {
		return BindingFactory.create();
	}
	
	

}

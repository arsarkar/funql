package edu.ohiou.mfgresearch.service.invocation;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import edu.ohiou.mfgresearch.lambda.functions.Suppl;

public interface ServiceInvoker {
	
	public Suppl<Table> invokeService(Binding input);

	void setInputArgument(ArgBinding binding);

	void setOutputArgument(ArgBinding binding);
	
	Binding createInputBinding(Binding b);

	ServiceInvoker clone() throws CloneNotSupportedException;
	
}

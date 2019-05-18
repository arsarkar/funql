package edu.ohiou.mfgresearch.service.invocation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;

import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.functions.Suppl;

public class AbstractServiceInvoker implements ServiceInvoker {

	List<ArgBinding> inputVars = new LinkedList<ArgBinding>(); 
	ArgBinding outArgBinding;
	
	@Override
	public void setInputArgument(ArgBinding binding) {
		inputVars.add(binding);
	}
	
	@Override
	public void setOutputArgument(ArgBinding binding){
		outArgBinding = binding;
	}

	@Override
	public Suppl<Table> invokeService(Binding input) {
		throw new NotImplementedException("invocation method is not implemented");
	}

	/**
	 * Default method for creating the input argument binding from the given 
	 * arguments, which are one of the row retuned by the query.
	 * Only variables which are in the input arg binding are returned as binding
	 */
	@Override
	public Binding createInputBinding(Binding b) {
		
		Binding nb = BindingFactory.create();
		Iterator<Var> itV = b.vars();
		while(itV.hasNext()){
			Var v = itV.next();
			//if the variable is present in input arg binding
			if(inputVars.stream()
					    .anyMatch(ab->{
					    	return ab.var.equals(v);
					    })){
				//transfer the binding for the variable to new binding
				nb = BindingFactory.binding(nb, v, b.get(v));
			}
		}
		return nb;
	}

	@Override
	public String toString() {
		return 	"JavaMethod_"+
				"("+ 
				inputVars.toString() +
				")->"+
				outArgBinding.toString();
	}
	
	
}

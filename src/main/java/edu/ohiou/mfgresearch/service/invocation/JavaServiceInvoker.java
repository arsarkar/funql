package edu.ohiou.mfgresearch.service.invocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.lambda.functions.Suppl;

public class JavaServiceInvoker extends AbstractServiceInvoker {

	static Logger log = LoggerFactory.getLogger(JavaServiceInvoker.class);
	Method method;
		
	public JavaServiceInvoker(Method m) {
		this.method = m;
	}

	@Override
	public Suppl<Table> invokeService(Binding input) {
		return ()->{
			return invokeJavaMethod(input);
		};
	}
	
	private Table invokeJavaMethod(Binding input){
		Table tab = TableFactory.create();
		Uni.of(input)
		   .map(in->createInputArguments(in))
		   .map(inp->{
			   return method.invoke(null, inp);
		   })
		   .map(out->createOutputBinding(out))
		   .set(ob->tab.addBinding(ob))
		   .onFailure(e->log.error("Failed to invoke query!!" + e.getMessage()));

		return tab;
	}

	private Binding createOutputBinding(Object out) {
		return
		Uni.of(outputVar)
		   .map(ov->{
			   Node oVal = NodeFactory.createLiteralByValue(out, ov.getVarType());
			   return BindingFactory.binding(ov.var, oVal);
		   })
		   .get();
	}

	private Object[] createInputArguments(Binding input) {
		Collections.sort(inputVars); // sort the arguments as per arg
		return
		Omni.of(inputVars)
			.map(ab->createInputObject(ab, input))
			.toList()
			.toArray();
	}

	private Object createInputObject(ArgBinding ab, Binding input) {
		//get the RDF data type from binding
		RDFDatatype dt = ab.getVarType(); 
		List<Object> inLit = new LinkedList<Object>();
		if(dt.getURI().equals(XSDDatatype.XSDinteger.getURI()) || dt.getURI().equals(XSDDatatype.XSDint.getURI())){
			inLit.add(input.get(ab.var).getLiteralValue());
		}
		else if(dt.getURI().equals(XSDDatatype.XSDdouble.getURI())){
			inLit.add(input.get(ab.var).getLiteralValue());
		}
		else if(dt.getURI().equals(XSDDatatype.XSDstring.getURI())){
			inLit.add(input.get(ab.var).getLiteralValue().toString());
		}
		return inLit.get(0);
	}
	
	
}

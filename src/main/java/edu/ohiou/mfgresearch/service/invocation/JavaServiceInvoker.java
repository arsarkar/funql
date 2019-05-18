package edu.ohiou.mfgresearch.service.invocation;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

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
import edu.ohiou.mfgresearch.lambda.functions.Func;
import edu.ohiou.mfgresearch.lambda.functions.Suppl;

public class JavaServiceInvoker extends AbstractServiceInvoker {

	static Logger log = LoggerFactory.getLogger(JavaServiceInvoker.class);
	Method method;
	Object instance;

	public JavaServiceInvoker(Method m) {
		this.method = m;
	}

	public JavaServiceInvoker(Method m, Object instance){
		this(m);
		this.instance = instance;
	}

	@Override
	public Suppl<Table> invokeService(Binding input) {
		return ()->{
			return invokeJavaMethod(input);
		};
	}

	private Table invokeJavaMethod(Binding input){

		return
				Uni.of(input)
				.map(in->createInputArguments(in))
				.map(inp->method.invoke(instance, inp))
				.map(out->createOutputBinding(out))
				.onFailure(e->log.error("Failed to invoke query!!" + e.getMessage()))
				.get();	
	}

	/**
	 * Create binding(s) from output of the method 
	 * should handle both single or collection output
	 * @param out
	 * @return
	 */
	private Table createOutputBinding(Object out) {

		Table tab = TableFactory.create();
		//check if the output is a collection
		if(out.getClass().isArray()){
			Uni.of(outArgBinding)
			.set(ov->{
				//				Omni.of(Arrays.asList(out))
				//					.map(o->NodeFactory.createLiteralByValue(o, ov.getVarType()))
				//					.map(n->BindingFactory.binding(ov.var, n))
				//					.set(b->tab.addBinding(b));
				IntStream.range(0, Array.getLength(out))
				.forEach(i->{
					if(ov.varType.getURI().equals(XSDDatatype.XSDanyURI.getURI())){
						tab.addBinding(BindingFactory.binding(ov.var, (Node)out));
					}else{
						Node oVal = NodeFactory.createLiteralByValue(Array.get(out, i), ov.getVarType());
						tab.addBinding(BindingFactory.binding(ov.var, oVal));									
					}
				});

			});
		}
		else{
			Uni.of(outArgBinding)
			.map(ov->{
				if(ov.varType.getURI().equals(XSDDatatype.XSDanyURI.getURI())){
					return BindingFactory.binding(ov.var, (Node)out);
				}else{
					Node oVal = NodeFactory.createLiteralByValue(out, ov.getVarType());
					return BindingFactory.binding(ov.var, oVal);						
				}
			})
			.set(b->tab.addBinding(b));
		}
		return tab;
	}

	private Object[] createInputArguments(Binding input) {
		Collections.sort(inputVars); // sort the arguments as per arg
		List<Object> args =
				Omni.of(inputVars)
				.map(ab->createInputObject(ab, input))
				.toList();
		return args.toArray(new Object[0]);
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
		else if(dt.getURI().equals(XSDDatatype.XSDanyURI.getURI())){
			inLit.add(input.get(ab.var));
		}
		return inLit.get(0);
	}


}

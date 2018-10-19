package edu.ohiou.mfgresearch.service.invocation;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;

public final class ArgBinding implements Comparable<ArgBinding> {

	int argPos = -1;
	String paramName = "";
	RDFNode paramType;
	Var var;
	RDFDatatype varType;
	
	public ArgBinding() {
	}
	
	public ArgBinding(String paramName, RDFNode paramType) {
		super();
		this.paramName = paramName;
		this.paramType = paramType;
	}
	
	public ArgBinding(int argPos, Var var, RDFDatatype xsdType) {
		super();
		this.argPos = argPos;
		this.var = var;
		this.varType = xsdType;
	}	

	public int getArgPos() {
		return argPos;
	}

	public void setArgPos(int argPos) {
		this.argPos = argPos;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public Var getVar() {
		return var;
	}

	public void setVar(Var var) {
		this.var = var;
	}

	public RDFNode getParamType() {
		return paramType;
	}

	public void setParamType(RDFNode paramType) {
		this.paramType = paramType;
	}

	public RDFDatatype getVarType() {
		return varType;
	}

	public void setVarType(RDFDatatype varType) {
		this.varType = varType;
	}

	public boolean equals(ArgBinding obj) {
		// TODO Auto-generated method stub
		return obj.argPos==this.argPos;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		if(paramType != null && paramType != null){
			return argPos + ":" + paramName + "(" + paramType.asResource().getLocalName() + ")" + "<->" + var.getName() + "(" + varType.toString() + ")";
		}
		else if(varType != null){
			return argPos + ":" + paramName + "<->" + var.getName() + "(" + varType.toString() + ")";
		}
		else{
			return argPos + ":" + paramName + "<->" + var.getName();
		}
	}

	@Override
	public int compareTo(ArgBinding o) {
		// TODO Auto-generated method stub
		return o.argPos<this.argPos?1:-1;
	}	

}

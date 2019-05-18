package edu.ohiou.mfgresearch.functions;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.ResourceFactory;

public class DummyFunction {
	
	String label = "";
	
	public DummyFunction(String label){
		this.label = label;
	}
	
	public static String[] calDummy(String input){
		String[] output = new String[3];
		output[0] = input + " & output1";
		output[1] = input + " & output2";
		output[2] = input + " & output3";
		return output;
	}

	public String[] calDummyIn(String input){
		String[] output = new String[3];
		output[0] = label + " universe!";
		output[1] = label + " soul!";
		output[2] = label + " truth!";
		return output;
	}
	
	public static String calDummyConcat(String in1, String in2, String in3){
		return in1 + "," + in2 + "," + in3;
	}

	public static Node calDummyIndi(Node c1, Node c2){
		return NodeFactory.createURI("http://www.ohio.edu/ontologies/apat1#Ic3420");
	}
	
	public static String getType(String input){
		return "http://www.ohio.edu/ontologies/tpat1#class4";
	}
}

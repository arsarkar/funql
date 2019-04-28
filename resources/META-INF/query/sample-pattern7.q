PREFIX tpat:<http://www.ohio.edu/ontologies/tpat1#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX dummy:<edu.ohiou.mfgresearch.functions.DummyFunction>

CONSTRUCT{	
	?c5 rdf:type tpat:class5.
	?c1 tpat:property5 ?c5.
	?c5 tpat:dprop5	?out.
}
WHERE{
	?c1 rdf:type tpat:class1.
	?c2 rdf:type tpat:class2.
	?c3 rdf:type tpat:class3.
	?c4 rdf:type tpat:class4.
	?c1 tpat:property2 ?c2.
	?c1 tpat:property3 ?c3.
	?c1 tpat:property4 ?c4.
	?c2 tpat:dprop1 ?in1.
	?c3 tpat:dprop3 ?in2.
	?c4 tpat:dprop4 ?in3.
}
FUNCTION{
	?out <- dummy:calDummyConcat(?in1, ?in2, ?in3) 
}


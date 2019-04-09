PREFIX tpat:<http://www.ohio.edu/ontologies/tpat1#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX dummy:<edu.ohiou.mfgresearch.functions.DummyFunction>

CONSTRUCT{	
	?c3 rdf:type tpat:class3.
	?c4 rdf:type tpat:class4.
	?c5 rdf:type tpat:class5.
	?c2 tpat:property2 ?c3.
	?c3 tpat:property3 ?c4.
	?c4 tpat:property3 ?c5.
	?c5 tpat:dprop2 ?out
}
WHERE{
	?c1 rdf:type tpat:class1.
	?c2 rdf:type tpat:class2.
	?c1 tpat:property1 ?c2.
	?c2 tpat:dprop1 ?in
}
FUNCTION{
	?out <- dummy:calDummy(?in) 
}


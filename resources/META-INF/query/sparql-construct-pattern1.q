PREFIX tpat:<http://www.ohio.edu/ontologies/tpat1#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX dummy:<edu.ohiou.mfgresearch.functions.DummyFunction>

CONSTRUCT{
	_:a tpat:dprop1 ?in	
}
WHERE{
	?c1 rdf:type tpat:class1.
	?c2 rdf:type tpat:class2.
	?c1 tpat:property1 ?c2.
	?c2 tpat:dprop1 ?in
}
PREFIX tpat:<http://www.ohio.edu/ontologies/tpat1#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX dummy:<edu.ohiou.mfgresearch.functions.DummyFunction>

CONSTRUCT{
	?c3 rdf:type tpat:class3.
	?c3 tpat:property3 ?c4
}
WHERE{
	?c4 rdf:type tpat:class4.
	?c4 tpat:dprop2 ?out
}


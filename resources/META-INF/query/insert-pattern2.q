PREFIX tpat: <http://www.ohio.edu/ontologies/tpat1#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

CREATE GRAPH <http://www.ohio.edu/ontologies/apat9>;
CREATE GRAPH <http://www.ohio.edu/ontologies/apat10>;
INSERT{
GRAPH <http://www.ohio.edu/ontologies/apat9>
	{	
		?c1 rdf:type tpat:class1.
		?c2 rdf:type tpat:class2.
		?c2 tpat:property1 ?c1.
	}
GRAPH <http://www.ohio.edu/ontologies/apat10>
	{
		?c1 rdf:type tpat:class1.
		?c3 rdf:type tpat:class3.
		?c3 tpat:property2 ?c1.	
	}
}
WHERE{
	?c1 rdf:type tpat:class1.
	?c2 rdf:type tpat:class2.
	?c3 rdf:type tpat:class3.
	?c1 tpat:property2 ?c2.
	?c1	tpat:property3 ?c3.
}


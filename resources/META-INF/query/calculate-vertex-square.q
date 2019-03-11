PREFIX astro:<http://www.astro.umd.edu/~eshaya/astro-onto/owl/astronomy.owl#>
PREFIX geom:<http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX ivao:<http://www.astro.umd.edu/~eshaya/astro-onto/owl/IVOAO.owl#>
PREFIX square:<edu.ohiou.mfgresearch.functions.Square>

CONSTRUCT{
	?s geom:definedBy ?v.
	?v rdf:type geom:vertex.
	?v ivao:hasValue ?p
}
WHERE{
	?s rdf:type geom:square.
	?l rdf:type geom:length.
	?s geom:definedBy ?l.
	?l ivao:hasValue ?len
}
FUNCTION{
	?p <- square:getVertices(?len)
}
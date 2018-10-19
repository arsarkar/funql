PREFIX astro:<http://www.astro.umd.edu/~eshaya/astro-onto/owl/astronomy.owl#>
PREFIX geom:<http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>

CONSTRUCT{
	?c geom:hasVolume ?v.
	?v rdf:type geom:volume
}
WHERE{
	?c astro:hasDiameter ?d.
	?c geom:hasHeight ?h.
	?c rdf:type geom:circularCone.
	?d rdf:type geom:diameter.
	?h rdf:type geom:height
}
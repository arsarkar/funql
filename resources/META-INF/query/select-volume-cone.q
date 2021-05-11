PREFIX astro:<http://www.astro.umd.edu/~eshaya/astro-onto/owl/astronomy.owl#>
PREFIX geom:<http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX ivao:<http://www.astro.umd.edu/~eshaya/astro-onto/owl/IVOAO.owl#>

SELECT
	?c ?d ?h
WHERE{
	?c astro:hasDiameter ?d.
	?c geom:hasHeight ?h.
	?c rdf:type geom:circularCone.
	?d rdf:type ivao:diameter.
	?h rdf:type ivao:height
	
}
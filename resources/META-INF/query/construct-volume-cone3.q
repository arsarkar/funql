PREFIX astro:<http://www.astro.umd.edu/~eshaya/astro-onto/owl/astronomy.owl#>
PREFIX geom:<http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX ivao:<http://www.astro.umd.edu/~eshaya/astro-onto/owl/IVOAO.owl#>
PREFIX conic:<edu.ohiou.mfgresearch.functions.ConicSection>

CONSTRUCT{
	?v ivao:hasVolume ?vol.
	?c geom:hasVolume ?v.
	?v rdf:type geom:volume
}
WHERE{
	?c astro:hasDiameter ?d.
	?c geom:hasHeight ?h.
	?d ivao:hasValue ?dia.
	?h ivao:hasValue ?ht.
	?c rdf:type geom:circularCone.
	?d rdf:type geom:diameter.
	?h rdf:type geom:height
}
FUNCTION{
	?vol <- conic:calculateVolume(?dia, ?ht)
}
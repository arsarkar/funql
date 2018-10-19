PREFIX astro:<http://www.astro.umd.edu/~eshaya/astro-onto/owl/astronomy.owl#>
PREFIX geom:<http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl#>

SELECT
	?c ?d ?h
WHERE{
	?c astro:hasDiameter ?d.
	?c geom:hasHeight ?h
}
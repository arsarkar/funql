PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX psl:<http://ontohub.org/PSL/psl_core#>
CONSTRUCT {?tp1 psl:before ?tp2}
	WHERE {?occ psl:begins ?tp1.
	       ?occ psl:ends ?tp2}
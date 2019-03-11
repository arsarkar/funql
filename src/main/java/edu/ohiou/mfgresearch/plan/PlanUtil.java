package edu.ohiou.mfgresearch.plan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.walker.Walker;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingUtils;
import org.apache.jena.sparql.syntax.Template;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import ru.avicomp.ontapi.OntologyModel;

public final class PlanUtil {

	static Logger log = LoggerFactory.getLogger(PlanUtil.class);
	
	/**
	 * Get the BasicPattern from a CONSTRUCT query
	 * @param q CONSTRUCT Query object
	 * @return
	 * @throws Exception is the query is not CONSTRUCT
	 */
	public static BasicPattern getConstructBasicPattern(Query q){
			Template t = q.getConstructTemplate();
			return t.getBGP();		
	}

	/**
	 * Extract the Basic Pattern from the query
	 * @param q
	 * @return
	 */
	public static BasicPattern getWhereBasicPattern(Query q) {
		PlanUtil.OPVbp extBP = new PlanUtil.OPVbp();
		Uni.of(q)
		.map(Algebra::compile)
		.set(op->Walker.walk(op, extBP));
		return extBP.getPat();
	}

	/**
	 * Convert Construct Query to Select Query
	 * by removing the Construct Pattern and 
	 * adding result vars 
	 * All triples with rdf:type is removed too.
	 * @param q
	 * @return
	 */
	public static Query convert2SelectQuery(Query q){
		SelectBuilder builder = 
		Uni.of(SelectBuilder::new)
			.set(b->b.addPrefixes(q.getPrefixMapping().removeNsPrefix("rdf").getNsPrefixMap()))
			.set(b->q.getProjectVars().forEach(v->b.addVar(v.toString()))) //same as adding project to algebra
			.set(b->Omni.of(getWhereBasicPattern(q).getList())
						.set(t->{
							if(!t.getPredicate().getLocalName().equals("type"))
								b.addWhere(t);
//							!t.getPredicate().getName().equals("rdf:type"), t->b.addWhere(t));
						}))
			.get();
		//add group by
		if(q.hasGroupBy()) 
			q.getGroupBy().forEachExpr((v, e)->builder.addGroupBy(v, e));
		return builder.build();
	}	

	/**
	 * Get list of vars which are part of select pattern
	 * @param q
	 * @return
	 */
	public static List<Var> getKnownVars(Query q){
		return q.getProjectVars();
	}

	/**
	 * get all vars from the select pattern
	 * @param q
	 * @return
	 */
	public static List<Var> getSelectVars(Query q){
		return  Omni.of(q.getResultVars())
					.map(v->Var.alloc(v))
					.toList();
	}
	
	/**
	 * get all vars from the construct pattern
	 * @param q
	 * @return
	 */
	public static List<Var> getConstructVars(Query q){
		Set<Var> uV = new HashSet<Var>();
		BasicPattern cPat =
				Uni.of(q)
				.map(PlanUtil::getConstructBasicPattern)
				.onFailure(e->e.printStackTrace())
				.get();
		cPat.forEach(t->{
			if(t.getSubject().isVariable()) uV.add(Var.alloc(t.getSubject()));
			if(t.getObject().isVariable()) uV.add(Var.alloc(t.getObject())); 
		});
		return new LinkedList<Var>(uV);
	}

	/**
	 * get the unknown vars declared in construct
	 * @param q
	 * @return
	 */
	public static List<Var> getUnknownVars(Query q){
		Set<Var> v1 = new HashSet<Var>(getConstructVars(q));
		Set<Var> v2 = new HashSet<Var>(getKnownVars(q));
		v1.removeAll(v2);
		return new ArrayList<Var>(v1);
	}
	
	/**
	 * collect all the vars from the given basic pattern 
	 * @param vars
	 * @return
	 */
	public static List<Var> getVars(BasicPattern pattern){
		List<Var> vars = new LinkedList<Var>();
		for(Triple t:pattern){
			if(t.getSubject().isVariable() && !vars.contains(Var.alloc(t.getSubject()))) vars.add(Var.alloc(t.getSubject()));
			if(t.getObject().isVariable() && !vars.contains(Var.alloc(t.getObject()))) vars.add(Var.alloc(t.getObject()));
		}
		return vars;
	}

	/**
	 * Convert ResultSet to Table
	 * Table already has method to convert back to resultset
	 * @param res
	 * @return
	 */
	public static Table toBindings(ResultSet res){
		Table tab = TableFactory.create();
		if(!res.hasNext()) log.warn("No result is retured by the query!"); 
		else log.info("Query retured result!");
		res.forEachRemaining(r->{
			log.info(r.toString());
			tab.addBinding(BindingUtils.asBinding(r));			
		});
		return tab;
	}
	
	/**
	 * Get the type of input Individual 
	 * @param m OntoModel
	 * @param URI URI of the individual
	 * @return
	 */
	public static OntClass getInputType(OntModel m, String URI){
		return m.getIndividual(URI).getOntClass();
	}


	/**
	 * Get the type of the output variable
	 * First check if a type statement is associated
	 * Otherwise check the property domain or range to infer type
	 * @param m OntoModel
	 * @param q Construct query: this is required to extract the type assertion
	 * @param v the unknown variable whose type needs to be asserted
	 * @return
	 */
//	public static List<OWLClass> getOutputType(OntologyModel m, Query q, Var v){
//		List<OWLClass> types = new LinkedList<OWLClass>();
//		//get all triples containing the variable
//		List<Triple> trips = getTriplesContainingVar(Uni.of(q).map(PlanUtil::getConstructBasicPattern).get(), v);
//		//check if any of the triples asserted a class
//		List<Triple> typeAsst = 
//				Omni.of(trips)
//				.filter(t->t.getPredicate().getName().equals("RDF:type"))
//				.toList();
//		//if type assertion found then return class
//		if(typeAsst.size()>0){
//			//it is assumed that only one type assertion is present in the query
//			IRI typeIRI = IRI.create(typeAsst.get(0).getObject().getURI());
//			types.add(getOWLClass(m, typeIRI));
//		}
//		else{
//			//infer class from the domain or range of one of the property assertion
//			//there may be more than one triples containing the var, but we will 
//			//consider only one triples for asssertion for now
//			//....on second thought, may be the one for domain and one for range
//			//of course this is a silly logic and no match to full blown type inference
//			//which should be done using a reasoner. reasoner.getType(OWLNamedIndividuaal ind)
//			Optional<OWLClass> domain =
//					trips.stream()
//					.filter(t->t.subjectMatches(v))
//					.map(t->t.getPredicate())
//					.map(p->getObjectProperty(m, IRI.create(p.getURI())))
//					.map(p->getDomainOfOProp(m, p))
//					.findFirst();
//			if(domain.isPresent()) 
//				types.add(domain.get());
//			else{
//				Optional<OWLClass> range =
//						trips.stream()
//						.filter(t->t.objectMatches(v))
//						.map(t->t.getPredicate())
//						.map(p->getObjectProperty(m, IRI.create(p.getURI())))
//						.map(op->getRangeOfOProp(m, op))
//						.findFirst();
//				if(range.isPresent()) 
//					types.add(range.get());
//				else{
//					//for construct type a dummy basic pattern may be asserted in the Tbox and run reasoner to assert type of the surrogate individual
//					
//				}	
//			}
//		}
//		return types;
//	}
	
	/**
	 * How to infer types? 
	 * 1. from domain and range. 
	 * 2. super types?
	 * 3. sub type axioms, should match sub set of basic pattern 
	 * !!no point fiddling with domain, let's assign the basic pattern in the ontology and infer
	 * every dummy individual should be asserted as sub type of owl:Thing, (if no type is asserted in the basic pattern)
	 * if there is data variable then obviously a dummy literal should be used, but it is difficult to guess the xsd data type of the literal.
	 * it is tested that arbitary datatype won't be recognized by the reasoner and no equivalence axiom can be written using data property 
	 * with a bottom XSD type
	 * Only way literal variables can be asserted if the XSD data type is asserted in the ranges of the data property.
	 * for now we will not support inference based on data property axioms
	 * @param v
	 * @param pat
	 * @param onto
	 * @return
	 */
//	public List<IRI> getInferredType(IPlan p, Belief b){
//		
//		
//		return null;
//	}
	

	
	

//	public IRI getAssertedType(Var v, BasicPattern pat){
//		
//		//first check if there is an explicit type declaration in the basic pattern supplied
//		List<Triple> typeAsserts =
//		Omni.of(pat.getList())
//			.filter(t->t.getPredicate().getName().equals("RDF:type"))
//			.toList();
//		
//		//throw an error if more than one type assertions are present in the basic pattern
//		Uni.of(typeAsserts)
//			.filter(ta->ta.size()>0)
//		    .onFailure(ta->log.error("There is more than one type asseertions for variable " + v.getName() + " in the pattern given"));
//		
//		//return the object as IRI
//		return
//		Uni.of(typeAsserts.get(0))
//		   .map(t->t.getObject())
//		   .map(c->c.getURI())
//		   .map(uri->IRI.create(uri))
//		   .get();
//	}
	

	
	public static boolean isObjectPropertyPresent(OntologyModel m, IRI iri){
		return getObjectProperty(m, iri)!=null ? true : false;
	}
	
	public static boolean isDataPropertyPresent(OntologyModel m, IRI iri){
		return getDataProperty(m, iri)!=null ? true : false;
	}
	
	public static OWLObjectProperty getObjectProperty(OntologyModel m, IRI iri){
		Set<OWLObjectProperty> properties = m.getObjectPropertiesInSignature(true);
		for(OWLObjectProperty op:properties){
			if (op.getIRI().toString().equals(iri.toString())) 
				return op;
		}
		return null;
	}
	
	public static OWLDataProperty getDataProperty(OntologyModel m, IRI iri){
		Set<OWLDataProperty> properties = m.getDataPropertiesInSignature(true);
		for(OWLDataProperty op:properties){
			if (op.getIRI().toString().equals(iri.toString())) 
				return op;
		}
		return null;
	}
	
	
	public static OWLClass getOWLClass(OntologyModel m, IRI iri){
		Set<OWLClass> classes = m.getClassesInSignature(true);
		return
		classes.stream()
			   .filter(c->c.getIRI().toString().equals(iri))
			   .findAny().get();
	}
	
	public static OWLClass getDomainOfOProp(OntologyModel m, OWLObjectProperty op){
		return m.getObjectPropertyDomainAxioms(op)
				.stream()
				.map(x->x.getDomain())
				.map(c->c.asOWLClass())
				.findAny().get();
	}
	
	public static OWLClass getRangeOfOProp(OntologyModel m, OWLObjectProperty op){
		return m.getObjectPropertyRangeAxioms(op)
				.stream()
				.map(x->x.getRange())
				.map(c->c.asOWLClass())
				.findAny().get();
	}
	
	public static OWLClass getDomainOfDProp(OntologyModel m, OWLDataProperty op){
		return m.getDataPropertyDomainAxioms(op)
				.stream()
				.map(x->x.getDomain())
				.map(c->c.asOWLClass())
				.findAny().get();
	}
	
	public static OWLDatatype getRangeOfDProp(OntologyModel m, OWLDataProperty op){
		return m.getDataPropertyRangeAxioms(op)
				.stream()
				.map(x->x.getRange())
				.map(c->c.asOWLDatatype())
				.findAny().get();
	}

	/**
	 * Extract the triples from the BasicPattern (p), which has the specified var (v)
	 * in the subject or object
	 * @param p
	 * @param v
	 * @return List of Triples
	 */
	static List<Triple> getTriplesContainingVar(BasicPattern p, Var v){
		return
				Uni.of(p)
				.map(bp->bp.getList())
				.get().stream()
				.filter(t->t.subjectMatches(v) || t.objectMatches(v))
				.collect(Collectors.toList());
	}
	
	 /**
	  * Loop through numbers from 1 to 2^length
	  * For each number if bit is set, append them
	  */
	 public static List<List<Var>> combinations(List<Var> vars) {
		  List<List<Var>> varComb = new LinkedList<List<Var>>();
		  int n = vars.size();
		  for (int i = 1; i < (1 << n); i++) {
			  List<Var> comb = new LinkedList<Var>();
			  for (int j = 0; j < n; j++) {
				  if ((i & 1<< j) != 0) {
					  comb.add(vars.get(j));
				  }
			  }
			  varComb.add(comb);
		  }
		  return varComb;
	 }
	 
	 

	/**
	 * an OpVisitor
	 * @author sarkara1
	 *
	 */
	static class OPVbp extends OpVisitorBase{

		public OPVbp(){			
		}

		BasicPattern pat = null;

		public BasicPattern getPat(){
			return pat;
		}

		@Override
		public void visit(OpBGP opBGP) {
			// TODO Auto-generated method stub
			pat =  opBGP.getPattern();
		}

	}


}

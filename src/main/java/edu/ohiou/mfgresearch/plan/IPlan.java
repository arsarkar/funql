package edu.ohiou.mfgresearch.plan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.lambda.functions.Pred;
import ru.avicomp.ontapi.OntologyModel;

public class IPlan {

	static Logger log = LoggerFactory.getLogger(IPlan.class);
	
	/**
	 * A1 - select type, no unknown variable
	 * B1 - construct type, no unknown variable
	 * A2A, select type, has unknown variable, has data variable in where clause
	 * B1A, //construct type, unknown variable, no data variable
	 * A2B, select type, unknown variable, no Data variable in where clause
	 * B1B  //construct type, unknown variable, has data variable
	 * @author sarkara1
	 *
	 */
	public enum PlanType{
		 A1,  //select type, no unknown variable
		 B1,  //construct type, no unknown variable
		 A2,  //select type, unknown variable but has no data variable
		 B2,  //construct type, unknown variable but has no data variable
		 A2A, //select type, unknown variable, no data variable in where clause
		 A2B, //select type, unknown variable, has data variable in where clause
		 A2C, //select type, unknown variable, has data variable in both where and select clause
		 B2A, //construct type, unknown variable, no data variable in where clause but in construct clause  
		 B2B, //construct type, unknown variable, has no data variable in where clause but in construct clause
		 B2C  //construct type, unknown variable, has data variable in both construct and where clause
	}
	
	private Query q;
	List<Var> vars = new ArrayList<Var>(); //all variables in the query
	List<Boolean> isDataVar =  new ArrayList<Boolean>(); //true = the var at index i is a data var,  
	List<Boolean> isKnownVar = new ArrayList<Boolean>(); //true = the var is in the select query thus grounded 
	Map<Var, List<String>> varTypes = new HashMap<Var, List<String>>();
	public PlanType type;
	private BasicPattern constructBP = new BasicPattern();
	private BasicPattern whereBP = new BasicPattern();
	
	
	public BasicPattern getConstructBasicPattern() {
		if(constructBP==null) constructBP = PlanUtil.getConstructBasicPattern(q);
		return constructBP;
	}
	
	public void add2constructBasicPattern(Triple t) {
		constructBP.add(t);
	}

	public BasicPattern getWhereBasicPattern() {
		if(whereBP==null) whereBP = PlanUtil.getWhereBasicPattern(q);
		return whereBP;
	}
	
	public void add2whereBasicPattern(Triple t) {
		whereBP.add(t);
	}
	
	public IPlan(){
	}
	
	/**
	 * Create a new Plan based on the supplied query
	 * either as file path or url or raw string
	 * @param q
	 */
	public IPlan(String q){
		Uni.of(q)
		   .map(u->QueryFactory.read(q)) //first treat the string as a path or url
		   .onFailure(e->{
			   log.error(e.getMessage());
			   this.q = Uni.of(q)
					   .map(u->QueryFactory.create(q)) //then treat the string as raq query
					   .onFailure(e1->log.error(e1.getMessage()))
					   .get();
		   })
		   .onSuccess(query->this.q=query);
	}
	
	public IPlan(Query q){
		this.q = q;
	}

	public Query getQuery(){
		return q;
	}	
	
	/**
	 * creates a query from the where and construct clauses 
	 * with group by
	 */
	public void createQuery(){
		
	}
	
	/**
	 * Deconstruct query to perform the following tasks
	 * - identify variable
	 * - determine the query type
	 * 
	 * @param onto
	 */
	public void deconstructQuery(OntologyModel onto){

		//quickly determine A1/B1
		Uni.of(q)
			.select(q0->q0.isSelectType(), q0->type=PlanType.A1)
			.select(q0->isPresentUnknownVar(q0), q0->type=PlanType.A2)
			.select(q0->q0.isConstructType(), q0->type=PlanType.B1)
			.select(q0->isPresentUnknownVar(q0), q0->type=PlanType.B2);

		//we can execute A1 and B1 without type determination
		if(type==PlanType.A1 || type==PlanType.B1){
			return;
		}

		//We still may not need to find a service if the query has no data variable in the post condition
		//but to detect data variables we need to tear apart the query anyway
		Uni.of(q)
			.set(q->deconstructWhereClause(q, onto))
			.select(q0->q0.isConstructType(), q0->deconstructConstructClause(q0, onto))
			.select(q0->q0.isSelectType(), q0->deconstructSelectClause(q0, onto));


		//there is at least one data variable
		if(isDataVar.contains(true)){
			if(q.isSelectType() && isDataVarInWhere() && !isDataVarInSelect()){
				type = PlanType.A2A; 
			}
			else if(q.isSelectType() && !isDataVarInWhere() && isDataVarInSelect()){
				type = PlanType.A2B; //that data var is bet to be unknown because it is not there in where clause
			}
			else if(q.isSelectType() && isDataVarInWhere() && !isDataVarInSelect()){
				type = PlanType.A2C; //but there is no guarantee that the data var is unknown 
			}
			else if(q.isConstructType() && isDataVarInWhere() && !isDataVarInConstruct()){
				type = PlanType.B2A; 
			}
			else if(q.isConstructType() && !isDataVarInWhere() && isDataVarInConstruct()){
				type = PlanType.B2B; //that data var is bet to be unknown because it is not there in where clause
			}
			else if(q.isConstructType() && isDataVarInWhere() && !isDataVarInConstruct()){
				type = PlanType.B2C; //but there is no guarantee that the data var is unknown 
			}
		}

		//print the variable types detected
		for(int i=0; i< vars.size(); i++){
			log.info("type of variable : "+vars.get(i).getName() + " is Known? " + isKnownVar.get(i) + " is Data Type? " + isDataVar.get(i));
			if(!isDataVar.get(i)){
				Omni.of(getVarTypes(vars.get(i)))
					.set(ty->log.info(ty));				
			}
		}
		
	}
	
	/**
	 * Returns true if unknown variable is present in the 
	 * given query
	 * @param q
	 * @return
	 */
	private boolean isPresentUnknownVar(Query q){
		List<Var> knownVars = PlanUtil.getKnownVars(q);
		List<Var> postVars;
		if(q.isSelectType()) postVars = PlanUtil.getSelectVars(q);
		else postVars = PlanUtil.getConstructVars(q);
		for(Var v : postVars){
			if(!knownVars.contains(v)) return true;
		}			
		return false;
	}
	
	private boolean isDataVarInWhere(){
		for(int i=0; i<vars.size(); i++){
			if(isDataVar.get(i) && isKnownVar.get(i)){
				 return true;
			 }
		}
		return false;
	}
	
	private boolean isDataVarInConstruct(){
		List<Var> cvars = PlanUtil.getConstructVars(q);
		for(Var v:cvars){
			if(isDataVar.get(vars.indexOf(v))){
				 return true;
			 }
		}
		return false;
	}
	
	private boolean isDataVarInSelect(){
		List<Var> cvars = PlanUtil.getSelectVars(q);
		for(Var v:cvars){
			if(isDataVar.get(vars.indexOf(v))){
				 return true;
			 }
		}
		return false;
	}
	
	/**
	 * analyze select clause to identify variables
	 * @param q
	 * @param onto
	 */
	
	private void deconstructSelectClause(Query q, OntologyModel onto) {
		Omni.of(PlanUtil.getSelectVars(q))
			.filter(v->!vars.contains(v))
			.set(v->vars.add(v))
			.set(v->isKnownVar.add(false))
			//it is irrelavant to know whether it is data var or may not be known till matched with service, but doesn't matter, we won't need it
			.set(v->isDataVar.add(false));  
	}
	
	/**
	 * analyze construct clause to identify variables
	 * @param q
	 * @param onto
	 */
	
	private void deconstructConstructClause(Query q, OntologyModel onto) {
		BasicPattern pat = getConstructBasicPattern();
//		Omni.of(PlanUtil.getVars(pat))
//			.filter(v->vars.contains(v))
//			.set(v->detectVarTypes(onto, pat, v));
		
		Omni.of(PlanUtil.getVars(pat))
			.filter(v->!vars.contains(v))
			.set(v->vars.add(v))
			.set(v->isKnownVar.add(false))
			.set(v->isDataVar.add(false)) 
			.set(v->detectVarTypes(onto, pat, v))
			.onFailure(e->e.printStackTrace());
			
	}

	/**
	 * analyze where clause to identify variables
	 * @param q
	 * @param onto
	 */
	public void deconstructWhereClause(Query q, OntologyModel onto){
		BasicPattern pat = getWhereBasicPattern();
		Omni.of(PlanUtil.getVars(pat))
			.set(v->vars.add(v))
			.set(v->isKnownVar.add(true))
			.set(v->isDataVar.add(false))
			.set(v->detectVarTypes(onto, pat, v))
			.onFailure(e->e.printStackTrace());		
	}
	
	/**
	 * Checks whether the variable is either subject or Object of a ObjectProperty 
	 * or only Subject of a data property in the given query. 
	 * Now this method also save the asserted types while analyzing
	 * @param m A-box to check property types
	 * @param query
	 * @param v0
	 * @return
	 */
	public void detectVarTypes(OntologyModel m, BasicPattern pat, Var v) {	
		List<Triple> trips = PlanUtil.getTriplesContainingVar(pat, v);
		List<String> types = new LinkedList<String>();
		
		//check if there is an explicit type declaration then the varable is definitely grounded by individual
		Omni.of(trips)
			.filter(t->t.getSubject().getName().equals(v.getName()) && 
					   t.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
			.set(t->types.add(t.getObject().getURI()))//store the types of the variable
			.toList();
		if(types.size()>0){
			if(types.size()>1) {
				log.warn("more than one type assertions are found in the triple for var "+v.getName());
			}
			//store the types for the variable
			varTypes.put(v, types);
			//Obviously this is an individul var
			isDataVar.set(vars.indexOf(v), false);
			return;
		}	
		
		//if the variable is found at least in one subject then it is an individual and not literal
		// because literal variables can only appear as object
		
		Omni<Triple> tSubV = 
		Omni.of(trips)
			.filter(t->t.getSubject().equals(v));			
		
		if(tSubV.toList().size()>0){
			
			//Obviously this is an individul var
			isDataVar.set(vars.indexOf(v), false);
			
			//try to retrieve the types 
			tSubV.map(t->IRI.create(t.getPredicate().getURI()))
				 .map(iri->PlanUtil.getObjectProperty(m, iri))
				 .set(op->{
					//save the type of the variable
					 Uni.of(PlanUtil.getDomainOfOProp(m, op))
						   .onSuccess(c->types.add(c.getIRI().toString()));
				  });
			
			if(types.size()>1) {
				log.warn("more than one type assertions are found from domain of properties where subject is var "+v.getName());
			}
			//store the types for the variable
			varTypes.put(v, types);		
			return;
		}
		//if the variable is found in only in object then 
		else{		
			
			Omni<Triple> tObjV = 
					Omni.of(trips)
						.filter(t->t.getObject().equals(v));
			
			if(tObjV.toList().size()>0){
				
				//try to retrieve the types 
				tObjV.map(t->IRI.create(t.getPredicate().getURI()))
					 .map(iri->PlanUtil.getObjectProperty(m, iri))
					 .set(op->{
						//save the type of the variable
						 Uni.of(PlanUtil.getDomainOfOProp(m, op))
							   .onSuccess(c->types.add(c.getIRI().toString()));
					  });
				if(types.size()>0){
					
					if(types.size()>1) {
						log.warn("more than one type assertions are found from domain of properties where subject is var "+v.getName());
					}
					//store the types for the variable
					varTypes.put(v, types);	
					
					//Obviously this is an individul var
					isDataVar.set(vars.indexOf(v), false);			
					return;
				}
				else{
					// the properties are all data properties, the var should be data var
					isDataVar.set(vars.indexOf(v), true);				
					return;
				}
			}
		}	
	}
	
	

	/**
	 * @return list of known variables
	 */
	public List<Var> getKnownVar() {
		List<Var> vs = new LinkedList<Var>();
		for(int i=0; i<vars.size(); i++){
			if(isKnownVar.get(i)) vs.add(vars.get(i));
		}
		return vs;
	}
	
	/**
	 * Return the first output variable in the list, 
	 * assuming there is maximum one unknown variable in the query
	 * returns null if no unknown variable in the query
	 * @return the unknown variable
	 */
	public Var getUnknownVar() {
		return vars.get(isKnownVar.indexOf(false));
	}
	
	/**
	 * get unknown var type	 
	 * assuming there is maximum one unknown variable in the query
	 * returns null if no unknown variable in the query
	 * @return
	 */
	public String getUnknownVarType() {
		return Uni.of(getUnknownVar())
				.fMap(v->Omni.of(varTypes.get(v)))
				.toList().get(0);
	}
	
	/**
	 * @return list of unknown variables
	 */
	public List<Var> getUnknownVars() {
		List<Var> vs = new LinkedList<Var>();
		for(int i=0; i<vars.size(); i++){
			if(!isKnownVar.get(i)) vs.add(vars.get(i));
		}
		return vs;
	}

	/**
	 * 
	 * @return
	 */
	public List<Var> getIndiVars() {
		List<Var> vs = new LinkedList<Var>();
		for(int i=0; i<vars.size(); i++){
			if(!isDataVar.get(i)) vs.add(vars.get(i));
		}
		return vs;
	}
	
 	/**
 	 * Special util class to create a surrogate graph from the pattern 
 	 * 
 	 * @param pat
 	 * @return
 	 */
//	private BasicPattern assertSurrogateGraphforWhereClause(Binding bind) {		
//		
//		OntModel m = ModelFactory.createOntologyModel();
//		BasicPattern pat = PlanUtil.getWhereBasicPattern(q);
//		List<Var> vs = PlanUtil.getVars(pat);
//		Binding b = BindingFactory.binding();
//		
//		//for every var, populate a dummy binding
//		for(Var v:vs){
//			//
//			OntClass dummyType = m.createClass("http://www.w3.org/2002/07/owl#Thing"); //can also be inferred from explicit assertion if supplied
//			if(!isDataVar.get(vars.indexOf(v))){
//				b = BindingFactory.binding(b, v, m.createIndividual(v.getName(), dummyType).asNode());
//			}else{
//				//handle data variable
//				if(bind.get(v)!=null){
//					//not all xsd types are handled here
//					//for example double
//					if(bind.get(v).getLiteral().getDatatype().equals(XSDDatatype.XSDdouble)){
//						b = BindingFactory.binding(b, v, m.createTypedLiteral(0.0).asNode());
//					}
//					else if(bind.get(v).getLiteral().getDatatype().equals(XSDDatatype.XSDstring)){
//						b = BindingFactory.binding(b, v, m.createTypedLiteral("").asNode());
//					}
//				}
//				else{
//					//currently no way we proceed if the xsdtype is not known, better to remove triples from basic pattern containing the variable
//					
//				}
//			}
//		}
//		BasicPattern groundPat = Substitute.substitute(pat, b);
//		
//		return groundPat;
//	}

	public List<Var> getVars() {
		// TODO Auto-generated method stub
		return vars;
	}

	public List<String> getVarTypes(Var v){
		return varTypes.get(v);
	}
	
	/**
	 * returns the triples which has the variable in the subject 
	 * and the object is a data variable if isSubject is true. 
	 * @param v
	 * @return
	 */
	public List<Triple> getDTypeTriples(BasicPattern p, Var v, boolean isSubject){
		Pred<Triple> isVarInSubject = t->{
			return t.getSubject().isVariable() && Var.alloc(t.getSubject()).equals(v);
		};
		
		Pred<Triple> isVarInObject = t->{
			return t.getObject().isVariable() && Var.alloc(t.getObject()).equals(v);
		};
		
		Pred<Triple> isObjectDVar = t->{
			return t.getObject().isVariable() && isDataVar.get(vars.indexOf(Var.alloc(t.getObject())));
		};
		
		if(isSubject){
			return Omni.of(p.getList())
						.filter(isVarInSubject)
						.filter(isObjectDVar)
						.toList();
		}
		else{
			return Omni.of(p.getList())
					.filter(isVarInObject)
					.filter(isObjectDVar)
					.toList();			
		}
		
	}
	
	public List<Triple> getDTypeVarTriples(BasicPattern p, Var v, boolean isSubject){
		Pred<Triple> isVarInObject = t->{
			return t.getObject().isVariable() && Var.alloc(t.getObject()).equals(v);
		};
		
		Pred<Triple> isObjectDVar = t->{
			return t.getObject().isVariable() && isDataVar.get(vars.indexOf(Var.alloc(t.getObject())));
		};
		
		return
		Omni.of(p.getList())
			.filter(isVarInObject)
			.filter(isObjectDVar)
			.toList();
	}
	
}

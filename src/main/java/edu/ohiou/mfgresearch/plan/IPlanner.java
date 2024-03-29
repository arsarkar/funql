package edu.ohiou.mfgresearch.plan;

import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.update.UpdateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.belief.Belief;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Success;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.service.base.Service;
import edu.ohiou.mfgresearch.service.invocation.ArgBinding;
import edu.ohiou.mfgresearch.service.invocation.DefaultIndividualSupplier;
import edu.ohiou.mfgresearch.service.invocation.JavaServiceInvoker;
import edu.ohiou.mfgresearch.service.invocation.ServiceInvoker;
public interface IPlanner {
	
	static Logger log = LoggerFactory.getLogger(IPlanner.class);
	Supplier<ResultSet> queryExecutor = null;
	IPlan plan = null;
	
	public Query constructSelectQuery();
	
	public Service selectService();
	
	public Table bindServiceResult();
	
	/**
	 * Given a model and a plan, create a function which can be executed in 
	 * the future to receive the result of the query
	 * @param m
	 * @param p
	 * @return
	 */
	public static Supplier<Table> createQueryExecutor(Model m, IPlan p){
		return ()->{
			return
			Uni.of(PlanUtil.convert2SelectQuery(p.getQuery()))
					.map(q->QueryExecutionFactory.create(q, m))
					.map(qe->qe.execSelect())
					.map(PlanUtil::toBindings)
					.get();
		};
	}
	
	/**
	 * Given a Belief and a plan, create a function which can be executed in 
	 * the future to receive the result of the query performed on the A-box 
	 * of the Belief
	 * @param m
	 * @param p
	 * @return
	 */
	public static Supplier<Table> createQueryExecutor(Belief b, IPlan p){
		return ()->{
			return
			Uni.of(PlanUtil.convert2SelectQuery(p.getQuery()))
					.map(q->QueryExecutionFactory.create(q, b.getaBox()))
					.map(qe->qe.execSelect())
					.map(PlanUtil::toBindings)
					.get();
		};
	}
	
	/**
	 * Given a query, create a function which can be executed in 
	 * the future to receive the result of the query performed against the given A-box 
	 * of the Belief
	 * @param m
	 * @param p
	 * @return Table
	 */
	public static Function<Query, Table> createQueryExecutor(Model m){
		return q->{
			log.info("Query being performed -->"+q.toString());
			return
			Uni.of(q)
					.map(q0->QueryExecutionFactory.create(q, m))
					.map(qe->qe.execSelect())
					.map(PlanUtil::toBindings)
					.get();
		};
	}
	
	public static Function<Query, Table> createQueryExecutorWithBind(Model m, QuerySolution b){
		return q->{
			log.info("Query being performed -->"+q.toString());
			return
			Uni.of(q)
					.map(q0->QueryExecutionFactory.create(q, m, b))
					.map(qe->qe.execSelect())
					.map(PlanUtil::toBindings)
					.get();
		};
	}
	
	/**
	 * Given a query, create a function which can be executed in 
	 * the future to receive the result of the query performed against the given A-box 
	 * of the Belief
	 * @param m
	 * @param p
	 * @return Table
	 */
	public static Function<Query, BasicPattern> createConstructExecutor(Model m){
		return q->{
			log.info("Query being performed -->"+q.toString());
			BasicPattern pattern = new BasicPattern();			
			Uni.of(q)
					.map(q0->QueryExecutionFactory.create(q, m))
					.map(qe->qe.execConstructTriples())
					.set(ti->ti.forEachRemaining(t->pattern.add(t)));
			return pattern;
		};
	}
	
	public static Function<Query, BasicPattern> createConstructExecutorWithBind(Model m, QuerySolution b){
		return q->{
			log.info("Query being performed -->"+q.toString());
			BasicPattern pattern = new BasicPattern();			
			Uni.of(q)
					.map(q0->QueryExecutionFactory.create(q, m, b))
					.map(qe->qe.execConstructTriples())
					.set(ti->ti.forEachRemaining(t->pattern.add(t)));
			return pattern;
		};
	}

	/**
	 * Given a query, create a function which can be executed in 
	 * the future to receive the result of the query performed against the given A-box 
	 * of the Belief
	 * @param m
	 * @param p
	 * @return ResultSet
	 */
	public static Function<Query, ResultSet> createQueryExecutorAlt(Model m){
		return q->{
			return
			Uni.of(q)
					.map(q0->QueryExecutionFactory.create(q, m))
					.map(qe->qe.execSelect())
					.get();
		};
	}
	
	/**
	 * Create an update executor
	 * @param m A-Box should have proper ns prefix mapping
	 * @param pat
	 * @return
	 */
	public static Function<BasicPattern, BasicPattern> createUpdateExecutor(Model m){
		return pattern->{	
			Uni<UpdateBuilder> builder =
			Uni.of(createUpdateBuilder(m, pattern))
				.set(b->UpdateAction.execute(b.build(), m))
				.onFailure(e->log.warn("Failed to update the A-Box with the given pattern \n"+e.getMessage()));
			return builder instanceof Success?pattern:new BasicPattern();
		};
	}
	
	/**
	 * Create update builder
	 * @param m
	 * @param pat
	 * @return
	 */
	static UpdateBuilder createUpdateBuilder(Model m, BasicPattern pat) {
		// TODO Auto-generated method stub
		return 
		Uni.of(UpdateBuilder::new)
			.set(b->b.addPrefixes(m.getNsPrefixMap()))
			.set(b->pat.forEach(t->b.addInsert(t)))
			.get();
	}

	/**
	 * Returns a function which takes a table and map the table values to the given ConstructPattern
	 * returns basic pattern 
	 * @param constructPat
	 * @return
	 */
	public static Function<Table, BasicPattern> createPatternExpander(BasicPattern constructPat) {
		// TODO Auto-generated method stub
		return tab->{		
				BasicPattern pat = new BasicPattern();
				
				tab.rows().forEachRemaining(b->{
					Substitute.substitute(constructPat, b)
							  .getList()
							  .forEach(t->pat.add(t));
			});
			return pat;
		};
	}

	/**
	 * Returns a Function which receives a Table (only known variable) and returns a 
	 * table which contains result of the service mapped to a new unknown variable
	 * expects that the invoker will return just one binding with the output variable name
	 * Cant handle multiple bindings
	 * @param invoker
	 * @return
	 */
	public static Function<Table, Table> createServiceResultMapper_deafault(ServiceInvoker invoker) {
		return tab->{
			Table res = TableFactory.create();
			tab.rows().forEachRemaining(b->{
				Uni.of(invoker)
				   .map(inv->inv.invokeService(null)) //no input needed for a supplier type method invocation
				   .map(ts->ts.get())
				   .map(t->t.rows().next()) //it should bind to multiple rows of output of the service
				   .set(b1->{
					   Var v = b1.vars().next();
					   res.addBinding(BindingFactory.binding(b, v, b1.get(v)));
				   });
			});
			return res;
		};
	}
	
	/**
	 * Returns a Function which receives a Table (only known variable) and returns a 
	 * table which contains result of the services mapped to one or more new unknown variables.
	 * Given services should return result with bindings for one output variable only (multiple variables are ignored). 
	 * 
	 * @param invoker
	 * @return
	 */
	public static Function<Table, Table> createServiceResultMapper(List<ServiceInvoker> invokers) {
		return tab->{
			Table res = TableFactory.create();
			tab.rows().forEachRemaining(b->{
				List<Binding> bindings = new LinkedList<Binding>();
				Uni.of(invokers.get(0))
				   .map(inv->inv.invokeService(inv.createInputBinding(b))) 
				   .map(ts->ts.get()) //execute the service!
				   .set(t->{
//					   t.toResultSet().forEachRemaining(r->log.info(r.toString()));
					   t.rows().forEachRemaining(b1->{
						   Var v1 = b1.vars().next(); //only one return binding is expected.
						   bindings.add(BindingFactory.binding(v1, b1.get(v1))); //add the binding to parent binding
//						   log.info(bind.get().toString());
					   });
				   })
				   .onSuccess(t->log.info("Service " + invokers.get(0).toString() + " is successfully executed!"));
				
					//print all the bindings  
					Omni.of(bindings).set(b1->log.info(b1.toString()));
					
					List<Binding> bindings1 = new LinkedList<Binding>();
//					invokers.remove(0);
					//this portion should create all individuals for unknown variable
					Omni.of(bindings)
						.set(b1->{
							   List<Binding> bindings2 = new LinkedList<Binding>();	 
							   //for every row of result add a new individual for the indi variable
							   Omni.of(invokers.subList(1, invokers.size()))
							   	   .map(inv->{
							   		   DefaultIndividualSupplier indiMaker = (DefaultIndividualSupplier) inv.clone();
							   		   ArgBinding oArg = indiMaker.getOutArgBinding();
							   		   if(oArg.getParamType().isVariable()){
							   			   oArg.setParamType(b.get(Var.alloc(oArg.getParamType())));
							   			   indiMaker.setOutputArgument(oArg);
							   		   }
							   		   return indiMaker.invokeService(null);
							   	   })		//no input needed for a supplier type method invocation
								   .map(ts->ts.get())
								   .set(t1->{
									   t1.toResultSet().forEachRemaining(r->log.info(r.toString()));
									   t1.rows().forEachRemaining(b2->{
//										   Var v2 = b2.vars().next();
//										   log.info(bind.get().toString());
//										   bind = BindingFactory.binding(v2, b2.get(v2));								   
//										   log.info(bind.get().toString());	
										   bindings2.add(b2);										   
									   });
								   });
							   Binding nBind = b1;
							   for(Binding b2:bindings2){
								   nBind = Algebra.merge(nBind, b2); 
							   }
							   bindings1.add(nBind);
						});
					
					//merge with input bindings
					Omni.of(bindings1)
						.set(b1->{
							Binding bind = Algebra.merge(b, b1);
							res.addBinding(bind);
						});
					//log.info("Resolution derived-->");
					//res.rows().forEachRemaining(r->log.info(r.toString()));
			}); 
			return res;
		};
	}
	
}

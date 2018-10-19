package edu.ohiou.mfgresearch.io;

import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.BasicPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.belief.Belief;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.lambda.functions.Cons;
import edu.ohiou.mfgresearch.lambda.functions.Pred;
import edu.ohiou.mfgresearch.plan.IPlan;
import edu.ohiou.mfgresearch.plan.IPlanner;
import edu.ohiou.mfgresearch.plan.PlanUtil;
import edu.ohiou.mfgresearch.service.ServiceFinder;
import edu.ohiou.mfgresearch.service.ServiceRegistry;
import edu.ohiou.mfgresearch.service.base.Service;
import edu.ohiou.mfgresearch.service.invocation.ArgBinding;
import edu.ohiou.mfgresearch.service.invocation.DefaultIndividualSupplier;
import edu.ohiou.mfgresearch.service.invocation.ServiceInvoker;

public class FunQL {
	
	static Logger log = LoggerFactory.getLogger(FunQL.class);
	
	static List<IPlan> plans = new LinkedList<IPlan>();
	static ServiceRegistry registry = new ServiceRegistry();
	static Belief belief = new Belief("RDFXML"); //RDFXML is defaulted as the format but should have provision to set from outside
	private static Pred<String> isQueryArg = arg->arg.trim().equals("-query");
	private static Pred<String> isServiceArg = arg->arg.trim().equals("-service");
	private static Pred<String> isBeliefArg = arg->arg.trim().equals("-belief");
	private static Pred<String> isKnowledgeArg = arg->arg.trim().equals("-knowledge");
	public static Cons<String> parseQueryToPlan = qs->plans.add(new IPlan(qs.trim()));
	public static Cons<String> parseServiceToRegistry = ss->registry.addService(new FileInputStream(ss)); //service string is assumed to be a path, but can also be an url
	public static Cons<String> parseOntologyToBelief = bs->belief.addTBox(bs.trim()); //is assumed to be from url but can also be from file, may be handled internally by JENA API
	public static Cons<String> parseKnowledgeToABox = kb->belief.addABox(kb.trim());
	
	public static void main(String[] args) throws Exception {	
		String currArg = "";
		log.debug("debug ..");
		log.trace("trace...");
		log.info("Starting to parse query.....");
		for(int i=0;i<args.length;i++){			
			//store currentArg
			if(args[i].startsWith("-") && !currArg.equals(args[i])){
				currArg = args[i];
				continue;
			}			
			final String value = args[i];
			Uni.of(currArg)
			   .select(isQueryArg, a->log.trace("Parsing query from " + a))	
			   .select(isQueryArg, a->parseQueryToPlan.accept(value)) //if the current token is after -query
			   .select(isServiceArg, a->log.trace("Parsing service from " + a))
			   .select(isServiceArg, a->parseServiceToRegistry.accept(value)) // if the token value is after -service 
			   .select(isBeliefArg, a->log.trace("Parsing ontology (belief) from " + a))
			   .select(isBeliefArg, a->parseOntologyToBelief.accept(value)) //if the current token is after -belief	
			   .select(isKnowledgeArg, a->log.trace("Parsing knowledge from " + a))
			   .select(isKnowledgeArg, a->parseKnowledgeToABox .accept(value)); //if the current token is after -knowledge	
			
		}
		log.info("Parsing completed....");
		
		//print parsed query, service, knowledge and belief
		Omni.of(plans)
			.map(p->p.getQuery().toString(Syntax.defaultQuerySyntax))
			.set(qs->log.info("Query parsed :: \n"+qs));
		
		Omni.of(registry.getServices())
			.set(p->log.info("Service regisered :"+p));
		
		log.info("Belief found : " + belief.gettBox().toString());
		log.info("Knowledge found : " + belief.getaBox().getNsPrefixURI(""));			
		execute();
		
	}

	public static void execute() {
		//analyze query and classify
		Omni.of(plans)
			.set(p->p.deconstructQuery(belief.gettBox()))
			.set(p->log.info("Type of plan is "+p.type.toString()));		
		
		Cons<IPlan> executeA1Plan = p->{
			Query selectQuery = PlanUtil.convert2SelectQuery(p.getQuery());
			Function<Query, Table> queryRes = IPlanner.createQueryExecutor(belief.getaBox());
			//display the result, should come from visualization package
			Function<Table, String> display = tab->{
				log.info(tab.toString());
				return "";
			};
			queryRes.andThen(display).apply(selectQuery);
		};	
		
		Cons<IPlan> executeB1Plan = p->{
			Query selectQuery = PlanUtil.convert2SelectQuery(p.getQuery());
			Function<Query, Table> queryRes = IPlanner.createQueryExecutor(belief.getaBox());	
			Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(p.getConstructBasicPattern());
			Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(belief.getaBox());
			BasicPattern updatedPattern = queryRes.andThen(expander).andThen(updater).apply(selectQuery);
			Uni.of(updatedPattern)
				.select(pat->!pat.isEmpty(), pat-> log.info("Successfully updated A-box with the following pattern: \n"+pat.toString()))
				.select(pat->pat.isEmpty(), pat-> log.info("Update could not be applied!"));
		}; 
		
		Cons<IPlan> executeA2Plan = p->{
			ServiceFinder finder = new ServiceFinder(p, belief, registry);
		};
		
		Cons<IPlan> executeB2Plan = p->{
			Query selectQuery = PlanUtil.convert2SelectQuery(p.getQuery());
			Function<Query, Table> queryRes = IPlanner.createQueryExecutor(belief.getaBox());	
//will move to B2A/B plans 
//			ServiceFinder servieFinder = new ServiceFinder(p, belief, registry);
//			List<Service> servicesFound = servieFinder.findService();
//			Function<Table, Table> serviceInvoker = IPlanner.createServiceInvoker(servieFinder.findService().get(0));
			//create a default individual generator service
			RDFNode oType = ResourceFactory.createResource(p.getUnknownVarType());
			ArgBinding oBind = new ArgBinding();
			oBind.setArgPos(0);
			oBind.setParamType(oType);
			oBind.setVar(p.getUnknownVar());
			ServiceInvoker invoker = new DefaultIndividualSupplier(oBind, belief.getaBox().getNsPrefixURI(""));
			Function<Table, Table> mapUnknownVar = IPlanner.createServiceResultMapper_deafault(invoker);
			
			Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(p.getConstructBasicPattern());
			Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(belief.getaBox());
			BasicPattern updatedPattern = queryRes.andThen(mapUnknownVar).andThen(expander).andThen(updater).apply(selectQuery);
			Uni.of(updatedPattern)
				.select(pat->!pat.isEmpty(), pat-> log.info("Successfully updated A-box with the following pattern: \n"+pat.toString()))
				.select(pat->pat.isEmpty(), pat-> log.info("Update could not be applied!"));
			
		};
		
		Cons<IPlan> executeB2APlan = p->{
			Query selectQuery = PlanUtil.convert2SelectQuery(p.getQuery());
			Function<Query, Table> queryRes = IPlanner.createQueryExecutor(belief.getaBox());	
			
			ServiceFinder servieFinder = new ServiceFinder(p, belief, registry);
			List<Service> servicesFound = servieFinder.findService();
			List<ServiceInvoker> serviceInvoker = servieFinder.createServiceInvoker(servicesFound.get(0));
			Function<Table, Table> mapUnknownVar = IPlanner.createServiceResultMapper(serviceInvoker);
			
			Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(p.getConstructBasicPattern());
			Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(belief.getaBox());
			BasicPattern updatedPattern = queryRes.andThen(mapUnknownVar).andThen(expander).andThen(updater).apply(selectQuery);
			Uni.of(updatedPattern)
				.select(pat->!pat.isEmpty(), pat-> log.info("Successfully updated A-box with the following pattern: \n"+pat.toString()))
				.select(pat->pat.isEmpty(), pat-> log.info("Update could not be applied!"));
			
		};
		
		
		Omni.of(plans)
		//if there is no need to match service then just execute the query and return result
			.select(p->p.type==IPlan.PlanType.A1, executeA1Plan)
			.select(p->p.type==IPlan.PlanType.B1, executeB1Plan)	
			.select(p->p.type==IPlan.PlanType.A2, executeA2Plan)
			.select(p->p.type==IPlan.PlanType.B2, executeB2Plan)
			.select(p->p.type==IPlan.PlanType.B2A, executeB2APlan);
	}

}

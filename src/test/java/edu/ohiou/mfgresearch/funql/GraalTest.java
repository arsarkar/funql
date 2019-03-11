package edu.ohiou.mfgresearch.funql;

import java.util.Iterator;

import org.junit.Test;

import fr.lirmm.graphik.graal.api.backward_chaining.QueryRewriter;
import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Ontology;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RulesCompilation;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.core.UnionOfConjunctiveQueries;
import fr.lirmm.graphik.graal.api.forward_chaining.Chase;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.backward_chaining.pure.PureRewriter;
import fr.lirmm.graphik.graal.core.DefaultUnionOfConjunctiveQueries;
import fr.lirmm.graphik.graal.core.atomset.graph.DefaultInMemoryGraphStore;
import fr.lirmm.graphik.graal.core.compilation.IDCompilation;
import fr.lirmm.graphik.graal.core.ruleset.DefaultOntology;
import fr.lirmm.graphik.graal.forward_chaining.BreadthFirstChase;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;

public class GraalTest {

	@Test
	public void test1() throws Exception{
		// 0 - Create a KBBuilder
		KBBuilder kbb = new KBBuilder();
		// 1 - Add a rule
		kbb.add(DlgpParser.parseRule("mortal(X), hasDegree(X, S) :- human(X), thinks(X, S), subject(S)."));
		kbb.add(DlgpParser.parseRule("course(D) :- hasDegree(X, S), hasCourse(S, D)."));
		// 2 - Add a fact
		kbb.add(DlgpParser.parseAtom("human(socrate)."));
		kbb.add(DlgpParser.parseAtom("thinks(socrate, philosophy)."));
		kbb.add(DlgpParser.parseAtom("subject(philosophy)."));
		kbb.add(DlgpParser.parseAtom("hasCourse(philosophy, mphil)."));
		// 3 - Generate the KB
		KnowledgeBase kb = kbb.build();		
		// 4 - Create a DLGP writer to print data
		DlgpWriter writer = new DlgpWriter();
		CloseableIterator<Atom> itAtom = kb.getFacts().iterator();
		writer.write("\n= facts =\n");
		if (itAtom.hasNext()) {
			do {
				writer.write(itAtom.next());
				writer.write("\n");
			} while (itAtom.hasNext());
		} else {
			writer.write("No facts.\n");
		}
		Iterator<Rule> itRule = kb.getOntology().iterator();
		writer.write("\n= rules =\n");
		if (itRule.hasNext()) {
			do {
				writer.write(itRule.next());
				writer.write("\n");
			} while (itRule.hasNext());
		} else {
			writer.write("No facts.\n");
		}
		// 5 - Parse a query from a Java String
		ConjunctiveQuery query = DlgpParser.parseQuery("?(D) :- course(D).");
		// 6 - Query the KB
		CloseableIterator<Substitution> resultIterator = kb.query(query);
		// 7 - Iterate and print results
		writer.write("\n= Answers =\n");
		if (resultIterator.hasNext()) {
			do {
				writer.write(resultIterator.next());
				writer.write("\n");
			} while (resultIterator.hasNext());
		} else {
			writer.write("No answers.\n");
		}
		CloseableIterator<Substitution> homoIterator = kb.homomorphism(query);
		writer.write("\n= homomorphisms =\n");
		if (homoIterator.hasNext()) {
			do {
				writer.write(homoIterator.next());
				writer.write("\n");
			} while (homoIterator.hasNext());
		} else {
			writer.write("No substitution.\n");
		}
		// 8 - Close resources
		kb.close();
		writer.close();
	}
	
	@Test
	public void test2() throws Exception{
		// 0 - Create a KBBuilder
		KBBuilder kbb = new KBBuilder();
		// 1 - Add a rule
		kbb.add(DlgpParser.parseRule("course(D) :- hasDegree(X, S), hasCourse(S, D)."));
		// 2 - Add a fact
//		kbb.add(DlgpParser.parseAtom("human(socrate)."));
//		kbb.add(DlgpParser.parseAtom("thinks(socrate, philosophy)."));
//		kbb.add(DlgpParser.parseAtom("subject(philosophy)."));
		kbb.add(DlgpParser.parseAtom("hasCourse(philosophy, mphil)."));
		kbb.add(DlgpParser.parseAtom("hasDegree(socrate, philosophy)."));
		// 3 - Generate the KB
		KnowledgeBase kb = kbb.build();		
		// 4 - Create a DLGP writer to print data
		DlgpWriter writer = new DlgpWriter();
		CloseableIterator<Atom> itAtom = kb.getFacts().iterator();
		writer.write("\n= facts =\n");
		if (itAtom.hasNext()) {
			do {
				writer.write(itAtom.next());
				writer.write("\n");
			} while (itAtom.hasNext());
		} else {
			writer.write("No facts.\n");
		}
		Iterator<Rule> itRule = kb.getOntology().iterator();
		writer.write("\n= rules =\n");
		if (itRule.hasNext()) {
			do {
				writer.write(itRule.next());
				writer.write("\n");
			} while (itRule.hasNext());
		} else {
			writer.write("No facts.\n");
		}
		// 5 - Parse a query from a Java String
		ConjunctiveQuery query = DlgpParser.parseQuery("?(S, D) :- hasCourse(S, D).");
		// 6 - Query the KB
		CloseableIterator<Substitution> resultIterator = kb.query(query);
		// 7 - Iterate and print results
		writer.write("\n= Answers =\n");
		if (resultIterator.hasNext()) {
			do {
				writer.write(resultIterator.next());
				writer.write("\n");
			} while (resultIterator.hasNext());
		} else {
			writer.write("No answers.\n");
		}
		CloseableIterator<Substitution> homoIterator = kb.homomorphism(query);
		writer.write("\n= homomorphisms =\n");
		if (homoIterator.hasNext()) {
			do {
				writer.write(homoIterator.next());
				writer.write("\n");
			} while (homoIterator.hasNext());
		} else {
			writer.write("No substitution.\n");
		}
		// 8 - Close resources
		kb.close();
		writer.close();
	}
	
	@Test
	public void test3() throws Exception{
		// 0 - Create a KBBuilder
		KBBuilder kbb = new KBBuilder();
		// 1 - Add a rule
		kbb.add(DlgpParser.parseRule("p1(X, Z), p2(Y, Z):- p(X, Y), c(Z)."));
		kbb.add(DlgpParser.parseRule("p(X, Y), c(Z):- p1(X, Z), p2(Y, Z)."));
		// 2 - Add a fact
		kbb.add(DlgpParser.parseAtom("p(a, b)."));
		kbb.add(DlgpParser.parseAtom("c(s)."));
		// 3 - Generate the KB
		KnowledgeBase kb = kbb.build();		
		// 4 - Create a DLGP writer to print data
		DlgpWriter writer = new DlgpWriter();
		CloseableIterator<Atom> itAtom = kb.getFacts().iterator();
		writer.write("\n= facts =\n");
		if (itAtom.hasNext()) {
			do {
				writer.write(itAtom.next());
				writer.write("\n");
			} while (itAtom.hasNext());
		} else {
			writer.write("No facts.\n");
		}
		Iterator<Rule> itRule = kb.getOntology().iterator();
		writer.write("\n= rules =\n");
		if (itRule.hasNext()) {
			do {
				writer.write(itRule.next());
				writer.write("\n");
			} while (itRule.hasNext());
		} else {
			writer.write("No facts.\n");
		}
		// 5 - Parse a query from a Java String
		ConjunctiveQuery query = DlgpParser.parseQuery("?(X, Z) :- p1(X, Z).");
		// 6 - Query the KB
		CloseableIterator<Substitution> resultIterator = kb.query(query);
		// 7 - Iterate and print results
		writer.write("\n= Answers =\n");
		if (resultIterator.hasNext()) {
			do {
				writer.write(resultIterator.next());
				writer.write("\n");
			} while (resultIterator.hasNext());
		} else {
			writer.write("No answers.\n");
		}
		CloseableIterator<Substitution> homoIterator = kb.homomorphism(query);
		writer.write("\n= homomorphisms =\n");
		if (homoIterator.hasNext()) {
			do {
				writer.write(homoIterator.next());
				writer.write("\n");
			} while (homoIterator.hasNext());
		} else {
			writer.write("No substitution.\n");
		}
		// 8 - Close resources
		kb.close();
		writer.close();
	}
	
	@Test
	public void test4() throws Exception{
		// 0 - Create a KBBuilder
		KBBuilder kbb = new KBBuilder();
		// 1 - Add a rule
		kbb.add(DlgpParser.parseRule("c(Z):- p1(X, Z), p2(Y, Z)."));
		// 2 - Add a fact
		kbb.add(DlgpParser.parseAtom("p1(a, c)."));
		kbb.add(DlgpParser.parseAtom("p2(b, c)."));
		kbb.add(DlgpParser.parseAtom("p1(a, p)."));
		kbb.add(DlgpParser.parseAtom("p2(b, p)."));
		// 3 - Generate the KB
		KnowledgeBase kb = kbb.build();		
		// 4 - Create a DLGP writer to print data
		DlgpWriter writer = new DlgpWriter();
		CloseableIterator<Atom> itAtom = kb.getFacts().iterator();
		writer.write("\n= facts =\n");
		if (itAtom.hasNext()) {
			do {
				writer.write(itAtom.next());
				writer.write("\n");
			} while (itAtom.hasNext());
		} else {
			writer.write("No facts.\n");
		}
		Iterator<Rule> itRule = kb.getOntology().iterator();
		writer.write("\n= rules =\n");
		if (itRule.hasNext()) {
			do {
				writer.write(itRule.next());
				writer.write("\n");
			} while (itRule.hasNext());
		} else {
			writer.write("No facts.\n");
		}
		// 5 - Parse a query from a Java String
		ConjunctiveQuery query = DlgpParser.parseQuery("?(Z) :- c(Z).");
		// 6 - Query the KB
		CloseableIterator<Substitution> resultIterator = kb.query(query);
		// 7 - Iterate and print results
		writer.write("\n= Answers =\n");
		if (resultIterator.hasNext()) {
			do {
				writer.write(resultIterator.next());
				writer.write("\n");
			} while (resultIterator.hasNext());
		} else {
			writer.write("No answers.\n");
		}
		CloseableIterator<Substitution> homoIterator = kb.homomorphism(query);
		writer.write("\n= homomorphisms =\n");
		if (homoIterator.hasNext()) {
			do {
				writer.write(homoIterator.next());
				writer.write("\n");
			} while (homoIterator.hasNext());
		} else {
			writer.write("No substitution.\n");
		}
		// 8 - Close resources
		kb.close();
		writer.close();
	}
	
	@Test
	public void test5() throws Exception{
		// 0 - Create a KBBuilder
		KBBuilder kbb = new KBBuilder();
		// 1 - Add a rule
		kbb.add(DlgpParser.parseRule("mortal(X), hasDegree(X, S) :- human(X), thinks(X, S), subject(S)."));
		kbb.add(DlgpParser.parseRule("course(D) :- hasDegree(X, S), hasCourse(S, D)."));
		// 2 - Add a fact
		kbb.add(DlgpParser.parseAtom("human(socrate)."));
		kbb.add(DlgpParser.parseAtom("thinks(socrate, philosophy)."));
		kbb.add(DlgpParser.parseAtom("subject(philosophy)."));
		kbb.add(DlgpParser.parseAtom("hasCourse(philosophy, mphil)."));
		// 3 - Generate the KB
		KnowledgeBase kb = kbb.build();		
		// 4 - Create a DLGP writer to print data
		DlgpWriter writer = new DlgpWriter();
		CloseableIterator<Atom> itAtom = kb.getFacts().iterator();
		writer.write("\n= facts =\n");
		if (itAtom.hasNext()) {
			do {
				writer.write(itAtom.next());
				writer.write("\n");
			} while (itAtom.hasNext());
		} else {
			writer.write("No facts.\n");
		}
		Ontology onto = new DefaultOntology(kb.getOntology());
		ConjunctiveQuery query = DlgpParser.parseQuery("?(L) :- course(L).");
		writer.write("\n= Query =\n");
	    writer.write(query);
	    // 6 - Query the KB
		CloseableIterator<Substitution> resultIterator = kb.query(query);
		// 7 - Iterate and print results
		writer.write("\n= Answers =\n");
		if (resultIterator.hasNext()) {
			do {
				writer.write(resultIterator.next());
				writer.write("\n");
			} while (resultIterator.hasNext());
		} else {
			writer.write("No answers.\n");
		}
	    
	    QueryRewriter rewriter = new PureRewriter();
	    CloseableIteratorWithoutException it = rewriter.execute(query, onto);
	    
	    writer.write("\n= Rewritings =\n");
	    while (it.hasNext()) {
	    	writer.write(it.next());
	    	writer.flush();
	    }
	    it.close();
	    /********************************************************************
		 * We will now show how compile a part of your ontology to produce
		 * "pivotal" rewritings according to this compilation. Then, we will
		 * unfold these "pivotal" rewritings to get back original rewritings.
		 ********************************************************************/

		// 7 - Compile a part of the ontology
		RulesCompilation compilation = new IDCompilation();
		compilation.compile(onto.iterator());

		// 8 - Initialize the rewriter with unfolding disabled
		PureRewriter pure = new PureRewriter(false);

		// 9 - Rewrite according to the specified ontology and compilation
		it = pure.execute(query, onto, compilation);
		
		// 10 - Save rewritings in an UnionOfConjunctiveQueries object
		UnionOfConjunctiveQueries ucq = new DefaultUnionOfConjunctiveQueries(query.getAnswerVariables(), it);

		// 11 - Print it
		writer.write("\n= Pivotal Rewritings =\n");
		writer.write(ucq);

		// 12 - Unfold rewritings
		writer.write("\n= Unfolded Rewritings =\n");
		it = PureRewriter.unfold(ucq, compilation);
		while (it.hasNext()) {
			writer.write(it.next());
			writer.flush();
		}
		it.close();
		//chase

		InMemoryAtomSet store = new DefaultInMemoryGraphStore();
		store.addAll(kb.getFacts());
		
		Chase chase = new BreadthFirstChase(onto, store);
		writer.write("\n= Starting Forward Chase =\n");
		while(chase.hasNext()){
			writer.write("\n= next chase.. =\n");
			chase.next();
			writer.write(store);
		}
		// 8 - Close resources
		kb.close();
		writer.close();
	}
	
	@Test
	public void test6() throws Exception{
		// 0 - Create a KBBuilder
		KBBuilder kbb = new KBBuilder();
		// 1 - Add a rule
		kbb.add(DlgpParser.parseRule("r(X, Y) :- p(X)."));
		kbb.add(DlgpParser.parseRule("p(Y) :- p(X), s(Y)."));
		kbb.add(DlgpParser.parseRule("r(X, Y) :- q(X)."));
		kbb.add(DlgpParser.parseRule("t(X) :- r(X, Y)."));
//		kbb.add(DlgpParser.parseRule("p(X) :- r(X, Y)."));
//		kbb.add(DlgpParser.parseRule("p(X), s(Y) :- p(Y)."));
//		kbb.add(DlgpParser.parseRule("q(X) :- r(X, Y)."));
//		kbb.add(DlgpParser.parseRule("r(X, Y) :- t(X)."));
		// 2 - Add a fact
		kbb.add(DlgpParser.parseAtom("p(a)."));
		kbb.add(DlgpParser.parseAtom("q(b)."));
		kbb.add(DlgpParser.parseAtom("s(b)."));
		// 3 - Generate the KB
		KnowledgeBase kb = kbb.build();		
		// 4 - Create a DLGP writer to print data
		DlgpWriter writer = new DlgpWriter();
		CloseableIterator<Atom> itAtom = kb.getFacts().iterator();
		writer.write("\n= facts =\n");
		if (itAtom.hasNext()) {
			do {
				writer.write(itAtom.next());
				writer.write("\n");
			} while (itAtom.hasNext());
		} else {
			writer.write("No facts.\n");
		}
		Ontology onto = new DefaultOntology(kb.getOntology());
		ConjunctiveQuery query = DlgpParser.parseQuery("?(L) :- t(L).");
		writer.write("\n= Query =\n");
	    writer.write(query);
	    // 6 - Query the KB
		CloseableIterator<Substitution> resultIterator = kb.query(query);
		// 7 - Iterate and print results
		writer.write("\n= Answers =\n");
		if (resultIterator.hasNext()) {
			do {
				writer.write(resultIterator.next());
				writer.write("\n");
			} while (resultIterator.hasNext());
		} else {
			writer.write("No answers.\n");
		}
	    
	    QueryRewriter rewriter = new PureRewriter();
	    CloseableIteratorWithoutException it = rewriter.execute(query, onto);
	    
	    writer.write("\n= Rewritings =\n");
	    while (it.hasNext()) {
	    	writer.write(it.next());
	    	writer.flush();
	    }
	    it.close();
	    /********************************************************************
		 * We will now show how compile a part of your ontology to produce
		 * "pivotal" rewritings according to this compilation. Then, we will
		 * unfold these "pivotal" rewritings to get back original rewritings.
		 ********************************************************************/

		// 7 - Compile a part of the ontology
		RulesCompilation compilation = new IDCompilation();
		compilation.compile(onto.iterator());

		// 8 - Initialize the rewriter with unfolding disabled
		PureRewriter pure = new PureRewriter(false);

		// 9 - Rewrite according to the specified ontology and compilation
		it = pure.execute(query, onto, compilation);
		
		// 10 - Save rewritings in an UnionOfConjunctiveQueries object
		UnionOfConjunctiveQueries ucq = new DefaultUnionOfConjunctiveQueries(query.getAnswerVariables(), it);

		// 11 - Print it
		writer.write("\n= Pivotal Rewritings =\n");
		writer.write(ucq);

		// 12 - Unfold rewritings
		writer.write("\n= Unfolded Rewritings =\n");
		it = PureRewriter.unfold(ucq, compilation);
		while (it.hasNext()) {
			writer.write(it.next());
			writer.flush();
		}
		it.close();
		//chase

		InMemoryAtomSet store = new DefaultInMemoryGraphStore();
		store.addAll(kb.getFacts());
//		store.add(DlgpParser.parseAtom("t(b)."));
		
		Chase chase = new BreadthFirstChase(onto, store);
		writer.write("\n= Starting Forward Chase =\n");
		while(chase.hasNext()){
			writer.write("\n= next chase.. =\n");
			chase.next();
			writer.write(store);
		}
		// 8 - Close resources
		kb.close();
		writer.close();
	}
	
	@Test
	public void test7() throws Exception{
		// 0 - Create a KBBuilder
		KBBuilder kbb = new KBBuilder();
		// 1 - Add a rule
		kbb.add(DlgpParser.parseRule("hasVolume(C, V) :- cone(C), hasDia(C, D), hasHeight(C, H)."));
//		kbb.add(DlgpParser.parseRule("p(X) :- r(X, Y)."));
//		kbb.add(DlgpParser.parseRule("p(X), s(Y) :- p(Y)."));
//		kbb.add(DlgpParser.parseRule("q(X) :- r(X, Y)."));
//		kbb.add(DlgpParser.parseRule("r(X, Y) :- t(X)."));
		// 2 - Add a fact
		kbb.add(DlgpParser.parseAtom("cone(cone1)."));
		kbb.add(DlgpParser.parseAtom("hasDia(cone1, dia1)."));
		kbb.add(DlgpParser.parseAtom("hasHeight(cone1, height1)."));
		// 3 - Generate the KB
		KnowledgeBase kb = kbb.build();		
		// 4 - Create a DLGP writer to print data
		DlgpWriter writer = new DlgpWriter();
		CloseableIterator<Atom> itAtom = kb.getFacts().iterator();
		writer.write("\n= facts =\n");
		if (itAtom.hasNext()) {
			do {
				writer.write(itAtom.next());
				writer.write("\n");
			} while (itAtom.hasNext());
		} else {
			writer.write("No facts.\n");
		}
		Ontology onto = new DefaultOntology(kb.getOntology());
		ConjunctiveQuery query = DlgpParser.parseQuery("?(C) :- hasVolume(C, V).");
		writer.write("\n= Query =\n");
	    writer.write(query);
	    // 6 - Query the KB
		CloseableIterator<Substitution> resultIterator = kb.query(query);
		// 7 - Iterate and print results
		writer.write("\n= Answers =\n");
		if (resultIterator.hasNext()) {
			do {
				writer.write(resultIterator.next());
				writer.write("\n");
			} while (resultIterator.hasNext());
		} else {
			writer.write("No answers.\n");
		}
	    
	    QueryRewriter rewriter = new PureRewriter();
	    CloseableIteratorWithoutException it = rewriter.execute(query, onto);
	    
	    writer.write("\n= Rewritings =\n");
	    while (it.hasNext()) {
	    	writer.write(it.next());
	    	writer.flush();
	    }
	    it.close();
	    /********************************************************************
		 * We will now show how compile a part of your ontology to produce
		 * "pivotal" rewritings according to this compilation. Then, we will
		 * unfold these "pivotal" rewritings to get back original rewritings.
		 ********************************************************************/

		// 7 - Compile a part of the ontology
		RulesCompilation compilation = new IDCompilation();
		compilation.compile(onto.iterator());

		// 8 - Initialize the rewriter with unfolding disabled
		PureRewriter pure = new PureRewriter(false);

		// 9 - Rewrite according to the specified ontology and compilation
		it = pure.execute(query, onto, compilation);
		
		// 10 - Save rewritings in an UnionOfConjunctiveQueries object
		UnionOfConjunctiveQueries ucq = new DefaultUnionOfConjunctiveQueries(query.getAnswerVariables(), it);

		// 11 - Print it
		writer.write("\n= Pivotal Rewritings =\n");
		writer.write(ucq);

		// 12 - Unfold rewritings
		writer.write("\n= Unfolded Rewritings =\n");
		it = PureRewriter.unfold(ucq, compilation);
		while (it.hasNext()) {
			writer.write(it.next());
			writer.flush();
		}
		it.close();
		//chase

		InMemoryAtomSet store = new DefaultInMemoryGraphStore();
		store.addAll(kb.getFacts());
//		store.add(DlgpParser.parseAtom("t(b)."));
		
		Chase chase = new BreadthFirstChase(onto, store);
		writer.write("\n= Starting Forward Chase =\n");
		while(chase.hasNext()){
			writer.write("\n= next chase.. =\n");
			chase.next();
			writer.write(store);
		}
		// 8 - Close resources
		kb.close();
		writer.close();
	}

}

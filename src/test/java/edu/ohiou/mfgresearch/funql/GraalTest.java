package edu.ohiou.mfgresearch.funql;

import java.util.Iterator;

import org.junit.Test;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.util.stream.CloseableIterator;

public class GraalTest {

	@Test
	public void test() throws Exception{
		// 0 - Create a KBBuilder
		KBBuilder kbb = new KBBuilder();
		// 1 - Add a rule
		kbb.add(DlgpParser.parseRule("mortal(X), hasDegree(X, S) :- human(X), thinks(X, S), subject(S)."));
		// 2 - Add a fact
		kbb.add(DlgpParser.parseAtom("human(socrate)."));
		kbb.add(DlgpParser.parseAtom("thinks(socrate, philosophy)."));
		kbb.add(DlgpParser.parseAtom("subject(philosophy)."));
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
		ConjunctiveQuery query = DlgpParser.parseQuery("?(X,S) :- hasDegree(X,S).");
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
		// 8 - Close resources
		kb.close();
		writer.close();
	}

}

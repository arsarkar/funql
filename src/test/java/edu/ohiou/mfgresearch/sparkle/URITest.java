package edu.ohiou.mfgresearch.sparkle;

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.xerces.util.URI.MalformedURIException;
import org.junit.Before;
import org.junit.Test;

public class URITest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void URL1() throws MalformedURLException {
		URL url1 = new URL("http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl");
		System.out.println(url1.toString());
	}

	@Test
	public void URI1() throws URISyntaxException, MalformedURLException {
		URI uriWeb = new URI("http://www.astro.umd.edu/~eshaya/astro-onto/owl/geometry.owl");
		URI uriFile = new File("C:/Users/sarkara1/Ohio University/Sormaz, Dusan - IMPlanner-research/NX-examples/NIST MBE PMI Samples/NIST_FTC_CTC_ASME_NX(.prt)").toURI();
		URI uriFile2 = new URI("file:/C:/Users/sarkara1/Ohio University/Sormaz, Dusan - IMPlanner-research/NX-examples/NIST MBE PMI Samples/NIST_FTC_CTC_ASME_NX(.prt)");
		System.out.println(uriWeb.toString());
		System.out.println(uriFile.toString());
		System.out.println(uriFile.toURL().toString());
	}
	
}

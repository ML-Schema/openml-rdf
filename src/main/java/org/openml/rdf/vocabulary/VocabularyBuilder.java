package org.openml.rdf.vocabulary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.openml.rdf.util.Util;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class VocabularyBuilder {

	private static final String ONTO_NAMESPACE = "http://www.openml.org/vocabulary#";
	
	private static Logger logger = Logger.getLogger(VocabularyBuilder.class);

	private static VocabularyBuilder instance;

	protected VocabularyBuilder() {
		super();
	}

	public static VocabularyBuilder getInstance() {
		if (instance == null)
			instance = new VocabularyBuilder();
		return instance;
	}

	/**
	 * Build all properties for each class contained in a specification file.<br>
	 * <br>
	 * Specification file contains tab-separated rows as:<br>
	 * [ClassName][TAB][JSON example URL]
	 * 
	 * @param specFile
	 * @param outFile
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public void buildAll(String specFile, String outFile) throws JSONException, IOException {
		logger.info("Started.");
		FileOutputStream file = new FileOutputStream(new File(System.getProperty("user.dir") + "/" + outFile));
		Model m = ModelFactory.createDefaultModel();
		Scanner in = new Scanner(new File(specFile));
		while (in.hasNextLine()) {
			String[] line = in.nextLine().split("\t");
			build(line[0], line[1], m);
		}
		in.close();
		logger.info("Writing to file "+outFile+ "...");
//		RDFDataMgr.write(file, m, RDFFormat.TTL);
		
		m.write(file, "TURTLE");
		
		file.close();
		logger.info("Done.");
	}

	/**
	 * @param className
	 * @param jsonURL
	 * @throws IOException 
	 * @throws JSONException 
	 */
	private void build(String className, String jsonURL, Model m) throws JSONException, IOException {

		logger.info("Processing class <" + className + ">...");
		logger.info("Parsing JSON URL " + jsonURL + "...");
		
		// fetch JSON file
		JSONObject json = Util.readJsonFromUrl(jsonURL);
		logger.info(json);
		
		// iterate among keys
		for(String key : json.keySet()) {
			String uri = ONTO_NAMESPACE + key;
			logger.info("Creating property <" + uri + ">...");
			Property p = m.createProperty(uri);
			m.add(p, RDF.type, OWL.OntologyProperty);
		}
		

	}


}

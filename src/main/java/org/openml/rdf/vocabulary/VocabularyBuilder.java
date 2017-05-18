package org.openml.rdf.vocabulary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openml.rdf.util.Util;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class VocabularyBuilder {

	public static final String COMMON_NAMESPACE = "https://www.openml.org/";
	public static final String ONTO_NAMESPACE = COMMON_NAMESPACE + "vocabulary#";

	/**
	 * Set this to 'true' to export tab-separated files for annotation.
	 */
	private static final boolean EXPORT_MODE = false;
	
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
	public void buildAll(String specFile, String inFile, String outFile) throws JSONException, IOException {
		logger.info("Started.");
		String base = System.getProperty("user.dir") + "/";
		FileOutputStream file = new FileOutputStream(new File(base + outFile));
		Model m = RDFDataMgr.loadModel(base + inFile);
		Scanner in = new Scanner(new File(specFile));
		while (in.hasNextLine()) {
			String[] line = in.nextLine().split("\t");
			build(line[0], line[1], m);
		}
		in.close();
		logger.info("Writing to file "+outFile+ "...");
		
		m.write(file);
		
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

		logger.info("Processing class <" + ONTO_NAMESPACE + className + ">...");
		logger.info("Parsing JSON URL " + jsonURL + "...");
		
		// fetch JSON file
		JSONObject json = Util.readJsonFromUrl(jsonURL);
		logger.info(json);
		
		PrintWriter pw;
		if(EXPORT_MODE)
			pw = new PrintWriter(new File(className + ".csv"));
		
		// iterate among keys
		for(String key : json.keySet()) {
			
			String uri = ONTO_NAMESPACE + key;
			logger.info("Creating property <" + uri + ">...");
			
			// get property information
			String type, obj;
			Object value = json.get(key);
			if(value instanceof JSONObject) {
				JSONObject objValue = (JSONObject) value;
				type = "object";
				obj = objValue.toString().replaceAll("\n", " ");
			} else if(value instanceof JSONArray) {
				JSONArray arrValue = (JSONArray) value;
				type = "object";
				obj = arrValue.toString().replaceAll("\n", " ");
			} else { // datatype?
				type = "datatype";
				obj = value.toString().replaceAll("\n", " ");
			}

			if(EXPORT_MODE)
				pw.println(key + "\t" + type + "\t" + obj);
			
			// save property to model
			Property p = m.createProperty(uri);
			m.add(p, RDF.type, OWL.OntologyProperty);
		}
		
		if(EXPORT_MODE)
			pw.close();
		

	}


}

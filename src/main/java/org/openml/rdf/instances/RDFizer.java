package org.openml.rdf.instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.openml.rdf.util.Util;
import org.openml.rdf.vocabulary.VocabularyBuilder;

import com.fasterxml.jackson.databind.util.Annotations;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class RDFizer {
	
	private static HashMap<String, String> classToNS = new HashMap<>();

	private static HashMap<String, String> nsToClass = new HashMap<>();
	
	private static HashMap<String, Annotation> annotations = new HashMap<>();
	

	static {
		
		classToNS.put("Task", VocabularyBuilder.COMMON_NAMESPACE + "t/");
		nsToClass.put("t", VocabularyBuilder.ONTO_NAMESPACE + "Task");
		
		// TODO
		
	}
	
	private static Logger logger = Logger.getLogger(VocabularyBuilder.class);

	private static RDFizer instance;

	protected RDFizer() {
		super();
	}

	public static RDFizer getInstance() throws FileNotFoundException {
		if (instance == null) {
			instance = new RDFizer();
			instance.readAnnotations("Task");
			// TODO
		}
		return instance;
	}


	public void rdfize(String className, String id) throws JSONException, IOException {
		
		
		JSONObject json = Util.readJsonFromUrl("http://www.openml.org/t/"+id+"/json");
		
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = m.createResource(classToNS.get(className) + id);
		Resource classRes = m.createResource(VocabularyBuilder.ONTO_NAMESPACE + className);
		m.add(subject, RDF.type, classRes);
		
		for(String key : json.keySet()) {
			
			String property = VocabularyBuilder.ONTO_NAMESPACE + key;
			Annotation a = annotations.get(property);
					
			if(a.keep == false) // skip
				continue;
			
			// understand what the object will be
			if(a.isEmpty()) {
				// TODO parse datatype
			} else {
				
				// TODO 
//				for(String )
				
			}
			
		}

		
		
		
		logger.info("====== STATEMENTS ======");
		for(Statement st : m.listStatements().toList())
			logger.info(st);
		
	}

	private void readAnnotations(String string) throws FileNotFoundException {
		
		Scanner in = new Scanner(new File("annotated/"+string+".csv"));
		
		while(in.hasNextLine()) {
			
			String[] line = in.nextLine().split("\t");
			
			Annotation a = new Annotation(line[0], line[1], line[2], Boolean.parseBoolean(line[3]));
			
			if(line.length > 4) {
				String[] entries = line[4].split(",");
				for(String entry : entries) {
					String[] e = entry.split("=");
					if(e.length == 1)
						a.put(e[0], "");
					else
						a.put(e[0], e[1]);
				}
			}
			
			annotations.put(a.propertyName, a);
			
		}
		
		for(Entry<String, Annotation> ann : annotations.entrySet())
			logger.info(ann);
		
		in.close();
		
	}

}

class Annotation extends HashMap<String, String> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4901752427257680375L;
	
	String propertyName;
	String propertyType;
	String exampleObject;
	boolean keep;
	
	public Annotation(String propertyName, String propertyType, String exampleObject,
			boolean keep) {
		super();
		this.propertyName = propertyName;
		this.propertyType = propertyType;
		this.exampleObject = exampleObject;
		this.keep = keep;
	}

	@Override
	public String toString() {
		return "Annotation [propertyName=" + propertyName + ", propertyType="
				+ propertyType + ", exampleObject=" + exampleObject + ", keep="
				+ keep + ", MAP=" + super.toString() + "]";
	}
	
	
	
}

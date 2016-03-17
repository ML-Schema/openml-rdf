package org.openml.rdf.instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
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
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");

	

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
		
		
		JSONObject json = Util.readJsonFromUrl("http://www.openml.org/"+classToNS.get(className)+id+"/json");
		logger.info(json);
		
		Model openML = RDFDataMgr.loadModel(System.getProperty("user.dir") + "/etc/OpenML.rdf");
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = openML.createResource(classToNS.get(className) + id);
		Resource classRes = openML.createResource(VocabularyBuilder.ONTO_NAMESPACE + className);
		m.add(subject, RDF.type, classRes);
		
		for(String jKey : json.keySet()) {
			
			String property = VocabularyBuilder.ONTO_NAMESPACE + jKey;
			Property propRes = m.createProperty(property);
			Annotation a = annotations.get(jKey);
			
			String object = json.get(jKey).toString();
			
			if(a.keep == false) // skip
				continue;
			
			// understand what the object will be
			if(a.isEmpty()) {
				// parse datatype
				parseDatatype(m, subject, propRes, object);
				continue;
			} else {
				// TODO 
				for(String aKey : a.keySet()) {
					
				}
				
			}
			
		}

		logger.info("====== GENERATED STATEMENTS ======");
		for(Statement st : m.listStatements().toList())
			logger.info(st);
		
		// save to output
		openML.add(m);
		FileOutputStream file = new FileOutputStream(System.getProperty("user.dir") + "/etc/OpenML_out.rdf");
		openML.write(file);
		
	}

	private void parseDatatype(Model m, Resource subject, Property propRes, String object) {
		// integer
		try {
			m.add(subject, propRes, m.createTypedLiteral(Integer.parseInt(object)));
			return;
		} catch (NumberFormatException e1) {}
		// double
		try {
			m.add(subject, propRes, m.createTypedLiteral(Double.parseDouble(object)));
			return;
		} catch (NumberFormatException e2) {}
		// date
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(sdf.parse(object));
			m.add(subject, propRes, m.createTypedLiteral(c));
			return;
		} catch (ParseException e) {}
		// last hope: as string
		m.add(subject, propRes, object);
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
	String object;
	boolean keep;
	
	public Annotation(String propertyName, String propertyType, String object,
			boolean keep) {
		super();
		this.propertyName = propertyName;
		this.propertyType = propertyType;
		this.object = object;
		this.keep = keep;
	}

	@Override
	public String toString() {
		return "Annotation [propertyName=" + propertyName + ", propertyType="
				+ propertyType + ", object=" + object + ", keep="
				+ keep + ", MAP=" + super.toString() + "]";
	}
	
	
	
}

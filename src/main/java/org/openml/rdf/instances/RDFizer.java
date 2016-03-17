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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openml.rdf.util.Util;
import org.openml.rdf.vocabulary.VocabularyBuilder;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class RDFizer {
	
	private static final HashMap<String, String> BASE_VAR = new HashMap<>();

	private static HashMap<String, Annotation> annotations = new HashMap<>();
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");

	private static enum Names {
		
		Dataset("d"),
		Task("t"),
		EstimationProcedure("ep"),
		Tag("tag");
		
		Names(String abbrev) {
			this.abbrev = abbrev;
			this.ns = VocabularyBuilder.COMMON_NAMESPACE + abbrev + "/";
			this.classURI = VocabularyBuilder.ONTO_NAMESPACE + name();
			
		}
		
		static Names byName(String name) {
			for(Names n : Names.values())
				if(n.name().equals(name))
					return n;
			return null;
		}

		static Names byAbbrev(String abbrev) {
			for(Names n : Names.values())
				if(n.abbrev.equals(abbrev))
					return n;
			return null;
		}

		String ns, abbrev, classURI;
	}
		
	private static Logger logger = Logger.getLogger(VocabularyBuilder.class);

	private static RDFizer instance;

	protected RDFizer() {
		super();
	}
	
	static {
		BASE_VAR.put("rdf", RDF.getURI());
		BASE_VAR.put("oml", VocabularyBuilder.ONTO_NAMESPACE);
		// ...
		for(Names n : Names.values())
			BASE_VAR.put("oml" + n.abbrev, n.ns);
		logger.info("base_var = " + BASE_VAR);
	}

	public static RDFizer getInstance() throws FileNotFoundException {
		if (instance == null) {
			instance = new RDFizer();
			// TODO complete here
			for(String s : new String[] { "Task" })
				instance.readAnnotations(s);
		}
		return instance;
	}


	public void rdfize(String className, String id) throws JSONException, IOException {
		
		
		String entityURI = Names.byName(className).ns + id;
		JSONObject json = Util.readJsonFromUrl(entityURI +"/json");
		logger.info(json);
		
		Model openML = RDFDataMgr.loadModel(System.getProperty("user.dir") + "/etc/OpenML.rdf");
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = openML.createResource(entityURI);
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
				Concatenation cc = new Concatenation();
				
				// go through all actions
				for(String aKey : a.keySet()) {
					
					String aObj = a.get(aKey);
					
					// build variables
					HashMap<String, String> var = new HashMap<>(BASE_VAR);
					for(String k : json.keySet()) // first level
						var.put(k, json.get(k).toString());
					try {
						JSONObject jObj = new JSONObject(object);
						for(String k : jObj.keySet()) // second level
							var.put("_" + k, jObj.get(k).toString());
					} catch (JSONException e) {}
					try {
						JSONArray jArr = new JSONArray(object);
						for(int i=0; i<jArr.length(); i++) {
							JSONObject jObj = jArr.getJSONObject(i);
							for(String k : jObj.keySet()) // second level
								var.put("_" + k + Integer.valueOf(i), jObj.get(k).toString());
						}
						var.put("multi", String.valueOf(jArr.length()));
					} catch (JSONException e) {}
					logger.info("var = " + var);
					
					// script core
					switch(aKey) {
					case "pred": // change predicate
						propRes = m.createProperty(aObj);
						continue;
					case "lookup": // lookup existing classes and assign result as object URI
						// TODO store class URI
						continue;
					case "ns": // build concatenation using this namespace
						cc.setNs(Names.byAbbrev(aObj).ns);
						continue;
					case "id": // build concatenation using this variable
						cc.setId(var.get(aObj));
						continue;
					case "add": // add triple
						// TODO
						if(var.containsKey("multi")) {
							int n = Integer.parseInt(var.get("multi"));
							for(int i=0; i<n; i++) {
								String[] triple = aObj.split(" ");
								String[] trS = triple[0].split(":");
								String[] trP = triple[1].split(":");
								String[] trO = triple[2].split(":");
								
								String suffS = var.containsKey(trS[1]) ? var.get(trS[1]) : null; 
								String suffP = var.containsKey(trP[1]) ? var.get(trP[1]) : null; 
								String suffO = var.containsKey(trO[1]) ? var.get(trO[1]) : null;
								
								if(suffS == null) {
									String s = trS[1] + String.valueOf(i);
									suffS = var.containsKey(s) ? suffS = var.get(s) : trS[1];
								}
								if(suffP == null) {
									String s = trP[1] + String.valueOf(i);
									suffP = var.containsKey(s) ? suffP = var.get(s) : trP[1];
								}
								if(suffO == null) {
									String s = trO[1] + String.valueOf(i);
									suffO = var.containsKey(s) ? suffO = var.get(s) : trO[1];
								}
								
								Resource s = m.createResource(var.get(trS[0]) + suffS);
								Property p = m.createProperty(var.get(trP[0]) + suffP);
								Resource o = m.createResource(var.get(trO[0]) + suffO);
								logger.info(s + " " + p + " " + o);
								m.add(s, p, o);
							}
						} else {
							String[] triple = aObj.split(" ");
							String[] trS = triple[0].split(":");
							String[] trP = triple[1].split(":");
							String[] trO = triple[2].split(":");
							String suffS = var.containsKey(trS[1]) ? var.get(trS[1]) : trS[1]; 
							String suffP = var.containsKey(trP[1]) ? var.get(trP[1]) : trP[1]; 
							String suffO = var.containsKey(trO[1]) ? var.get(trO[1]) : trO[1]; 
							Resource s = m.createResource(var.get(trS[0]) + suffS);
							Property p = m.createProperty(var.get(trP[0]) + suffP);
							Resource o = m.createResource(var.get(trO[0]) + suffO);
							logger.info(s + " " + p + " " + o);
							m.add(s, p, o);
						}
						continue;
					}
					
				}
				
				try { // executes only if ns and id are there
					m.add(subject, propRes, m.createResource(cc.toString()));
				} catch (NullPointerException e) {}
				
				// execute script for lookup
//				m.add(subject, propRes, uri);
				
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
		try {
			JSONArray arr = new JSONArray(object);
			for(int i=0; i<arr.length(); i++)
				m.add(subject, propRes, arr.getString(i));
			return;
		} catch(JSONException e) {}
		
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

class Concatenation {
	String ns;
	String id;
	
	public void setNs(String ns) {
		this.ns = ns;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Override
	public String toString() {
		return ns.trim() + id.trim();
	}
}

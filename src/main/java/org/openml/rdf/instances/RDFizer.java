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
		Run("r"),
		Flow("f"),
		Study("s"),
		DataQuality("dq"),
		FlowQuality("fq"),
		EstimationProcedure("ep"),
		EvaluationMeasure("em"),
		Tag("tag"),
		User("u");
		
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
		
	protected static Logger logger = Logger.getLogger(RDFizer.class);

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
			for(String s : new String[] { "Task", "Dataset", "Flow", "Run" })
				instance.readAnnotations(s);
		}
		return instance;
	}


	public void rdfize(String className, String id) throws JSONException, IOException {
		rdfize(className, id, false);
	}
	
	public void rdfize(String className, String id, boolean saveOutput) throws JSONException, IOException {
		
		String entityURI = Names.byName(className).ns + id;
		logger.info("Downloading JSON file: "+entityURI+"/json");
		JSONObject json = Util.readJsonFromUrl(entityURI +"/json");
		logger.info(json);
		
		Model openML = ModelFactory.createDefaultModel();
//		File f = new File(System.getProperty("user.dir") + "/etc/OpenML.rdf");
//		openML.read(f.toURI().toString());
		Lookup.getInstance().populate(openML);
		Model m = ModelFactory.createDefaultModel();
		
		Resource subject = openML.createResource(entityURI);
		Resource classRes = openML.createResource(Names.byName(className).classURI);
		m.add(subject, RDF.type, classRes);
		
		for(String jKey : json.keySet()) {
			
			String property = VocabularyBuilder.ONTO_NAMESPACE + jKey;
			Property propRes = m.createProperty(property);
			Annotation a = annotations.get(className + "_" + jKey);
			
			// new properties? log them!
			if(a == null) {
				logger.error("Property "+jKey + " was not annotated for class "+className+ "! Skipping...");
				continue;
			}
			
			String object = json.get(jKey).toString();
			
			if(a.keep == false) // skip
				continue;
			
			// understand what the object will be
			if(a.isEmpty()) {
				// parse datatype
				parseDatatype(m, subject, propRes, object);
				continue;
			} else {
				
				Concatenation cc = new Concatenation();
				Resource uri = null;
				
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
						String[] p1 = aObj.split(":");
						String sf1 = var.containsKey(p1[1]) ? var.get(p1[1]) : p1[1]; 
						propRes = m.createProperty(var.get(p1[0]) + sf1);
						continue;
					case "lookup": // lookup existing classes and assign result as object URI
						String string = var.get(aObj);
						uri = Lookup.getInstance().mostSimilar(string);
						continue;
					case "ns": // build concatenation using this namespace
						cc.setNs(Names.byAbbrev(aObj).ns);
						continue;
					case "id": // build concatenation using this variable
						if(var.containsKey("multi")) {
							int n = Integer.parseInt(var.get("multi"));
							for(int i=0; i<n; i++)
								cc.addId(var.get(aObj + i));
						} else
							cc.addId(var.get(aObj));
						continue;
					case "add": // add triple
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
				
				logger.info("ns = "+cc.ns+"; id = "+cc.id);
				
				try { // executes only if ns and id are there
					for(int i=0; i<cc.id.size(); i++)
						m.add(subject, propRes, m.createResource(cc.get(i)));
				} catch (Exception e) {}
				
				// execute script for lookup
				if(uri != null)
					m.add(subject, propRes, uri);
				
			}
			
		}

		logger.info("====== GENERATED STATEMENTS ======");
		for(Statement st : m.listStatements().toList())
			logger.info(st);
		
		m.setNsPrefixes(openML.getNsPrefixMap());
		openML.add(m);
		
		if(saveOutput) {
			// save data cube
			File dir = new File(System.getProperty("user.dir") + "/rdf/"+className);
			dir.mkdirs();
			FileOutputStream cube = new FileOutputStream(dir+"/"+id+".rdf");
			m.write(cube);
			
			// save to output
			FileOutputStream file = new FileOutputStream(System.getProperty("user.dir") + "/etc/OpenML_out.rdf");
			openML.write(file);
		} else {
			// print output
			openML.write(System.out);
		}
		
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
		if(!object.equals("null"))
			m.add(subject, propRes, object);
	}

	private void readAnnotations(String string) throws FileNotFoundException {
		
		Scanner in = new Scanner(new File("annotated/"+string+".csv"));
		
		while(in.hasNextLine()) {
			
			String[] line = in.nextLine().split("\t");
			if(line.length < 4)
				continue;
			
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
			
			annotations.put(string + "_" + a.propertyName, a);
			
		}
		
		for(Entry<String, Annotation> ann : annotations.entrySet())
			logger.info(ann);
		
		in.close();
		
	}

}

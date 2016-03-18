package org.openml.rdf.instances;

import java.io.FileNotFoundException;
import java.util.HashMap;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.log4j.Logger;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
class Lookup extends HashMap<String, Resource> {

	private static Logger logger = Logger.getLogger(Lookup.class); 
	
	private static Lookup instance;
	private StringMetric metric;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3237344053686202331L;
	
	protected Lookup() {
		super();
		metric = StringMetrics.levenshtein();
	}
	
	public static Lookup getInstance() throws FileNotFoundException {
		if (instance == null) {
			instance = new Lookup();
		}
		return instance;
	}


	/**
	 * Populate lookup table (name to URI).
	 * @param model 
	 * 
	 * @return
	 */
	public void populate(Model model) {
		
		ResIterator list = model.listResourcesWithProperty(RDF.type, OWL.Class);
		while(list.hasNext()) {
			Resource r = list.next();
			this.put(r.getLocalName(), r);
		}
		logger.info("Classes map = " + this);
		
	}
	
	public Resource mostSimilar(String jsonObject) {
		Resource r = null;
		double max = 0.0;
		
		for(String name : this.keySet()) {
			Resource res = this.get(name);
			double d = metric.compare(jsonObject.toLowerCase(), res.getLocalName().toLowerCase());
			logger.info("sim("+jsonObject+", "+res.getLocalName()+" = "+ d);
			if(d > max) {
				max = d;
				r = res;
			}
		}
		logger.info(r + " is the most similar to "+jsonObject+ " (sim="+max+")");
		return r;
	}

}

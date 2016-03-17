package org.openml.rdf.instances;

import java.util.HashMap;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
class Lookup extends HashMap<String, Resource> {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3237344053686202331L;

	/**
	 * Populate lookup table (name to URI).
	 * @param openML 
	 * 
	 * @return
	 */
	public static void populate(Model openML) {
		
		ResIterator list = openML.listResourcesWithProperty(RDF.type, OWL.class);
		while(list.hasNext()) {
			Resource r = list.next();
//			put(r.getLocalName(), r);
		}
		
	}

}

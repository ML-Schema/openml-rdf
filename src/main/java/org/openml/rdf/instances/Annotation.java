package org.openml.rdf.instances;

import java.util.HashMap;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
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
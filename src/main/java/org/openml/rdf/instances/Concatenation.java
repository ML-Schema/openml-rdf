package org.openml.rdf.instances;

import java.util.ArrayList;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
class Concatenation {
	String ns;
	ArrayList<String> id;
	
	public Concatenation() {
		super();
		id = new ArrayList<>();
	}
	
	public void setNs(String ns) {
		this.ns = ns;
	}
	public void addId(String id) {
		this.id.add(id);
	}
	@Override
	public String toString() {
		return ns.trim() + id.get(0);
	}
	
	public String get(int i) {
		return ns + id.get(i);
	}
}
package org.openml.rdf.instances;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
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
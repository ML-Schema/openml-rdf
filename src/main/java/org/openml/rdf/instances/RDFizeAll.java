package org.openml.rdf.instances;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONException;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class RDFizeAll {

	public static void run(String args, int from, int to) {
		RDFizer rdf = null;
		try {
			rdf = RDFizer.getInstance();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			RDFizer.logger.error(e1.getMessage());
		}
		for (int i = from; i <= to; i++)
			try {
				rdf.rdfize(args, String.valueOf(i));
			} catch (JSONException | IOException e) {
				// TODO Auto-generated catch block
				RDFizer.logger.error(e.getMessage());
			}
	}

}

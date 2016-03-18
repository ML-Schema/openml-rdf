package org.openml.rdf.instances;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONException;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class RDFizeAll {

	public static void run(String args) {
		RDFizer rdf = null;
		try {
			rdf = RDFizer.getInstance();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			RDFizer.logger.error(e1.getMessage());
		}
		for (int i = 1; i <= 8707; i++)
			try {
				rdf.rdfize(args, String.valueOf(i));
			} catch (JSONException | IOException e) {
				// TODO Auto-generated catch block
				RDFizer.logger.error(e.getMessage());
			}
	}

}

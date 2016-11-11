package org.openml.rdf.instances;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class RDFizeAll {

	public static void run(String args) {
		RDFizer rdf;
		try {
			rdf = RDFizer.getInstance();
		} catch (FileNotFoundException e1) {
			RDFizer.logger.error(e1.getMessage());
			return;
		}
		ArrayList<String> array;
		try {
			array = APICaller.getIDs(args.toLowerCase());
		} catch (JSONException | IOException e1) {
			RDFizer.logger.error(e1.getMessage());
			return;
		}
		for (String id : array)
			try {
				rdf.rdfize(args, id, true);
			} catch (JSONException | IOException e) {
				RDFizer.logger.error(e.getMessage());
			}
	}

}

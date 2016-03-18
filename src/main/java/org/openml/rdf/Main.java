package org.openml.rdf;

import java.io.IOException;

import org.json.JSONException;
import org.openml.rdf.instances.RDFizeAll;
import org.openml.rdf.instances.RDFizer;
import org.openml.rdf.vocabulary.VocabularyBuilder;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Main {

	public static void main(String[] args) {
		
		switch(args[0]) {
		case "build-vocabulary":
			try {
				VocabularyBuilder.getInstance().buildAll(args[1], args[2], args[3]);
			} catch (JSONException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		case "rdfize":
			try {
				RDFizer.getInstance().rdfize(args[1], args[2]);
			} catch (JSONException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		case "populate":
			RDFizeAll.run(args[1]);
			return;
		}
		
	}

}

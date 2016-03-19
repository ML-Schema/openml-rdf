package org.openml.rdf.instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openml.rdf.util.Util;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class APICaller {

	private static Logger logger = Logger.getLogger(APICaller.class);

	public static ArrayList<String> getIDs(String what) throws JSONException,
			IOException {

		ArrayList<String> ids = new ArrayList<>();

		String key = getAPIKey();

		String endpoint = "http://www.openml.org/api_new/v1/json/" + what
				+ "/list?api_key=" + key;
		logger.info("Downloading JSON list via API call...");
		JSONObject json = Util.readJsonFromUrl(endpoint);
		logger.info("Downloaded.");
		JSONObject jobj = (JSONObject) json.get(json.keys().next());
		JSONArray arr = (JSONArray) jobj.get(jobj.keys().next());
		for (int i = 0; i < arr.length(); i++) {
			JSONObject obj = arr.getJSONObject(i);
			ids.add((String) obj.get(what + "_id"));
		}

		return ids;

	}

	private static String getAPIKey() throws FileNotFoundException {

		Scanner in = new Scanner(new File("api_key"));
		String key = in.nextLine();
		in.close();
		return key;
	}

}

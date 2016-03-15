package org.openml.rdf.vocabulary;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class VocabularyBuilder {
	
	private static final String ONTO_NAMESPACE = "http://www.openml.org/vocabulary#";
	
	private static VocabularyBuilder instance;
	
	protected VocabularyBuilder() {
		super();
	}
	
	public static VocabularyBuilder getInstance() {
		if(instance == null)
			instance = new VocabularyBuilder();
		return instance;
	}
	
	/**
	 * Build all properties for each class contained in a specification file.<br><br>
	 * Specification file contains tab-separated rows as:<br>
	 * [ClassName][TAB][JSON example URL]
	 * 
	 * @param specFile
	 * @throws FileNotFoundException 
	 */
	public void buildAll(String specFile) throws FileNotFoundException {
		Scanner in = new Scanner(new File(specFile));
		while(in.hasNextLine()) {
			String[] line = in.nextLine().split("\t");
			build(line[0], line[1]);
		}
		in.close();
	}
	
	/**
	 * @param className
	 * @param jsonURL
	 */
	private void build(String className, String jsonURL) {
		// TODO
		
		// fetch JSON file
		
		
	}
	
}

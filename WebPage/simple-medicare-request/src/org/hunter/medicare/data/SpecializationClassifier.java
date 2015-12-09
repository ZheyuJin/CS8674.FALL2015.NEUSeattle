package org.hunter.medicare.data;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.feature.IDFModel;
import org.apache.spark.mllib.linalg.SparseVector;
import org.apache.spark.mllib.linalg.Vector;

public class SpecializationClassifier {

    private static final int MAX_TFHASH_SIZE = 10000;
    /**
     * 
     * @param resourceClass
     * @param resourceName
     * @throws URISyntaxException
     */
    public static Path getResourcePath(Class<?> resourceClass, String resourceName) throws URISyntaxException {
	URL url = resourceClass.getResource(resourceName);
	return Paths.get(url.toURI());
    } 

    /**
     * 
     * @param desc
     * @return prediction
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static String predictDocs(String desc) throws ClassNotFoundException, IOException, URISyntaxException{
	
	NaiveBayesModel model = NBLoader.getSavedObjectNBModel();
	HashingTF tf = new HashingTF(MAX_TFHASH_SIZE);
	Vector v = (Vector) tf.transform(new ArrayList<String>(Arrays.asList(desc.split(" "))));
	IDFModel tfidf = NBLoader.loadTFIDFModelObject();
	Vector sv = tfidf.transform(v);
	String res = NBLoader.loadCategoryMap().get((int)model.predict(v));
	return res;
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException, URISyntaxException{
	System.out.println(predictDocs("Anesthesia for lens"));
	System.out.println(predictDocs("Exploration of brain"));
    }
}

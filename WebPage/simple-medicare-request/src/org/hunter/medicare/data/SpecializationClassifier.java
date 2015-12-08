package org.hunter.medicare.data;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.linalg.Vector;

public class SpecializationClassifier {

    public static String predictDocs(String desc) throws ClassNotFoundException, IOException, URISyntaxException{
	System.setProperty("hadoop.home.dir", "D:\\winutils\\");
	SparkConf conf = new SparkConf().setAppName("Classifier").setMaster("local[*]");
	JavaSparkContext sc = new JavaSparkContext(conf);
	NBLoader nbl = new NBLoader(sc);
	NaiveBayesModel model = nbl.getSavedNBModel();
	HashMap<Integer, String> labels = NBLoader.loadCategoryMap();
	HashingTF tf = new HashingTF(10000);
	Vector v = (Vector) tf.transform(new ArrayList<String>(Arrays.asList(desc.split(" "))));
	String res = labels.get((int)model.predict(v));
	sc.stop();
	return res;
    }
    
    public static void main(String[] args) throws ClassNotFoundException, IOException, URISyntaxException{
	System.setProperty("hadoop.home.dir", "D:\\winutils\\");
	System.out.println(predictDocs(" Anesthesia for lens"));
    }
}

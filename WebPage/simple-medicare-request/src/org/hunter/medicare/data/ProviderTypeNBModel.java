package org.hunter.medicare.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

//import org.apache.spark.SparkConf;
import org.apache.spark.Accumulator;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.feature.IDF;
import org.apache.spark.mllib.feature.IDFModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;

import scala.Tuple2;

public class ProviderTypeNBModel {

    private final JavaSparkContext sc;

    //@Value("${file.directory}")
    //private String dataPath;

    // Hardcoding the data and model paths for now.
    private static String DATAPATH = "C:\\Users\\Brian\\Desktop\\PUFDataSample.csv";
    private static String MODELPATH = "C:\\Users\\Brian\\Desktop\\NaiveBayesModel";

    public ProviderTypeNBModel(JavaSparkContext sc){
	System.setProperty("hadoop.home.dir", "D:\\winutils\\");
	this.sc = sc;
    }

    /*
     * 	trainNaiveBayesModel String -> NaiveBayesModel
     * 	Draws from data stored in HDFS to train a NaiveBayesModel
     * 	saves the model to the specified outputModelPath, unless
     * 	a Model is already saved there
     */
    public NaiveBayesModel trainNaiveBayesModel(String outputModelPath){
	NaiveBayesModel model = null;
	Path path = FileSystems.getDefault().getPath(outputModelPath);
	try{
	    // Configure SparkContext, and load and parse data, then remove header
	    JavaRDD<String> data = sc.textFile(DATAPATH);
	    JavaRDD<Record> dataParsed = data.map(new parseCSV());
	    removeHeader(dataParsed);

	    //	    TEST TRAINING SET
	    //	    Record one = new Record("one", "foo foo foo bar foo");
	    //	    Record two = new Record("two", "foo foo buzz foo");
	    //	    Record three = new Record("one", "foo foo bar");
	    //	    Record four = new Record("two", "foo buzz buzz foo");
	    //	    Record[] recs = {one, two, three};
	    //	    List<Record> list = new ArrayList<Record>();
	    //	    list.add(one); 
	    //	    list.add(two);
	    //	    list.add(three);
	    //	    list.add(four);
	    //	    JavaRDD<Record> dataParsed = sc.parallelize(list);

	    // Extract the Data Labels
	    List<String> labels = getDistinctLabels(dataParsed);

	    // Get number of labels, and prepare a map of labels to integers
	    Accumulator<Integer> numTypesAcc = sc.accumulator(labels.size());
	    Broadcast<HashMap<String,Integer>> labelMap = sc.broadcast(generateLabelMap(labels, numTypesAcc));

	    // Convert Records of Provider_Type labels, and HCPCSDescription values
	    // to LabeledPoints of labels, and TFIDF vectors of the HCPCSDescriptions
	    JavaRDD<LabeledPoint> labeledPoints = recordRDDtoLabeledPointRDD(dataParsed, labelMap);
	    model = NaiveBayes.train(labeledPoints.rdd(), 1.0);
	}
	finally{
	    // Save the model
	    if (model != null && Files.notExists(path)){ model.save(sc.sc(), path.toString()); }
	}
	return model;
    }

    /*
     * 	loadNaiveBayesModel: String -> NaiveBayesModel
     * 	Loads a pre-exisiting NaiveBayesModel from the given modelPath
     * 	Throws error if the file is not found
     */
    public NaiveBayesModel loadNaiveBayesModel(String modelPath) throws FileNotFoundException{
	Path path = FileSystems.getDefault().getPath(modelPath);
	NaiveBayesModel model = null;
	try {
	    if (Files.exists(path)){
		// Load the model from given path
		model = NaiveBayesModel.load(sc.sc(), path.toString());
	    }
	}catch(Exception e){
	    System.err.println("Pre-existing NaiveBayesModel not found at given filepath!");
	    e.printStackTrace();
	}
	return model;
    }

    /*
     *  generateLabelMap: List<String>, Accumulator<Integer> -> HashMap<String, Integer>
     *  Takes a List of distinctLabels, and the number of distinct labels and creates a
     *  HashMap mapping each label to an integer
     *  Utilizes a SparkContext Accumulator for concurrency across partitions
     */
    private static HashMap<String, Integer> generateLabelMap(List<String> distinctLabels, Accumulator<Integer> numTypesAcc){

	HashMap<String, Integer> labelMap = new HashMap<String, Integer>();

	for (String label : distinctLabels){
	    if (!labelMap.containsKey(label)){
		labelMap.put(label, numTypesAcc.value());
		numTypesAcc.add(-1);
	    }
	}
	return labelMap;
    }


    /*
     *  detDistinctLabels: JavaRDD<Record> -> List<String>
     *  Takes an RDD of Records and returns a List of the distinct labels in the RDD
     */
    private static List<String> getDistinctLabels(JavaRDD<Record> dataParsed){

	HashMap<String, Integer> labelMap = new HashMap<String, Integer>();
	class extractProviderType implements Function<Record, String>{
	    public String call(Record rec) throws Exception { return rec.getProviderType(); }
	}
	return dataParsed.map(new extractProviderType()).distinct().collect();
    }

    /*
     * 	recordRDDtoLabelePointRDD: JavaRDD<Record>, Broadcast<HashMap<String, Integer>> 
     * 					-> JavaRDD<LabeledPoint>
     * 	Takes an RDD of Records and returns an RDD of LabeledPoints, where the labels
     * 	are mapped to integers using labelMap, and the values are TFIDF vectors of the
     * 	HCPCS Descriptions
     */

    protected static JavaRDD<LabeledPoint> recordRDDtoLabeledPointRDD(
	    JavaRDD<Record> recordRDD, 
	    Broadcast<HashMap<String, Integer>> labelMap){
	class getLabels implements Function<Record, Integer>{
	    public Integer call(Record rec) throws Exception {
		return mapLabel(rec.getProviderType(), labelMap);
	    }
	}
	JavaRDD<Integer> labels = recordRDD.map(new getLabels());

	HashingTF tf = new HashingTF();
	class getTF implements Function<Record, Vector>{
	    public Vector call(Record rec) throws Exception {
		List<String> contentsList = new ArrayList(Arrays.asList(rec.getHCPCSCode().split(" ")));
		return tf.transform(contentsList);
	    }
	}
	JavaRDD<Vector> tfVectors = recordRDD.map(new getTF());

	IDFModel idfModel = new IDF().fit(tfVectors);
	JavaRDD<Vector> tfidf = idfModel.transform(tfVectors);
	JavaPairRDD<Integer, Vector> labelAndTfIdfPairs = labels.zip(tfidf);
	JavaRDD<Tuple2<Integer, Vector>> labelAndTfIdfRDD = JavaRDD.fromRDD(JavaPairRDD.toRDD(labelAndTfIdfPairs), labelAndTfIdfPairs.classTag());

	class getLabeledPoint implements Function<Tuple2<Integer, Vector>, LabeledPoint>{
	    public LabeledPoint call(Tuple2<Integer, Vector> tuple) throws Exception { 
		return new LabeledPoint((double)tuple._1, tuple._2); }
	}
	return labelAndTfIdfRDD.map(new getLabeledPoint());
    }


    /*
     * 	mapLabel: String HashMap<String, Integer> -> int
     * 	Maps the input key to its respective Integer as per the Broadcast HashMap, broadcastTypeLabels
     */
    protected static int mapLabel(String key, Broadcast<HashMap<String, Integer>> broadcastTypeLabels){
	return broadcastTypeLabels.value().get(key);
    }

    /*
     * 	removeHeader: JavaRDD<Record> -> JavaRDD<Record>
     * 	Removes the first line from the given RDD
     */

    protected static JavaRDD<Record> removeHeader(JavaRDD<Record> rdd){
	Record header = rdd.first();
	class filterHeader implements Function<Record, Boolean>{
	    public Boolean call(Record record) throws Exception{ return !record.equals(header); }
	};
	return rdd = rdd.filter(new filterHeader());
    }

    /*
     * 	getTrainingPredictions: NaiveBayesModel JavaRDD<LabeledPoint> -> JavaPairRDD<Double, Double>
     * 	Given a NaiveBayesModel and an RDD of LabeledPoints where the values are TFIDF Vectors
     * 	returns the predicted label, and actual label. Useful for testing the accuracy of the model
     */

    protected static JavaPairRDD<Double, Double> getTrainingPredictions(NaiveBayesModel model, JavaRDD<LabeledPoint> labeledPoints ){
	class getPredictionsAndLabels implements PairFunction<LabeledPoint, Double, Double>{
	    public Tuple2<Double, Double> call (LabeledPoint p) {
		return new Tuple2<Double, Double>(model.predict(p.features()), p.label());
	    }
	}
	return labeledPoints.mapToPair(new getPredictionsAndLabels());
    }

    /*
     * 	Sub-Class: parseCSV
     * 		call: String -> Record
     * 	Parses the given String as a CSV line and creates a Record from it
     */
    static class parseCSV implements Function<String, Record>{
	private static final Pattern CSV = Pattern.compile(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

	@Override
	public Record call(String line){
	    String[] features = CSV.split(line);
	    Record record = new Record(features[13], features[17]);
	    return record;
	}
    }

    /*
     * 	Sub-Class: accuracyFilter
     * 		call: Tuple2<Double, Double> -> Boolean
     * 	Filters a PairRDD of predicted and actual labels. Useful for testing
     *  the number of wrong predictions against the total number of records
     */
    static class accuracyFilter implements Function<Tuple2<Double, Double>, Boolean>{
	public Boolean call(Tuple2<Double, Double> p){ return p._1().equals(p._2()); }
    }

    public static void main(String[] args){
	SparkConfig conf = new SparkConfig();
	JavaSparkContext sc = conf.javaSparkContext();
	ProviderTypeNBModel nbModel = new ProviderTypeNBModel(sc);
	NaiveBayesModel model = nbModel.trainNaiveBayesModel(MODELPATH);
	//NaiveBayesModel model = nbModel.loadNaiveBayesModel(MODELPATH);

	Record one = new Record("one", "foo foo foo bar foo");
	Record two = new Record("two", "foo foo buzz foo");
	Record three = new Record("one", "foo foo bar");
	Record four = new Record("two", "foo buzz buzz foo");
	Record[] recs = {one, two, three};
	List<Record> list = new ArrayList<Record>();
	list.add(one); 
	list.add(two);
	list.add(three);
	list.add(four);
	JavaRDD<Record> dataParsed = sc.parallelize(list);

	List<String> labels = getDistinctLabels(dataParsed);

	Accumulator<Integer> numTypesAcc = sc.accumulator(labels.size());
	Broadcast<HashMap<String,Integer>> labelMap = sc.broadcast(generateLabelMap(labels, numTypesAcc));
	JavaRDD<LabeledPoint> labeledPoints = recordRDDtoLabeledPointRDD(dataParsed, labelMap);

	JavaPairRDD<Double, Double> predictionAndLabel = getTrainingPredictions(model, labeledPoints);
	System.out.println(predictionAndLabel.take(5).toString());
    }
}

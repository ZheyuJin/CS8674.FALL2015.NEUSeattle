package org.hunter.medicare.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

//import org.apache.spark.SparkConf;
import org.apache.spark.Accumulator;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.launcher.SparkLauncher;
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
    //private static String DATAPATH = "s3:/bg-medicare/Medicare500.csv";
    //private static String HDFSDNS = "hdfs://ec2-52-34-41-203.us-west-2.compute.amazonaws.com";
    //private static String DATAPATH = HDFSDNS + "/user/root/medicareData/Medicare500.txt";
    //private static String MOCKDATAPATH = "hdfs://ec2-52-34-41-203.us-west-2.compute.amazonaws.com:9010/user/root/medicareData/Mock5.txt";
    private static String MOCKDATAPATH = "C:\\Users\\Brian\\Desktop\\Mock5.txt";
    private static String MODELPATH = "NaiveBayesModel";
    //private static String MODELPATH = HDFSDNS + "/user/root/medicareData/NaiveBayes";
    //private static String TESTMODELPATH = HDFSDNS + "/user/root/medicareData/TestModel";
    private static String TFIDFPATH = "TFIDFModel";
    //private static String TFIDFPATH = HDFSDNS + "/user/root/medicareData/TFIDFModel";


    public ProviderTypeNBModel(JavaSparkContext sc){
	System.setProperty("hadoop.home.dir", "D:\\winutils\\");
	this.sc = sc;
    }

    /*
     * 	trainNaiveBayesModel -> NaiveBayesModel
     * 	Draws from data stored at preset filepath to train a NaiveBayesModel
     * 	saves the model to the specified outputModelPath, unless
     * 	a Model is already saved there
     */
    public NaiveBayesModel trainNaiveBayesModel(){
	NaiveBayesModel model = null;
	Path path = FileSystems.getDefault().getPath(MODELPATH);
	try{
	    //Configure SparkContext, and load and parse data, then remove header
	    JavaRDD<String> data = sc.textFile(DATAPATH);
	    JavaRDD<Record> dataParsed = data.map(new parseCSV());
	    removeHeader(dataParsed);
	    // Extract the Data Labels
	    List<String> labels = getDistinctLabels(dataParsed);

	    // Get number of labels, and prepare a map of labels to integers
	    Accumulator<Integer> numTypesAcc = sc.accumulator(labels.size());
	    Broadcast<HashMap<String,Integer>> labelMap = sc.broadcast(generateLabelMap(labels, numTypesAcc));

	    // Convert Records of Provider_Type labels, and HCPCSDescription values
	    // to LabeledPoints of labels, and TFIDF vectors of the HCPCSDescriptions
	    JavaRDD<LabeledPoint> labeledPoints = recordRDDtoLabeledPointRDD(dataParsed, labelMap, false);
	    model = NaiveBayes.train(labeledPoints.rdd(), 1.0);
	}finally{
	    // Save the model
	    if (model != null && Files.notExists(path)){ model.save(sc.sc(), path.toString()); }
	}
	return model;
    }

    /*
     * 	trainNaiveBayesModel String -> NaiveBayesModel
     * 	Draws from data stored in HDFS to train a NaiveBayesModel
     * 	saves the model to the specified outputModelPath, unless
     * 	a Model is already saved there
     */
    public static NaiveBayesModel trainNaiveBayesModel(String outputModelPath, String inputDataPath, JavaSparkContext sc){
	NaiveBayesModel model = null;
	System.setProperty("hadoop.home.dir", "D:\\winutils\\");
	System.out.println("HERE1");
	Path path = FileSystems.getDefault().getPath(outputModelPath);
	System.out.println("HERE2");
	try{
	    //Configure SparkContext, and load and parse data, then remove header
	    JavaRDD<String> data = sc.textFile(inputDataPath);
	    JavaRDD<Record> dataParsed = data.map(new parseCSV());
	    removeHeader(dataParsed);

	    // Extract the Data Labels
	    List<String> labels = getDistinctLabels(dataParsed);

	    // Get number of labels, and prepare a map of labels to integers
	    Accumulator<Integer> numTypesAcc = sc.accumulator(labels.size());
	    Broadcast<HashMap<String,Integer>> labelMap = sc.broadcast(generateLabelMap(labels, numTypesAcc));

	    // Convert Records of Provider_Type labels, and HCPCSDescription values
	    // to LabeledPoints of labels, and TFIDF vectors of the HCPCSDescriptions
	    JavaRDD<LabeledPoint> labeledPoints = recordRDDtoLabeledPointRDD(dataParsed, labelMap, true);
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
    public static NaiveBayesModel loadNaiveBayesModel(String modelPath, JavaSparkContext sc) throws FileNotFoundException{
	//Path path = FileSystems.getDefault().getPath(modelPath);
	NaiveBayesModel model = null;
	try {
	    if (modelPath != null){
		// Load the model from given path
		model = NaiveBayesModel.load(sc.sc(), modelPath);
	    }
	}catch(Exception e){
	    System.err.println("Pre-existing NaiveBayesModel not found at given filepath!");
	    e.printStackTrace();
	}
	return model;
    }

    /*
     * 	convertToTFVector: String -> Vector
     * 	Takes an input HCPCS Description and converts it to a 
     * 	TFIDF Vector based on the current TFIDF Models
     */
    @SuppressWarnings("unchecked")
    public static Vector convertStringArrRDDToTFVector(String[] document, JavaRDD<Vector> tfVectors){
	HashingTF tf = new HashingTF();
	List<String> hcpcsCodesAsList = 
		new ArrayList<String>(Arrays.asList(document));
	Vector newTFVector = tf.transform(hcpcsCodesAsList);
	//tfVectors = tfVectors.union(sc.parallelize(new ArrayList<Vector>(Arrays.asList(newTFVector))));
	IDFModel idfModel = new IDF().fit(tfVectors);
	return idfModel.transform(newTFVector);
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
     * 	recordRDDtoLabeledPointRDD: JavaRDD<Record>, Broadcast<HashMap<String, Integer>> 
     * 					-> JavaRDD<LabeledPoint>
     * 	Takes an RDD of Records and returns an RDD of LabeledPoints, where the labels
     * 	are mapped to integers using labelMap, and the values are TFIDF vectors of the
     * 	HCPCS Descriptions
     */
    protected static JavaRDD<LabeledPoint> recordRDDtoLabeledPointRDD(
	    JavaRDD<Record> recordRDD, 
	    Broadcast<HashMap<String, Integer>> labelMap,
	    Boolean saveModel){
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
	if(saveModel) tfVectors.saveAsObjectFile(TFIDFPATH);

	IDFModel idfModel = new IDF().fit(tfVectors);

	JavaRDD<Vector> tfidf = idfModel.transform(tfVectors);
	//tfidf.saveAsObjectFile(TFIDFPATH);
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
	    Record record = new Record(features[0], features[1]);
	    //Record record = new Record(features[0], features[13], features[16]);
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

    /*
     * 	getPrediction: String[] -> Double
     * 	Given a list of HCPCS codes, predicts its label using the existing NaiveBayesModel
     */
    public static Double getPrediction(String[] hcpcsCodes) throws FileNotFoundException{

	SparkConfig conf = new SparkConfig();
	JavaSparkContext sc = conf.javaSparkContext();
	String testString = "This is a test";
	ArrayList test = new ArrayList(Arrays.asList(testString.split(" ")));
	System.out.println(sc.parallelize(test).take(10));
	System.setProperty("hadoop.home.dir", "D:\\winutils\\");
	NaiveBayesModel model = loadNaiveBayesModel(MODELPATH, sc); 
	JavaRDD<Vector> tfVectors = sc.objectFile(TFIDFPATH);
	Vector hcpcsAsTFIDF = convertStringArrRDDToTFVector(hcpcsCodes, tfVectors);
	return model.predict(hcpcsAsTFIDF);

    }

    public static void main(String[] args) throws IOException, InterruptedException{
	System.setProperty("hadoop.home.dir", "D:\\winutils\\");
	String[] hcpcsCodes = {"foo foo foo bar foo"};
	System.out.println(getPrediction(hcpcsCodes));
	//SparkConfig conf = new SparkConfig();
	//JavaSparkContext sc = conf.javaSparkContext();
	//	//String classpathLocation = "C:\\Users\\Brian\\Documents\\GitHub\\CS8674.FALL2015.NEUSeattle\\WebPage\\simple-medicare-request\\target\\classes\\org\\hunter\\medicare\\data\\NaiveBayesModel";
	//	String classpathLocation = "org/hunter/medicare/data/NaiveBayesModel";
	//	//URL classpathResource = Thread.currentThread().getContextClassLoader().getResource(classpathLocation);
	//	//System.out.println(Thread.currentThread().getContextClassLoader().getResource(classpathLocation));
	//NaiveBayesModel model = loadNaiveBayesModel(MODELPATH, sc); 
	//	//NaiveBayesModel model = trainNaiveBayesModel(MODELPATH, MOCKDATAPATH, sc);
	//JavaRDD<Vector> tfVectors = sc.objectFile(TFIDFPATH);
	//	System.out.println(tfVectors.take(5).toString());
	//Vector hcpcsAsTFIDF = convertStringArrRDDToTFVector(hcpcsCodes, tfVectors);
	//	System.out.println(hcpcsAsTFIDF.toString());
	//System.out.println(model.predict(hcpcsAsTFIDF));
    }
}

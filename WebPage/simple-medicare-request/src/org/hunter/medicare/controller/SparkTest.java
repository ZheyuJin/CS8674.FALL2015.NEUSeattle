package org.hunter.medicare.controller;

import org.apache.spark.SparkConf;
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


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hunter.medicare.data.Record;
import org.hunter.medicare.data.SparkConfig;

import scala.Tuple2;

public class SparkTest {

    // SparkContext and Broadcast Variable need to be shared
    protected static JavaSparkContext sc;
    protected static Broadcast<HashMap<String, Integer>> broadcastLabelMap;

    // Constants
    private static double[] WEIGHTS = {0.01, 0.99};
    private static String MODELPATH = "C:\\Users\\Brian\\Desktop\\NaiveBayesModel";
    private static String INPUTPATH = "C:\\Users\\Brian\\Desktop\\PUFDataSample.csv";

    //    private static void handleArgs(String[] args){
    //	if (args.length == 0 || args.length > 2){
    //	    System.out.println("Incorrect usage");
    //	    System.out.println("Usage: SparkTest <input_path> <opts: {-header}>");
    //	    System.exit(1);
    //	}
    //	if (args.length == 1){
    //	    inputFile = args[0];
    //	}
    //	if (args.length == 2){
    //	    inputFile = args[0];
    //	    removeHeader = (args[1].equals("-header")) ? true : false;
    //	}
    //    }



    //THIS IS ONLY TEST CODE
    //	Record one = new Record("one", "foo foo foo bar foo");
    //	Record two = new Record("two", "foo foo buzz foo");
    //	Record three = new Record("one", "foo foo bar");
    //	Record four = new Record("two", "foo buzz buzz foo");
    //	Record[] recs = {one, two, three};
    //	List<Record> list = new ArrayList<Record>();
    //	list.add(one); 
    //	list.add(two);
    //	list.add(three);
    //	list.add(four);
    //	dataParsed = sc.parallelize(list);
    //System.out.println(dataParsed.take(3).toString());
    // END TEST CODE
    private static HashMap<String, Integer> generateLabelMap(JavaRDD<Record> dataParsed){

	HashMap<String, Integer> labelMap = new HashMap<String, Integer>();
	class extractProviderType implements Function<Record, String>{
	    public String call(Record rec) throws Exception { return rec.getProviderType(); }
	}
	List<String> types = dataParsed.map(new extractProviderType()).distinct().collect();

	Accumulator<Integer> numTypesAcc = sc.accumulator(types.size());

	for (String item : types){
	    if (!labelMap.containsKey(item)){
		labelMap.put(item, numTypesAcc.value());
		numTypesAcc.add(-1);
	    }
	}
	return labelMap;
    }

    protected static int mapLabel(String key, Broadcast<HashMap<String, Integer>> broadcastTypeLabels){
	return broadcastTypeLabels.value().get(key);
    }

    protected static JavaRDD<Record> removeHeader(JavaRDD<Record> rdd){
	Record header = rdd.first();
	class filterHeader implements Function<Record, Boolean>{
	    public Boolean call(Record record) throws Exception{ return !record.equals(header); }
	};
	return rdd = rdd.filter(new filterHeader());
    }

    static class parseCSV implements Function<String, Record>{
	private static final Pattern CSV = Pattern.compile(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

	@Override
	public Record call(String line){
	    String[] features = CSV.split(line);
	    Record record = new Record(features[13], features[17]);
	    return record;
	}
    }

    static class accuracyFilter implements Function<Tuple2<Double, Double>, Boolean>{
	public Boolean call(Tuple2<Double, Double> p){ return p._1().equals(p._2()); }
    }

    public static JavaRDD<Record> proceduresAndLabelsETL(String inputPath, Boolean removeHeader){
	System.setProperty("hadoop.home.dir", "D:\\winutils\\");
	//SparkConfig conf = new SparkConfig();
	//sc = conf.javaSparkContext();
	JavaRDD<String> data = sc.textFile(inputPath);
	JavaRDD<Record> dataParsed = data.map(new parseCSV());
	if (removeHeader)removeHeader(dataParsed);
	return dataParsed;
    }

    protected static JavaRDD<LabeledPoint> recordRDDtoLabeledPointRDD(JavaRDD<Record> recordRDD, Broadcast<HashMap<String, Integer>> labelMap){
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

    // TODO: Hunter's functions
    public String predictProviderType(List<String> procedures){
	return "In progress";
    }

    public String predictProviderType(String procedure){
	
	return "In progress";
    }

    public NaiveBayesModel trainNaiveBayesModel(String inputPath, Boolean removeHeader){
	NaiveBayesModel model = null;
	try{
	    SparkConfig conf = new SparkConfig();
	    sc = conf.javaSparkContext();
	    JavaRDD<Record> trainingData = proceduresAndLabelsETL(inputPath, removeHeader);
	    broadcastLabelMap = sc.broadcast(generateLabelMap(trainingData));
	    JavaRDD<LabeledPoint> labeledPoints = recordRDDtoLabeledPointRDD(trainingData, broadcastLabelMap);
	    Path path = FileSystems.getDefault().getPath(MODELPATH);
	    model = NaiveBayes.train(labeledPoints.rdd(), 1.0);
	    model.save(sc.sc(), path.toString());
	}
	catch(Exception e){
	    e.printStackTrace();
	}
	return model;
    }

    public static NaiveBayesModel getNBModel(){
	Path path = FileSystems.getDefault().getPath(MODELPATH);
	NaiveBayesModel model;
	if (Files.exists(path)){
	    return model = NaiveBayesModel.load(sc.sc(), path.toString());
	}
	else{

	    JavaRDD<Record> dataParsed = proceduresAndLabelsETL(INPUTPATH, true);
	    JavaRDD<Record>[] splitData = dataParsed.randomSplit(WEIGHTS);
	    JavaRDD<Record> trainingData = splitData[0];
	    JavaRDD<Record> testData = splitData[1];

	    //trainingData = sc.parallelize(trainingData.take(2));

	    broadcastLabelMap = sc.broadcast(generateLabelMap(trainingData));

	    JavaRDD<LabeledPoint> labeledPoints = recordRDDtoLabeledPointRDD(trainingData, broadcastLabelMap);

	    model = NaiveBayes.train(labeledPoints.rdd(), 1.0);
	    model.save(sc.sc(), path.toString());
	    return model;
	}
    }

    public static void main(String[] args) throws FileNotFoundException {

	// This is for a bug workaround. Some spark libs call, but do not use
	// several Hadoop classes. This simply sets a directory from which they can be 
	// read, so that there aren't any errors thrown.
	System.setProperty("hadoop.home.dir", "D:\\winutils\\");

	Path path = FileSystems.getDefault().getPath(MODELPATH);
	NaiveBayesModel model;
	if (Files.exists(path)){
	    model = NaiveBayesModel.load(sc.sc(), path.toString());
	}
	else{

	    JavaRDD<Record> dataParsed = proceduresAndLabelsETL(INPUTPATH, true);
	    JavaRDD<Record>[] splitData = dataParsed.randomSplit(WEIGHTS);
	    JavaRDD<Record> trainingData = splitData[0];
	    JavaRDD<Record> testData = splitData[1];

	    //trainingData = sc.parallelize(trainingData.take(2));

	    broadcastLabelMap = sc.broadcast(generateLabelMap(trainingData));

	    JavaRDD<LabeledPoint> labeledPoints = recordRDDtoLabeledPointRDD(trainingData, broadcastLabelMap);

	    model = NaiveBayes.train(labeledPoints.rdd(), 1.0);
	    model.save(sc.sc(), path.toString());
	}

	//	class getPredictionsAndLabels implements PairFunction<LabeledPoint, Double, Double>{
	//	    public Tuple2<Double, Double> call (LabeledPoint p) {
	//		return new Tuple2<Double, Double>(model.predict(p.features()), p.label());
	//	    }
	//	}
	//	JavaPairRDD<Double, Double> predictionAndLabel = 
	//		  labeledPoints.mapToPair(new getPredictionsAndLabels());
	//	double accuracy = predictionAndLabel.filter(new accuracyFilter()).count() / (double) labeledPoints.count();
    }
}

package org.hunter.medicare.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

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

    private static String DATAPATH = "C:\\Users\\Brian\\Desktop\\PUFDataSample.csv";
    private static String MODELPATH = "C:\\Users\\Brian\\Desktop\\NaiveBayesModel";

    public ProviderTypeNBModel(JavaSparkContext sc){
	System.setProperty("hadoop.home.dir", "D:\\winutils\\");
	this.sc = sc;
    }

    public NaiveBayesModel trainNaiveBayesModel(String outputModelPath){
	NaiveBayesModel model = null;
	Path path = FileSystems.getDefault().getPath(outputModelPath);
	try{
	    JavaRDD<String> data = sc.textFile(DATAPATH);
	    JavaRDD<Record> dataParsed = data.map(new parseCSV());
	    removeHeader(dataParsed);

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

	    List<String> labels = getDistinctLabels(dataParsed);

	    Accumulator<Integer> numTypesAcc = sc.accumulator(labels.size());
	    Broadcast<HashMap<String,Integer>> labelMap = sc.broadcast(generateLabelMap(labels, numTypesAcc));
	    
	    JavaRDD<LabeledPoint> labeledPoints = recordRDDtoLabeledPointRDD(dataParsed, labelMap);
	    model = NaiveBayes.train(labeledPoints.rdd(), 1.0);
	}
	finally{
	    if (model != null && Files.notExists(path)){ model.save(sc.sc(), path.toString()); }
	}
	return model;
    }

    public NaiveBayesModel loadNaiveBayesModel(String modelPath){
	Path path = FileSystems.getDefault().getPath(modelPath);
	NaiveBayesModel model = null;
	try {
	if (Files.exists(path)){
	    model = NaiveBayesModel.load(sc.sc(), path.toString());
	}
	}catch(Exception e){
	    System.err.println("Pre-existing NaiveBayesModel not found at given filepath!");
	    e.printStackTrace();
	}
	return model;
    }
    
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

    private static List<String> getDistinctLabels(JavaRDD<Record> dataParsed){

	HashMap<String, Integer> labelMap = new HashMap<String, Integer>();
	class extractProviderType implements Function<Record, String>{
	    public String call(Record rec) throws Exception { return rec.getProviderType(); }
	}
	return dataParsed.map(new extractProviderType()).distinct().collect();
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

    protected static JavaPairRDD<Double, Double> getTrainingPredictions(NaiveBayesModel model, JavaRDD<LabeledPoint> labeledPoints ){
	class getPredictionsAndLabels implements PairFunction<LabeledPoint, Double, Double>{
	    public Tuple2<Double, Double> call (LabeledPoint p) {
		return new Tuple2<Double, Double>(model.predict(p.features()), p.label());
	    }
	}
	return labeledPoints.mapToPair(new getPredictionsAndLabels());
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

package com.gillespie.etl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.feature.IDF;
import org.apache.spark.mllib.feature.IDFModel;
import org.apache.spark.mllib.linalg.SparseVector;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.util.MLUtils;

import scala.Tuple2;

/**
 * 
 * @author Brian
 *
 */
public class PrepData {

    private static final int NUM_FEATURES = 28;
    private static final int npi = 0;
    private static final int providerType = 1;
    private static final int hcpcsDesc = 2;
    private static final Pattern CSV = Pattern.compile(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
    private static final int MAX_TFHASH_SIZE = 20000;
    private static final String MAP_DIRECTORY = "C:\\Users\\Brian\\Desktop\\Medicareouts\\potential";
    private static final String TFIDF_PATH = "C:\\Users\\Brian\\Desktop\\Medicareouts\\potential\\TFIDF";
    private static final String[] words = {"and", "or", "by", "in", "the", "to", "with"};
    private static final List<String> BADDIES = new ArrayList<String>(Arrays.asList(words));
    private static final HashSet<String> BAD_WORDS = new HashSet<String>(BADDIES);

    private static HashMap<Integer, String> backMap;
    private static HashMap<String, Integer> forwardMap;
    private static HashMap<String, Integer> h = new HashMap<String, Integer>(MAX_TFHASH_SIZE);

    /*
     * 	Sub-Class: parseCSV
     * 		call: String -> Record
     * 	Parses the given String as a CSV line and creates a Record from it
     */
    static class parseCSVAsRecord implements Function<String, Record>{
	//private static final Pattern CSV = Pattern.compile(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

	private static Boolean invalidLine(String[] features){
	    return (features[npi].equals("") || features[providerType].equals("") || features[hcpcsDesc ].equals("") || 
		    features[npi] == null || features[providerType] == null || features[hcpcsDesc ] == null);
	}

	@Override
	public Record call(String line){
	    String[] features = CSV.split(line, NUM_FEATURES);
	    Record rec = null;
	    try{
		if (!invalidLine(features)) {
		    rec = new Record(features[npi].replaceAll("[^A-Za-z0-9]", ""), 
			    features[providerType].replaceAll("[^A-Za-z0-9 ]", "").trim(), 
			    features[hcpcsDesc ].replaceAll("[^A-Za-z0-9 ]", "").trim());
		}
	    } catch (Exception e){
		System.err.println("Invalid Record encountered!");
	    }
	    return rec;
	}
    }

    /*
     * 	Sub-Class: parseCSV
     * 		call: String -> Record
     * 	Parses the given String as a CSV line and creates a Record from it
     */
    static class parseCSV implements Function<String, String>{
	private static Boolean invalidLine(String[] features){
	    return (features[npi].equals("") || features[providerType].equals("") || features[hcpcsDesc].equals("") || 
		    features[npi] == null || features[providerType] == null || features[hcpcsDesc ] == null);
	}
	@Override
	public String call(String line){
	    String[] features = CSV.split(line, NUM_FEATURES);
	    String rec = "";
	    try{
		if (!invalidLine(features)) {
		    rec = features[npi].replaceAll("[^A-Za-z0-9 ]", "") + ", " +
			    features[providerType].replaceAll("[^A-Za-z0-9 ]", "") + ", " +
			    features[hcpcsDesc].replaceAll("[^A-Za-z0-9 ]", "");
		}
	    } catch (Exception e){
		System.err.println("Invalid Record encountered!");
	    }
	    return rec;
	}
    }

    /**
     * 
     * @return
     */
    protected static HashMap<String, Double> getForwardMap(){
	HashMap<String, Double> forwardMap = null;
	String directory = MAP_DIRECTORY + "\\forwardMap";
	try {
	    FileInputStream mapFile = new FileInputStream(directory);
	    ObjectInputStream mapObj = new ObjectInputStream(mapFile);
	    forwardMap = (HashMap<String, Double>) mapObj.readObject();
	    mapFile.close();
	    mapObj.close();
	} catch(Exception e){
	    e.printStackTrace();
	}
	return forwardMap;
    }

    /**
     * 
     * @return
     */
    protected static HashMap<Double, String> getBackwardMap(){
	HashMap<Double, String> backMap = null;
	String directory = MAP_DIRECTORY + "\\backMap";
	//String directory = "C:\\Users\\Brian\\Desktop\\backMap";
	try {
	    FileInputStream mapFile = new FileInputStream(directory);
	    ObjectInputStream mapObj = new ObjectInputStream(mapFile);
	    backMap = (HashMap<Double, String>) mapObj.readObject();
	    System.out.println("HEY" + backMap.toString());
	    mapFile.close();
	    mapObj.close();
	} catch(Exception e){
	    e.printStackTrace();
	}
	return backMap;
    }

    /**
     * 
     * @param output
     * @param h
     * @throws IOException
     */
    private static void writeToOut(String output, HashMap h) throws IOException{
	try(FileOutputStream fos = new FileOutputStream(output);){
	    try(ObjectOutputStream oos = new ObjectOutputStream(fos);){
		oos.writeObject(forwardMap);
		fos.close();
		oos.close();
	    }
	}
    }
    
    /**
     * 
     * @param output
     * @param model
     * @throws IOException
     */
    private static void writeToOut(String output, IDFModel model) throws IOException{
	try(FileOutputStream fos = new FileOutputStream(output);){
	    try(ObjectOutputStream oos = new ObjectOutputStream(fos);){
		oos.writeObject(model);
		fos.close();
		oos.close();
	    }
	}
    }

    /**
     * 
     * @param labels
     * @throws IOException
     */
    protected static void labelMaps(List<String> labels) throws IOException{
	backMap = new HashMap<Integer, String>();
	forwardMap = new HashMap<String, Integer>();
	int counter = 1;
	for (String item : labels){
	    if (!forwardMap.containsKey(item)){
		System.out.println(item);
		forwardMap.put(item, counter);
		backMap.put(counter, item);
		counter++;
	    }
	}
	writeToOut(MAP_DIRECTORY + "\\forwardMap", forwardMap);
	System.out.println(getForwardMap().toString());
	writeToOut(MAP_DIRECTORY + "\\backMap", backMap);
	System.out.println(getBackwardMap().toString());
    }


    /**
     * 
     * @param rdd
     * @param useTFIDF
     * @return
     * @throws IOException
     */
    private static JavaRDD<LabeledPoint> groupAndProcess(JavaRDD<Record> rdd, boolean useTFIDF) throws IOException{
	class getKeysAndCodes implements PairFunction<Record, String, String>{
	    public Tuple2<String, String> call(Record r){
		return new Tuple2<String, String>(r.getKey(), r.getHCPCSCode().trim());
	    }
	}
	class reduceValues implements Function2<String, String, String>{
	    public String call (String s1, String s2){
		if (!BAD_WORDS.contains(s2.trim().toLowerCase())){
		    return s1 + " " + s2;
		}else{
		    return s1 + "";
		}
	    }
	}
	
	JavaPairRDD<String, String> joined = rdd.mapToPair(new getKeysAndCodes())
		.reduceByKey(new reduceValues());
	
	HashingTF tf = new HashingTF(MAX_TFHASH_SIZE);
	class tfConvert implements Function<Tuple2<String, String>, Vector>{
	    public Vector call(Tuple2<String, String> t){
		return tf.transform(new ArrayList(Arrays.asList(t._2().trim().split(" "))));
	    }
	}
	JavaRDD<Vector> tfVectors = joined.map(new tfConvert());
	System.out.println(tfVectors.take(20).toString());
	
	List<String> labels = joined.map(t -> t._1.split(":")[1].trim()).distinct().collect();
	labelMaps(labels);
	
	JavaRDD<Double> convertedLabels = joined.map(t -> (double)forwardMap.get(t._1().split(":")[1]));
	if (useTFIDF){
	    IDFModel idfModel = new IDF().fit(tfVectors);
	    writeToOut(TFIDF_PATH, idfModel);
	    JavaRDD<Vector> tfidf = idfModel.transform(tfVectors);
	    JavaPairRDD<Double, Vector> labelAndTfIdfPairs = convertedLabels.zip(tfidf);
	    return labelAndTfIdfPairs.map(t -> new LabeledPoint(t._1(), t._2()));
	}
	JavaPairRDD<Double, Vector> zip = convertedLabels.zip(tfVectors);
	return zip.map(t -> new LabeledPoint(t._1(), t._2()));

    }

    /**
     * 
     * @param rdd
     * @param useTFIDF
     * @return
     * @throws IOException
     */
    private static JavaRDD<LabeledPoint> processTFData(JavaRDD<Record> rdd, boolean useTFIDF) throws IOException{
	labelMaps(rdd.map(x -> x.getProviderType().trim()).distinct().collect());
	HashingTF tf = new HashingTF(MAX_TFHASH_SIZE);
	JavaRDD<Vector> tfVectors =
		rdd.map(x -> tf.transform(new ArrayList(Arrays.asList(x.getHCPCSCode().trim().split(" ")))));
	JavaRDD<Double> convertedLabels = rdd.map(x -> (double)forwardMap.get(x.getHCPCSCode()));
	if (useTFIDF){
	    IDFModel idfModel = new IDF().fit(tfVectors);
	    writeToOut(TFIDF_PATH, idfModel);
	    JavaRDD<Vector> tfidf = idfModel.transform(tfVectors);
	    JavaPairRDD<Double, Vector> labelAndTfIdfPairs = convertedLabels.zip(tfidf);
	    return labelAndTfIdfPairs.map(t -> new LabeledPoint(t._1(), t._2()));
	}
	JavaPairRDD<Double, Vector> zip = convertedLabels.zip(tfVectors);
	return zip.map(t -> new LabeledPoint(t._1(), t._2()));
    }

    /**
     * 
     * @param sc
     * @param input
     * @param output
     * @param joinData
     * @throws IOException
     */
    private static void loadTFDataForText(JavaSparkContext sc, String input, String output, boolean joinData) throws IOException{
	System.setProperty("hadoop.home.dir", "D:\\winutils\\");
	JavaRDD<Record> data = sc.textFile(input)
		.map(new parseCSVAsRecord())
		.filter(x -> x != null);

	JavaRDD<LabeledPoint> ret;

	if (joinData){
	    ret = groupAndProcess(data, false);
	}
	else{
	    ret = processTFData(data, false);
	}
	MLUtils.saveAsLibSVMFile(ret.rdd(), output);
    }

    /**
     * 
     * @param sc
     * @param input
     * @param output
     * @param joinData
     * @param useIDF
     * @throws IOException
     */
    private static void loadTFIDFDataForText(JavaSparkContext sc, String input, String output, boolean joinData, boolean useIDF) throws IOException{
	System.setProperty("hadoop.home.dir", "D:\\winutils\\");
	JavaRDD<Record> data = sc.textFile(input)
		.map(new parseCSVAsRecord())
		.filter(x -> x != null);

	JavaRDD<LabeledPoint> ret;

	if (joinData){
	    ret = groupAndProcess(data, useIDF);
	}
	else{
	    ret = processTFData(data, useIDF);
	}
	MLUtils.saveAsLibSVMFile(ret.rdd(), output);
    }


    public static void main(String[] args) throws IOException{

	// Setup Config and SparkContext
	/**
	 * TODO: This line is a workaround for a known bug.
	 * 	 Details at https://issues.apache.org/jira/browse/SPARK-2356 
	 * 	 Essentially, Spark is relying on some Hadoop libs, but those 
	 *       require that HADOOP_HOME is set. The winutils file can be found
	 *       at http://public-repo-1.hortonworks.com/hdp-win-alpha/winutils.exe
	 */
	System.setProperty("hadoop.home.dir", "D:\\winutils\\");
	SparkConfig conf = new SparkConfig();
	JavaSparkContext sc = conf.javaSparkContext();
	String inputDirectory = args[0];
	String outputDirectory = args[1];
	String operation = args[2];

	if (operation.equals("clean")) {
	    JavaRDD<String> data = sc.textFile(inputDirectory)
		    .map(new parseCSV())
		    .filter(x -> x != null);
	    data.saveAsTextFile(outputDirectory);
	}
	else if (operation.equals("term-freq")){
	    loadTFIDFDataForText(sc, inputDirectory, outputDirectory, true, false);
	}
	else if (operation.equals("tfidf")){
	    loadTFIDFDataForText(sc, inputDirectory, outputDirectory, true, true);
	}
	sc.stop();
    }
}

package org.hunter.medicare.data;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.util.MLUtils;
import org.apache.spark.rdd.RDD;

import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;

import scala.Tuple2;

public class NBFromSVM {

    private final JavaSparkContext sc;
    private static final int NUM_FEATURES = 28;
    private static int count = 0;

    // Hardcoding the data and model paths for now.
    private static double[] TRAININGWEIGHTS = {0.9, 0.1};

    private static final int npi = 0;
    private static final int providerType = 1;
    private static final int hcpcsCode = 2;
    private static final Boolean TRUE_TEST = true;

    private static String DATAPATH;
    public static String MODELPATH;
    private static String OBJECTMODELPATH = "C:\\Users\\Brian\\Desktop\\NBObjectModel";
    private static String TFIDFPATH = "TFIDFModel";
    private static int MAX_TFHASH_SIZE;
    private static int MINDF;

    public NBFromSVM(JavaSparkContext sc){
	this.sc = sc;
    }    

    /*
     * 	trainNaiveBayesModel String -> NaiveBayesModel
     * 	Draws from data stored in HDFS to train a NaiveBayesModel
     * 	saves the model to the specified outputModelPath, unless
     * 	a Model is already saved there
     */
    public static NaiveBayesModel trainNaiveBayesModel(String outputModelPath, String inputDataPath, SparkContext sc){
	Path path = FileSystems.getDefault().getPath(outputModelPath);
	RDD<LabeledPoint> training = MLUtils.loadLibSVMFile(sc, inputDataPath);
	JavaRDD<LabeledPoint>[] splitData = training.toJavaRDD().randomSplit(TRAININGWEIGHTS);
	training = splitData[0].rdd();
	final NaiveBayesModel model = NaiveBayes.train(training, 1.0);
	RDD<LabeledPoint> test = splitData[1].rdd().cache();
	long numLeft = 0;
	double numRows = 0;

	class accTest implements Function<LabeledPoint, Boolean>{
	    public Boolean call(LabeledPoint lp){
		return lp.label() == model.predict(lp.features());
	    }
	}
	if (TRUE_TEST){
	    numLeft = test.toJavaRDD().filter(new accTest()).count();
	    numRows = test.count();
	}else{
	    numLeft = training.toJavaRDD().filter(new accTest()).count();
	    numRows = training.count();
	}
	System.out.println("Done!");
	double accuracy = numLeft/numRows;
	System.out.println("Naive Bayes Model trained on " + numRows + " rows of medicare data.");
	System.out.println("Accuracy: " + accuracy);
	return model;
    }

    public static void main(String[] args) throws IOException, InterruptedException{
	System.setProperty("hadoop.home.dir", "D:\\winutils\\");
	//PropertyConfigurator.configure("log4j.properties");
	DATAPATH = args[0];
	MODELPATH = args[1];
	SparkConfig conf = new SparkConfig();
	JavaSparkContext sc = conf.javaSparkContext();
	NaiveBayesModel model = trainNaiveBayesModel(MODELPATH, DATAPATH, sc.sc());
	model.save(sc.sc(), MODELPATH);
	try(FileOutputStream fos = new FileOutputStream(OBJECTMODELPATH);){
	    try(ObjectOutputStream oos = new ObjectOutputStream(fos);){
		oos.writeObject(model);
		fos.close();
		oos.close();
	    }
	}
	sc.stop();
    }
}

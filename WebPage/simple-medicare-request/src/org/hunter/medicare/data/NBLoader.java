package org.hunter.medicare.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.ClassUtils;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.feature.IDFModel;
import org.apache.spark.mllib.linalg.SparseVector;
import org.apache.spark.mllib.linalg.Vector;

/**
 * 
 * @author Brian
 * 
 * NBLoader: Loads Model files for a NaiveBayesModel, this includes a NaiveBayesModel
 *           As well as any Maps for categorical variables
 */
public class NBLoader{

    private static final String MODELPATH = "WebContent/WEB-INF/classes/resources/tf-nb-model";
    private static final String ABSPATH = "C:\\Users\\Brian\\Desktop\\Medicareouts\\textTfidfModel";
    private static final String PATH = "file:\\C:\\User\\Brian\\Documents\\GitHub\\CS8674.FALL2015.NEUSeattle\\WebPage\\simple-medicare-request\\resources\\tf-nb-model";
    private static final String MAPPATH = "WebContent/WEB-INF/classes/resources/backMap";
    private static final String MODEL = "NBObjectModel";
    private static final String MAP = "backMap";
    private static final String TFIDF = "TFIDF";
    public JavaSparkContext sc;

    public NBLoader(JavaSparkContext sc)
    {
	this.sc = sc;
    }
    
    /**
     * 
     * @param resourceClass
     * @param resourceName
     * @return
     * @throws URISyntaxException
     */
    public static Path getResourcePath(Class<?> resourceClass, String resourceName) throws URISyntaxException {
	    URL url = resourceClass.getResource(resourceName);
	    return Paths.get(url.toURI());
	}  

    /**
     * 
     * @return 
     * @throws IOException
     * @throws ClassNotFoundException
     */
//    public NaiveBayesModel getSavedNBModel() throws IOException, ClassNotFoundException{
//	System.setProperty("hadoop.home.dir", "D:\\winutils\\");
//	NaiveBayesModel model = null;
//	try{
//	    model = NaiveBayesModel.load(sc.sc(), getResourcePath(NBLoader.class, MODEL).toString());
//	}catch(Exception e){
//	    e.printStackTrace();
//	    sc.close();
//	}
//	return model;
//    }
//    
    /**
     * TODO: Figure out how to work around serialization version issues
     * 		to load model directly
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws URISyntaxException
     */
    public static NaiveBayesModel getSavedObjectNBModel() throws FileNotFoundException, IOException, ClassNotFoundException, URISyntaxException{
	//try(FileInputStream fs = new FileInputStream(new ClassPathResource("resources/NBObjectModel").getPath());){
	//try(FileInputStream fs = new FileInputStream("C:\\Users\\Brian\\Desktop\\NBObjectModel");){
	try(FileInputStream fs = new FileInputStream(getResourcePath(NBLoader.class, MODEL).toString());){
	    try(ObjectInputStream ois = new ObjectInputStream(fs);){
		NaiveBayesModel model = 
			(org.apache.spark.mllib.classification.NaiveBayesModel) ois.readObject();
		fs.close();
		ois.close();
		return model;
	    }
	}
    }

    /**
     * 
     * @return backMap : Maps Numeric labels to Provider Types
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws URISyntaxException
     */
    static HashMap<Integer, String> loadCategoryMap() throws IOException, ClassNotFoundException, URISyntaxException{
	HashMap<Integer, String> h = null;
	//try(FileInputStream fs = new FileInputStream(new ClassPathResource("resources/backMap").getPath());){
	//try(FileInputStream fs = new FileInputStream("C:\\Users\\Brian\\Desktop\\Medicareouts\\potential\\backMap"))
	try(FileInputStream fs = new FileInputStream(getResourcePath(NBLoader.class, MAP).toString())){
	
	    try(ObjectInputStream oos = new ObjectInputStream(fs))
	    {
		h = (HashMap<Integer,String>) oos.readObject();
		oos.close();
		fs.close();
	    }
	}
	return h;
    } 
    
    /**
     * 
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws URISyntaxException
     */
    public static IDFModel loadTFIDFModelObject() throws IOException, ClassNotFoundException, URISyntaxException {
	IDFModel idf = null;
	//try(FileInputStream fs = new FileInputStream(getClasspathResource("resources/TFIDF"));){
	//try(FileInputStream fs = new FileInputStream("C:\\Users\\Brian\\Desktop\\Medicareouts\\potential\\TFIDF"))
	try(FileInputStream fs = new FileInputStream(getResourcePath(NBLoader.class, TFIDF).toString()))
	{
	    try(ObjectInputStream oos = new ObjectInputStream(fs))
	    {
		idf = (IDFModel) oos.readObject();
		oos.close();
		fs.close();
	    }
	}
	return idf;
    } 

    /**
     * FOR TEST
     * @param args
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws ClassNotFoundException, IOException, URISyntaxException{
	getSavedObjectNBModel();
    }
}

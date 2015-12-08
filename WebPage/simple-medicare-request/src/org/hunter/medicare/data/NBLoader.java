package org.hunter.medicare.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.ClassUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.linalg.SparseVector;
public class NBLoader {

    private static final String MODELPATH = "WebContent/WEB-INF/classes/resources/tf-nb-model";
    private static final String ABSPATH = "C:\\Users\\Brian\\Desktop\\Medicareouts\\textTfidfModel";
    private static final String PATH = "file:\\C:\\User\\Brian\\Documents\\GitHub\\CS8674.FALL2015.NEUSeattle\\WebPage\\simple-medicare-request\\resources\\tf-nb-model";
    private static final String MAPPATH = "WebContent/WEB-INF/classes/resources/backMap";
    public JavaSparkContext sc;

    public NBLoader(JavaSparkContext sc)
    {
	this.sc = sc;
    }
    
    public static Path getResourcePath(Class<?> resourceClass, String resourceName) throws URISyntaxException {
	    URL url = resourceClass.getResource(resourceName);
	    return Paths.get(url.toURI());
	}  

    public NaiveBayesModel getSavedNBModel() throws IOException, ClassNotFoundException{
	NaiveBayesModel model = null;
	try{
	    System.out.println(getResourcePath(NBLoader.class, "tf-nb-model"));
	    //System.out.println(new ClassPathResource("resources/tf-nb-model").getPath());
	    //System.out.println(NBLoader.class.getClass().getClassLoader().getResource("tf-mb-model"));
	    //sSystem.out.println(NBLoader.class.getClass().getResource("/tf-mb-model"));
	    model = NaiveBayesModel.load(sc.sc(), getResourcePath(NBLoader.class, "tf-nb-model").toString());
	}catch(Exception e){
	    e.printStackTrace();
	    sc.close();
	}
	return model;
    }

    static HashMap<Integer, String> loadCategoryMap() throws IOException, ClassNotFoundException, URISyntaxException{
	HashMap<Integer, String> h = null;
	try(FileInputStream fs = new FileInputStream(getResourcePath(NBLoader.class, "backMap").toString()))
	{
	    try(ObjectInputStream oos = new ObjectInputStream(fs))
	    {
		h = (HashMap<Integer,String>) oos.readObject();
		oos.close();
		fs.close();
	    }
	}
	return h;
    } 

    public static void main(String[] args) throws ClassNotFoundException, IOException, URISyntaxException{
	System.setProperty("hadoop.home.dir", "D:\\winutils\\");
	SparkConf conf = new SparkConf().setAppName("NB Load").setMaster("local");
	JavaSparkContext sc = new JavaSparkContext(conf);
	NaiveBayesModel m = null;
	NBLoader nbl = new NBLoader(sc);
	try {
	    m = nbl.getSavedNBModel();
	} catch (Exception e){
	    e.printStackTrace();
	    System.out.println("NO GOOD");
	    System.exit(1);
	}
	HashMap<Integer, String> h = loadCategoryMap();
	int[] ind = {123, 124, 1234};
	double[] val = {1.0,1.0,1.0};
	SparseVector sv = new SparseVector(10000,ind,val);
	System.out.println(h.toString());
	System.out.println((int)m.predict(sv));
	System.out.println(h.get((int)m.predict(sv)));
    }
}

package org.hunter.medicare.data;


import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
//import org.springframework.context.annotation.Profile;

public class SparkConfig {

    private static final Logger LOGGER = Logger.getLogger(SparkConfig.class);

    private String master;

    public JavaSparkContext javaSparkContext(){
	
	//master = "spark://ec2-52-34-97-105.us-west-2.compute.amazonaws.com:7077";
	master = "local";

	LOGGER.info("Creating SparkContext. Master=" + master);;
	SparkConf conf = new SparkConf().setAppName("NaiveBayesProviderType")
		.setMaster(master);

	return new JavaSparkContext(conf);
    }
}

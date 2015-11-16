package org.hunter.medicare.data;

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;

@Configuration
public class SparkConfig {

    private static final Logger LOGGER = Logger.getLogger(SparkConfig.class);

    private String master;

    public JavaSparkContext javaSparkContext(){
	
	master = "local";

	LOGGER.info("Creating SparkContext. Master=" + master);;
	SparkConf conf = new SparkConf().setAppName("NaiveBayesProviderType")
		.setMaster(master);

	return new JavaSparkContext(conf);
    }
}

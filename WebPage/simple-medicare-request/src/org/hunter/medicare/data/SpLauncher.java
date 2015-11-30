package org.hunter.medicare.data;

import java.io.IOException;

import org.apache.spark.launcher.SparkLauncher;

public class SpLauncher {

    public static void main(String[] args) throws IOException, InterruptedException {
        Process spark = new SparkLauncher().setMainClass("org.hunter.medicare.data.Main")
                .setMaster("spark://52.34.97.105:7077").setConf(SparkLauncher.DRIVER_MEMORY, "2g")
                .launch();
        spark.waitFor();
    }
}

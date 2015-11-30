package org.hunter.medicare.gaussian;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.StatUtils;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Utility class for traning and saving data.
 * 
 * Model is represented as Map, and can be loaded from previously saved file.
 * See {@link #load()}
 * 
 * @author Zheyu
 *
 */
public class GMM {
    static final int idxProcCode = 16;
    static final int idxCost = 24;

    String inputFilePath;
    static final String savePath = "2g.gmm.model.zip";

    /**
     * Model loaded from disk will be held by this field.
     */
    static Map<String, NormalDistribution> model;

    public GMM(String path) {
        this.inputFilePath = path;
    }

    private HashMap<String, List<Double>> populateProcCodeMap() throws Exception {
        final HashMap<String, List<Double>> map = new HashMap<>();
        boolean firstLine = true;
        try (CSVReader read = new CSVReader(new FileReader(inputFilePath), '\t')) {
            String[] ss = null;

            while ((ss = read.readNext()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String proc = ss[idxProcCode];
                double cost = Double.parseDouble(ss[idxCost]);
                // System.err.println(proc + "\t " + cost);

                List<Double> lst = map.get(proc);
                if (lst == null) {
                    lst = new ArrayList<Double>();
                    map.put(proc, lst);
                }
                lst.add(cost);

            }
        }

        return map;
    }

    /**
     * Tranning is done offline. Do not call this from web-apps.
     * 
     * @return
     * @throws Exception
     */
    GMM train() throws Exception {
        HashMap<String, List<Double>> map = populateProcCodeMap();

        HashMap<String, NormalDistribution> tmp = new HashMap<>();

        for (Entry<String, List<Double>> ent : map.entrySet()) {
            try {
                double[] vals = ArrayUtils.toPrimitive(ent.getValue().toArray(new Double[0]));
                double mean = StatUtils.mean(vals);
                double stdev = Math.sqrt(StatUtils.variance(vals));

                tmp.put(ent.getKey(), new NormalDistribution(mean, stdev));
            } catch (Exception e) {
                System.err.println(ent.getKey());
                e.printStackTrace();
            }
        }

        this.model = tmp;
        return this;
    }

    public void save() throws Exception {
        ZipIO.saveZippedModelMap(savePath, model);
    }

    /**
     * Load mode data from disk file
     * 
     * @throws Exception
     */
    public void load() throws Exception {
        if (model == null) {
            model = ZipIO.loadZippedModelMap(savePath);
        }
    }

    /**
     * @param procName
     * @return Price's NormalDistribution of given proc name.
     */
    public NormalDistribution getIndividualModel(String procName) {
        return model.get(procName);
    }

    /**
     * for test only.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // train and save
        // new GMM(args[0]).train().save();
        //
        // // load
        GMM gmm = new GMM(args[0]);
        gmm.load();
        System.err.println(gmm.model.size());
    }

}

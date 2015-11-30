package org.hunter.medicare.gaussian;

import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.core.io.ClassPathResource;

/**
 * Utility class to read GMM model data from zip file.
 * 
 * @author Zheyu
 *
 */
public class ZipIO {
    /**
     * for test only
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String file = args[0];
        for (java.util.Map.Entry<String, NormalDistribution> e : loadZippedModelMap(file)
                .entrySet()) {
            System.out.println(e.getKey() + "\t" + e.getValue());
        }
    }

    /**
     * Load GMM model file from zipped disk file.
     * 
     * @param file
     * @return
     * @throws Exception
     */
    public static Map<String, NormalDistribution> loadZippedModelMap(String file) throws Exception {

        // new ClassPathResource("resources/2g.gmm.model.zip").getInputStream()
        try (ZipInputStream zip = new ZipInputStream(
                new ClassPathResource("resources/2g.gmm.model.zip").getInputStream());) {
            zip.getNextEntry();
            try (ObjectInputStream data = new ObjectInputStream(zip)) {
                Map<String, NormalDistribution> map = (Map<String, NormalDistribution>) data
                        .readObject();
                return map;
            }
        }
    }

    /**
     * Save model to disk, to the given path.
     * 
     * @param path
     * @param model
     * @throws Exception
     */
    public static void saveZippedModelMap(String path, Map model) throws Exception {
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(path));) {
            ZipEntry zentry = new ZipEntry("2g.gmm.model");
            zentry.setComment("model hashmap object");
            zip.putNextEntry(zentry);

            try (ObjectOutputStream data = new ObjectOutputStream(zip)) {
                data.writeObject(model);
            }
        }
    }

}

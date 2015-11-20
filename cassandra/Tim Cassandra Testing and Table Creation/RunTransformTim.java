package fortesting;

import java.io.IOException;

public class RunTransformTim {

    /**
     * Loads data into appropriate tables (assumes scheme already created)
     * 
     * This will import the tables from my CSVs, which I have on dropbox. Let me
     * (Tim) know if you need the CSVs No guarantee that it works on CSVs
     * generated differently
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // String host = "127.0.0.1";
        String host = "52.32.209.104";
        // String keyspace = "new";
        String keyspace = "main";
        String year = "2012";
        // if an error occurs during upload of first CSV, use
        // GetLastRow.getLastEntry() to find its
        // npi value and use that as start. This will not work in other CSVs
        // unless the last npi
        // added is greater than the largest npi in all previously loaded CSVs
        String start = "";

        TransformTim t = new TransformTim();
        t.injest(host, keyspace, "CSV/MedicareA2012.csv", year, start);
        t.injest(host, keyspace, "CSV/MedicareB2012.csv", year, "");
        t.injest(host, keyspace, "CSV/MedicareC2012.csv", year, "");
        t.injest(host, keyspace, "CSV/MedicareD2012.csv", year, "");
        t.injest(host, keyspace, "CSV/MedicareEG2012.csv", year, "");
        t.injest(host, keyspace, "CSV/MedicareHJ2012.csv", year, "");
        t.injest(host, keyspace, "CSV/MedicareKL2012.csv", year, "");
        t.injest(host, keyspace, "CSV/MedicareMN2012.csv", year, "");
        t.injest(host, keyspace, "CSV/MedicareOQ2012.csv", year, "");
        t.injest(host, keyspace, "CSV/MedicareR2012.csv", year, "");
        t.injest(host, keyspace, "CSV/MedicareS2012.csv", year, "");
        t.injest(host, keyspace, "CSV/MedicareTX2012.csv", year, "");
        t.injest(host, keyspace, "CSV/MedicareYZ2012.csv", year, "");

    }

}

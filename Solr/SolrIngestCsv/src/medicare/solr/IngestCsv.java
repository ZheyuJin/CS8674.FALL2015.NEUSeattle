package medicare.solr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import com.opencsv.CSVReader;

public class IngestCsv {

    // id excel =CONCATENATE($C2,$I2,$R2,$S2,$B2)
    // Also ensure no commas in numerics (format cells in excel to numeric
    // format no commas)

    // ToDo: get this from config instead?
    public static String solrUrlBase = "http://localhost:8983/solr/";
    // public static String solrUrlBase = "http://54.200.138.99:8983/solr/";
    // private static String solrUrlBase = "http://52.32.209.104:8983/solr/"; //
    // Tim's

    public static String collectionName = "csvtest";
    public static String solrIngestBase = solrUrlBase + collectionName;

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.err
                    .println("Error, input parameters = file.csv [http://yourhost:8983/solr/yourcollection]");
        }

        if (args.length > 1) {
            solrIngestBase = args[1];
        }

        System.out.println("Pushing csv file " + args[0] + " to solr collection at "
                + solrIngestBase);

        SolrClient solr = new HttpSolrClient(solrIngestBase);

        Collection<SolrInputDocument> docsInChunk = new ArrayList<SolrInputDocument>();

        BufferedReader br = null;
        CSVReader reader = null;

        File f = new File(args[0]);
        try {
            Integer chunkSize = 10000;
            Long numDocs = 0L;
            Long docsSkipped = 0L;
            boolean header = true;
            String[] headerFields = null;

            reader = new CSVReader(new FileReader(f.getAbsolutePath()));
            headerFields = reader.readNext();

            if (headerFields == null || headerFields.length <= 0) {
                throw new Exception("Could not read header line from csv");
            } else {
                System.out.println("Ingesting " + args[0]);
            }

            // Workaround for
            Integer drugIndicatorFieldNumber = -1;
            Integer startOfAmounts = -1; // We assume these are on the end
            for (int i = 0; i < headerFields.length; i++) {
                if (headerFields[i].equalsIgnoreCase("HCPCS_DRUG_INDICATOR")) {
                    drugIndicatorFieldNumber = i;
                }
                if (headerFields[i].toUpperCase().endsWith("_AMT")) {
                    startOfAmounts = i;
                }
            }

            String[] fields = null;
            while ((fields = reader.readNext()) != null) {

                if ((headerFields.length != fields.length) || fields[0].isEmpty()) {
                    System.err.println(fields[0]);
                    // throw new Exception(
                    // "Could not parse input line for given # headers. Line starts with "
                    // + fields[0]);
                    docsSkipped++;
                    continue;
                }
                if (headerFields.length == fields.length) {

                    SolrInputDocument doc = new SolrInputDocument();
                    for (int i = 0; i < headerFields.length; i++) {

                        // This seems to have leading and trailing spaces
                        // sometimes.
                        // Consider - do we EVER want leading/trailing
                        // spaces?
                        if (drugIndicatorFieldNumber == i) {
                            fields[i] = fields[i].trim();
                        }
                        if ((startOfAmounts >= 0) && (i >= startOfAmounts)) {
                            if (fields[i].charAt(0) == '$') {
                                fields[i] = fields[i].substring(1);
                            }
                        }
                        doc.addField(headerFields[i], fields[i]);
                    }

                    docsInChunk.add(doc);
                    numDocs++;

                    if (docsInChunk.size() % chunkSize == 0) {
                        // server.commit();
                        UpdateResponse response = solr.add(docsInChunk);
                        // System.out.println("Update status = " +
                        // response.getStatus() + " for "
                        // + docsInChunk.size() + " docs");
                        if (response.getStatus() != 0) {
                            throw new Exception("Could not add docs - response status = "
                                    + response.getStatus());
                        }
                        if (numDocs % 10000 == 0) {
                            System.out.println("..# docs processed = " + numDocs);

                        }
                        docsInChunk.clear();
                    }
                }
            }

            if (docsInChunk.size() > 0) {
                // One last batch to commit
                UpdateResponse response = solr.add(docsInChunk);
                // System.out.println("Update status = " + response.getStatus()
                // + " for "
                // + docsInChunk.size() + " docs");

                if (response.getStatus() != 0) {
                    throw new Exception("Could not add docs - response status = "
                            + response.getStatus());
                }
                docsInChunk.clear();
            }

            System.out.println("Done with ingestion of " + args[0] + ", # docs ingested = "
                    + numDocs + ", # docs skipped = " + docsSkipped);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (br != null) {
                br.close();
            }

            solr.commit();
            solr.close();
        }
    }
}

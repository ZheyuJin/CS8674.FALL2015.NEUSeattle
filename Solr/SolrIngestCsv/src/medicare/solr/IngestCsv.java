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

    // ToDo: get this from config instead?
    public static String solrUrlBase = "http://localhost:8983/solr/";
    // public static String solrUrlBase = "http://54.200.138.99:8983/solr/";
    // private static String solrUrlBase = "http://52.32.209.104:8983/solr/"; //
    // Tim's

    public static String collectionName = "csvtest";
    public static String solrIngestBase = solrUrlBase + collectionName;

    public static void main(String[] args) throws Exception {

        SolrClient solr = new HttpSolrClient(solrIngestBase);

        Collection<SolrInputDocument> docsInChunk = new ArrayList<SolrInputDocument>();

        BufferedReader br = null;
        CSVReader reader = null;

        File f = new File(args[0]);
        try {
            boolean useOpenCsv = true;

            Integer chunkSize = 5000;
            Long numDocs = 0L;
            Long docsSkipped = 0L;
            boolean header = true;
            String[] headerFields = null;

            if (useOpenCsv) {

                reader = new CSVReader(new FileReader(f.getAbsolutePath()));
                headerFields = reader.readNext();

                if (headerFields == null || headerFields.length <= 0) {
                    throw new Exception("Could not read header line from csv");
                } else {
                    System.out.println("Ingesting " + args[0]);
                }

                String[] fields = null;
                while ((fields = reader.readNext()) != null) {

                    if (headerFields.length != fields.length) {
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

            } else {

                br = new BufferedReader(new FileReader(f.getAbsolutePath()));
                String line = null;

                while ((line = br.readLine()) != null) {

                    String[] origFields = line.split(",");
                    String[] fields = null;
                    if ((origFields != null) && (origFields.length > 1)) {

                        fields = origFields;

                        if (header) {
                            // Initialize header array
                            headerFields = fields.clone();
                            header = false;
                        } else {

                            if (headerFields.length < fields.length) {
                                // We probably had a field with an embedded
                                // comma -
                                // special work required.

                                // We make a couple big assumptions here
                                // - if a field has a comma embedded, it is
                                // double
                                // quote delimited
                                // - ...and that field contains no other double
                                // quotes

                                if (fields[3].equalsIgnoreCase("Ayuso")
                                        || fields[3].equals("ALLEMAN")) {
                                    System.out.println("special");
                                }

                                ArrayList<String> newFields = new ArrayList<String>();

                                int firstQuote = line.indexOf(",\""); // ie: ,"
                                int endQuote = -1;
                                int start = 0;

                                while (firstQuote != -1) {
                                    endQuote = line.indexOf("\",", firstQuote + 1);
                                    if (endQuote == -1) {
                                        // This doesn't look right - we want
                                        // matching ".."
                                        break;
                                    }

                                    if (start != firstQuote) {
                                        String line1 = line.substring(start, firstQuote) + ",";

                                        String[] line1Fields = line1.split(",");
                                        for (String s : line1Fields) {
                                            newFields.add(s);
                                        }

                                        // Get around an odd split behavior
                                        // where ,,
                                        // does not turn into a trailing field
                                        if (line1.endsWith(",,")) {
                                            newFields.add("");
                                        }
                                    }

                                    String fieldWithCommas = line.substring(firstQuote + 2,
                                            endQuote);
                                    start = endQuote + 1; // Start next bit
                                                          // after
                                                          // the quote

                                    newFields.add(fieldWithCommas);

                                    // Set up for next field
                                    firstQuote = line.indexOf(",\"", start);
                                    if (firstQuote != -1) {
                                        String restLine = line.substring(start, firstQuote);
                                        // System.out.println("> 1 embedded comma");
                                    }
                                }

                                if (start < line.length()) {
                                    // done with quote/comma fields, now tack on
                                    // the
                                    // rest
                                    String lineRest = line.substring(endQuote + 2);
                                    String[] lineRestFields = lineRest.split(",");
                                    for (String s : lineRestFields) {
                                        newFields.add(s);
                                    }
                                }

                                // After all that, we should have 30 fields, no
                                // more
                                // and no less
                                if (newFields.size() != headerFields.length) {
                                    System.out.println(line);
                                    // throw new Exception(
                                    // "Could not parse input line for given # headers. Line starts with "
                                    // + fields[0]);
                                    docsSkipped++;
                                    continue;
                                }

                                // Stitch together our new set of fields.
                                String[] modFields = new String[headerFields.length];
                                modFields = newFields.toArray(modFields);
                                fields = modFields;
                            }

                            if (headerFields.length == fields.length) {

                                SolrInputDocument doc = new SolrInputDocument();
                                for (int i = 0; i < headerFields.length; i++) {

                                    doc.addField(headerFields[i], fields[i]);
                                }

                                docsInChunk.add(doc);
                                numDocs++;

                                if (docsInChunk.size() % chunkSize == 0) {
                                    // server.commit();
                                    UpdateResponse response = solr.add(docsInChunk);
                                    // System.out.println("Update status = " +
                                    // response.getStatus()
                                    // + " for " + docsInChunk.size() +
                                    // " docs");
                                    if (response.getStatus() != 0) {
                                        throw new Exception(
                                                "Could not add docs - response status = "
                                                        + response.getStatus());
                                    }
                                    docsInChunk.clear();
                                }
                            }
                        }
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

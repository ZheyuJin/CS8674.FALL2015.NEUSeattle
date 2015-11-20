package org.hunter.medicare.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * each CSV is arranged in order of the npi code, but the CSVs are broken up by
 * lastname this returns the last npi in a table. If an error occurs when
 * loading the A's this will determine where you need to resume. If an error
 * occurs after loading the A's this may or may not work
 * 
 * This also prints the last npi and the row count. Once table gets sufficiently
 * big the row count may time out. But the row count will tell you how many
 * entries there are and can be use to find where to start uploading again
 * 
 * @author tim
 *
 */
public class GetLastRow {
    // HOSTS

    // private static String HOST = "127.0.0.1"; // If mock=false and run local
    private static String HOST = "52.32.209.104"; // EC2 Tim
    // private static String HOST = "54.200.138.99"; // mock=false and EC2 brian
    // private static String HOST = "54.191.107.167"; // ec2 josh

    // KEYSPACES
    private static String KEYSPACE = "main";
    // private static String KEYSPACE = "blah";
    // private static String KEYSPACE = "old";
    // private static String KEYSPACE = "new";
    // private static String KEYSPACE = "demo";

    private static String USERNAME = "cassandra";
    private static String PASSWORD = "cassandra";

    public static void main(String[] args) {
        getLastEntry();

    }

    public static String getLastEntry() {
        Cluster cluster = null;
        Session session = null;
        String lastNPI = "";
        try {
            cluster = Cluster.builder().addContactPoint(HOST).withCredentials(USERNAME, PASSWORD)
                    .build();
            session = cluster.connect(KEYSPACE);
            String query = "SELECT * FROM providers;";
            ResultSet result = session.execute(query);
            List<String> idList = new ArrayList<String>();
            for (Row row : result) {
                idList.add(row.getString("npi"));
            }
            Collections.sort(idList);
            lastNPI = idList.get(idList.size() - 1);
            System.out.println(lastNPI);

            System.out.println(ColumnCounter.count(HOST, KEYSPACE, "proceduresstats"));
        } catch (Exception e) {
            System.out.println("An error occured " + e);
        } finally {
            if (session != null) {
                session.close();
            }
            if (cluster != null) {
                cluster.close();
            }
            System.out.println("session closed");
        }
        return lastNPI;
    }

}

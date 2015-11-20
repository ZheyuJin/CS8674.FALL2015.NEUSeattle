package org.hunter.medicare.test;

import java.util.ArrayList;
import java.util.List;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * Did one of our team mates do something weird to a table? Did they upload that
 * table they promissed? Does it contain all the entries?
 * 
 * This function tells the names of the tables in the given keyspace and how
 * many rows they are. Note that for large tables count times out and returns 0
 * 
 * Running this is also a quick way of determining if Cassandra is down
 * 
 * @author tim
 *
 */
public class DescribeKeyspace {
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

    /**
     * MAIN
     * 
     * @param args
     */
    public static void main(String[] args) {
        describe(HOST, KEYSPACE);
    }

    public static void describe(String host, String keyspace) {
        Cluster cluster = null;
        Session session = null;
        try {
            cluster = Cluster.builder().addContactPoint(host).withCredentials(USERNAME, PASSWORD)
                    .build();

            session = cluster.connect(keyspace);

            List<String> tableList = new ArrayList<String>();

            String query = "SELECT columnfamily_name FROM system.schema_columnfamilies WHERE keyspace_name = '"
                    + keyspace + "';";
            ResultSet result = session.execute(query);
            for (Row row : result) {
                String tableName = row.getString("columnfamily_name");
                tableList.add(tableName);
            }
            for (String t : tableList) {
                System.out.println(t + " : " + ColumnCounter.count(host, keyspace, t));
            }

            // for (String t : tableList) {
            // System.out.println(t);
            // }
            // String q = "SELECT nppes_provider_last_org_name FROM providers
            // LIMIT 100;";
            // ResultSet result2 = session.execute(q);
            // for (Row row : result2) {
            // System.out.println(row.getString("nppes_provider_last_org_name"));
            // }

        } catch (Exception e) {
            System.out.println("!!!ERROR " + e);
        } finally {
            if (session != null) {
                session.close();
            }
            if (cluster != null) {
                cluster.close();
            }
            System.out.println("session closed");
        }
    }

}

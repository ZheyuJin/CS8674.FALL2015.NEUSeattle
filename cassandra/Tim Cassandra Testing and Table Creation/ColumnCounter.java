package org.hunter.medicare.test;

import java.util.HashSet;
import java.util.Set;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * has two functions count tells you the row count of a table
 * 
 * groupByCount is similar to SQL count(*) on a table with a GROUP BY In other
 * words it returns the number of unique values in a column of that table
 * 
 * works for tables of 100,000 or less rows (e.g. proceduresinfo) but not for 9
 * million (e.g. the size of the fully proceduresstats table);
 * 
 * @author tim
 *
 */
public class ColumnCounter {
    private static String HOST = "127.0.0.1"; // If mock=false and run local
    // private static String HOST = "54.200.138.99"; // mock=false and EC2 brian
    // private static String HOST = "54.191.107.167"; // ec2 josh
    private static String KEYSPACE = "new";
    private static String MVTABLE = "mv";
    private static String USERNAME = "cassandra";
    private static String PASSWORD = "cassandra";

    public static long count(String host, String keyspace, String table) {
        Cluster cluster = null;
        Session session = null;
        long count = 0;
        try {
            cluster = Cluster.builder().addContactPoint(host).withCredentials(USERNAME, PASSWORD)
                    .build();

            session = cluster.connect(keyspace);
            String query = "SELECT count(*) FROM " + table + ";";
            ResultSet result = session.execute(query);
            Row row = result.one();
            count = row.getLong(0);
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
        return count;
    }

    public static int groupByCount(String host, String keyspace, String table, String column) {
        Cluster cluster = null;
        Session session = null;
        Set<String> set = new HashSet<String>();

        try {
            cluster = Cluster.builder().addContactPoint(host).withCredentials(USERNAME, PASSWORD)
                    .build();

            session = cluster.connect(keyspace);
            String query = "SELECT " + column + " FROM " + table + ";";
            ResultSet result = session.execute(query);

            for (Row row : result) {
                String value = row.getString(column);
                set.add(value);
            }
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
        return set.size();
    }

}
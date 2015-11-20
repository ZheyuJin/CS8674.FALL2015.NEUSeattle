package fortesting;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class CreateScheme {

    /**
     * creates a keyspace of the indicated kespaceName still need to create
     * tables before loading data
     * 
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        createKeyspace("52.32.209.104", "main");

    }

    public static void createKeyspace(String host, String keyspaceName) {
        Cluster cluster = null;
        Session session = null;
        String query = "CREATE KEYSPACE " + keyspaceName
                + " WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3};";

        try {
            cluster = Cluster.builder().addContactPoint(host)
                    // .withCredentials(USERNAME, PASSWORD)
                    .build();
            session = cluster.connect();
            session.execute(query);

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
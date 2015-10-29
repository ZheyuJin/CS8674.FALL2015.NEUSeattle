package org.hunter.medicare.data;
import java.util.ArrayList;
import java.util.List;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * given a $state and $hcpcs_code, returns a list of the top $limit
 * providers (ids) sorted by $order
 */
public class AccessorMostExpensiveStateCodeMV {

	public static final String STRINGIFIER = "x";
	public static final String LEAST = "least";
	public static final String MOST = "most";

	// cql constants
	public static final String SPACE = " ";
	public static final String COMMA = ",";
	public static final String LEFT = "(";
	public static final String RIGHT = ")";
	public static final String OPEN_STRING = "'";
	public static final String CLOSE_STRING = "'";
	public static final String TEXT = "text";
	public static final String PRIMARY_KEY = "primary key";
	public static final String INSERT_INTO = "insert into";
	public static final String VALUES = "values";
	public static final String SELECT = "select";
	public static final String WILDCARD = "*";
	public static final String FROM = "from";
	public static final String WHERE = "where";
	public static final String STATE = "nppes_provider_state";
	public static final String EQUALS = "=";
	public static final String CREATE_TABLE = "create table";
	public static final String LIMIT = "limit";

	// for testing
//	public static void main(String[] args) {
//		ArrayList<String> x = getProviders("CA", "99223", LEAST, 10);
//		System.out.println(x);
//	}

	public static ArrayList<String> getProviders(String state, String code, String order, int limit) {

		String host = "127.0.0.1";
		String keyspace = "demo";
		String mvTable = "mv";

		// incremented until $limit or EOF
		int numberOfInstances = 0;

		Cluster cluster;
		Session session;
		cluster = Cluster.builder().addContactPoint(host).build();
		session = cluster.connect(keyspace);

		String selectCostsByStateQuery = 
				SELECT + SPACE +
				WILDCARD + SPACE +
				FROM + SPACE +
				mvTable + SPACE + 
				WHERE + SPACE +
				STATE + SPACE +
				EQUALS + SPACE +
				OPEN_STRING + state + CLOSE_STRING;
		ResultSet resultSet = session.execute(selectCostsByStateQuery);
		Row row;
		ArrayList<String> orderedIds = null;
		if ( (row = resultSet.one()) != null ) {
			orderedIds = new ArrayList<String>();
			ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
			List<Definition> columns = columnDefinitions.asList();

			if (order.equals(LEAST)) {
				// columnsIndex starts at 1 because 0 is the index for 'nppes_provider_state'
				for (int columnsIndex = 1; columnsIndex < columns.size(); columnsIndex++) {
					Definition column = columns.get(columnsIndex);
					String columnName = column.getName();
					String ids = row.getString(columnName);
					if (ids != null) {
						String[] idsArray = ids.split(",");
						for (int idsIndex = 0; idsIndex < idsArray.length; idsIndex++) {
							String id = idsArray[idsIndex];
							int idLength = id.length();
							// start at 11 because 10 for npi, 1 for office/faculty. subtract 4 to peel the year.
							String idCode = id.substring(11,idLength-4);
							if (idCode.equals(code) && numberOfInstances < limit) {
								orderedIds.add(id);
								numberOfInstances++;
							}
						}
					}
				}
			} 
			else if (order.equals(MOST)) {
				// columnsIndex ends at 1 because 0 is the index for 'nppes_provider_state'
				for (int columnsIndex = columns.size() - 1; columnsIndex > 0; columnsIndex--) {
					Definition column = columns.get(columnsIndex);
					String columnName = column.getName();
					String ids = row.getString(columnName);
					if (ids != null) {
						String[] idsArray = ids.split(",");
						for (int idsIndex = 0; idsIndex < idsArray.length; idsIndex++) {
							String id = idsArray[idsIndex];
							int idLength = id.length();
							// start at 11 because 10 for npi, 1 for place_of_service
							// subtract 4 from the end to peel the year.
							String idCode = id.substring(11,idLength-4);
							if (idCode.equals(code) && numberOfInstances < limit) {
								orderedIds.add(id);
								numberOfInstances++;
							}
						}
					}
				}
			}
		}

		// close resources
		session.close();

		return orderedIds;
	}

}

package org.hunter.medicare.data;
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
public class GetAveragePerStateAndCode {

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


	public static Double getAverage(String state, String code) {

		String host = "127.0.0.1";
		String keyspace = "demo";
		String mvTable = "mv";

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
		Double sumOfCosts = 0.0;
		Double numberOfInstances = 0.0;
		Double average = 0.0;
		if ( (row = resultSet.one()) != null ) {
			ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
			List<Definition> columns = columnDefinitions.asList();

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
						if (idCode.equals(code)) {
							double cost = Double.parseDouble(columnName.substring(1));
							sumOfCosts += cost;
							numberOfInstances++;
						}
					}
				}
			}

			average = sumOfCosts / numberOfInstances;
		}

		// close resources
		session.close();

		return average;
	}

}

package de.cbw.jav.account.fx;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.mariadb.jdbc.MariaDbDataSource;
import de.cbw.jav.jdbc.ResultSets;


public class DataSources {
	static private final String SQL_QUERY_CUSTOMER = "SELECT * FROM Account WHERE identity=?";
	static private final String SQL_INSERT_MESSAGE = "INSERT INTO Account VALUES (?, ?, ?, ?, ?)";
	static private final String SQL_UPDATE_ACCOUNT = "UPDATE Account SET name=?, balance=?, interestRate=?, lastModification=? WHERE identity=?";
	static private final String PACKAGE_PATH = DataSources.class.getPackage().getName().replace('.', '/');
	static private final String PROPERTIES_PATH  = PACKAGE_PATH + "/main.properties";
	static public final DataSource ACCOUNT_SOURCE;

	static {
		try {
			final Properties properties = new Properties();
			final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			properties.load(classLoader.getResourceAsStream(PROPERTIES_PATH));

			final MariaDbDataSource dataSource = new MariaDbDataSource();
			dataSource.setUser(properties.getProperty("userAlias"));
			dataSource.setPassword(properties.getProperty("userPassword"));
			dataSource.setUrl("jdbc:mariadb://localhost:3306/bank?useUnicode=true&characterEncoding=UTF-8");
			ACCOUNT_SOURCE = dataSource;
		} catch (final IOException | SQLException exception) {
			throw new ExceptionInInitializerError(exception);
		}
	}


	static public List<Map<String,Object>> querySparbuch (final Connection jdbcConnection, final Sparbuch sparbuch) throws SQLException{
		try (PreparedStatement jdbcStatement = jdbcConnection.prepareStatement(SQL_QUERY_CUSTOMER)) {
			jdbcStatement.setLong(1, sparbuch.getKontonummer());
			try (ResultSet resultSet = jdbcStatement.executeQuery()) {
				return ResultSets.toMaps(resultSet);
			}
		}
	}


	static public void insertSparbuch (final Connection jdbcConnection, final Sparbuch sparbuch) throws SQLException {
		try (PreparedStatement jdbcStatement = jdbcConnection.prepareStatement(SQL_INSERT_MESSAGE, Statement.RETURN_GENERATED_KEYS)) {
			jdbcStatement.setLong(1, sparbuch.getKontonummer());
			jdbcStatement.setString(2, sparbuch.getInhaber());
			jdbcStatement.setLong(3, sparbuch.getGuthaben());
			jdbcStatement.setDouble(4, sparbuch.getZinssatz());
			jdbcStatement.setLong(5, System.currentTimeMillis());

			final int rowCount = jdbcStatement.executeUpdate();
			if(rowCount == 1) {
				try (ResultSet generatedKeys = jdbcStatement.getGeneratedKeys()) {
		            if (generatedKeys.next()) {
		            	sparbuch.setKontonummer(generatedKeys.getLong(1));
		            }
				}
			}
		}
	}


	static public boolean updateSparbuch (final Connection jdbcConnection, final Sparbuch sparbuch) throws SQLException{
		try (PreparedStatement jdbcStatement = jdbcConnection.prepareStatement(SQL_UPDATE_ACCOUNT)) {
			jdbcStatement.setString(1, sparbuch.getInhaber());
			jdbcStatement.setLong(2, sparbuch.getGuthaben());
			jdbcStatement.setDouble(3, sparbuch.getZinssatz());
			jdbcStatement.setLong(4, System.currentTimeMillis());
			jdbcStatement.setLong(5, sparbuch.getKontonummer());

			final int rowCount = jdbcStatement.executeUpdate();
			return rowCount == 1;
		}
	}
}
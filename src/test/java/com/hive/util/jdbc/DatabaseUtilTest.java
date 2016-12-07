package com.hive.util.jdbc;

import java.security.PrivilegedActionException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;

public class DatabaseUtilTest {

	@Test
	public void testHiveConnectionKeytabBased() throws SQLException, PrivilegedActionException {
		Connection conn = null;
		try {
			conn = DatabaseUtil.getHiveConnection("org.apache.hive.jdbc.HiveDriver", "jdbc:hive2://<hive_server2>:10000/default;krb5_kdc=<kdc_admin_server>;principal=hive/<hive_server2>@<realm_name>;auth=kerberos;kerberosAuthType=fromSubject",
					"<username>", "<keytab_location>", true);
			validateConnection(conn, 10);
		} catch (Exception e) {
			e.printStackTrace();
		}
		DatabaseUtil.closeConnection(conn);
	}

	@Test
	public void testHiveConnectionPasswordBased() throws SQLException, PrivilegedActionException {
		Connection conn = null;
		try {
			conn = DatabaseUtil.getHiveConnection("org.apache.hive.jdbc.HiveDriver", "jdbc:hive2://<hive_server2>:10000/default;krb5_kdc=<kdc_admin_server>;principal=hive/<hive_server2>@<realm_name>;auth=kerberos;kerberosAuthType=fromSubject",
					"<username>", "<password>", false);
			validateConnection(conn, 10);
		} catch (Exception e) {
			e.printStackTrace();
		}
		DatabaseUtil.closeConnection(conn);
	}

	private void validateConnection(Connection conn, int max) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("show databases");
			ResultSetMetaData metaData = rs.getMetaData();
			int rowIndex = 0;
			while (rs.next()) {
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					System.out.print("  " + rs.getString(i));
				}
				System.out.println();
				rowIndex++;
				if (max > 0 && rowIndex >= max)
					break;
			}
		} catch (SQLException e) {
			throw e;
		}

		DatabaseUtil.closeResultSet(rs);
		DatabaseUtil.closeStatement(stmt);
	}
}

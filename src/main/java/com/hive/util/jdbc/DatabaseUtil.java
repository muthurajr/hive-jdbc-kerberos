package com.hive.util.jdbc;

import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Class DatabaseUtil is used to create jdbc connection by utilizing the password and other parameters.
 */
public class DatabaseUtil {
	
	/** The Constant EMPTY_STRING. */
	public static final String EMPTY_STRING = "";
	
	/** The Constant log. */
	public static final Log log = LogFactory.getLog(DatabaseUtil.class);

	/**
	 * Gets the hive connection.
	 *
	 * @param driver
	 *            the driver for Hive connection
	 * @param url
	 *            the url to connect to hive of the format jdbc:hive2://<hive_server2>:10000/default;krb5_kdc=<kdc_admin_server>;principal=hive/<hive_server2>@<realm_name>;auth=kerberos;kerberosAuthType=fromSubject
	 * @param username
	 *            the username for Hive connection
	 * @param password
	 *            the password/location of the keytab file for Hive connection
	 * @param isKeytabBased
	 *            the flag to denote whether it is password based or keytab based
	 * @return the hive connection
	 * @throws Exception
	 *             the exception
	 */
	public static Connection getHiveConnection(String driver, String url, String username, String password, boolean isKeytabBased) throws Exception {
		Subject subject = getSubject(url, username, password, isKeytabBased);
		return getHiveConnection(driver, url, subject);
	}

	/**
	 * Gets the hive connection.
	 *
	 * @param driver
	 *            the driver
	 * @param url
	 *            the url
	 * @param subject
	 *            the subject
	 * @return the hive connection
	 * @throws Exception
	 *             the exception
	 */
	public static Connection getHiveConnection(final String driver, final String url, Subject subject) throws Exception {
		Connection conn = (Connection) Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
			
			/* (non-Javadoc)
			 * @see java.security.PrivilegedExceptionAction#run()
			 */
			public Object run() {
				Connection con = null;
				try {
					Class.forName(driver);
					con = DriverManager.getConnection(url, EMPTY_STRING, EMPTY_STRING);
				} catch (SQLException e) {
					log.error("DatabaseUtil - getHiveConnection() : Exception obtaining Hive connection ".concat(e.getMessage()));
				} catch (ClassNotFoundException e) {
					log.error("DatabaseUtil - getHiveConnection() : Driver class not found ".concat(e.getLocalizedMessage()));
				}
				return con;
			}
		});

		if (conn == null) {
			throw new Exception("Exception occurred while retrieving hive connection");
		}
		return conn;
	}

	/**
	 * Gets the subject.
	 *
	 * @param url
	 *            the url
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @param isKeytabBased
	 *            the is keytab based
	 * @return the subject
	 * @throws LoginException
	 *             the login exception
	 */
	public static Subject getSubject(String url, String username, String password, boolean isKeytabBased) throws LoginException {
		String[] urlParams = url.split(";");
		String krb5_realm = null;
		String krb5_kdc = null;
		for (String _urlParam : urlParams) {
			if (_urlParam.contains("principal=")) {
				krb5_realm = _urlParam.replace("krb5_realm=", "");
				krb5_realm = krb5_realm.substring(krb5_realm.indexOf("@") + 1);
			}
			if (_urlParam.contains("krb5_kdc=")) {
				krb5_kdc = _urlParam.replace("krb5_kdc=", "");
			}
		}
		return KerberosUtil.getSubject(username, password, isKeytabBased, krb5_realm, krb5_kdc);

	}
	
	/**
	 * Close result set.
	 *
	 * @param rs
	 *            the rs
	 */
	public static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
			}
		}

	}

	/**
	 * Close statement.
	 *
	 * @param stmt
	 *            the stmt
	 */
	public static void closeStatement(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
			}
		}
	}

	/**
	 * Close connection.
	 *
	 * @param conn
	 *            the conn
	 */
	public static void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
	}

}

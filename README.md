# hive-jdbc-kerberos
This projects helps to make Jdbc Connection to Hive in an kerberized environment using Keytab/Password and without creating jaas.conf and krb5.conf
The same class can be used to establish impala connection as well

Usage: 
	class : com.hive.util.jdbc.DatabaseUtil
	method : getHiveConnection 
		should be called to get Hive Connection with Kerberos
Parameters:
	driver : Driver class to connect to Hive : org.apache.hive.jdbc.HiveDriver
	url : Url to connect to Hive : jdbc:hive2://<hive_server2>:10000/default;krb5_kdc=<kdc_admin_server>;principal=hive/<hive_server2>@<realm_name>;auth=kerberos;kerberosAuthType=fromSubject
	username : Username to connect to Hive
	password : Password or keytab file location to connect to Hive
	isKeytabBased : true or false to whether password parameter is keytab or not

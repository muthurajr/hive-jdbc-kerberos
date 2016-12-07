package com.hive.util.jdbc;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Class KerberosUtil intended to support Kerberos related utils like 
 * subject creation, jaas file configuration, keytab/password controlled
 */
public class KerberosUtil {
	
	/** The Constant SYNCHRONIZED_OBJ for controlling sequential creation of the subject */
	public static final String SYNCHRONIZED_OBJ = "subject";
	
	/** The Constant log. */
	public static final Log log = LogFactory.getLog(KerberosUtil.class);

	/**
	 * Gets the subject.
	 *
	 * @param username
	 *            the username for the subject creation
	 * @param password
	 *            the password or keytab location for subject creation
	 * @param isKeytabBased
	 *            the flag to denote whether it is keytab based or password based
	 * @param krb5_realm
	 *            the krb5 realm name
	 * @param krb5_kdc
	 *            the krb5 kdc admin server name
	 * @return the subject
	 * @throws LoginException
	 *             the login exception
	 */
	public static Subject getSubject(String username, String password, boolean isKeytabBased, String krb5_realm, String krb5_kdc) throws LoginException {
		log.info("KerberosUtil - getSubject() Invoked with parameters : ".concat(" username : ").concat(password).concat(" isKeytabBased : ")
				.concat(Boolean.toString(isKeytabBased)).concat(" krb5_realm : ").concat(krb5_realm).concat(" krb5_kdc : ").concat(krb5_kdc));
		synchronized (SYNCHRONIZED_OBJ) {
			Subject signedOnUserSubject = null;
			System.setProperty("java.security.krb5.realm", krb5_realm);
			System.setProperty("java.security.krb5.kdc", krb5_kdc);
			String subjectName = "SampleClient" + (new Date().getTime());

			JaasConfiguration jaasConf = new JaasConfiguration(isKeytabBased, password);
			Configuration.setConfiguration(jaasConf);
			LoginContext lc;
			try {
				lc = new LoginContext(subjectName, new MyCallbackHandler(username, password, krb5_realm));
				lc.login();
				signedOnUserSubject = lc.getSubject();
			} catch (LoginException e1) {
				log.error("KerberosUtil - getSubject() : "+ e1.getMessage());
				throw e1;
			}
			return signedOnUserSubject;
		}
	}

	/**
	 * The Class JaasConfiguration.
	 */
	private static class JaasConfiguration extends Configuration {
		
		/** The is keytab based. */
		boolean isKeytabBased = false;
		
		/** The password. */
		String password = null;

		/**
		 * Instantiates a new jaas configuration.
		 *
		 * @param isKeytabBased
		 *            the is keytab based
		 * @param password
		 *            the password
		 */
		public JaasConfiguration(boolean isKeytabBased, String password) {
			this.isKeytabBased = isKeytabBased;
			this.password = password;
		}

		/* (non-Javadoc)
		 * @see javax.security.auth.login.Configuration#getAppConfigurationEntry(java.lang.String)
		 */
		@Override
		public AppConfigurationEntry[] getAppConfigurationEntry(String appName) {
			Map<String, String> krbOptions = new HashMap<String, String>();
			krbOptions.put("refreshKrb5Config", "true");
			krbOptions.put("storeKey", "true");
			if (isKeytabBased) {
				krbOptions.put("useKeyTab", "true");
				krbOptions.put("keyTab", password);
			} else {
				krbOptions.put("useKeyTab", "false");
			}
			AppConfigurationEntry testClientEntry = new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule", LoginModuleControlFlag.REQUIRED, krbOptions);
			return new AppConfigurationEntry[] { testClientEntry };
		}
	}

	/**
	 * The Class MyCallbackHandler.
	 */
	private static class MyCallbackHandler implements CallbackHandler {
		
		/** The username. */
		String username = null;
		
		/** The krb 5 realm. */
		String krb5_realm = null;
		
		/** The password. */
		String password = null;

		/**
		 * Instantiates a new my callback handler.
		 *
		 * @param username
		 *            the username
		 * @param password
		 *            the password
		 * @param krb5_realm
		 *            the krb5 realm
		 */
		public MyCallbackHandler(String username, String password, String krb5_realm) {
			this.username = username;
			this.krb5_realm = krb5_realm;
			this.password = password;
		}

		/* (non-Javadoc)
		 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
		 */
		public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
			for (int i = 0; i < callbacks.length; i++) {
				if (callbacks[i] instanceof NameCallback) {
					NameCallback nc = (NameCallback) callbacks[i];
					nc.setName(username + "@" + krb5_realm);
				} else if (callbacks[i] instanceof PasswordCallback) {
					PasswordCallback nc = (PasswordCallback) callbacks[i];
					nc.setPassword(password.toCharArray());
				} else
					throw new UnsupportedCallbackException(callbacks[i], "Unrecognised callback");
			}
		}
	}
}

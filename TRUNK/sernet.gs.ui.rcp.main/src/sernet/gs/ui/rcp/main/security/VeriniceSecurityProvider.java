/*******************************************************************************
 * Copyright (c) 2011 Robert Schuster <r.schuster[at]tarent[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster[at]tarent[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sun.security.pkcs11.SunPKCS11;

@SuppressWarnings({ "serial", "restriction" })
public final class VeriniceSecurityProvider extends Provider {

	public static final String NAME = "VeriniceSecurityProvider";

	public static final double VERSION = 1.0;

	private Preferences prefs;

	private PasswordHolder holder = new PasswordHolder();

	static VeriniceSecurityProvider INSTANCE;
	
	/** Does the registration of verinice's built-in security provider when the
	 * respective preferences have been set.
	 * 
	 * <p>Changing the security provider has modifies the way the SSL engine
	 * does client and server certificate validation.</p>
	 * 
	 * <p>Unfortunately changing the parameters makes it neccessary to restart
	 * the VM. Otherwise it is not possible to make sure SSL does the right
	 * things.</p>
	 *  
	 * @param prefs
	 */
	public static void register(Preferences prefs) {
		if (prefs.getBoolean(PreferenceConstants.CRYPTO_VERINICE_SSL_SECURITY_ENABLED)
				&& Security.getProvider(VeriniceSecurityProvider.NAME) == null) {
			// Create and register the provider
			VeriniceSecurityProvider provider = new VeriniceSecurityProvider(prefs);
			Security.addProvider(provider);
			
			// Add some services to the provider - can also cause the initialization
			// of a PKCS#11 library (which in turn depends on the existance of an
			// installed VeriniceSecurityProvider - hence the split of things between
			// the constructor and the init() method).
			provider.init();
			
			// Routes Key- and TrustManager calls through our code.
			Security.setProperty("ssl.KeyManagerFactory.algorithm", "verinice");
			Security.setProperty("ssl.TrustManagerFactory.algorithm", "verinice");

			// Routes Key- and TrustStore generation through our code.
			System.setProperty("javax.net.ssl.trustStoreType", "verinice-ts");
			System.setProperty("javax.net.ssl.keyStoreType", "verinice-ks");
		}

	}

	@SuppressWarnings("restriction")
	public VeriniceSecurityProvider(Preferences prefs) {
		super(NAME, VERSION, "Verinice' Security Provider");
		this.prefs = prefs;
		INSTANCE = this;
	}

	private void init() {
		putService(new Service(this, "KeyManagerFactory", "verinice",
				DelegatingKeyManagerFactory.class.getName(), null, null));
		putService(new Service(this, "TrustManagerFactory", "verinice",
				DelegatingTrustManagerFactory.class.getName(), null, null));
		putService(new Service(this, "KeyStore", "verinice-ks",
				VeriniceKeyStore.class.getName(), null, null));
		putService(new Service(this, "KeyStore", "verinice-ts",
				VeriniceTrustStore.class.getName(), null, null));
		
		if (isPKCS11LibraryEnabled()) {
			// If the user enabled anything PKCS#11 related we need to lead the PKCS#11 library and add its
			// provider.
			String configFile = createPKCS11ConfigFile();
			if (configFile != null) {
				SunPKCS11 p = new SunPKCS11(configFile);
				p.setCallbackHandler(new Helper() {
					@Override
					protected void handle(PasswordCallback cb) {
						cb.setPassword(getTokenPIN());
					}
				});
				Security.addProvider(p);
			}
		}
		
	}

	char[] initKeyStore(KeyStore ks, char[] password) throws NoSuchAlgorithmException,
			CertificateException, IOException, UnrecoverableKeyException,
			KeyStoreException {
		// If no file is mentioned we cannot initialize the key store.
		if (!useFileAsKeyStore())
			return null;

		String keyStoreFile = getKeyStoreFile();

		boolean wasWrong = false;
		try {
			loadKeystore(ks, keyStoreFile, password);
		} catch (NoSuchAlgorithmException e) {
			holder.reset();
		}
		
		return password;
	}

	void initTrustStore(KeyStore ks) throws NoSuchAlgorithmException,
			CertificateException, IOException, UnrecoverableKeyException,
			KeyStoreException {
		// If no file is mentioned we cannot initialize the key store.
		if (!useFileAsTrustStore())
			return;

		String trustStoreFile = getTrustStoreFile();

		try {
			loadKeystore(ks, trustStoreFile, null);
		} catch (NoSuchAlgorithmException e) {
			holder.reset();
		}

	}

	char[] getTrustStorePassword(boolean wasWrong) {
		return holder.getTrustStorePassword(wasWrong);
	}

	char[] getKeyStorePassword(boolean wasWrong) {
		return holder.getKeyStorePassword(wasWrong);
	}
	
	char[] getTokenPIN() {
		return holder.getTokenPIN();
	}

	private void loadKeystore(KeyStore ks, String file, char[] password)
			throws NoSuchAlgorithmException, CertificateException, IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			ks.load(fis, password);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}

	}

	private boolean useFileAsTrustStore() {
		return PreferenceConstants.CRYPTO_TRUSTSTORE_SOURCE_FROM_FILE.equals(prefs.getString(PreferenceConstants.CRYPTO_TRUSTSTORE_SOURCE));
	}

	private boolean useFileAsKeyStore() {
		return PreferenceConstants.CRYPTO_KEYSTORE_SOURCE_FROM_FILE.equals(prefs.getString(PreferenceConstants.CRYPTO_KEYSTORE_SOURCE));
	}
	
	private boolean isPKCS11LibraryEnabled() {
		return usePKCS11LibraryAsKeyStore() || usePKCS11LibraryAsTrustStore();
	}
	
	private boolean usePKCS11LibraryAsTrustStore() {
		return PreferenceConstants.CRYPTO_TRUSTSTORE_SOURCE_FROM_PKCS11_LIBRARY.equals(prefs.getString(PreferenceConstants.CRYPTO_TRUSTSTORE_SOURCE));
	}

	private boolean usePKCS11LibraryAsKeyStore() {
		return PreferenceConstants.CRYPTO_KEYSTORE_SOURCE_FROM_PKCS11_LIBRARY.equals(prefs.getString(PreferenceConstants.CRYPTO_KEYSTORE_SOURCE));
	}

	private String getTrustStoreFile() {
		return prefs.getString(PreferenceConstants.CRYPTO_TRUSTSTORE_FILE);
	}

	private String getKeyStoreFile() {
		return prefs.getString(PreferenceConstants.CRYPTO_KEYSTORE_FILE);
	}
	
	private String createPKCS11ConfigFile() {
		File f = null;
		PrintWriter writer = null;
		try {
			f = File.createTempFile("pkcs11", ".cfg");
			f.deleteOnExit();
			writer = new PrintWriter(new FileOutputStream(f));
			writer.println("name = verinice");
			writer.println("description = verinice PKCS#11 configuration");
			writer.println("library = " + prefs.getString(PreferenceConstants.CRYPTO_PKCS11_LIBRARY_PATH));
			writer.close();
		} catch (IOException e) {
			return null;
		} finally {
			if (writer != null)
				IOUtils.closeQuietly(writer);
		}
		
		return f.getAbsolutePath();
	}

	/** Helper class which automatically asks the user for passwords/PINs
	 * if those have not been provided yet.
	 * 
	 */
	private class PasswordHolder {
		private char[] trustStorePassword = null;
		private char[] keyStorePassword = null;
		private char[] tokenPIN = null;

		private void showDialog(final PasswordDialog.Type t) {
			final boolean tse = useFileAsTrustStore();
			final boolean kse = useFileAsKeyStore();
			final boolean tpe = isPKCS11LibraryEnabled();
			
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					PasswordDialog d = new PasswordDialog(Display.getCurrent()
							.getActiveShell(), tse, kse, tpe);
					
					d.setFocus(t);
					d.open();

					trustStorePassword = d.getTrustStorePassword();
					keyStorePassword = d.getKeyStorePassword();
					tokenPIN = d.getTokenPIN();

					d.clearPasswords();
				}
			});
		}

		char[] getTrustStorePassword(boolean wasWrong) {
			if (wasWrong || trustStorePassword == null)
				showDialog(PasswordDialog.Type.TRUST);

			return trustStorePassword;
		}

		char[] getKeyStorePassword(boolean wasWrong) {
			if (wasWrong || keyStorePassword == null)
				showDialog(PasswordDialog.Type.KEY);

			return keyStorePassword;
		}
		
		char[] getTokenPIN() {
			// Token PINs are never considered stored because that could lead to the wrong
			// PIN being delivered multiple times causing the token to be locked (usually
			// after 3-5 times).
			showDialog(PasswordDialog.Type.TOKEN);

			return tokenPIN;
		}
		
		/*
		 * Clears all the sensitive data in the password arrays and
		 * then allows them to be
		 * garbage collected.
		 */
		void reset() {
			for(int i=0;i<trustStorePassword.length;i++)
				trustStorePassword[i] = 0;
			
			for(int i=0;i<keyStorePassword.length;i++)
				keyStorePassword[i] = 0;
			
			for(int i=0;i<tokenPIN.length;i++)
				tokenPIN[i] = 0;
			
			trustStorePassword = null;
			keyStorePassword = null;
			tokenPIN = null;
		}

	}

	/**
	 * Helper class which plays together with the JDK's credentials callback mechanism.
	 * 
	 * <p>So far this implementation can only handle password callbacks and does so using
	 * the {@link PasswordHolder} class.
	 * 
	 */
	abstract static class Helper implements CallbackHandler {

		@Override
		public void handle(Callback[] callbacks) throws IOException,
				UnsupportedCallbackException {
			for (int i = 0;i < callbacks.length;i++) {
				if (callbacks[i] instanceof PasswordCallback) {
					PasswordCallback cb = (PasswordCallback) callbacks[i];
					handle(cb);
				}
			}
		}
		
		/** This is supposed to be implemented by classes using this. */
		protected abstract void handle(PasswordCallback cb);
		
	}

	/**
	 * A {@link KeyManagerFactorySpi} implementation that uses the {@link VeriniceSecurityProvider}
	 * to initialize {@link KeyStore} objects but then delegates to the default implementation
	 * called 'SunX509'.
	 * 
	 * <p>This approach allows defining our own way to retrieve the key store content and
	 * credentials (for use with SSL connections).</p>
	 *
	 */
	public static class DelegatingKeyManagerFactory extends KeyManagerFactorySpi {

		KeyManager[] keyManagers;

		public DelegatingKeyManagerFactory() {
			// Default constructor
		}

		@Override
		protected KeyManager[] engineGetKeyManagers() {
			return keyManagers;
		}

		@Override
		protected void engineInit(ManagerFactoryParameters spec)
				throws InvalidAlgorithmParameterException {
			throw new IllegalStateException("Not implemented");
		}

		@Override
		protected void engineInit(KeyStore ks, char[] password)
				throws KeyStoreException, NoSuchAlgorithmException,
				UnrecoverableKeyException {
			try {
				// calling back into VeriniceSecurityProvider
				password = INSTANCE.initKeyStore(ks, password);
				
				// Otherwise act completely like the reference implementation.
				KeyManagerFactory kmf = KeyManagerFactory
						.getInstance("SunX509");
				kmf.init(ks, password);
				keyManagers = kmf.getKeyManagers();
			} catch (CertificateException e) {
				throw new KeyStoreException(e);
			} catch (IOException e) {
				throw new KeyStoreException(e);
			}
		}
	}

	/**
	 * A {@link TrustManagerFactorySpi} implementation that uses the {@link VeriniceSecurityProvider}
	 * to initialize {@link KeyStore} objects but then delegates to the default implementation
	 * called 'PKIX'.
	 * 
	 * <p>This approach allows defining our own way to retrieve the key store content and
	 * credentials (for use with SSL connections).</p>
	 * 
	 * <p>A trust store is used to verify server certificates.</p>
	 *
	 */
	public static class DelegatingTrustManagerFactory extends
			TrustManagerFactorySpi {

		TrustManager[] trustManagers;

		public DelegatingTrustManagerFactory() {
			// Default constructor
		}

		@Override
		protected TrustManager[] engineGetTrustManagers() {
			return trustManagers;
		}

		@Override
		protected void engineInit(ManagerFactoryParameters spec)
				throws InvalidAlgorithmParameterException {
			throw new IllegalStateException("Not implemented");
		}

		@Override
		protected void engineInit(KeyStore ks) throws KeyStoreException {
			try {
				// calling back into VeriniceSecurityProvider
				INSTANCE.initTrustStore(ks);

				// Like with the KeyManager the behavior is that of the reference implementation.
				TrustManagerFactory tmf = TrustManagerFactory
						.getInstance("PKIX");
				tmf.init(ks);
				trustManagers = tmf.getTrustManagers();

			} catch (CertificateException e) {
				throw new KeyStoreException(e);
			} catch (IOException e) {
				throw new KeyStoreException(e);
			} catch (UnrecoverableKeyException e) {
				throw new KeyStoreException(e);
			} catch (NoSuchAlgorithmException e) {
				throw new KeyStoreException(e);
			}
		}

	}
	
	/**
	 * Implementation of a {@link KeyStoreSpi} that delegates all its
	 * call to another implementation. Which one that is depends the user's
	 * configuration (and this is the sole reason for having this class in
	 * the first place).
	 * 
	 * TODO: One could easily support other keystore formats (like pkcs#12) if
	 * the type (right now: 'jks') could be changed to something else by the user.
	 *
	 */
	public static class VeriniceTrustStore extends DelegatingKeyStore {
		public VeriniceTrustStore() {
			super();
		}
		
		@Override
		protected Configuration init() {
			Configuration c = new Configuration();
			try {
				// If the user wants to use a smartcard/token for doing *server* certificate
				// validation, the 'PKCS11' algorithm from the 'SunPKCS11-verinice' provider
				// needs to be used. (The '-verinice' suffix is because of the 'name' attribute
				// in the configuration file used by the SunPKCS class.
				if (INSTANCE.usePKCS11LibraryAsTrustStore()) {
					// Adding a password protection callback is not possible for this kind
					// of keystore using the KeyStore.Builder API.
					c.keyStore = KeyStore.getInstance("PKCS11", "SunPKCS11-verinice");
				}
				else {
					c.maxAttempts = 3;
					c.passwordHandler = new PasswordHandler() {
						public void handle(PasswordSession session) {
							session.setPassword(INSTANCE.getTrustStorePassword(session.wasWrong()));
						}
					};
					
					c.keyStore = KeyStore.getInstance("jks");
				}
			} catch (KeyStoreException e) {
				throw new RuntimeException(e);
			}
			catch (NoSuchProviderException e) {
				throw new RuntimeException(e);
			}
			
			return c;
		}
	}

	/**
	 * See {@link VeriniceTrustStore} for why this class exists and what it does.
	 */
	public static class VeriniceKeyStore extends DelegatingKeyStore {
		public VeriniceKeyStore() {
			super();
		}
			
		@Override
		protected Configuration init() {
			Configuration c = new Configuration();
			try {
				// If the user wants to use a smartcard/token for doing *client*
				// certificate
				// validation, the 'PKCS11' algorithm from the
				// 'SunPKCS11-verinice' provider
				// needs to be used. (The '-verinice' suffix is because of the
				// 'name' attribute
				// in the configuration file used by the SunPKCS class.
				if (INSTANCE.usePKCS11LibraryAsKeyStore()) {
					// Adding a password protection callback is not possible for this kind
					// of keystore using the KeyStore.Builder API.
					c.keyStore = KeyStore.getInstance("PKCS11", "SunPKCS11-verinice");
				} else {
					c.maxAttempts = 3;
					c.passwordHandler = new PasswordHandler() {
						public void handle(PasswordSession session) {
							session.setPassword(INSTANCE.getKeyStorePassword(session.wasWrong()));
						}
					};
					
					c.keyStore = KeyStore.getInstance("jks");
				}
			} catch (KeyStoreException e) {
				throw new RuntimeException(e);
			} catch (NoSuchProviderException e) {
				throw new RuntimeException(e);
			}
			
			return c;
		}
	}

}

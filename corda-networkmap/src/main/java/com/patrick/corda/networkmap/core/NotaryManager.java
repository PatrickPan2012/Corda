package com.patrick.corda.networkmap.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.security.auth.x500.X500Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.corda.core.crypto.Crypto;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.NotaryInfo;
import net.corda.nodeapi.internal.crypto.KeyStoreUtilities;

/**
 * 
 * @author Patrick Pan
 *
 */
public class NotaryManager {

	private List<NotaryInfo> notaryInfoList = new ArrayList<>();

	private NotaryManager() {
		try {
			initNotaryInfoList();
		} catch (FileNotFoundException e) {
			LOGGER.error("The file is probably deleted illegally.", e);
		} catch (IOException e) {
			LOGGER.error("Probably, there is an error during reading the key store from the file.", e);
		} catch (KeyStoreException e) {
			LOGGER.error("Probably, the key store password is incorrect or the key store is damaged.", e);
		} catch (Exception e) {
			LOGGER.error("Unexpected exception occurs.", e);
		}
	}

	private void initNotaryInfoList() throws KeyStoreException, IOException {
		URL url = NotaryManager.class.getClassLoader().getResource("notaries");

		if (Objects.isNull(url)) {
			LOGGER.error(
					"The resource [notaries] could not be found or the process doesn't have adequate  privileges to get the resource.");
			return;
		}

		File notaryKeyStoreDirectory = new File(url.getPath());

		if (!notaryKeyStoreDirectory.isDirectory()) {
			LOGGER.error("The resource [notaries] must be a directory instead of a file.");
			return;
		}

		File[] notaryKeyStores = notaryKeyStoreDirectory.listFiles();

		if (Objects.isNull(notaryKeyStores) || notaryKeyStores.length == 0) {
			LOGGER.error("The directory 'notaries' is empty but at least one notary must be specified.");
			return;
		}

		initNotaryInfoList(notaryKeyStores);
	}

	private void initNotaryInfoList(File[] notaryKeyStores) throws KeyStoreException, IOException {
		for (File notaryKeyStore : notaryKeyStores) {
			X509Certificate x509Certificate = loadCertificate(notaryKeyStore);

			if (Objects.isNull(x509Certificate)) {
				continue;
			}

			notaryInfoList.add(buildNotaryInfo(x509Certificate));
		}
	}

	private X509Certificate loadCertificate(File notaryKeyStore) throws KeyStoreException, IOException {
		try (InputStream inputStream = new FileInputStream(notaryKeyStore)) {
			KeyStore keyStore = KeyStoreUtilities.loadKeyStore(inputStream, STORE_PASSWORD);

			Certificate certificate = keyStore.getCertificate(ALIAS);

			if (!(certificate instanceof X509Certificate)) {
				LOGGER.warn("Certificate under alias [{}] is not an X.509 certificate.", ALIAS);
				return null;
			}

			return (X509Certificate) certificate;
		}
	}

	private NotaryInfo buildNotaryInfo(X509Certificate x509Certificate) {
		X500Principal x500Principal = x509Certificate.getSubjectX500Principal();
		CordaX500Name cordaX500Name = CordaX500Name.build(x500Principal);
		PublicKey publicKey = Crypto.toSupportedPublicKey(x509Certificate.getPublicKey());

		Party party = new Party(cordaX500Name, publicKey);

		/**
		 * Currently, "validating" is always false.
		 */
		boolean validating = false;
		return new NotaryInfo(party, validating);
	}

	public List<NotaryInfo> getNotaryInfoList() {
		return notaryInfoList;
	}

	/**
	 * Currently, the key store password must be "cordacadevpass".
	 */
	private static final String STORE_PASSWORD = "cordacadevpass"; // NOSONAR
	private static final String ALIAS = "identity-private-key";

	private static final Logger LOGGER = LoggerFactory.getLogger("cordaNetworkMapLogger");

	public static NotaryManager getInstance() {
		return SingletonHelper.INSTANCE;
	}

	private static class SingletonHelper {
		private static final NotaryManager INSTANCE = new NotaryManager();
	}
}

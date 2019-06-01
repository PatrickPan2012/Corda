package com.patrick.corda.networkmap.core;

import static net.corda.core.crypto.Crypto.toSupportedPrivateKey;
import static net.corda.core.crypto.Crypto.toSupportedPublicKey;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.corda.nodeapi.internal.crypto.CertificateAndKeyPair;
import net.corda.nodeapi.internal.crypto.KeyStoreUtilities;

/**
 * 
 * @author Patrick Pan
 *
 */
public class NetworkMapCertificateManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("cordaNetworkMapLogger");

	private CertificateAndKeyPair rootCertificateAndKeyPair;
	private CertificateAndKeyPair networkMapCertificateAndKeyPair;

	private NetworkMapCertificateManager() {
	}

	private void initRootCertificateAndKeyPair() {
		try {
			rootCertificateAndKeyPair = loadRootJKS();
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
			LOGGER.error("Exception occurs during loading 'root.jks'.", e);
		}
	}

	private CertificateAndKeyPair loadRootJKS()
			throws IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
		return loadCertificateAndKeyPair("certificates/root.jks", "keystorepass", "cordarootca");
	}

	private CertificateAndKeyPair loadNetworkMapJKS()
			throws IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
		return loadCertificateAndKeyPair("certificates/network-map.jks", "keystorepass", "cordanetworkmap");
	}

	private KeyStore loadKeyStore(String filename, String keyStorePassword) throws IOException, KeyStoreException {
		try (InputStream inputStream = NetworkMapCertificateManager.class.getClassLoader()
				.getResourceAsStream(filename)) {
			return KeyStoreUtilities.loadKeyStore(inputStream, keyStorePassword);
		}
	}

	private CertificateAndKeyPair loadCertificateAndKeyPair(String filename, String keyStorePassword, String alias)
			throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException {
		KeyStore keyStore = loadKeyStore(filename, keyStorePassword);
		X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
		PublicKey publicKey = toSupportedPublicKey(certificate.getPublicKey());

		PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, "keypass".toCharArray());
		privateKey = toSupportedPrivateKey(privateKey);

		return new CertificateAndKeyPair(certificate, new KeyPair(publicKey, privateKey));
	}

	private void initNetworkMapCertificateAndKeyPair() {
		try {
			networkMapCertificateAndKeyPair = loadNetworkMapJKS();
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
			LOGGER.error("Exception occurs during loading 'network-map.jks'.", e);
		}
	}

	public CertificateAndKeyPair getRootCertificateAndKeyPair() {
		return rootCertificateAndKeyPair;
	}

	public CertificateAndKeyPair getNetworkMapCertificateAndKeyPair() {
		return networkMapCertificateAndKeyPair;
	}

	public void init() {
		try {
			initRootCertificateAndKeyPair();
			initNetworkMapCertificateAndKeyPair();
		} catch (Exception e) {
			LOGGER.error("Unexpected exception occurs in CertificateManager.init().", e);
		}
	}

	public static NetworkMapCertificateManager getInstance() {
		return SingletonHelper.INSTANCE;
	}

	private static class SingletonHelper {
		private static final NetworkMapCertificateManager INSTANCE = new NetworkMapCertificateManager();
	}
}

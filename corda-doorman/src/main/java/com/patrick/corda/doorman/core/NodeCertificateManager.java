package com.patrick.corda.doorman.core;

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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.corda.core.crypto.Crypto;
import net.corda.nodeapi.internal.crypto.CertificateAndKeyPair;
import net.corda.nodeapi.internal.crypto.KeyStoreUtilities;

/**
 * 
 * @author Patrick Pan
 *
 */
public class NodeCertificateManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("cordaDoormanLogger");

	private CertificateAndKeyPair rootCertificateAndKeyPair;
	private CertificateAndKeyPair doormanCertificateAndKeyPair;

	private ExecutorService threadPool = Executors.newCachedThreadPool();
	private CSRResponse csrResponse = CSRResponse.getInstance();

	public String processCSR(PKCS10CertificationRequest pkcs10Holder) {
		String id = UUID.randomUUID().toString();
		threadPool.submit(CreateNodeCertificateTask.newInstance(doormanCertificateAndKeyPair, pkcs10Holder, id));
		return id;
	}

	public X509Certificate[] retrieveCSRResponse(String id) {
		X509Certificate cer = csrResponse.get(id);
		if (cer == null) {
			return new X509Certificate[] {};
		} else {
			return new X509Certificate[] { cer, doormanCertificateAndKeyPair.getCertificate(),
					rootCertificateAndKeyPair.getCertificate() };
		}
	}

	private void initRootCertificateAndKeyPair() {
		try {
			rootCertificateAndKeyPair = loadRootJKS();
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
			LOGGER.error("Exception occurs during loading 'root.jks'.", e);
		}
	}

	private KeyStore loadKeyStore(String filename, String keyStorePassword) throws IOException, KeyStoreException {
		try (InputStream inputStream = NodeCertificateManager.class.getClassLoader().getResourceAsStream(filename)) {
			return KeyStoreUtilities.loadKeyStore(inputStream, keyStorePassword);
		}
	}

	private CertificateAndKeyPair loadCertificateAndKeyPair(String filename, String keyStorePassword, String alias)
			throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException {
		KeyStore keyStore = loadKeyStore(filename, keyStorePassword);
		X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
		PublicKey publicKey = Crypto.toSupportedPublicKey(certificate.getPublicKey());

		PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, "keypass".toCharArray());
		privateKey = Crypto.toSupportedPrivateKey(privateKey);

		return new CertificateAndKeyPair(certificate, new KeyPair(publicKey, privateKey));
	}

	private CertificateAndKeyPair loadDoormanJKS()
			throws IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
		return loadCertificateAndKeyPair("certificates/doorman.jks", "keystorepass", "cordadoormanca");
	}

	private CertificateAndKeyPair loadRootJKS()
			throws IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
		return loadCertificateAndKeyPair("certificates/root.jks", "keystorepass", "cordarootca");
	}

	private void initDoormanCertificateAndKeyPair() {
		try {
			doormanCertificateAndKeyPair = loadDoormanJKS();
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
			LOGGER.error("Exception occurs during loading 'doorman.jks'.", e);
		}
	}

	public void init() {
		try {
			initRootCertificateAndKeyPair();
			initDoormanCertificateAndKeyPair();
		} catch (Exception e) {
			LOGGER.error("Unexpected exception occurs in NodeCertificateManager.init().", e);
		}
	}

	public void destroy() {
		threadPool.shutdown();
	}

	private NodeCertificateManager() {
	}

	public static NodeCertificateManager getInstance() {
		return SingletonHelper.INSTANCE;
	}

	private static class SingletonHelper {
		private static final NodeCertificateManager INSTANCE = new NodeCertificateManager();
	}
}

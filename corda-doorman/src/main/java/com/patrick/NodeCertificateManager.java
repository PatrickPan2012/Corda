package com.patrick;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kotlin.Pair;
import net.corda.core.crypto.Crypto;
import net.corda.core.identity.CordaX500Name;
import net.corda.nodeapi.internal.DevCaHelper;
import net.corda.nodeapi.internal.crypto.CertificateAndKeyPair;
import net.corda.nodeapi.internal.crypto.CertificateType;
import net.corda.nodeapi.internal.crypto.X509Utilities;

/**
 * 
 * @author Patrick Pan
 *
 */
public class NodeCertificateManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("cordaDoormanLogger");

	private CertificateAndKeyPair rootCertificateAndKeyPair;
	private CertificateAndKeyPair doormanCertAndKeyPair;

	private ExecutorService threadPool = Executors.newCachedThreadPool();
	private CSRResponse csrResponse = CSRResponse.getInstance();

	public String doormanProcessCSR(PKCS10CertificationRequest pkcs10Holder) {
		String id = UUID.randomUUID().toString();
		threadPool.submit(CreateNodeCertificateTask.newInstance(doormanCertAndKeyPair, pkcs10Holder, id));
		return id;
	}

	public X509Certificate[] doormanRetrieveCSRResponse(String id) {
		X509Certificate cer = csrResponse.get(id);
		if (cer == null) {
			return new X509Certificate[] {};
		} else {
			return new X509Certificate[] { cer, doormanCertAndKeyPair.getCertificate(),
					rootCertificateAndKeyPair.getCertificate() };
		}
	}

	private void createRootCertificateAndKeyPair() {
		/**
		 * A new root certificate should be created for production environment.
		 */
		rootCertificateAndKeyPair = DevCaHelper.INSTANCE.loadDevCa(X509Utilities.CORDA_ROOT_CA);
	}

	private void createDoormanCertificateAndKeyPair() {
		final String CN = "Common Name";
		final String OU = "Organizational Unit";
		final String ST = "State";
		final String O = "Organization";
		final String L = "Locality";
		final String C = "Country";
		CordaX500Name name = new CordaX500Name(CN, OU, O, L, ST, C);

		KeyPair keyPair = Crypto.generateKeyPair(Crypto.ECDSA_SECP256R1_SHA256);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		X509Certificate certificate = X509Utilities.createCertificate(CertificateType.INTERMEDIATE_CA,
				rootCertificateAndKeyPair.getCertificate(), rootCertificateAndKeyPair.getKeyPair(),
				name.getX500Principal(), keyPair.getPublic(), new Pair(Duration.ofMillis(0), Duration.ofDays(3650)),
				null);
		doormanCertAndKeyPair = new CertificateAndKeyPair(certificate, keyPair);
	}

	public void init() {
		try {
			createRootCertificateAndKeyPair();
			createDoormanCertificateAndKeyPair();
		} catch (Exception e) {
			LOGGER.error("Exception occurs in NodeCertificateManager.init().", e);
		}
	}

	public void destroy() {
		threadPool.shutdown();
	}

	private NodeCertificateManager() {
		init();
	}

	public static NodeCertificateManager getInstance() {
		return SingletonHelper.INSTANCE;
	}

	private static class SingletonHelper {
		private static final NodeCertificateManager INSTANCE = new NodeCertificateManager();
	}
}

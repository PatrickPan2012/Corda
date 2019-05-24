package com.patrick.corda.networkmap.core;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.time.Duration;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.NameConstraints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kotlin.Pair;
import net.corda.core.crypto.Crypto;
import net.corda.nodeapi.internal.DevCaHelper;
import net.corda.nodeapi.internal.crypto.CertificateAndKeyPair;
import net.corda.nodeapi.internal.crypto.CertificateType;
import net.corda.nodeapi.internal.crypto.X509Utilities;

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

	private void createRootCertificateAndKeyPair() {
		/**
		 * A new root certificate should be created for production environment.
		 */
		rootCertificateAndKeyPair = DevCaHelper.INSTANCE.loadDevCa(X509Utilities.CORDA_ROOT_CA);
	}

	private X500Principal buildX500Principal() {
		final String CN = "Common Name";
		final String O = "Organization";
		final String L = "Locality";
		final String C = "GB";

		String name = String.format("CN=%s,O=%s,L=%s,C=%s", CN, O, L, C);
		LOGGER.debug("The X.500 distinguished name of Network Map is [{}].", name);

		return new X500Principal(name);
	}

	private void createNetworkMapCertificateAndKeyPair() {
		X500Principal x500Principal = buildX500Principal();
		KeyPair keyPair = Crypto.generateKeyPair(Crypto.ECDSA_SECP256R1_SHA256);
		NameConstraints nameConstraints = null;
		X509Certificate certificate = X509Utilities.createCertificate(CertificateType.NETWORK_MAP,
				rootCertificateAndKeyPair.getCertificate(), rootCertificateAndKeyPair.getKeyPair(), x500Principal,
				keyPair.getPublic(), new Pair<Duration, Duration>(Duration.ofMillis(0), Duration.ofDays(3650)),
				nameConstraints);

		networkMapCertificateAndKeyPair = new CertificateAndKeyPair(certificate, keyPair);
	}

	public CertificateAndKeyPair getRootCertificateAndKeyPair() {
		return rootCertificateAndKeyPair;
	}

	public CertificateAndKeyPair getNetworkMapCertificateAndKeyPair() {
		return networkMapCertificateAndKeyPair;
	}

	public void init() {
		try {
			createRootCertificateAndKeyPair();
			createNetworkMapCertificateAndKeyPair();
		} catch (Exception e) {
			LOGGER.error("Exception occurs in CertificateManager.init().", e);
		}
	}

	public static NetworkMapCertificateManager getInstance() {
		return SingletonHelper.INSTANCE;
	}

	private static class SingletonHelper {
		private static final NetworkMapCertificateManager INSTANCE = new NetworkMapCertificateManager();
	}
}

package com.patrick.corda;

import static net.corda.core.crypto.Crypto.ECDSA_SECP256R1_SHA256;
import static net.corda.core.crypto.Crypto.generateKeyPair;
import static net.corda.core.crypto.Crypto.toSupportedPublicKey;
import static net.corda.nodeapi.internal.crypto.CertificateType.INTERMEDIATE_CA;
import static net.corda.nodeapi.internal.crypto.CertificateType.NETWORK_MAP;
import static net.corda.nodeapi.internal.crypto.X509Utilities.createCertificate;
import static net.corda.nodeapi.internal.crypto.X509Utilities.createSelfSignedCACertificate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;

import org.bouncycastle.asn1.x509.NameConstraints;

import kotlin.Pair;
import net.corda.core.identity.CordaX500Name;
import net.corda.nodeapi.internal.crypto.CertificateType;
import net.corda.nodeapi.internal.crypto.KeyStoreUtilities;

/**
 * 
 * @author Patrick Pan
 *
 */
public class KeyStoreUtils {

	private static final String CORDA_ROOT_CA = "cordarootca";
	private static final String KEY_STORE_PASSWORD = "keystorepass"; // NOSONAR
	private static final String KEY_PASSWORD = "keypass"; // NOSONAR

	private KeyStoreUtils() {
	}

	private static void generateKeyStore(String filename, String certAlias, X509Certificate certificate,
			PrivateKey privateKey)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(null, null);
		keyStore.setCertificateEntry(certAlias, certificate);
		keyStore.setKeyEntry(certAlias, privateKey, KEY_PASSWORD.toCharArray(), new X509Certificate[] { certificate });

		try (FileOutputStream fileOutputStream = new FileOutputStream(filename)) {
			keyStore.store(fileOutputStream, KEY_STORE_PASSWORD.toCharArray());
		}
	}

	public static KeyStore loadKeyStore(String filename, String keyStorePassword)
			throws IOException, KeyStoreException {
		try (InputStream inputStream = KeyStoreUtils.class.getClassLoader().getResourceAsStream(filename)) {
			return KeyStoreUtilities.loadKeyStore(inputStream, keyStorePassword);
		}
	}

	public static void createNetworkMapCertificate(X509Certificate rootCertificate, KeyPair rootKeyPair,
			String certAlias, CordaX500Name cordaX500Name)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		createCert("network-map.jks", NETWORK_MAP, rootCertificate, rootKeyPair, certAlias, cordaX500Name);
	}

	public static void createDoormanCA(X509Certificate rootCertificate, KeyPair rootKeyPair, String certAlias,
			CordaX500Name cordaX500Name)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		createCert("doorman.jks", INTERMEDIATE_CA, rootCertificate, rootKeyPair, certAlias, cordaX500Name);
	}

	private static void createCert(String filename, CertificateType certificateType, X509Certificate rootCertificate,
			KeyPair rootKeyPair, String certAlias, CordaX500Name cordaX500Name)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		KeyPair keyPair = generateKeyPair(ECDSA_SECP256R1_SHA256);
		NameConstraints nameConstraints = null;
		X509Certificate certificate = createCertificate(certificateType, rootCertificate, rootKeyPair,
				cordaX500Name.getX500Principal(), keyPair.getPublic(),
				new Pair<Duration, Duration>(Duration.ofMillis(0), Duration.ofDays(3650)), nameConstraints);
		generateKeyStore(filename, certAlias, certificate, keyPair.getPrivate());
	}

	/**
	 * @see the 46th line of io/cordite/networkmap/keystore/KeyStores.kt
	 * 
	 * @param keyStorePassword
	 * @param keyPassword
	 * @param cordaX500Name
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static void createRootCA(CordaX500Name cordaX500Name)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		KeyPair keyPair = generateKeyPair(ECDSA_SECP256R1_SHA256);
		X509Certificate certificate = createSelfSignedCACertificate(cordaX500Name.getX500Principal(), keyPair,
				new Pair<Duration, Duration>(Duration.ofMillis(0), Duration.ofDays(3650)));

		generateKeyStore("root.jks", CORDA_ROOT_CA, certificate, keyPair.getPrivate());
	}

	public static void createNetworkRootTruststore(String filename, String keyStorePassword)
			throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
		KeyStore rootKeyStore = loadKeyStore(filename, keyStorePassword);
		X509Certificate rootCertificate = (X509Certificate) rootKeyStore.getCertificate(CORDA_ROOT_CA);

		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(null, null);
		keyStore.setCertificateEntry(CORDA_ROOT_CA, rootCertificate);

		try (FileOutputStream fileOutputStream = new FileOutputStream("network-root-truststore.jks")) {
			keyStore.store(fileOutputStream, keyStorePassword.toCharArray());
		}
	}

	public static PublicKey getPublicKey(String jks, String storePassword, String alias)
			throws KeyStoreException, IOException {
		KeyStore keyStore = KeyStoreUtilities.loadKeyStore(Utils.class.getClassLoader().getResourceAsStream(jks),
				storePassword);
		Certificate certificate = keyStore.getCertificate(alias);

		if (!(certificate instanceof X509Certificate)) {
			throw new IllegalStateException(
					String.format("Certificate under alias [%s] is not an X.509 certificate.", alias));
		}

		X509Certificate x509Certificate = (X509Certificate) certificate;

		return toSupportedPublicKey(x509Certificate.getPublicKey());
	}
}

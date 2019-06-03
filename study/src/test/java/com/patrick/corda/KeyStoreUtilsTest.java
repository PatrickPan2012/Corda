package com.patrick.corda;

import static com.patrick.corda.KeyStoreUtils.createDoormanCA;
import static com.patrick.corda.KeyStoreUtils.createNetworkMapCertificate;
import static com.patrick.corda.KeyStoreUtils.createNetworkRootTruststore;
import static com.patrick.corda.KeyStoreUtils.createRootCA;
import static com.patrick.corda.KeyStoreUtils.getPublicKey;
import static com.patrick.corda.KeyStoreUtils.loadKeyStore;
import static com.patrick.corda.Utils.inputStreamToByteArray;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import org.junit.Test;

import net.corda.core.crypto.Crypto;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.NodeInfo;
import net.corda.nodeapi.internal.SignedNodeInfo;

/**
 * 
 * @author Patrick Pan
 *
 */
public class KeyStoreUtilsTest {

	@Test
	public void testCreateRootCA()
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		final String commonName = "Corda Node Root CA";
		final String organisationUnit = "Organisation Unit";
		final String organisation = "Organisation";
		final String locality = "Locality";
		final String state = "State";
		final String country = "GB";

		CordaX500Name cordaX500Name = CordaX500NameBuilder.newInstance().setCommonName(commonName)
				.setOrganisationUnit(organisationUnit).setOrganisation(organisation).setLocality(locality)
				.setState(state).setCountry(country).build();
		createRootCA(cordaX500Name);
		assertTrue(new File("root.jks").delete());
	}

	@Test
	public void testGetPublicKey() throws KeyStoreException, IOException {
		String jks = "nodekeystore.jks";
		String storePassword = "cordacadevpass";
		String alias = "identity-private-key";
		PublicKey publicKey1 = getPublicKey(jks, storePassword, alias);
		assertNotNull(publicKey1);

		SignedNodeInfo signedNodeInfo = SignedNodeInfoUtils
				.deserializeSignedNodeInfo(inputStreamToByteArray("nodeInfo-{hash}"));
		NodeInfo nodeInfo = signedNodeInfo.verified();
		List<Party> partyList = nodeInfo.getLegalIdentities();
		assertEquals(1, partyList.size());
		Party party = partyList.get(0);
		PublicKey publicKey2 = party.getOwningKey();
		assertNotNull(publicKey2);

		assertEquals(publicKey2, publicKey1);
	}

	@Test
	public void testCreateDoormanCA() throws KeyStoreException, IOException, UnrecoverableKeyException,
			NoSuchAlgorithmException, CertificateException {
		KeyStore keyStore = loadKeyStore("root.jks", "keystorepass");

		X509Certificate rootCertificate = (X509Certificate) keyStore.getCertificate("cordarootca");
		PublicKey rootPublicKey = Crypto.toSupportedPublicKey(rootCertificate.getPublicKey());

		PrivateKey rootPrivateKey = (PrivateKey) keyStore.getKey("cordarootca", "keypass".toCharArray());
		rootPrivateKey = Crypto.toSupportedPrivateKey(rootPrivateKey);

		final String certAlias = "cordadoormanca";

		final String commonName = "Corda Node Doorman CA";
		final String organisationUnit = "Organisation Unit";
		final String organisation = "Organisation";
		final String locality = "Locality";
		final String state = "State";
		final String country = "GB";

		CordaX500Name cordaX500Name = CordaX500NameBuilder.newInstance().setCommonName(commonName)
				.setOrganisationUnit(organisationUnit).setOrganisation(organisation).setLocality(locality)
				.setState(state).setCountry(country).build();

		createDoormanCA(rootCertificate, new KeyPair(rootPublicKey, rootPrivateKey), certAlias, cordaX500Name);
		assertTrue(new File("doorman.jks").delete());
	}

	@Test
	public void testCreateNetworkMapCertificate() throws KeyStoreException, IOException, UnrecoverableKeyException,
			NoSuchAlgorithmException, CertificateException {
		KeyStore keyStore = loadKeyStore("root.jks", "keystorepass");

		X509Certificate rootCertificate = (X509Certificate) keyStore.getCertificate("cordarootca");
		PublicKey rootPublicKey = Crypto.toSupportedPublicKey(rootCertificate.getPublicKey());

		PrivateKey rootPrivateKey = (PrivateKey) keyStore.getKey("cordarootca", "keypass".toCharArray());
		rootPrivateKey = Crypto.toSupportedPrivateKey(rootPrivateKey);

		final String certAlias = "cordanetworkmap";

		final String commonName = "Corda Node Network Map";
		final String organisationUnit = "Organisation Unit";
		final String organisation = "Organisation";
		final String locality = "Locality";
		final String state = "State";
		final String country = "GB";

		CordaX500Name cordaX500Name = CordaX500NameBuilder.newInstance().setCommonName(commonName)
				.setOrganisationUnit(organisationUnit).setOrganisation(organisation).setLocality(locality)
				.setState(state).setCountry(country).build();

		createNetworkMapCertificate(rootCertificate, new KeyPair(rootPublicKey, rootPrivateKey), certAlias,
				cordaX500Name);
		assertTrue(new File("network-map.jks").delete());
	}

	@Test
	public void testCreateNetworkRootTruststore()
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		createNetworkRootTruststore("root.jks", "keystorepass");
		assertTrue(new File("network-root-truststore.jks").delete());
	}
}

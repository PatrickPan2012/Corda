package com.patrick;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kotlin.Pair;
import net.corda.core.identity.CordaX500Name;
import net.corda.nodeapi.internal.crypto.CertificateAndKeyPair;
import net.corda.nodeapi.internal.crypto.CertificateType;
import net.corda.nodeapi.internal.crypto.X509Utilities;

/**
 * 
 * @author Patrick Pan
 *
 */
public class CreateNodeCertificateTask implements Callable<Void> {

	private static final Logger LOGGER = LoggerFactory.getLogger("cordaDoormanLogger");

	private CSRResponse csrResponse = CSRResponse.getInstance();

	private CertificateAndKeyPair doormanCertAndKeyPair;
	private PKCS10CertificationRequest pkcs10Holder;
	private String id;

	private CreateNodeCertificateTask(CertificateAndKeyPair doormanCertAndKeyPair,
			PKCS10CertificationRequest pkcs10Holder, String id) {
		super();
		this.doormanCertAndKeyPair = doormanCertAndKeyPair;
		this.pkcs10Holder = pkcs10Holder;
		this.id = id;
	}

	@Override
	public Void call() throws Exception {
		LOGGER.debug("Start to create node certificate for id [{}]", id);
		try {
			createNodeCertificate();
		} catch (Exception e) {
			LOGGER.error("Failed to create certificate for CSR.", e);
		}
		LOGGER.debug("Finish creating node certificate for id [{}]", id);

		return null;
	}

	private void createNodeCertificate() throws IOException {
		PublicKey nodePublicKey = new JcaPEMKeyConverter().getPublicKey(pkcs10Holder.getSubjectPublicKeyInfo());

		X500Name x500Name = pkcs10Holder.getSubject();
		CordaX500Name name = x500NameToCordaX500Name(x500Name);

		X509Certificate certificate = createNodeCertificate(name, nodePublicKey);
		csrResponse.put(id, certificate);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private X509Certificate createNodeCertificate(CordaX500Name name, PublicKey publicKey) {
		return X509Utilities.createCertificate(CertificateType.NODE_CA, doormanCertAndKeyPair.getCertificate(),
				doormanCertAndKeyPair.getKeyPair(), name.getX500Principal(), publicKey,
				new Pair(Duration.ofMillis(0), Duration.ofDays(3650)), null);
	}

	private CordaX500Name x500NameToCordaX500Name(X500Name x500Name) {
		Map<ASN1ObjectIdentifier, ASN1Encodable> attributesMap = new HashMap<>();

		RDN[] rDNs = x500Name.getRDNs();

		for (RDN rDN : rDNs) {
			AttributeTypeAndValue[] attributeTypeAndValues = rDN.getTypesAndValues();
			for (AttributeTypeAndValue attributeTypeAndValue : attributeTypeAndValues) {
				ASN1ObjectIdentifier type = attributeTypeAndValue.getType();
				ASN1Encodable value = attributeTypeAndValue.getValue();

				if (attributesMap.put(type, value) != null) {
					LOGGER.warn("Duplicate attribute [{}].", type.getId());
				}
			}
		}

		String cn = Optional.ofNullable(attributesMap.get(BCStyle.CN)).map(Object::toString).orElse(null);
		String ou = Optional.ofNullable(attributesMap.get(BCStyle.OU)).map(Object::toString).orElse(null);
		String st = Optional.ofNullable(attributesMap.get(BCStyle.ST)).map(Object::toString).orElse(null);

		String o = Optional.ofNullable(attributesMap.get(BCStyle.O)).map(Object::toString)
				.orElseThrow(() -> new IllegalArgumentException("X500 name must have an organisation!"));
		String l = Optional.ofNullable(attributesMap.get(BCStyle.L)).map(Object::toString)
				.orElseThrow(() -> new IllegalArgumentException("X500 name must have a locality!"));
		String c = Optional.ofNullable(attributesMap.get(BCStyle.C)).map(Object::toString)
				.orElseThrow(() -> new IllegalArgumentException("X500 name must have a country!"));

		return new CordaX500Name(cn, ou, o, l, st, c);
	}

	public static CreateNodeCertificateTask newInstance(CertificateAndKeyPair doormanCertAndKeyPair,
			PKCS10CertificationRequest pkcs10Holder, String id) {
		return new CreateNodeCertificateTask(doormanCertAndKeyPair, pkcs10Holder, id);
	}
}

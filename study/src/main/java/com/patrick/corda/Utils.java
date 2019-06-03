package com.patrick.corda;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.X509Certificate;

import net.corda.core.crypto.Crypto;
import net.corda.core.internal.DigitalSignatureWithCert;
import net.corda.core.internal.SignedDataWithCert;
import net.corda.core.serialization.SerializationContext;
import net.corda.core.serialization.SerializationFactory;
import net.corda.core.serialization.SerializedBytes;
import net.corda.nodeapi.internal.crypto.CertificateAndKeyPair;

/**
 * 
 * @author Patrick Pan
 *
 */
public class Utils {

	private Utils() {
	}

	public static byte[] inputStreamToByteArray(String name) throws IOException {
		int hasRead = 0;
		byte[] bytes = new byte[1024];
		try (InputStream inputStream = Utils.class.getClassLoader().getResourceAsStream(name);
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			while ((hasRead = inputStream.read(bytes)) > 0) {
				byteArrayOutputStream.write(bytes, 0, hasRead);
			}

			return byteArrayOutputStream.toByteArray();
		}
	}

	/**
	 * @see the 389th line of InternalUtils.kt
	 * 
	 * @param obj
	 * @param serializationFactory
	 * @param context
	 * @param certificateAndKeyPair
	 * @return
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public static <T> SignedDataWithCert<T> signObject(T obj, SerializationFactory serializationFactory,
			SerializationContext context, CertificateAndKeyPair certificateAndKeyPair)
			throws InvalidKeyException, SignatureException {
		SerializedBytes<T> serialised = serializeObject(obj, serializationFactory, context);
		X509Certificate certificate = certificateAndKeyPair.getCertificate();
		PrivateKey privateKey = certificateAndKeyPair.getKeyPair().getPrivate();
		byte[] signature = Crypto.doSign(privateKey, serialised.getBytes());
		return new SignedDataWithCert<>(serialised, new DigitalSignatureWithCert(certificate, signature));
	}

	/**
	 * @see the 229th line of SerializationAPI.kt
	 * 
	 * @param obj
	 * @return
	 */
	public static <T> SerializedBytes<T> serializeObject(T obj, SerializationFactory serializationFactory,
			SerializationContext context) {
		return serializationFactory.serialize(obj, context);
	}
}

package com.patrick.corda.networkmap;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.patrick.corda.networkmap.core.NetworkMapCertificateManager;
import com.patrick.corda.networkmap.core.SerializationEnvironmentManager;

import io.netty.handler.codec.http.HttpHeaderValues;
import net.corda.core.crypto.Crypto;
import net.corda.core.crypto.SecureHash;
import net.corda.core.internal.DigitalSignatureWithCert;
import net.corda.core.internal.SignedDataWithCert;
import net.corda.core.serialization.SerializedBytes;
import net.corda.nodeapi.internal.crypto.CertificateAndKeyPair;

/**
 * 
 * @author Patrick Pan
 *
 */
public class Utils {

	private static final Logger LOGGER = LoggerFactory.getLogger("cordaNetworkMapLogger");
	private static final String CACHE_CONTROL_VALUE = String.format("public, max-age=%s", 10);

	private Utils() {
	}

	public static <T> SignedDataWithCert<T> signData(T obj) throws InvalidKeyException, SignatureException {
		SerializedBytes<T> serialised = SerializationEnvironmentManager.getInstance().serializeObjectOnContext(obj);
		CertificateAndKeyPair certificateAndKeyPair = NetworkMapCertificateManager.getInstance()
				.getNetworkMapCertificateAndKeyPair();
		X509Certificate certificate = certificateAndKeyPair.getCertificate();
		PrivateKey privateKey = certificateAndKeyPair.getKeyPair().getPrivate();
		byte[] signature = Crypto.doSign(privateKey, serialised.getBytes());
		return new SignedDataWithCert<>(serialised, new DigitalSignatureWithCert(certificate, signature));
	}

	public static void writeByteArrayToResponse(HttpServletResponse resp, byte[] byteArray) throws IOException {
		resp.getOutputStream().write(byteArray);
		resp.setContentType(HttpHeaderValues.APPLICATION_OCTET_STREAM.toString());
		resp.addHeader("Cache-Control", CACHE_CONTROL_VALUE);
	}

	public static String getHashString(String url) {
		int index = url.lastIndexOf('/');
		String raw = url.substring(index + 1);
		LOGGER.debug("The hash string is [{}].", raw);
		return SecureHash.parse(raw).toString();
	}
}

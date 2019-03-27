package com.patrick;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Patrick Pan
 *
 */
@WebServlet("/certificate/*")
public class GetNodeCertificate extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8881383068882166694L;
	private static final Logger LOGGER = LoggerFactory.getLogger("cordaDoormanLogger");

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		LOGGER.info("Receive a new request for downloading the certificate.");
		execute(req, resp);
	}

	private void execute(HttpServletRequest req, HttpServletResponse resp) {
		try {
			getCertificates(req, resp);
		} catch (IOException e) {
			LOGGER.error("Exception occurs in GetNodeCertificate.execute.", e);
			resp.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}

	private void getCertificates(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String id = getId(req.getRequestURI());
		LOGGER.debug("ID is [{}].", id);

		X509Certificate[] certificates = NodeCertificateManager.getInstance().doormanRetrieveCSRResponse(id);

		if (certificates.length == 0) {
			/*
			 * The poll interval can be controlled by the server returning a response with a
			 * "Cache-Control" header.
			 */
			resp.setStatus(HttpURLConnection.HTTP_NO_CONTENT);
			return;
		}

		writeResponse(resp, certificates);
	}

	private void writeResponse(HttpServletResponse resp, X509Certificate[] certificates) throws IOException {
		try {
			writeBytes(resp, certificates);
		} catch (CertificateEncodingException e) {
			LOGGER.error("Exception occurs in GetNodeCertificate.writeResponse.", e);
			resp.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}

	private void writeBytes(HttpServletResponse resp, X509Certificate[] certificates)
			throws IOException, CertificateEncodingException {
		byte[] bytes = certificatesToBytes(certificates);
		resp.setContentType("application/octet-stream");
		resp.setContentLength(bytes.length);
		ServletOutputStream outputStream = resp.getOutputStream();
		outputStream.write(bytes);
		outputStream.flush();
	}

	private String getId(String uri) {
		LOGGER.debug("Request URI is [{}].", uri);
		int lastIndex = uri.lastIndexOf('/');
		return uri.substring(lastIndex + 1);
	}

	private byte[] certificatesToBytes(X509Certificate[] certificates)
			throws IOException, CertificateEncodingException {
		byte[] bytes = null;

		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			writeCertificatesBytes(byteArrayOutputStream, certificates);
			bytes = byteArrayOutputStream.toByteArray();
		}

		return bytes;
	}

	private void writeCertificatesBytes(ByteArrayOutputStream byteArrayOutputStream, X509Certificate[] certificates)
			throws IOException, CertificateEncodingException {
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
			for (X509Certificate certificate : certificates) {
				zipOutputStream.putNextEntry(new ZipEntry(certificate.getSubjectX500Principal().getName()));
				byte[] bytes = certificate.getEncoded();
				zipOutputStream.write(bytes, 0, bytes.length);
				zipOutputStream.closeEntry();
			}
		}
	}
}

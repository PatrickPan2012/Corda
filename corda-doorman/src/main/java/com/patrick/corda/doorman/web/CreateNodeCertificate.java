package com.patrick.corda.doorman.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.patrick.corda.doorman.core.NodeCertificateManager;

/**
 * 
 * @author Patrick Pan
 *
 */
@WebServlet("/certificate")
public class CreateNodeCertificate extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -549055534232599477L;
	private static final Logger LOGGER = LoggerFactory.getLogger("cordaDoormanLogger");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		LOGGER.info("Receive a new request for creating a certificate.");
		execute(req, resp);
	}

	private void execute(HttpServletRequest req, HttpServletResponse resp) {
		try {
			createCertificate(req, resp);
		} catch (IOException e) {
			LOGGER.error("Exception occurs in CreateNodeCertificate.execute.", e);
			resp.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}

	private void createCertificate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String id = postCSR(streamToBytes(req.getInputStream()));
		LOGGER.debug("ID is [{}].", id);

		resp.setContentType("text/html;charset=UTF-8");
		PrintWriter pw = resp.getWriter();
		pw.println(id);
		pw.flush();
	}

	private String postCSR(byte[] pkcs10CertificationRequest) throws IOException {
		PKCS10CertificationRequest csr = new PKCS10CertificationRequest(pkcs10CertificationRequest);
		return NodeCertificateManager.getInstance().processCSR(csr);
	}

	private byte[] streamToBytes(InputStream inputStream) throws IOException {
		byte[] bytes = new byte[1024];
		int hasRead = -1;

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			while (0 < (hasRead = inputStream.read(bytes))) {
				outputStream.write(bytes, 0, hasRead);
			}

			return outputStream.toByteArray();
		}
	}
}

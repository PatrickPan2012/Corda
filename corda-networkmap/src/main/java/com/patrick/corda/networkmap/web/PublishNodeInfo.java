package com.patrick.corda.networkmap.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.patrick.corda.networkmap.core.DuplicateCordaX500NameException;
import com.patrick.corda.networkmap.core.NodeInfoHashManager;
import com.patrick.corda.networkmap.core.SerializationEnvironmentManager;

import net.corda.nodeapi.internal.SignedNodeInfo;

/**
 * For the node to upload its signed "NodeInfo" object to the network map.
 * 
 * @author Patrick Pan
 *
 */
@WebServlet("/network-map/publish")
public class PublishNodeInfo extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1577064056926226713L;
	private static final Logger LOGGER = LoggerFactory.getLogger("cordaNetworkMapLogger");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		LOGGER.info("Receive a new request for uploading a NodeInfo object.");
		publishSignedNodeInfo(req, resp);
	}

	private void publishSignedNodeInfo(HttpServletRequest req, HttpServletResponse resp) {
		try {
			SignedNodeInfo signedNodeInfo = deserializeSignedNodeInfoOnContext(streamToByteArray(req.getInputStream()));
			NodeInfoHashManager.getInstance().addNode(signedNodeInfo);
		} catch (IOException e) {
			LOGGER.error("This exception should not occur.", e);
			resp.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (DuplicateCordaX500NameException e) {
			LOGGER.error("The request is invalid!", e);
			resp.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
		} catch (Exception e) {
			LOGGER.error("Exception occurs in PublishNodeInfo.execute.", e);
			resp.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}

	private byte[] streamToByteArray(InputStream inputStream) throws IOException {
		byte[] bytes = new byte[1024];
		int hasRead = -1;

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			while (0 < (hasRead = inputStream.read(bytes))) {
				outputStream.write(bytes, 0, hasRead);
			}

			return outputStream.toByteArray();
		}
	}

	private SignedNodeInfo deserializeSignedNodeInfoOnContext(byte[] bytes) {
		return SerializationEnvironmentManager.getInstance().deserializeObjectOnContext(SignedNodeInfo.class, bytes);
	}
}

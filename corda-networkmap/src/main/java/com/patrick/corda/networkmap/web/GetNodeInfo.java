package com.patrick.corda.networkmap.web;

import static com.patrick.corda.networkmap.Utils.getHashString;
import static com.patrick.corda.networkmap.Utils.writeByteArrayToResponse;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.patrick.corda.networkmap.core.NodeInfoHashManager;
import com.patrick.corda.networkmap.core.SerializationEnvironmentManager;

import net.corda.nodeapi.internal.SignedNodeInfo;

/**
 * Retrieve a signed "NodeInfo" as specified in the network map object.
 * 
 * @author Patrick Pan
 *
 */
@WebServlet("/network-map/node-info/*")
public class GetNodeInfo extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3090076759409823593L;
	private static final Logger LOGGER = LoggerFactory.getLogger("cordaNetworkMapLogger");

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		LOGGER.info(
				"Receive a new request for retrieving a signed \"NodeInfo\" as specified in the network map object.");

		try {
			getSignedNodeInfo(req, resp);
		} catch (IllegalArgumentException e) {
			LOGGER.error("The request is invalid!", e);
			resp.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
		} catch (IOException e) {
			LOGGER.error("This exception should not occur.", e);
			resp.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (Exception e) {
			LOGGER.error("Unexpected exception occurs.", e);
			resp.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}

	private void getSignedNodeInfo(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String hash = getHashString(req.getRequestURI());
		SignedNodeInfo signedNodeInfo = NodeInfoHashManager.getInstance().getSignedNodeInfo(hash);
		writeByteArrayToResponse(resp,
				SerializationEnvironmentManager.getInstance().serializeObjectOnContext(signedNodeInfo).getBytes());
	}
}

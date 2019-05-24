package com.patrick.corda.networkmap.web;

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

import com.patrick.corda.networkmap.core.NetworkMapManager;

/**
 * Retrieve the current signed public network map object. The entire object is
 * signed with the network map certificate which is also attached.
 * 
 * @author Patrick Pan
 *
 */
@WebServlet("/network-map")
public class GetNetworkMap extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6976605309360281748L;
	private static final Logger LOGGER = LoggerFactory.getLogger("cordaNetworkMapLogger");

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		LOGGER.info("Receive a new request for retrieving the current signed public network map object.");
		try {
			getSignedNetworkMap(resp);
		} catch (IOException e) {
			LOGGER.error("This exception should not occur.", e);
			resp.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (Exception e) {
			LOGGER.error("Unexpected exception occurs.", e);
			resp.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}

	private void getSignedNetworkMap(HttpServletResponse resp) throws IOException {
		byte[] signedNetworkMapByteArray = NetworkMapManager.getInstance().getSignedNetworkMapByteArray();
		writeByteArrayToResponse(resp, signedNetworkMapByteArray);
	}
}

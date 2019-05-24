package com.patrick.corda.networkmap.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For the node operator to acknowledge network map that new parameters were
 * accepted forture update.
 * 
 * @author Patrick Pan
 *
 */
@WebServlet("/network-map/ack-parameters")
public class AckNetworkParameters extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3659930737716743214L;
	private static final Logger LOGGER = LoggerFactory.getLogger("cordaNetworkMapLogger");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		LOGGER.info("Receive a new request for acknowledging network map.");
	}
}

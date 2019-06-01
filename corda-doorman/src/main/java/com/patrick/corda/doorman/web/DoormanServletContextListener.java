package com.patrick.corda.doorman.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.patrick.corda.doorman.core.NodeCertificateManager;

/**
 * 
 * @author Patrick Pan
 *
 */
@WebListener
public class DoormanServletContextListener implements ServletContextListener {
	private NodeCertificateManager certificateManager = NodeCertificateManager.getInstance();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		certificateManager.init();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		certificateManager.destroy();
	}
}

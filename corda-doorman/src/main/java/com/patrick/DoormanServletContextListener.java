package com.patrick;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

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

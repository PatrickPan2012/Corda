package com.patrick.corda.networkmap.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.patrick.corda.networkmap.core.NetworkMapCertificateManager;
import com.patrick.corda.networkmap.core.SerializationEnvironmentManager;

/**
 * 
 * @author Patrick Pan
 *
 */
@WebListener
public class NetworkMapServletContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		NetworkMapCertificateManager.getInstance().init();
		SerializationEnvironmentManager.getInstance().init();
	}
}

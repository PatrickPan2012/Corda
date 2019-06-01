package com.patrick.corda.doorman.core;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Patrick Pan
 *
 */
public class CSRResponse {

	private Map<String, X509Certificate> certificateMap = new HashMap<>();

	public void put(String id, X509Certificate cer) {
		certificateMap.put(id, cer);
	}

	public X509Certificate get(String id) {
		return certificateMap.get(id);
	}

	private CSRResponse() {
	}

	public static CSRResponse getInstance() {
		return SingletonHelper.INSTANCE;
	}

	private static class SingletonHelper {
		private static final CSRResponse INSTANCE = new CSRResponse();
	}
}

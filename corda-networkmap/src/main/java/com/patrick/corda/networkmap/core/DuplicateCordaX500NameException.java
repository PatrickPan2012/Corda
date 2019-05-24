package com.patrick.corda.networkmap.core;

/**
 * 
 * @author Patrick Pan
 *
 */
public class DuplicateCordaX500NameException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4730572668651765546L;

	public DuplicateCordaX500NameException(String s) {
		super(s);
	}
}

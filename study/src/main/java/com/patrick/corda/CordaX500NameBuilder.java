package com.patrick.corda;

import net.corda.core.identity.CordaX500Name;

/**
 * 
 * @author Patrick Pan
 *
 */
public class CordaX500NameBuilder {

	private String commonName;
	private String organisationUnit;
	private String organisation;
	private String locality;
	private String state;
	private String country;

	private CordaX500NameBuilder() {
	}

	public CordaX500NameBuilder setCommonName(String commonName) {
		this.commonName = commonName;
		return this;
	}

	public CordaX500NameBuilder setOrganisationUnit(String organisationUnit) {
		this.organisationUnit = organisationUnit;
		return this;
	}

	public CordaX500NameBuilder setOrganisation(String organisation) {
		this.organisation = organisation;
		return this;
	}

	public CordaX500NameBuilder setLocality(String locality) {
		this.locality = locality;
		return this;
	}

	public CordaX500NameBuilder setState(String state) {
		this.state = state;
		return this;
	}

	public CordaX500NameBuilder setCountry(String country) {
		this.country = country;
		return this;
	}

	public CordaX500Name build() {
		return new CordaX500Name(commonName, organisationUnit, organisation, locality, state, country);
	}

	public static CordaX500NameBuilder newInstance() {
		return new CordaX500NameBuilder();
	}
}

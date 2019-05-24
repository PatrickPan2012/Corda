package com.patrick.corda.networkmap.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.patrick.corda.networkmap.core.NotaryManager;

import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.NotaryInfo;

/**
 * 
 * @author Patrick Pan
 *
 */
public class NotaryManagerTest {

	@Test
	public void testGetNotaryInfoList() {
		List<NotaryInfo> notaryInfoList = NotaryManager.getInstance().getNotaryInfoList();
		assertNotNull(notaryInfoList);
		assertEquals(2, notaryInfoList.size());

		Set<String> commonNames = new HashSet<>();
		commonNames.add("Common Name 1");
		commonNames.add("Common Name 2");

		for (NotaryInfo notaryInfo : notaryInfoList) {
			assertFalse(notaryInfo.getValidating());

			Party party = notaryInfo.getIdentity();

			assertNotNull(party.getOwningKey());

			CordaX500Name cordaX500Name = party.getName();

			assertTrue(commonNames.contains(cordaX500Name.getCommonName()));
			assertEquals("Organizational Unit", cordaX500Name.getOrganisationUnit());
			assertEquals("Organization", cordaX500Name.getOrganisation());
			assertEquals("Locality", cordaX500Name.getLocality());
			assertEquals("State", cordaX500Name.getState());
			assertEquals("GB", cordaX500Name.getCountry());
		}
	}

}

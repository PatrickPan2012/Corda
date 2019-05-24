package com.patrick.corda.networkmap.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.patrick.corda.networkmap.core.WhitelistedContractImplManager;

import net.corda.core.crypto.SecureHash;

/**
 * 
 * @author Patrick Pan
 *
 */
public class WhitelistedContractImplManagerTest {

	@Test
	public void testGetWhitelistedContractImplementations() {
		/**
		 * ContractImplA and ContractImplB simply implement the Corda Contract
		 * interface.
		 */
		final String CONTRACT_A = "com.patrick.corda.contract.ContractImplA";
		final String CONTRACT_B = "com.patrick.corda.contract.ContractImplB";

		/**
		 * ContractImplC extends an abstract class that implements the Corda Contract
		 * interface.
		 */
		final String CONTRACT_C = "com.patrick.corda.contract.ContractImplC";

		/**
		 * ContractImplD implements an interface that extends the Corda Contract
		 * interface.
		 */
		final String CONTRACT_D = "com.patrick.corda.contract.ContractImplD";

		Map<String, Integer> contractAndNumMap = new HashMap<>();

		/**
		 * ContractImplA is in both "contracts_1.jar" and "contracts_2.jar".
		 */
		contractAndNumMap.put(CONTRACT_A, 2);
		contractAndNumMap.put(CONTRACT_B, 1);
		contractAndNumMap.put(CONTRACT_C, 1);
		contractAndNumMap.put(CONTRACT_D, 1);

		Map<String, List<SecureHash>> whitelistedContractImplementations = WhitelistedContractImplManager.getInstance()
				.getWhitelistedContractImplementations();

		assertEquals(4, whitelistedContractImplementations.size());
		for (Map.Entry<String, List<SecureHash>> entry : whitelistedContractImplementations.entrySet()) {
			assertTrue(contractAndNumMap.containsKey(entry.getKey()));
			assertNotNull(entry.getValue());
			assertEquals(contractAndNumMap.get(entry.getKey()).intValue(), entry.getValue().size());
		}

		SecureHash jarHashB = whitelistedContractImplementations.get(CONTRACT_A).get(0);
		SecureHash jarHashC = whitelistedContractImplementations.get(CONTRACT_C).get(0);
		SecureHash jarHashD = whitelistedContractImplementations.get(CONTRACT_D).get(0);

		/**
		 * Both ContractImplB and ContractImplC are in "contracts_1.jar".
		 */
		assertTrue(jarHashB == jarHashC);

		/**
		 * ContractImplB is in "contracts_1.jar" but ContractImplD is in
		 * "contracts_2.jar".
		 */
		assertTrue(jarHashB != jarHashD);
	}
}

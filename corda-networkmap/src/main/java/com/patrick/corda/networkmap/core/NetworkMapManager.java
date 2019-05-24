package com.patrick.corda.networkmap.core;

import static com.patrick.corda.networkmap.Utils.signData;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.corda.core.crypto.SecureHash;
import net.corda.core.internal.SignedDataWithCert;
import net.corda.core.serialization.SerializedBytes;
import net.corda.nodeapi.internal.network.NetworkMap;
import net.corda.nodeapi.internal.network.ParametersUpdate;

/**
 * 
 * @author Patrick Pan
 *
 */
public class NetworkMapManager {

	private byte[] signedNetworkMapByteArray;

	private NetworkMapManager() {
		refreshNetworkMap();
	}

	private byte[] buildSignedNetworkMapByteArray() throws InvalidKeyException, SignatureException {
		NetworkMap networkMap = buildNetworkMap();

		/**
		 * @see the 16th line of NetworkMap.kt
		 */
		SignedDataWithCert<NetworkMap> signedNetworkMap = signNetworkMap(networkMap);
		return serializeSignedNetworkMapOnContext(signedNetworkMap).getBytes();
	}

	private SerializedBytes<SignedDataWithCert<NetworkMap>> serializeSignedNetworkMapOnContext(
			SignedDataWithCert<NetworkMap> signedNetworkMap) {
		return SerializationEnvironmentManager.getInstance().serializeObjectOnContext(signedNetworkMap);
	}

	private SignedDataWithCert<NetworkMap> signNetworkMap(NetworkMap networkMap)
			throws InvalidKeyException, SignatureException {
		return signData(networkMap);
	}

	private NetworkMap buildNetworkMap() {
		List<SecureHash> nodeInfoHashes = NodeInfoHashManager.getInstance().getSignedNodeInfoHashList();

		SecureHash networkParameterHash = NetworkParametersManager.getInstance().getSignedNetworkParametersHash();
		LOGGER.debug("The value of [networkParameterHash] is [{}].", networkParameterHash);

		ParametersUpdate parametersUpdate = null;
		return new NetworkMap(nodeInfoHashes, networkParameterHash, parametersUpdate);
	}

	public void refreshNetworkMap() {
		try {
			signedNetworkMapByteArray = buildSignedNetworkMapByteArray();
		} catch (InvalidKeyException | SignatureException e) {
			LOGGER.error("This exception should not occur.", e);
		} catch (Exception e) {
			LOGGER.error("Unexpected exception occurs.", e);
		}
	}

	public byte[] getSignedNetworkMapByteArray() {
		return signedNetworkMapByteArray;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger("cordaNetworkMapLogger");

	public static NetworkMapManager getInstance() {
		return SingletonHelper.INSTANCE;
	}

	private static class SingletonHelper {
		private static final NetworkMapManager INSTANCE = new NetworkMapManager();
	}
}

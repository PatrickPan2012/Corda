package com.patrick.corda.networkmap.core;

import static com.patrick.corda.networkmap.Utils.signData;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.corda.core.crypto.SecureHash;
import net.corda.core.internal.SignedDataWithCert;
import net.corda.core.node.NetworkParameters;
import net.corda.core.node.NotaryInfo;
import net.corda.core.serialization.SerializedBytes;

/**
 * 
 * @author Patrick Pan
 *
 */
public class NetworkParametersManager {

	private byte[] signedNetworkParametersByteArray;
	private SecureHash signedNetworkParametersHash;

	private NetworkParametersManager() {
		try {
			init();
		} catch (InvalidKeyException | SignatureException e) {
			LOGGER.error("This exception should not occur.", e);
		} catch (Exception e) {
			LOGGER.error("Unexpected exception occurs.", e);
		}
	}

	private void init() throws InvalidKeyException, SignatureException {
		NetworkParameters networkParameters = buildNetworkParameters();

		/**
		 * @see the 17th line of NetworkMap.kt
		 * @see the 20th line of NetworkParametersCopier.kt
		 */
		SignedDataWithCert<NetworkParameters> signedNetworkParameters = signNetworkParametersWithCert(
				networkParameters);

		/**
		 * @see the 112th line of io/cordite/networkmap/service/ServiceStorages.kt
		 */
		signedNetworkParametersHash = signedNetworkParameters.getRaw().getHash();

		/**
		 * @see the 339th line of io/cordite/networkmap/service/NetworkMapService.kt
		 */
		signedNetworkParametersByteArray = serializeSignedNetworkParameters(signedNetworkParameters).getBytes();
	}

	private SerializedBytes<SignedDataWithCert<NetworkParameters>> serializeSignedNetworkParameters(
			SignedDataWithCert<NetworkParameters> signedNetworkParameters) {
		return SerializationEnvironmentManager.getInstance().serializeObjectOnContext(signedNetworkParameters);
	}

	private SignedDataWithCert<NetworkParameters> signNetworkParametersWithCert(NetworkParameters networkParameters)
			throws InvalidKeyException, SignatureException {
		return signData(networkParameters);
	}

	private NetworkParameters buildNetworkParameters() {
		int minimumPlatformVersion = 3;
		List<NotaryInfo> notaries = NotaryManager.getInstance().getNotaryInfoList();

		/**
		 * This is currently ignored. However, it will be wired up in a future release
		 * according to Corda documents.
		 */
		int maxMessageSize = 10485760;
		int maxTransactionSize = Integer.MAX_VALUE;
		Instant modifiedTime = Instant.now();
		int epoch = 1;
		Map<String, List<SecureHash>> whitelistedContractImplementations = WhitelistedContractImplManager.getInstance()
				.getWhitelistedContractImplementations();
		Duration eventHorizon = Duration.ofDays(30);

		return new NetworkParameters(minimumPlatformVersion, notaries, maxMessageSize, maxTransactionSize, modifiedTime,
				epoch, whitelistedContractImplementations, eventHorizon);
	}

	public byte[] getSignedNetworkByteArray() {
		return signedNetworkParametersByteArray;
	}

	public SecureHash getSignedNetworkParametersHash() {
		return signedNetworkParametersHash;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger("cordaNetworkMapLogger");

	public static NetworkParametersManager getInstance() {
		return SingletonHelper.INSTANCE;
	}

	private static class SingletonHelper {
		private static final NetworkParametersManager INSTANCE = new NetworkParametersManager();
	}
}

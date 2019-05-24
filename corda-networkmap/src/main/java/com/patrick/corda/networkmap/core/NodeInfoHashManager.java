package com.patrick.corda.networkmap.core;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.corda.core.crypto.SecureHash;
import net.corda.core.crypto.SecureHash.SHA256;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.PartyAndCertificate;
import net.corda.core.node.NodeInfo;
import net.corda.core.serialization.SerializedBytes;
import net.corda.nodeapi.internal.SignedNodeInfo;
import net.corda.nodeapi.internal.crypto.CertificateAndKeyPair;
import net.corda.nodeapi.internal.crypto.X509Utilities;

/**
 * 
 * @author Patrick Pan
 *
 */
public class NodeInfoHashManager {

	private static final Lock LOCK = new ReentrantLock();
	private static final Logger LOGGER = LoggerFactory.getLogger("cordaNetworkMapLogger");

	private ConcurrentMap<String, SignedNodeInfo> map = new ConcurrentHashMap<>();

	public void addNode(SignedNodeInfo signedNodeInfo) throws DuplicateCordaX500NameException {
		NodeInfo nodeInfo = signedNodeInfo.verified();
		validateNodeInfoCertificate(nodeInfo);
		List<PartyAndCertificate> partyAndCertificateList = nodeInfo.getLegalIdentitiesAndCerts();

		LOCK.lock();
		try {
			checkPartyAndCertificateList(partyAndCertificateList);

			SerializedBytes<NodeInfo> raw = signedNodeInfo.getRaw();
			SHA256 sha256 = SecureHash.sha256(raw.getBytes());
			map.put(sha256.toString(), signedNodeInfo);
			NetworkMapManager.getInstance().refreshNetworkMap();
		} finally {
			LOCK.unlock();
		}
	}

	public List<SecureHash> getSignedNodeInfoHashList() {
		List<SecureHash> signedNodeInfoHashList = new ArrayList<>();

		LOCK.lock();
		try {
			map.keySet().forEach(hash -> signedNodeInfoHashList.add(SecureHash.parse(hash)));
		} finally {
			LOCK.unlock();
		}

		return signedNodeInfoHashList;
	}

	public SignedNodeInfo getSignedNodeInfo(String hash) {
		return map.get(hash);
	}

	private void validateNodeInfoCertificate(NodeInfo nodeInfo) {
		CertificateAndKeyPair rootCertificateAndKeyPair = NetworkMapCertificateManager.getInstance()
				.getRootCertificateAndKeyPair();

		List<PartyAndCertificate> partyAndCertificateList = nodeInfo.getLegalIdentitiesAndCerts();
		for (PartyAndCertificate partyAndCertificate : partyAndCertificateList) {
			X509Utilities.INSTANCE.validateCertPath(rootCertificateAndKeyPair.getCertificate(),
					partyAndCertificate.getCertPath());
		}
	}

	private void checkPartyAndCertificateList(List<PartyAndCertificate> unCheckedPartyAndCertificateList)
			throws DuplicateCordaX500NameException {
		Collection<SignedNodeInfo> registeredSignedNodeInfos = map.values();

		for (SignedNodeInfo registeredSignedNodeInfo : registeredSignedNodeInfos) {
			List<PartyAndCertificate> registeredPartyAndCertificateList = registeredSignedNodeInfo.verified()
					.getLegalIdentitiesAndCerts();

			for (PartyAndCertificate registeredPartyAndCertificate : registeredPartyAndCertificateList) {
				checkPartyAndCertificateList(unCheckedPartyAndCertificateList, registeredPartyAndCertificate);
			}
		}
	}

	private void checkPartyAndCertificateList(List<PartyAndCertificate> unCheckedPartyAndCertificateList,
			PartyAndCertificate registeredPartyAndCertificate) throws DuplicateCordaX500NameException {
		CordaX500Name registeredName = registeredPartyAndCertificate.getParty().getName();
		PublicKey registeredPublicKey = registeredPartyAndCertificate.getOwningKey();

		for (PartyAndCertificate unCheckedPartyAndCertificate : unCheckedPartyAndCertificateList) {
			CordaX500Name name = unCheckedPartyAndCertificate.getParty().getName();
			PublicKey unCheckedPublicKey = unCheckedPartyAndCertificate.getOwningKey();

			if (registeredName.equals(name) && !registeredPublicKey.equals(unCheckedPublicKey)) {
				String msg = String.format(
						"Failed to register the node because name [%s] has already been registered with a different public key.",
						name);
				LOGGER.warn(msg);
				throw new DuplicateCordaX500NameException(msg);
			}
		}
	}

	private NodeInfoHashManager() {
	}

	public static NodeInfoHashManager getInstance() {
		return SingletonHelper.INSTANCE;
	}

	private static class SingletonHelper {
		private static final NodeInfoHashManager INSTANCE = new NodeInfoHashManager();
	}
}

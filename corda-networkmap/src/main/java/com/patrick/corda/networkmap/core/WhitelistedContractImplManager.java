package com.patrick.corda.networkmap.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import net.corda.core.contracts.Contract;
import net.corda.core.crypto.SecureHash;

/**
 * 
 * @author Patrick Pan
 *
 */
public class WhitelistedContractImplManager {

	/**
	 * @see the 12th line of AttachmentStorage.kt
	 * @see the 111th line of MapSerializer.kt
	 */
	private Map<String, List<SecureHash>> whitelistedContractImplementations = new LinkedHashMap<>();

	private WhitelistedContractImplManager() {
		try {
			initWhitelistedContractImplementations();
		} catch (IOException | URISyntaxException e) {
			LOGGER.error("This exception should not occur.", e);
		} catch (Exception e) {
			LOGGER.error("Unexpected exception occurs.", e);
		}
	}

	private void initWhitelistedContractImplementations() throws IOException, URISyntaxException {
		URL url = NotaryManager.class.getClassLoader().getResource("cordapps");
		if (Objects.isNull(url)) {
			LOGGER.warn(
					"The resource [cordapps] could not be found or the process doesn't have adequate  privileges to get the resource.");
			return;
		}

		File cordappDirectory = new File(url.getPath());

		if (!cordappDirectory.isDirectory()) {
			LOGGER.error("The resource [cordapps] must be a directory instead of a file.");
			return;
		}

		File[] cordapps = cordappDirectory.listFiles();

		if (Objects.isNull(cordapps) || cordapps.length == 0) {
			LOGGER.warn(
					"The directory 'cordapps' is empty but whitelist of contract implementations should not be empty. Otherwise, all contract implementations could be executed.");
			return;
		}

		initWhitelistedContractImplementations(cordapps);
	}

	private void initWhitelistedContractImplementations(File[] cordapps) throws IOException, URISyntaxException {
		for (File cordapp : cordapps) {
			SecureHash jarHash = getJarHash(cordapp);
			List<String> contracts = scanJarForContracts(cordapp);

			if (!contracts.isEmpty() && LOGGER.isDebugEnabled()) {
				LOGGER.debug("The Corda Contract implementations are [{}] and jar hash is [{}].",
						String.join(",", contracts), jarHash);
			}

			initWhitelistedContractImplementations(jarHash, contracts);
		}
	}

	private void initWhitelistedContractImplementations(SecureHash jarHash, List<String> contracts) {
		for (String contract : contracts) {
			List<SecureHash> jarHashList = whitelistedContractImplementations.get(contract);

			if (Objects.isNull(jarHashList)) {
				jarHashList = new ArrayList<>();
				whitelistedContractImplementations.put(contract, jarHashList);
			}

			jarHashList.add(jarHash);
		}
	}

	/**
	 * @see the 252nd line of NetworkBootstrapper
	 * @param cordapp
	 * @return
	 * @throws IOException
	 */
	private SecureHash getJarHash(File cordapp) throws IOException {
		try (InputStream inputStream = new FileInputStream(cordapp);
				HashingInputStream hashingInputStream = new HashingInputStream(Hashing.sha256(), inputStream)) {
			if (hashingInputStream.read(new byte[hashingInputStream.available()]) < 0) {
				LOGGER.warn("Fail to read [{}].", cordapp.getAbsolutePath());
			}
			return new SecureHash.SHA256(hashingInputStream.hash().asBytes());
		}
	}

	/**
	 * Scans the jar for contracts.
	 * 
	 * @see the 21st line of ClassloaderUtils.kt
	 * @param cordapp
	 * @return found contract class names or empty if none found
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private List<String> scanJarForContracts(File cordapp) throws URISyntaxException, IOException {
		ClassLoader currentClassLoader = Contract.class.getClassLoader();

		List<String> contracts = buildContractImplList(cordapp, currentClassLoader);
		List<String> distinctContracts = contracts.stream().distinct().collect(Collectors.toList());

		try (URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { cordapp.toURI().toURL() },
				currentClassLoader)) {
			removeUninstantiableContracts(distinctContracts, urlClassLoader);
		}

		return distinctContracts;
	}

	private List<String> buildContractImplList(File cordapp, ClassLoader currentClassLoader) throws URISyntaxException {
		URI sourceCodeURI = Contract.class.getProtectionDomain().getCodeSource().getLocation().toURI();
		ScanResult scanResult = new FastClasspathScanner().addClassLoader(currentClassLoader)
				.overrideClasspath(cordapp.getAbsolutePath(), Paths.get(sourceCodeURI).toString()).scan();
		return scanResult.getNamesOfClassesImplementing(Contract.class.getName());
	}

	/**
	 * Only keep instantiable contracts.
	 * 
	 * @param contracts
	 * @param urlClassLoader
	 */
	private void removeUninstantiableContracts(List<String> contracts, URLClassLoader urlClassLoader) {
		contracts.removeIf(contract -> {
			try {
				Class<?> clazz = urlClassLoader.loadClass(contract);
				return clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers());
			} catch (ClassNotFoundException e) {
				LOGGER.error("The class file is probably deleted illegally.", e);
				return true;
			}
		});
	}

	public Map<String, List<SecureHash>> getWhitelistedContractImplementations() {
		return whitelistedContractImplementations;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger("cordaNetworkMapLogger");

	public static WhitelistedContractImplManager getInstance() {
		return SingletonHelper.INSTANCE;
	}

	private static class SingletonHelper {
		private static final WhitelistedContractImplManager INSTANCE = new WhitelistedContractImplManager();
	}
}

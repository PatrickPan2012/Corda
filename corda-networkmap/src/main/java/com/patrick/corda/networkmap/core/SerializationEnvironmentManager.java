package com.patrick.corda.networkmap.core;

import net.corda.client.rpc.internal.KryoClientSerializationScheme;
import net.corda.core.serialization.SerializationContext;
import net.corda.core.serialization.SerializationDefaults;
import net.corda.core.serialization.SerializationFactory;
import net.corda.core.serialization.SerializedBytes;
import net.corda.core.serialization.internal.SerializationEnvironmentImpl;
import net.corda.core.serialization.internal.SerializationEnvironmentKt;
import net.corda.core.utilities.ByteSequence;
import net.corda.nodeapi.internal.serialization.SerializationFactoryImpl;
import net.corda.nodeapi.internal.serialization.SharedContexts;
import net.corda.nodeapi.internal.serialization.amqp.AMQPServerSerializationScheme;

/**
 * 
 * @author Patrick Pan
 *
 */
public class SerializationEnvironmentManager {

	/**
	 * @see the 61th line of io/cordite/networkmap/serialisation/SerializationEnvironment.kt
	 */
	public void init() {
		SerializationFactoryImpl serializationFactoryImpl = new SerializationFactoryImpl();
		serializationFactoryImpl.registerScheme(new KryoClientSerializationScheme());
		serializationFactoryImpl.registerScheme(new AMQPServerSerializationScheme());

		SerializationContext rpcServerContext = null;
		SerializationContext rpcClientContext = null;
		SerializationContext storageContext = null;
		SerializationContext checkpointContext = null;
		SerializationEnvironmentImpl serializationEnvironmentImpl = new SerializationEnvironmentImpl(
				serializationFactoryImpl, SharedContexts.getAMQP_P2P_CONTEXT(), rpcServerContext, rpcClientContext,
				storageContext, checkpointContext);

		SerializationEnvironmentKt.setNodeSerializationEnv(serializationEnvironmentImpl);
	}

	/**
	 * @see the 87th line of io/cordite/networkmap/serialisation/SerializationEnvironment.kt
	 * @see the 229th line of net/corda/core/serialization/SerializationAPI.kt
	 * 
	 * @param obj
	 * @return
	 */
	public <T> SerializedBytes<T> serializeObjectOnContext(T obj) {
		SerializationFactory serializationFactory = SerializationFactory.Companion.getDefaultFactory();
		return serializationFactory.withCurrentContext(SerializationDefaults.INSTANCE.getP2P_CONTEXT(),
				() -> serializationFactory.serialize(obj, serializationFactory.getDefaultContext()));
	}

	/**
	 * @see the 99th line of io/cordite/networkmap/serialisation/SerializationEnvironment.kt
	 * @see the 212th line of net/corda/core/serialization/SerializationAPI.kt
	 * 
	 * @param clazz
	 * @param bytes
	 * @return
	 */
	public <T> T deserializeObjectOnContext(Class<T> clazz, byte[] bytes) {
		SerializationFactory serializationFactory = SerializationFactory.Companion.getDefaultFactory();
		return serializationFactory.withCurrentContext(SerializationDefaults.INSTANCE.getP2P_CONTEXT(),
				() -> serializationFactory.deserialize(ByteSequence.of(bytes), clazz,
						serializationFactory.getDefaultContext()));
	}

	private SerializationEnvironmentManager() {
	}

	public static SerializationEnvironmentManager getInstance() {
		return SingletonHelper.INSTANCE;
	}

	private static class SingletonHelper {
		private static final SerializationEnvironmentManager INSTANCE = new SerializationEnvironmentManager();
	}
}

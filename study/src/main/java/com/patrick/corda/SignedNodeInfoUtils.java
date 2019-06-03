package com.patrick.corda;

import com.esotericsoftware.kryo.pool.KryoPool;

import net.corda.core.serialization.SerializationContext;
import net.corda.core.serialization.SerializationFactory;
import net.corda.core.serialization.SerializationContext.UseCase;
import net.corda.core.serialization.internal.SerializationEnvironment;
import net.corda.core.serialization.internal.SerializationEnvironmentImpl;
import net.corda.core.serialization.internal.SerializationEnvironmentKt;
import net.corda.core.utilities.ByteSequence;
import net.corda.nodeapi.internal.SignedNodeInfo;
import net.corda.nodeapi.internal.serialization.SerializationFactoryImpl;
import net.corda.nodeapi.internal.serialization.SharedContexts;
import net.corda.nodeapi.internal.serialization.amqp.AMQPServerSerializationScheme;
import net.corda.nodeapi.internal.serialization.kryo.AbstractKryoSerializationScheme;
import net.corda.nodeapi.internal.serialization.kryo.KryoSerializationSchemeKt;

/**
 * 
 * @author Patrick Pan
 *
 */
public class SignedNodeInfoUtils {

	private SignedNodeInfoUtils() {
	}

	/**
	 * NetworkBootstrapper.bootstrap(72) => NetworkBootstrapper.initialiseSerialization(283)
	 * NetworkBootstrapper.bootstrap(72) => NetworkBootstrapper.gatherNotaryInfos(183)
	 * 
	 * @param byteArray
	 * @return
	 */
	public static SignedNodeInfo deserializeSignedNodeInfo(byte[] byteArray) {
		SerializationFactoryImpl serializationFactoryImpl = new SerializationFactoryImpl();
		// KryoParametersSerializationScheme
		serializationFactoryImpl.registerScheme(new AbstractKryoSerializationScheme() {

			@Override
			public boolean canDeserializeVersion(ByteSequence byteSequence, UseCase target) {
				return byteSequence == KryoSerializationSchemeKt.getKryoHeaderV0_1()
						&& target == SerializationContext.UseCase.P2P;
			}

			@Override
			protected KryoPool rpcClientKryoPool(SerializationContext arg0) {
				throw new UnsupportedOperationException();
			}

			@Override
			protected KryoPool rpcServerKryoPool(SerializationContext arg0) {
				throw new UnsupportedOperationException();
			}
		});
		// AMQPServerSerializationScheme
		serializationFactoryImpl.registerScheme(new AMQPServerSerializationScheme());

		SerializationEnvironmentImpl serializationEnvironmentImpl = new SerializationEnvironmentImpl(
				serializationFactoryImpl, SharedContexts.getAMQP_P2P_CONTEXT(), null, null, null, null);

		SerializationEnvironment serializationEnvironment = serializationEnvironmentImpl;
		SerializationEnvironmentKt.get_contextSerializationEnv().set(serializationEnvironment);

		SerializationFactory serializationFactory = SerializationFactory.Companion.getDefaultFactory();
		return serializationFactory.deserialize(ByteSequence.of(byteArray), SignedNodeInfo.class,
				serializationFactory.getDefaultContext());

	}
}

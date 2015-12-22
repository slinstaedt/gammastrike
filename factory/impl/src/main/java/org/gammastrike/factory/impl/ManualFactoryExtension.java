package org.gammastrike.factory.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import org.gammastrike.factory.api.annotation.ManualFactory;

/**
 * {@link Extension} for handling {@link ManualFactoryBinding} types.
 *
 * @author sven.linstaedt
 */
public class ManualFactoryExtension implements Extension {

	private final Set<AnnotatedType<?>> manualFactoryTypes;
	private final Map<Method, MethodParameterMetadata> methodParameterMetadata;

	private MethodParameterMetadataBuilder metadataBuilder;

	public ManualFactoryExtension() {
		this.manualFactoryTypes = new HashSet<>();
		this.methodParameterMetadata = new HashMap<>();
	}

	void buildMethodMetadata(@Observes AfterDeploymentValidation event) {
		Set<MethodParameterMetadata> metadatas = metadataBuilder.buildMethodParameterMetadatas(event);
		for (MethodParameterMetadata metadata : metadatas) {
			methodParameterMetadata.put(metadata.getMethod().getJavaMember(), metadata);
		}
		metadataBuilder = null;
	}

	void findUserFactoryBindings(@Observes @WithAnnotations(ManualFactory.class) ProcessAnnotatedType<?> event) {
		if (event.getAnnotatedType().isAnnotationPresent(ManualFactory.class)) {
			manualFactoryTypes.add(event.getAnnotatedType());
		}
	}

	public MethodParameterMetadata lookupMetadata(Method method) {
		MethodParameterMetadata metadata = methodParameterMetadata.get(method);
		if (metadata == null) {
			throw new IllegalArgumentException("Not a factory method: " + method);
		}
		return metadata;
	}

	void onShutdown(@Observes BeforeShutdown event) {
		methodParameterMetadata.clear();
	}

	void registerParameterBeans(@Observes AfterBeanDiscovery event, BeanManager manager) {
		metadataBuilder = new MethodParameterMetadataBuilder(manager);
		for (AnnotatedType<?> factoryType : manualFactoryTypes) {
			for (AnnotatedMethod<?> factoryMethod : factoryType.getMethods()) {
				Method method = factoryMethod.getJavaMember();
				if (!Modifier.isAbstract(method.getModifiers())) {
					continue;
				}
				metadataBuilder.registerParameterBeans(event, factoryMethod);
			}
		}

		manualFactoryTypes.clear();
	}
}

package org.gammastrike.factory.impl;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.gammastrike.factory.api.annotation.Destroy;
import org.gammastrike.factory.api.annotation.Manual;
import org.gammastrike.util.AnnotatedMetadataReader;

/**
 * Builder for {@link MethodParameterMetadata}.
 *
 * @author sven.linstaedt
 */
public class MethodParameterMetadataBuilder {

	private final BeanManager beanManager;
	private final Map<AnnotatedMetadataReader<?>, Bean<?>> existingParameterBeans;
	private final Map<AnnotatedMethod<?>, List<Bean<?>>> methodParameterBeans;

	public MethodParameterMetadataBuilder(BeanManager beanManager) {
		this.beanManager = requireNonNull(beanManager);
		this.existingParameterBeans = new HashMap<>();
		this.methodParameterBeans = new HashMap<>();
	}

	public Set<MethodParameterMetadata> buildMethodParameterMetadatas(AfterDeploymentValidation validation) {
		Set<MethodParameterMetadata> metadata = new HashSet<>();
		for (Entry<AnnotatedMethod<?>, List<Bean<?>>> entry : methodParameterBeans.entrySet()) {
			AnnotatedMethod<?> method = entry.getKey();
			List<Bean<?>> parameterBeans = entry.getValue();
			Bean<?> returnBean = findReturnBean(validation, method, parameterBeans);
			metadata.add(new MethodParameterMetadata(method, returnBean, parameterBeans));
		}
		methodParameterBeans.clear();
		return metadata;
	}

	protected <T> Bean<T> createParameterBean(AfterBeanDiscovery discovery, AnnotatedParameter<?> parameter) {
		AnnotatedMetadataReader<T> reader = AnnotatedMetadataReader.<T> create(beanManager, parameter);
		@SuppressWarnings("unchecked")
		Bean<T> parameterBean = (Bean<T>) existingParameterBeans.get(reader);
		if (parameterBean != null) {
			return parameterBean;
		}

		BeanBuilder<T> builder = reader.buildBean(beanManager);
		builder.toString(ManualFactoryExtension.class.getName() + ": " + parameter);
		if (parameter.isAnnotationPresent(Destroy.class)) {
			builder.beanLifecycle(VoidLifecycle.<T> instance());
			parameterBean = builder.create();
		} else {
			builder.beanLifecycle(new ManualParameterLifecycle<T>());
			if (!parameter.isAnnotationPresent(Manual.class)) {
				builder.addQualifier(Manual.Literal.DEFAULT);
			}
			parameterBean = builder.create();
			discovery.addBean(parameterBean);
		}
		existingParameterBeans.put(reader, parameterBean);

		return parameterBean;
	}

	protected Bean<?> findReturnBean(AfterDeploymentValidation validation, AnnotatedMethod<?> method, List<Bean<?>> parameterBeans) {
		AnnotatedMetadataReader<Object> reader = AnnotatedMetadataReader.create(beanManager, method);
		if (method.getBaseType() == void.class || method.getBaseType() == Void.class) {
			return reader.buildBean(beanManager).beanLifecycle(VoidLifecycle.instance()).create();
		}

		Set<Bean<?>> beans = reader.getBeans(beanManager);
		Iterator<Bean<?>> iterator = beans.iterator();
		while (iterator.hasNext()) {
			iterateReturnCandidates: for (InjectionPoint ip : iterator.next().getInjectionPoints()) {
				Set<Bean<?>> injectionCandidates = AnnotatedMetadataReader.create(beanManager, ip).getBeans(beanManager);
				if (injectionCandidates.isEmpty()) {
					iterator.remove();
					break iterateReturnCandidates;
				}
				for (Bean<?> injectionCandidate : injectionCandidates) {
					for (Annotation qualifier : injectionCandidate.getQualifiers()) {
						if (qualifier.annotationType() == Manual.class && !parameterBeans.contains(injectionCandidate)) {
							iterator.remove();
							break iterateReturnCandidates;
						}
					}
				}
			}
		}

		switch (beans.size()) {
		case 0:
			validation.addDeploymentProblem(new UnsatisfiedResolutionException("No beans found for satisfiying return type of \n" + method
					+ "\nfor parameters " + parameterBeans));
			return reader.buildBean(beanManager).create();
		case 1:
			return beans.iterator().next();
		default:
			validation.addDeploymentProblem(new AmbiguousResolutionException("Mutiple beans found for satisfiying return type of \n" + method
					+ "\nfor parameters " + parameterBeans + ":\nBeans: " + beans));
			return reader.buildBean(beanManager).create();
		}
	}

	public void registerParameterBeans(AfterBeanDiscovery discovery, AnnotatedMethod<?> method) {
		List<Bean<?>> parameterBeans = new ArrayList<>(method.getParameters().size());
		for (AnnotatedParameter<?> parameter : method.getParameters()) {
			Bean<?> parameterBean = createParameterBean(discovery, parameter);
			parameterBeans.add(parameterBean);
		}
		methodParameterBeans.put(method, parameterBeans);
	}
}

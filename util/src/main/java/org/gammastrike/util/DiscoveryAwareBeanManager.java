package org.gammastrike.util;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ObserverMethod;

/**
 * Wrapper for {@link BeanManager} and {@link AfterBeanDiscovery}, that enables the lookup of via AfterBeanDiscovery added beans from the BeanManager.
 *
 * @author sven.linstaedt
 */
public class DiscoveryAwareBeanManager extends BeanManagerDecorator implements AfterBeanDiscovery {

	private static final long serialVersionUID = 1L;

	private final BeanManager manager;
	private final AfterBeanDiscovery discovery;
	private final Map<AnnotatedMetadataReader<?>, Bean<?>> registeredBeans;

	public DiscoveryAwareBeanManager(BeanManager manager, AfterBeanDiscovery discovery) {
		this.manager = requireNonNull(manager);
		this.discovery = requireNonNull(discovery);
		this.registeredBeans = new HashMap<>();
	}

	@Override
	public void addBean(Bean<?> bean) {
		AnnotatedMetadataReader<?> reader = AnnotatedMetadataReader.create(manager, bean);
		if (!registeredBeans.containsKey(reader)) {
			registeredBeans.put(reader, bean);
			discovery.addBean(bean);
		}
	}

	@Override
	public void addContext(Context context) {
		discovery.addContext(context);
	}

	@Override
	public void addDefinitionError(Throwable t) {
		discovery.addDefinitionError(t);
	}

	@Override
	public void addObserverMethod(ObserverMethod<?> observerMethod) {
		discovery.addObserverMethod(observerMethod);
	}

	@Override
	public BeanManager delegate() {
		return manager;
	}

	@Override
	public <T> AnnotatedType<T> getAnnotatedType(Class<T> type, String id) {
		return discovery.getAnnotatedType(type, id);
	}

	@Override
	public <T> Iterable<AnnotatedType<T>> getAnnotatedTypes(Class<T> type) {
		return discovery.getAnnotatedTypes(type);
	}

	@Override
	public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
		Set<Bean<?>> beans = new HashSet<>(manager.getBeans(beanType, qualifiers));
		AnnotatedMetadataReader<?> reader = AnnotatedMetadataReader.create(beanType, qualifiers);
		if (registeredBeans.containsKey(reader)) {
			beans.add(registeredBeans.get(reader));
		}
		return beans;
	}
}

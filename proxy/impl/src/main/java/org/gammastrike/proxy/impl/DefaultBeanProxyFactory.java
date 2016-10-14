package org.gammastrike.proxy.impl;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;

import org.gammastrike.proxy.api.BeanProxyFactory;
import org.gammastrike.proxy.api.BeanRuntimeInformationProvider;
import org.gammastrike.proxy.api.Proxy;
import org.gammastrike.proxy.api.ProxyFactory;
import org.gammastrike.value.TypeClosure;

public class DefaultBeanProxyFactory implements BeanProxyFactory {

	private static final List<Class<?>> STATIC_PROXY_TYPES = Arrays.asList(Proxy.class, Serializable.class);

	private final ProxyFactory proxyFactory;
	private final BeanRuntimeInformationProvider informationProvider;

	DefaultBeanProxyFactory() {
		proxyFactory = null;
		informationProvider = null;
	}

	@Inject
	public DefaultBeanProxyFactory(ProxyFactory proxyFactory, BeanRuntimeInformationProvider informationProvider) {
		this.proxyFactory = requireNonNull(proxyFactory);
		this.informationProvider = requireNonNull(informationProvider);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T createBeanProxy(Bean<T> bean, InvocationHandler handler) {
		ClassLoader classLoader = informationProvider.provideClassLoader(bean);
		TypeClosure typeClosure = informationProvider.extractImplementingClasses(bean).withTypes(STATIC_PROXY_TYPES);
		return (T) proxyFactory.createProxy(classLoader, handler, typeClosure.toClassArray());
	}
}

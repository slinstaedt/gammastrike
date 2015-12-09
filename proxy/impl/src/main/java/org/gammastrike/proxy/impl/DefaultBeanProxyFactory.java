package org.gammastrike.proxy.impl;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.InvocationHandler;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;

import org.gammastrike.proxy.api.BeanProxyFactory;
import org.gammastrike.proxy.api.BeanRuntimeInformationProvider;
import org.gammastrike.proxy.api.ProxyFactory;

public class DefaultBeanProxyFactory implements BeanProxyFactory {

	private final ProxyFactory proxyFactory;
	private final BeanRuntimeInformationProvider informationProvider;
	private final Instance<InvocationHandler> handlerInstance;

	DefaultBeanProxyFactory() {
		proxyFactory = null;
		informationProvider = null;
		handlerInstance = null;
	}

	@Inject
	public DefaultBeanProxyFactory(ProxyFactory proxyFactory, BeanRuntimeInformationProvider informationProvider, Instance<InvocationHandler> handlerInstance) {
		this.proxyFactory = requireNonNull(proxyFactory);
		this.informationProvider = requireNonNull(informationProvider);
		this.handlerInstance = requireNonNull(handlerInstance);
	}

	@Override
	public <T> T createBeanProxy(Contextual<T> contextual, CreationalContext<T> creationalContext) {
		if (!(contextual instanceof Bean)) {
			throw new UnsupportedOperationException("Can not handle contextual: " + contextual);
		}

		Bean<T> bean = (Bean<T>) contextual;
		ClassLoader classLoader = informationProvider.provideClassLoader(bean);
		Class<?>[] proxyClasses = informationProvider.extractImplementingClasses(bean);
		InvocationHandler invocationHandler = handlerInstance.get();

		@SuppressWarnings("unchecked")
		T proxy = (T) proxyFactory.createProxy(classLoader, invocationHandler, proxyClasses);
		return proxy;
	}
}

package org.gammastrike.store.impl;

import java.lang.annotation.Annotation;

import javax.annotation.PreDestroy;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;

import org.gammastrike.proxy.api.ProxyFactory;
import org.gammastrike.store.api.annotation.Proxied;

public class BeanProxyingContext implements Context {

	private ProxyFactory proxyFactory;

	@PreDestroy
	void destroy() {
		this.proxyFactory = null;
	}

	@Override
	public <T> T get(Contextual<T> contextual) {
		return get(contextual, null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
		Bean<?> bean = (Bean<?>) contextual;
		ClassLoader classLoader = bean.getBeanClass().getClassLoader();
		BeanProxyInvocationHandler handler = new BeanProxyInvocationHandler();
		Class<?>[] proxyClasses = bean.getTypes().toArray(new Class<?>[bean.getTypes().size()]);
		T proxy = (T) proxyFactory.createProxy(classLoader, handler, proxyClasses);
		return proxy;
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return Proxied.class;
	}

	@Inject
	void initWithProxyFactory(ProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}

	@Override
	public boolean isActive() {
		return proxyFactory != null;
	}
}

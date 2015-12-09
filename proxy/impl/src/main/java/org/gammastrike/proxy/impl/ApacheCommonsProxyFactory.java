package org.gammastrike.proxy.impl;

import java.lang.reflect.InvocationHandler;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.proxy.invoker.InvocationHandlerAdapter;
import org.gammastrike.proxy.api.ProxyFactory;

@ApplicationScoped
public class ApacheCommonsProxyFactory implements ProxyFactory {

	private final org.apache.commons.proxy.ProxyFactory delegate;

	public ApacheCommonsProxyFactory() {
		delegate = new org.apache.commons.proxy.ProxyFactory();
	}

	@Override
	public Object createProxy(ClassLoader classLoader, InvocationHandler handler, Class<?>[] proxyClasses) {
		InvocationHandlerAdapter invoker = new InvocationHandlerAdapter(handler);
		return delegate.createInvokerProxy(classLoader, invoker, proxyClasses);
	}
}

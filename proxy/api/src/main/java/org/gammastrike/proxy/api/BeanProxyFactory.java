package org.gammastrike.proxy.api;

import java.lang.reflect.InvocationHandler;

import javax.enterprise.inject.spi.Bean;

public interface BeanProxyFactory {

	<T> T createBeanProxy(Bean<T> bean, InvocationHandler handler);
}
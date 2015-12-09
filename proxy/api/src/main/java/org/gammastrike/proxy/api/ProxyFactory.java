package org.gammastrike.proxy.api;

import java.lang.reflect.InvocationHandler;

public interface ProxyFactory {

	Object createProxy(ClassLoader classLoader, InvocationHandler handler, Class<?>[] proxyClasses);
}

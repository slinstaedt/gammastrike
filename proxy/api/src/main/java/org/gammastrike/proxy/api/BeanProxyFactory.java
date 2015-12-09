package org.gammastrike.proxy.api;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

public interface BeanProxyFactory {

	<T> T createBeanProxy(Contextual<T> contextual, CreationalContext<T> creationalContext);
}
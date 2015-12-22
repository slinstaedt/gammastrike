package org.gammastrike.store.impl;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import org.gammastrike.store.api.BeanIdentifier;
import org.gammastrike.store.api.BeanStore;

public class BeanProxyInvocationHandler implements InvocationHandler, Serializable {

	private static final long serialVersionUID = 1L;

	private final BeanIdentifier identifier;

	public BeanProxyInvocationHandler() {
		this.identifier = null;
	}

	public BeanProxyInvocationHandler(BeanIdentifier identifier) {
		this.identifier = identifier;
	}

	protected BeanManager getBeanManager() {
		return CDI.current().getBeanManager();
	}

	protected Instance<BeanStore> getBeanStores() {
		return CDI.current().select(BeanStore.class);
	}

	private Object invoke(Method method, Object[] args, Bean<?> bean, BeanIdentifier identifier) throws Exception {
		Annotation[] qualifiers = identifier.getQualifiers();
		Object instance = getBeanStores().select(qualifiers).get().get(identifier).getInstance();
		return method.invoke(instance, args);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Class<?> returnType = method.getReturnType();
		if (identifier == null && returnType != void.class && returnType != Void.class) {
			throw new IllegalStateException("Could not intercept method call to " + method + ", because it has no void return type");
		}

		Bean<?> proxiedBean = proxiedBeanFor(method);
		if (proxiedBean == null) {
			throw new IllegalArgumentException("Could not resolve bean for invoked type of " + method);
		}

		Object result = null;
		Collection<Exception> occuredExceptions = new HashSet<>(2);
		if (identifier != null) {
			try {
				result = invoke(method, args, proxiedBean, identifier);
			} catch (Exception exception) {
				occuredExceptions.add(exception);
			}
		} else {
			Iterator<BeanStore> storeIterator = getBeanStores().iterator();
			while (storeIterator.hasNext()) {
				Iterator<BeanIdentifier> identifierIterator = storeIterator.next().iterator();
				while (identifierIterator.hasNext()) {
					try {
						invoke(method, args, proxiedBean, identifierIterator.next());
					} catch (Exception exception) {
						occuredExceptions.add(exception);
					}
				}
			}
		}

		if (occuredExceptions.isEmpty()) {
			return result;
		} else {
			throw occuredExceptions.iterator().next();
		}
	}

	private Bean<?> proxiedBeanFor(Method method) {
		BeanManager manager = getBeanManager();
		Set<Bean<?>> beans = manager.getBeans(method.getDeclaringClass(), identifier.getQualifiers());
		Bean<?> resolvedBean = manager.resolve(beans);
		return resolvedBean;
	}
}

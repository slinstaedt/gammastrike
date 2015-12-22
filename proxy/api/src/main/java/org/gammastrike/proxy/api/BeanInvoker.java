package org.gammastrike.proxy.api;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Function;

import javax.enterprise.inject.Instance;

import org.gammastrike.value.BeanIdentifier;

public interface BeanInvoker<T> {

	class Invocation<T, R> implements Serializable {

		private static final long serialVersionUID = 1L;

		private UUID proxyIdentity;
		private BeanIdentifier<? extends T> beanIdentifier;
		private Method method;
		private Object[] parameters;

		@SuppressWarnings("unchecked")
		public R performOn(T target) throws Throwable {
			try {
				return (R) method.invoke(target, parameters);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		}

		public UUID getProxyIdentity() {
			return proxyIdentity;
		}

		public BeanIdentifier<? extends T> getBeanIdentifier() {
			return beanIdentifier;
		}

		public Method getMethod() {
			return method;
		}

		public Object[] getParameters() {
			return parameters;
		}
	}

	<R> R perform(Invocation<T, R> invocation, Function<Instance<? super T>, T> targetFactory);

	void notifyDisposed(UUID identity);
}

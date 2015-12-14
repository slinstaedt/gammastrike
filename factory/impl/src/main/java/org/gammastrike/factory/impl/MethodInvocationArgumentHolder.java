package org.gammastrike.factory.impl;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * Holder for the current factory method arguments. {@link Dependent} scoped, so do not inject directly, but rather an {@link javax.inject.Provider} of.
 *
 * @author sven.linstaedt
 */
public interface MethodInvocationArgumentHolder {

	/**
	 * Delegate implementation for for {@link MethodInvocationArgumentHolder}.
	 *
	 * @author sven.linstaedt
	 */
	@ApplicationScoped
	class MethodInvocationArgumentHolderDelegate implements MethodInvocationArgumentHolder {

		private final BeanManager manager;
		private final Instance<MethodInvocationArgumentHolder> instance;

		MethodInvocationArgumentHolderDelegate() {
			manager = null;
			instance = null;
		}

		@Inject
		public MethodInvocationArgumentHolderDelegate(BeanManager manager, Instance<MethodInvocationArgumentHolder> instance) {
			this.manager = requireNonNull(manager);
			this.instance = requireNonNull(instance);
		}

		@Override
		public MethodParameterMetadata currentInvocation(Method method, Object[] arguments) {
			return delegate().currentInvocation(method, arguments);
		}

		private MethodInvocationArgumentHolder delegate() {
			try {
				manager.getContext(RequestScoped.class);
				return instance.select(ThreadLocalScopedArgumentHolder.class).get();
			} catch (ContextNotActiveException e) {
				return instance.select(SessionDestructionArgumentHolder.class).get();
			}
		}

		@Override
		public <T> T methodArgumentFor(Bean<T> parameterBean) {
			return delegate().methodArgumentFor(parameterBean);
		}

		@Override
		public void reset() {
			delegate().reset();
		}
	}

	/**
	 * Special implementation of {@link MethodInvocationArgumentHolder} for timing out sessions, where no request context is available.
	 *
	 * @author sven.linstaedt
	 */
	@ApplicationScoped
	@Typed(SessionDestructionArgumentHolder.class)
	class SessionDestructionArgumentHolder implements MethodInvocationArgumentHolder {

		private final ManualFactoryExtension extension;

		SessionDestructionArgumentHolder() {
			extension = null;
		}

		@Inject
		public SessionDestructionArgumentHolder(BeanManager manager) {
			this.extension = manager.getExtension(ManualFactoryExtension.class);
		}

		@Override
		public MethodParameterMetadata currentInvocation(Method method, Object[] arguments) {
			return extension.lookupMetadata(method);
		}

		@Override
		public <T> T methodArgumentFor(Bean<T> parameterBean) {
			throw new IllegalStateException("Unable to supply dependent parameters due to inactive request context");
		}

		@Override
		public void reset() {
		}
	}

	/**
	 * Regular implementation of the {@link MethodInvocationArgumentHolder}.
	 *
	 * @author sven.linstaedt
	 */
	@RequestScoped
	@Typed(ThreadLocalScopedArgumentHolder.class)
	class ThreadLocalScopedArgumentHolder implements MethodInvocationArgumentHolder {

		private final ManualFactoryExtension extension;

		private Object[] currentArguments;
		private MethodParameterMetadata currentMetadata;

		ThreadLocalScopedArgumentHolder() {
			extension = null;
		}

		@Inject
		public ThreadLocalScopedArgumentHolder(BeanManager manager) {
			this.extension = manager.getExtension(ManualFactoryExtension.class);
		}

		@Override
		public MethodParameterMetadata currentInvocation(Method method, Object[] arguments) {
			if (method.getParameterTypes().length != arguments.length) {
				throw new IllegalArgumentException("Unbalanced parameters for " + method + " and arguments " + Arrays.asList(arguments));
			}

			currentArguments = arguments;
			currentMetadata = extension.lookupMetadata(method);
			return currentMetadata;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T methodArgumentFor(Bean<T> parameterBean) {
			if (currentMetadata == null) {
				throw new IllegalStateException("Could not lookup " + parameterBean
						+ " because bean constructor parameter was not provided using the appropriate" + " @ManualFactoryBinding factory method");
			}
			int parameterIndex = currentMetadata.parameterIndexFor(parameterBean);
			return (T) currentArguments[parameterIndex];
		}

		@Override
		public void reset() {
			currentArguments = null;
			currentMetadata = null;
		}
	}

	/**
	 * Returns the current invocation stack's {@link MethodParameterMetadata} for the given method.
	 *
	 * @param method
	 *            The method to lookup metadata for
	 * @param arguments
	 *            The invocation arguments to store in the stack
	 * @return The method parameter metadata for this invocation
	 */
	MethodParameterMetadata currentInvocation(Method method, Object[] arguments);

	/**
	 * Returns the previously bound method argument for a given parameter {@link Bean}.
	 *
	 * @param parameterBean
	 *            The parameter bean
	 * @return The method argument
	 */
	<T> T methodArgumentFor(Bean<T> parameterBean);

	/**
	 * Resets the invocation stack.
	 */
	void reset();
}

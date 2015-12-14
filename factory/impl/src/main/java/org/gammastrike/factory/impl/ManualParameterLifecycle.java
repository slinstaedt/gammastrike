package org.gammastrike.factory.impl;

import static java.util.Objects.requireNonNull;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;

import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

/**
 * {@link ContextualLifecycle} for {@link kn.gvs.framework.cdi.factory.annotation.Manual} annotated arguments, that are hold by a request scoped
 * {@link MethodInvocationArgumentHolder}.
 *
 * @author sven.linstaedt
 *
 * @param <T>
 *            the contextual type
 */
public class ManualParameterLifecycle<T> implements ContextualLifecycle<T> {

	private final MethodInvocationArgumentHolder holder;

	@Inject
	public ManualParameterLifecycle(MethodInvocationArgumentHolder holder) {
		this.holder = requireNonNull(holder);
	}

	@Override
	public T create(Bean<T> bean, CreationalContext<T> creationalContext) {
		return holder.methodArgumentFor(bean);
	}

	@Override
	public void destroy(Bean<T> bean, T instance, CreationalContext<T> creationalContext) {
	}
}

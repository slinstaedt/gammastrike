package org.gammastrike.factory.impl;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

/**
 * A {@link ContextualLifecycle}, that always returns {@code null} and destroys nothing.
 *
 * @author sven.linstaedt
 *
 * @param <T>
 *            The contextual type
 */
public class VoidLifecycle<T> implements ContextualLifecycle<T> {

	@SuppressWarnings("rawtypes")
	private static final VoidLifecycle INSTANCE = new VoidLifecycle<>();

	@SuppressWarnings("unchecked")
	public static final <T> VoidLifecycle<T> instance() {
		return INSTANCE;
	}

	private VoidLifecycle() {
	}

	@Override
	public T create(Bean<T> bean, CreationalContext<T> creationalContext) {
		return null;
	}

	@Override
	public void destroy(Bean<T> bean, T instance, CreationalContext<T> creationalContext) {
	}
}

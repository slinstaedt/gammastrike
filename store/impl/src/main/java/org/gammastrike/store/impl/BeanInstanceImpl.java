package org.gammastrike.store.impl;

import static java.util.Objects.requireNonNull;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.gammastrike.store.api.BeanInstance;

public class BeanInstanceImpl<T> implements BeanInstance<T> {

	public static <T> BeanInstance<T> create(BeanManager manager, Bean<T> bean) {
		CreationalContext<T> context = manager.createCreationalContext(bean);
		return create(manager, bean, context);
	}

	public static <T> BeanInstance<T> create(BeanManager manager, Bean<T> bean, CreationalContext<T> context) {
		@SuppressWarnings("unchecked")
		T instance = (T) manager.getReference(bean, bean.getBeanClass(), context);
		return new BeanInstanceImpl<T>(bean, instance, context);
	}

	private final Contextual<T> contextual;

	private final T instance;

	private final CreationalContext<T> context;

	public BeanInstanceImpl(Contextual<T> contextual, T instance, CreationalContext<T> context) {
		this.contextual = requireNonNull(contextual);
		this.instance = requireNonNull(instance);
		this.context = context;
	}

	public void destroyInstance() {
		contextual.destroy(instance, context);
	}

	@Override
	public Contextual<T> getContextual() {
		return contextual;
	}

	@Override
	public CreationalContext<T> getCreationalContext() {
		return context;
	}

	@Override
	public T getInstance() {
		return instance;
	}
}

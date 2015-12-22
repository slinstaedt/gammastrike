package org.gammastrike.util;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.Producer;

public class ProducerBasedInjectionTarget<T> implements InjectionTarget<T> {

	public static <X> InjectionTarget<X> wrapIfNecessary(Producer<X> producer) {
		if (producer instanceof InjectionTarget) {
			return (InjectionTarget<X>) producer;
		} else {
			return new ProducerBasedInjectionTarget<>(producer);
		}
	}

	private final Producer<T> delegate;

	public ProducerBasedInjectionTarget(Producer<T> delegate) {
		this.delegate = requireNonNull(delegate);
	}

	@Override
	public T produce(CreationalContext<T> ctx) {
		return delegate.produce(ctx);
	}

	@Override
	public void dispose(T instance) {
		delegate.dispose(instance);
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return delegate.getInjectionPoints();
	}

	@Override
	public void inject(T instance, CreationalContext<T> ctx) {
		// noop
	}

	@Override
	public void postConstruct(T instance) {
		// noop
	}

	@Override
	public void preDestroy(T instance) {
		// noop
	}
}
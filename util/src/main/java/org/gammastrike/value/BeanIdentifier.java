package org.gammastrike.value;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

public class BeanIdentifier<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	public static <T> BeanIdentifier<T> create(Class<T> beanType, Qualifiers qualifiers) {
		return new BeanIdentifier<>(beanType, qualifiers);
	}

	private final Type type;
	private final Qualifiers qualifiers;

	private BeanIdentifier(Type type, Qualifiers qualifiers) {
		this.type = requireNonNull(type);
		this.qualifiers = requireNonNull(qualifiers);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((qualifiers == null) ? 0 : qualifiers.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BeanIdentifier<?> other = (BeanIdentifier<?>) obj;
		if (qualifiers == null) {
			if (other.qualifiers != null) {
				return false;
			}
		} else if (!qualifiers.equals(other.qualifiers)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

	private Set<Bean<? extends T>> candidates(BeanManager manager) {
		return manager.getBeans(type, qualifiers.asArray()).stream().map(Bean.class::cast).collect(Collectors.toSet());
	}

	public T getReference(BeanManager manager) {
		Set<Bean<? extends T>> candidates = candidates(manager);
		Bean<? extends T> resolved = manager.resolve(candidates);
		CreationalContext<? extends T> ctx = manager.createCreationalContext(resolved);
		@SuppressWarnings("unchecked")
		T reference = (T) manager.getReference(resolved, type, ctx);
		return reference;
	}
}

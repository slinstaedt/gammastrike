package org.gammastrike.binding.api.event;

import static java.util.Objects.requireNonNull;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ProcessBeanAttributes;

public class ProcessBoundedBean<T> {

	public static class Replacement<T> {

		public enum Type {
			ATTRIBUTES, BEAN;
		}

		private final BeanAttributes<T> attributes;

		private final Bean<T> bean;

		public Replacement(Bean<T> bean) {
			this.attributes = null;
			this.bean = requireNonNull(bean);
		}

		public Replacement(BeanAttributes<T> attributes) {
			this.attributes = requireNonNull(attributes);
			this.bean = null;
		}

		public BeanAttributes<T> getAttributes() {
			return requireNonNull(attributes);
		}

		public Bean<T> getBean() {
			return requireNonNull(bean);
		}

		public Type getType() {
			if (attributes != null) {
				return Type.ATTRIBUTES;
			} else if (bean != null) {
				return Type.BEAN;
			} else {
				throw new IllegalStateException();
			}
		}
	}

	public static <T> Replacement<T> fire(BeanManager manager, ProcessBeanAttributes<T> delegate) {
		ProcessBoundedBean<T> event = new ProcessBoundedBean<>(delegate.getAnnotated(), delegate.getBeanAttributes());
		manager.fireEvent(event);
		return event.getReplacement();
	}

	private final Annotated annotated;
	private final BeanAttributes<T> beanAttributes;

	private Replacement<T> replacement;

	public ProcessBoundedBean(Annotated annotated, BeanAttributes<T> attributes) {
		this.annotated = requireNonNull(annotated);
		this.beanAttributes = requireNonNull(attributes);
		this.replacement = null;
	}

	private void checkReplacementState() {
		if (replacement != null) {
			throw new IllegalStateException("Bean already replaced.");
		}
	}

	public Annotated getAnnotated() {
		return annotated;
	}

	public BeanAttributes<T> getBeanAttributes() {
		return beanAttributes;
	}

	public Replacement<T> getReplacement() {
		return replacement;
	}

	public boolean isReplaced() {
		return replacement != null;
	}

	public void replaceWith(Bean<T> bean) {
		checkReplacementState();
		replacement = new Replacement<>(bean);
	}

	public void replaceWith(BeanAttributes<T> attributes) {
		checkReplacementState();
		replacement = new Replacement<>(attributes);
	}
}

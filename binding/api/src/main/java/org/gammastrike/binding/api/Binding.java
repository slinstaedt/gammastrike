package org.gammastrike.binding.api;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import org.gammastrike.binding.api.annotation.BoundWith;

public class Binding implements Serializable {

	private static final long serialVersionUID = 1L;

	private final BindingType bindingType;
	private final NonBindingAwareAnnotation<Annotation> annotation;

	public Binding(BindingType bindingType, Annotation annotation) {
		this.bindingType = requireNonNull(bindingType);
		this.annotation = NonBindingAwareAnnotation.wrap(annotation);
		if (!bindingType.isBindingInstance(annotation)) {
			throw new IllegalArgumentException("Annotation " + annotation + " does not match " + bindingType);
		}
	}

	public BoundWith createBoundWithQualifier() {
		return bindingType.createBoundWithQualifier();
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
		Binding other = (Binding) obj;
		if (annotation == null) {
			if (other.annotation != null) {
				return false;
			}
		} else if (!annotation.equals(other.annotation)) {
			return false;
		}
		if (bindingType == null) {
			if (other.bindingType != null) {
				return false;
			}
		} else if (!bindingType.equals(other.bindingType)) {
			return false;
		}
		return true;
	}

	public Annotation getAnnotation() {
		return annotation.getAnnotation();
	}

	public <A extends Annotation> A getAnnotationAs(Class<A> annotationType) {
		return annotationType.cast(annotation.getAnnotation());
	}

	public BindingType getBindingType() {
		return bindingType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotation == null) ? 0 : annotation.hashCode());
		result = prime * result + ((bindingType == null) ? 0 : bindingType.hashCode());
		return result;
	}

	public boolean hasMetaBindingType(MetaBindingType metaBindingType) {
		return bindingType.getMetaBindingType().equals(metaBindingType);
	}

	@Override
	public String toString() {
		return "Binding [annotation=" + annotation + ", type=" + bindingType + "]";
	}
}

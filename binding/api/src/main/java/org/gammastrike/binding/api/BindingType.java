package org.gammastrike.binding.api;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.gammastrike.binding.api.annotation.BoundWith;

public class BindingType implements Serializable {

	private static final long serialVersionUID = 1L;

	private final MetaBindingType metaBindingType;
	private final Class<? extends Annotation> javaType;
	private final Set<Annotation> definitions;

	BindingType(MetaBindingType metaBindingType, Class<? extends Annotation> javaType, Collection<Annotation> definitions) {
		this.metaBindingType = requireNonNull(metaBindingType);
		this.javaType = requireNonNull(javaType);
		this.definitions = Collections.unmodifiableSet(new HashSet<>(definitions));
	}

	public Binding createBinding(Annotation bindingAnnotation) {
		return new Binding(this, bindingAnnotation);
	}

	public BoundWith createBoundWithQualifier() {
		return metaBindingType.createBoundWithQualifier();
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
		BindingType other = (BindingType) obj;
		if (javaType == null) {
			if (other.javaType != null) {
				return false;
			}
		} else if (!javaType.equals(other.javaType)) {
			return false;
		}
		if (metaBindingType == null) {
			if (other.metaBindingType != null) {
				return false;
			}
		} else if (!metaBindingType.equals(other.metaBindingType)) {
			return false;
		}
		return true;
	}

	public Set<Annotation> getDefinitions() {
		return definitions;
	}

	public Class<? extends Annotation> getJavaType() {
		return javaType;
	}

	public MetaBindingType getMetaBindingType() {
		return metaBindingType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((javaType == null) ? 0 : javaType.hashCode());
		result = prime * result + ((metaBindingType == null) ? 0 : metaBindingType.hashCode());
		return result;
	}

	public boolean isBindingInstance(Annotation annotation) {
		return javaType.isInstance(annotation);
	}

	@Override
	public String toString() {
		return "BindingType [javaType=" + javaType.getName() + ", definitions=" + definitions + "]";
	}
}

package org.gammastrike.binding.api;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

public class BoundedAnnotated implements Serializable {

	public static final BoundedAnnotated UNBOUND = new BoundedAnnotated();

	private static final long serialVersionUID = 1L;

	private static Class<?> baseTypeOf(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			return baseTypeOf(parameterizedType.getRawType());
		} else if (type instanceof GenericArrayType) {
			GenericArrayType genericArrayType = (GenericArrayType) type;
			Class<?> componentType = baseTypeOf(genericArrayType.getGenericComponentType());
			return Array.newInstance(componentType, 0).getClass();
		} else {
			throw new IllegalArgumentException("Unable to derive base type of " + type);
		}
	}

	private static Set<Annotated> findMembers(Annotated annotated) {
		Set<Annotated> members = new HashSet<>();
		members.add(annotated);
		if (annotated instanceof AnnotatedType) {
			AnnotatedType<?> annotatedType = (AnnotatedType<?>) annotated;
			for (AnnotatedField<?> annotatedField : annotatedType.getFields()) {
				members.addAll(findMembers(annotatedField));
			}
			for (AnnotatedConstructor<?> annotatedConstructor : annotatedType.getConstructors()) {
				members.addAll(findMembers(annotatedConstructor));
			}
			for (AnnotatedMethod<?> annotatedMethod : annotatedType.getMethods()) {
				members.addAll(findMembers(annotatedMethod));
			}
		} else if (annotated instanceof AnnotatedCallable) {
			AnnotatedCallable<?> annotatedCallable = (AnnotatedCallable<?>) annotated;
			members.addAll(annotatedCallable.getParameters());
		}
		return Collections.unmodifiableSet(members);
	}

	private final Annotated annotated;
	private final Class<?> baseType;
	private final Map<Binding, BoundedGroup> bindings;
	private final Set<Annotated> members;

	private BoundedAnnotated() {
		this.annotated = null;
		this.baseType = Void.class;
		this.bindings = Collections.emptyMap();
		this.members = Collections.emptySet();
	}

	public BoundedAnnotated(Annotated annotated) {
		this.annotated = requireNonNull(annotated);
		this.baseType = baseTypeOf(annotated.getBaseType());
		this.bindings = new HashMap<>();
		this.members = findMembers(annotated);
	}

	void attach(BoundedGroup group) {
		bindings.put(group.getBinding(), group);
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
		BoundedAnnotated other = (BoundedAnnotated) obj;
		if (annotated == null) {
			if (other.annotated != null) {
				return false;
			}
		} else if (!annotated.equals(other.annotated)) {
			return false;
		}
		return true;
	}

	public BoundedGroup findBoundedGroup(Binding binding) {
		BoundedGroup bounded = bindings.get(binding);
		if (bounded == null) {
			bounded = BoundedGroup.NONE;
		}
		return bounded;
	}

	public Annotated getAnnotated() {
		return annotated;
	}

	public Class<?> getBaseType() {
		return baseType;
	}

	public Bindings getBindings() {
		return Bindings.of(bindings.keySet());
	}

	public Set<Annotated> getMembers() {
		return members;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotated == null) ? 0 : annotated.hashCode());
		return result;
	}

	public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
		return annotated.isAnnotationPresent(annotationType);
	}

	public boolean isAssignableTo(Class<?> type) {
		return type.isAssignableFrom(baseType);
	}

	public boolean isBound() {
		return !bindings.isEmpty();
	}

	@Override
	public String toString() {
		return "BoundedAnnotated [" + annotated + "]";
	}
}
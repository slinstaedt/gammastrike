package org.gammastrike.value;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;

public class TypeClosure implements Iterable<Type>, Serializable {

	private static final Comparator<Class<?>> CLASS_HIERARCHY_COMPARATOR = (c1, c2) -> {
		if (c1.equals(c2)) {
			return 0;
		} else if (c1.isAssignableFrom(c2)) {
			return 1;
		} else if (c2.isAssignableFrom(c1)) {
			return -1;
		} else {
			return c1.getName().compareTo(c2.getName());
		}
	};

	private static final long serialVersionUID = 1L;

	public static Set<Type> analyze(Type type, Set<Type> collected) {
		collected.add(type);
		if (type instanceof Class) {
			analyze(((Class<?>) type).getGenericSuperclass(), collected);
			for (Type genericInterface : ((Class<?>) type).getGenericInterfaces()) {
				analyze(genericInterface, collected);
			}
		} else if (type instanceof GenericArrayType) {
			// ignore
		} else if (type instanceof ParameterizedType) {
			analyze(((ParameterizedType) type).getRawType(), collected);
		} else if (type instanceof TypeVariable<?>) {
			for (Type bound : ((TypeVariable<?>) type).getBounds()) {
				analyze(bound, collected);
			}
		} else if (type instanceof WildcardType) {
			for (Type bound : ((WildcardType) type).getUpperBounds()) {
				analyze(bound, collected);
			}
		} else {
			throw new IllegalArgumentException("Unhandled: " + type);
		}
		return collected;
	}

	public static TypeClosure from(Annotated annotated) {
		return new TypeClosure(annotated.getBaseType());
	}

	public static TypeClosure from(Bean<?> bean) {
		return new TypeClosure(bean.getBeanClass());
	}

	public static TypeClosure from(BeanAttributes<?> attributes) {
		return new TypeClosure(attributes.getTypes());
	}

	public static Class<?> toRawType(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return toRawType(((ParameterizedType) type).getRawType());
		} else {
			throw new IllegalArgumentException("Unhandled: " + type);
		}
	}

	private final Set<Type> types;

	private TypeClosure(Set<? extends Type> types) {
		this.types = Collections.unmodifiableSet(types);
	}

	public TypeClosure(Type type) {
		this.types = Collections.unmodifiableSet(analyze(type, new HashSet<>()));
	}

	public Set<Type> getTypes() {
		return types;
	}

	public Class<?> getBaseClass() {
		return toClassArray()[0];
	}

	@Override
	public Iterator<Type> iterator() {
		return types.iterator();
	}

	public Class<?>[] toClassArray() {
		List<Class<?>> types = new ArrayList<>(this.types.size());
		for (Type type : types) {
			types.add(toRawType(type));
		}
		Collections.sort(types, CLASS_HIERARCHY_COMPARATOR);
		return types.toArray(new Class<?>[types.size()]);
	}

	public TypeClosure toRawTypes() {
		Set<Class<?>> types = new HashSet<>(this.types.size());
		for (Type type : types) {
			types.add(toRawType(type));
		}
		return new TypeClosure(types);
	}

	public TypeClosure with(Type... types) {
		Set<Type> newTypes = new HashSet<>(this.types);
		if (newTypes.addAll(Arrays.asList(types))) {
			return new TypeClosure(newTypes);
		} else {
			return this;
		}
	}
}

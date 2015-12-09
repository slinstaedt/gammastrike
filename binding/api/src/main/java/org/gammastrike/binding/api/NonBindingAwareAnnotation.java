package org.gammastrike.binding.api;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.util.Nonbinding;

public class NonBindingAwareAnnotation<A extends Annotation> implements Annotation, Serializable {

	private static final long serialVersionUID = 1L;

	private static Object getMemberValue(Method member, Annotation instance) {
		Object value = invoke(member, instance);
		if (value == null) {
			throw new IllegalArgumentException("Annotation member value " + instance.getClass().getName() + "." + member.getName() + " must not be null");
		}
		return value;
	}

	private static Object invoke(Method method, Object instance) {
		try {
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
			return method.invoke(instance);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Error checking value of member method " + method.getName() + " on " + method.getDeclaringClass(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Error checking value of member method " + method.getName() + " on " + method.getDeclaringClass(), e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Error checking value of member method " + method.getName() + " on " + method.getDeclaringClass(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <A extends Annotation> NonBindingAwareAnnotation<A> wrap(A annotation) {
		if (annotation instanceof NonBindingAwareAnnotation) {
			return (NonBindingAwareAnnotation<A>) annotation;
		} else {
			return new NonBindingAwareAnnotation<>(annotation);
		}
	}

	private final A annotation;

	private Integer cachedHashCode;

	public NonBindingAwareAnnotation(A annotation) {
		this.annotation = requireNonNull(annotation);
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return annotation.annotationType();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (!(other instanceof Annotation)) {
			return false;
		}
		Annotation that = (Annotation) other;
		if (!this.annotationType().equals(that.annotationType())) {
			return false;
		}
		if (this.hashCode() != that.hashCode()) {
			return false;
		}

		for (Method member : getMembers()) {
			Object thisValue = getMemberValue(member, this);
			Object thatValue = getMemberValue(member, that);
			if (thisValue instanceof byte[] && thatValue instanceof byte[]) {
				if (!Arrays.equals((byte[]) thisValue, (byte[]) thatValue)) {
					return false;
				}
			} else if (thisValue instanceof short[] && thatValue instanceof short[]) {
				if (!Arrays.equals((short[]) thisValue, (short[]) thatValue)) {
					return false;
				}
			} else if (thisValue instanceof int[] && thatValue instanceof int[]) {
				if (!Arrays.equals((int[]) thisValue, (int[]) thatValue)) {
					return false;
				}
			} else if (thisValue instanceof long[] && thatValue instanceof long[]) {
				if (!Arrays.equals((long[]) thisValue, (long[]) thatValue)) {
					return false;
				}
			} else if (thisValue instanceof float[] && thatValue instanceof float[]) {
				if (!Arrays.equals((float[]) thisValue, (float[]) thatValue)) {
					return false;
				}
			} else if (thisValue instanceof double[] && thatValue instanceof double[]) {
				if (!Arrays.equals((double[]) thisValue, (double[]) thatValue)) {
					return false;
				}
			} else if (thisValue instanceof char[] && thatValue instanceof char[]) {
				if (!Arrays.equals((char[]) thisValue, (char[]) thatValue)) {
					return false;
				}
			} else if (thisValue instanceof boolean[] && thatValue instanceof boolean[]) {
				if (!Arrays.equals((boolean[]) thisValue, (boolean[]) thatValue)) {
					return false;
				}
			} else if (thisValue instanceof Object[] && thatValue instanceof Object[]) {
				if (!Arrays.equals((Object[]) thisValue, (Object[]) thatValue)) {
					return false;
				}
			} else {
				if (!thisValue.equals(thatValue)) {
					return false;
				}
			}
		}
		return true;
	}

	public A getAnnotation() {
		return annotation;
	}

	private List<Method> getMembers() {
		List<Method> methods = new ArrayList<>(Arrays.asList(annotationType().getDeclaredMethods()));
		Iterator<Method> iterator = methods.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().isAnnotationPresent(Nonbinding.class)) {
				iterator.remove();
			}
		}
		return methods;
	}

	@Override
	public int hashCode() {
		if (cachedHashCode == null) {
			cachedHashCode = 0;
			for (Method member : getMembers()) {
				int memberNameHashCode = 127 * member.getName().hashCode();
				Object value = getMemberValue(member, this);
				int memberValueHashCode;
				if (value instanceof boolean[]) {
					memberValueHashCode = Arrays.hashCode((boolean[]) value);
				} else if (value instanceof short[]) {
					memberValueHashCode = Arrays.hashCode((short[]) value);
				} else if (value instanceof int[]) {
					memberValueHashCode = Arrays.hashCode((int[]) value);
				} else if (value instanceof long[]) {
					memberValueHashCode = Arrays.hashCode((long[]) value);
				} else if (value instanceof float[]) {
					memberValueHashCode = Arrays.hashCode((float[]) value);
				} else if (value instanceof double[]) {
					memberValueHashCode = Arrays.hashCode((double[]) value);
				} else if (value instanceof byte[]) {
					memberValueHashCode = Arrays.hashCode((byte[]) value);
				} else if (value instanceof char[]) {
					memberValueHashCode = Arrays.hashCode((char[]) value);
				} else if (value instanceof Object[]) {
					memberValueHashCode = Arrays.hashCode((Object[]) value);
				} else {
					memberValueHashCode = value.hashCode();
				}
				cachedHashCode += memberNameHashCode ^ memberValueHashCode;
			}
		}
		return cachedHashCode;
	}

	@Override
	public String toString() {
		return annotation.toString();
	}
}

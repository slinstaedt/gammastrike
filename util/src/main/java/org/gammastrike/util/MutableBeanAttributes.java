package org.gammastrike.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.BeanAttributes;

import org.gammastrike.literal.DefaultLiteral;

/**
 * A {@link BeanAttributes} implementation, that allows modification after creation.
 *
 * @author sven.linstaedt
 *
 * @param <T>
 *            the bean's type
 */
public class MutableBeanAttributes<T> implements BeanAttributes<T> {

	private static final Set<Annotation> DEFAULT_QUALIFIERS = Collections.<Annotation> singleton(DefaultLiteral.INSTANCE);

	private Set<Type> types;
	private Set<Annotation> qualifiers;
	private Class<? extends Annotation> scope;
	private String name;
	private Set<Class<? extends Annotation>> stereotypes;
	private boolean alternative;

	public MutableBeanAttributes() {
		types = new HashSet<>();
		qualifiers = new HashSet<>();
		scope = null;
		name = null;
		stereotypes = new HashSet<>();
		alternative = false;
	}

	public MutableBeanAttributes(BeanAttributes<? extends T> delegate) {
		this();
		copyFrom(delegate);
	}

	public void addQualifiers(Annotation... qualifiers) {
		this.qualifiers.addAll(Arrays.asList(qualifiers));
	}

	public void addQualifiers(Set<Annotation> qualifiers) {
		this.qualifiers.addAll(qualifiers);
	}

	@SafeVarargs
	public final void addStereotypes(Class<? extends Annotation>... stereoTypes) {
		this.stereotypes.addAll(Arrays.asList(stereoTypes));
	}

	public void addStereotypes(Set<Class<? extends Annotation>> stereoTypes) {
		this.stereotypes.addAll(stereoTypes);
	}

	public void addTypes(Set<Type> types) {
		this.types.addAll(types);
	}

	public void addTypes(Type... types) {
		this.types.addAll(Arrays.asList(types));
	}

	public void copyFrom(BeanAttributes<?> delegate) {
		setTypes(delegate.getTypes());
		setQualifiers(delegate.getQualifiers());
		setScope(delegate.getScope());
		setName(delegate.getName());
		setStereotypes(delegate.getStereotypes());
		setAlternative(delegate.isAlternative());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Set<Annotation> getQualifiers() {
		return qualifiers.isEmpty() ? DEFAULT_QUALIFIERS : qualifiers;
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return scope != null ? scope : Dependent.class;
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes() {
		return stereotypes;
	}

	@Override
	public Set<Type> getTypes() {
		return types;
	}

	public boolean hasScope(Class<? extends Annotation> scope) {
		return this.scope == scope;
	}

	@Override
	public boolean isAlternative() {
		return alternative;
	}

	public void setAlternative(boolean alternative) {
		this.alternative = alternative;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setQualifiers(Set<Annotation> qualifiers) {
		this.qualifiers = new HashSet<>(qualifiers);
	}

	public void setScope(Class<? extends Annotation> scope) {
		this.scope = scope;
	}

	public void setStereotypes(Set<Class<? extends Annotation>> stereoTypes) {
		this.stereotypes = new HashSet<>(stereoTypes);
	}

	public void setTypes(Set<Type> types) {
		this.types = new HashSet<>(types);
	}
}

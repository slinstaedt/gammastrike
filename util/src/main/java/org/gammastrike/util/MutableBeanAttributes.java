package org.gammastrike.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.BeanAttributes;

import org.apache.deltaspike.core.api.literal.DefaultLiteral;

/**
 * A {@link BeanAttributes} implementation, that allows modification after creation.
 *
 * @author sven.linstaedt
 *
 * @param <T>
 *            the bean's type
 */
public class MutableBeanAttributes<T> implements BeanAttributes<T> {

	private static final Set<Annotation> DEFAULT_QUALIFIERS = Collections.<Annotation>singleton(new DefaultLiteral());

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

	public void copyFrom(BeanAttributes<?> delegate) {
		setTypes(delegate.getTypes());
		setQualifiers(delegate.getQualifiers());
		setScope(delegate.getScope());
		setName(delegate.getName());
		setStereotypes(delegate.getStereotypes());
		setAlternative(delegate.isAlternative());
	}

	@Override
	public Set<Type> getTypes() {
		return types;
	}

	public void setTypes(Set<Type> types) {
		this.types = new HashSet<>(types);
	}

	public void addTypes(Set<Type> types) {
		this.types.addAll(types);
	}

	public void addTypes(Type... types) {
		this.types.addAll(Arrays.asList(types));
	}

	@Override
	public Set<Annotation> getQualifiers() {
		return qualifiers.isEmpty() ? DEFAULT_QUALIFIERS : qualifiers;
	}

	public void addQualifiers(Set<Annotation> qualifiers) {
		this.qualifiers.addAll(qualifiers);
	}

	public void addQualifiers(Annotation... qualifiers) {
		this.qualifiers.addAll(Arrays.asList(qualifiers));
	}

	public void setQualifiers(Set<Annotation> qualifiers) {
		this.qualifiers = new HashSet<>(qualifiers);
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return scope != null ? scope : Dependent.class;
	}

	public boolean hasScope(Class<? extends Annotation> scope) {
		return this.scope == scope;
	}

	public void setScope(Class<? extends Annotation> scope) {
		this.scope = scope;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes() {
		return stereotypes;
	}

	public void setStereotypes(Set<Class<? extends Annotation>> stereoTypes) {
		this.stereotypes = new HashSet<>(stereoTypes);
	}

	public void addStereotypes(Set<Class<? extends Annotation>> stereoTypes) {
		this.stereotypes.addAll(stereoTypes);
	}

	@SafeVarargs
	public final void addStereotypes(Class<? extends Annotation>... stereoTypes) {
		this.stereotypes.addAll(Arrays.asList(stereoTypes));
	}

	@Override
	public boolean isAlternative() {
		return alternative;
	}

	public void setAlternative(boolean alternative) {
		this.alternative = alternative;
	}
}

package org.gammastrike.binding.api;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.gammastrike.binding.api.annotation.BoundWith;
import org.gammastrike.binding.api.annotation.MetaBinding.AssignmentStrategy;

public class MetaBindingType implements Serializable {

	private static final Collection<Class<? extends Annotation>> DEFAULT_NON_DEFINING = Arrays.asList(Target.class, Retention.class, Inherited.class,
			Documented.class);

	private static final long serialVersionUID = 1L;

	private final Class<? extends Annotation> javaType;
	private final AssignmentStrategy assignmentStrategy;
	private final ConcurrentMap<Class<? extends Annotation>, BindingType> bindingTypes;
	private final Set<Class<? extends Annotation>> nonDefiningAnnotationTypes;

	public MetaBindingType(Class<? extends Annotation> javaType, AssignmentStrategy assignmentStrategy) {
		this.javaType = requireNonNull(javaType);
		this.assignmentStrategy = requireNonNull(assignmentStrategy);
		this.bindingTypes = new ConcurrentHashMap<>();
		this.nonDefiningAnnotationTypes = new CopyOnWriteArraySet<>(DEFAULT_NON_DEFINING);
		this.nonDefiningAnnotationTypes.add(javaType);
		if (javaType.getDeclaredMethods().length > 0) {
			throw new IllegalArgumentException("MetaBinding annotation must not contains methods: " + javaType);
		}
	}

	protected Set<Annotation> analyzeBindingDefinitions(Class<?> bindingClass) {
		Set<Annotation> definitions = new HashSet<>(Arrays.asList(bindingClass.getAnnotations()));
		Iterator<Annotation> iterator = definitions.iterator();
		while (iterator.hasNext()) {
			Class<? extends Annotation> annotationType = iterator.next().annotationType();
			if (getNonDefiningAnnotationTypes().contains(annotationType)) {
				iterator.remove();
			}
		}
		return definitions;
	}

	public BindingType bindingTypeFor(Class<? extends Annotation> bindingClass) {
		BindingType bindingType = bindingTypes.get(bindingClass);
		if (bindingType != null) {
			return bindingType;
		} else {
			throw new IllegalArgumentException(bindingClass.getName() + " is not bound by " + this);
		}
	}

	public BindingType createBindingType(Class<? extends Annotation> bindingClass, Set<Annotation> definitions) {
		if (isMetaBindingTypeFor(bindingClass)) {
			throw new IllegalStateException(bindingClass.getName() + " is already defined");
		}

		if (definitions == null) {
			definitions = analyzeBindingDefinitions(bindingClass);
		}

		BindingType bindingType = newBindingType(bindingClass, definitions);
		bindingTypes.put(bindingType.getJavaType(), bindingType);
		return bindingType;
	}

	public BoundWith createBoundWithQualifier() {
		return BoundWith.Literal.of(javaType);
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
		MetaBindingType other = (MetaBindingType) obj;
		if (javaType == null) {
			if (other.javaType != null) {
				return false;
			}
		} else if (!javaType.equals(other.javaType)) {
			return false;
		}
		return true;
	}

	public AssignmentStrategy getAssignmentStrategy() {
		return assignmentStrategy;
	}

	public Collection<BindingType> getBindingTypes() {
		return Collections.unmodifiableCollection(bindingTypes.values());
	}

	public Class<? extends Annotation> getJavaType() {
		return javaType;
	}

	protected Set<Class<? extends Annotation>> getNonDefiningAnnotationTypes() {
		return nonDefiningAnnotationTypes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((javaType == null) ? 0 : javaType.hashCode());
		return result;
	}

	public boolean isMetaBindingTypeFor(Class<? extends Annotation> bindingClass) {
		return bindingTypes.containsKey(bindingClass);
	}

	protected BindingType newBindingType(Class<? extends Annotation> bindingClass, Set<Annotation> definitions) {
		return new BindingType(this, bindingClass, definitions);
	}

	public void remove(BindingType bindingType) {
		bindingTypes.remove(bindingType.getJavaType());
	}

	@Override
	public String toString() {
		return "MetaBinding [" + javaType.getName() + "]";
	}
}

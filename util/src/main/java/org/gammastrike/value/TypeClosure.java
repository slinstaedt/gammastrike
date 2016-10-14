package org.gammastrike.value;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.New;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.inject.Inject;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.deltaspike.core.api.literal.DefaultLiteral;

public class TypeClosure implements Iterable<Type>, Serializable {

	public interface AnnotationChecker {

		boolean equivalent(Annotation annotation1, Annotation annotation2);

		boolean relevant(Class<? extends Annotation> annotationType);
	}

	public static class CdiInterceptorChecker implements AnnotationChecker, Serializable {

		private static final long serialVersionUID = 1L;

		private final BeanManager manager;

		@Inject
		public CdiInterceptorChecker(BeanManager manager) {
			this.manager = requireNonNull(manager);
		}

		@Override
		public boolean equivalent(Annotation annotation1, Annotation annotation2) {
			return manager.areInterceptorBindingsEquivalent(annotation1, annotation2);
		}

		@Override
		public boolean relevant(Class<? extends Annotation> annotationType) {
			return manager.isInterceptorBinding(annotationType);
		}
	}

	public static class CdiQualifierChecker implements AnnotationChecker, Serializable {

		private static final long serialVersionUID = 1L;

		private final BeanManager manager;

		@Inject
		public CdiQualifierChecker(BeanManager manager) {
			this.manager = requireNonNull(manager);
		}

		@Override
		public boolean equivalent(Annotation annotation1, Annotation annotation2) {
			return manager.areQualifiersEquivalent(annotation1, annotation2);
		}

		@Override
		public boolean relevant(Class<? extends Annotation> annotationType) {
			return manager.isQualifier(annotationType);
		}
	}

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

	public static TypeClosure annotated(BeanManager manager, Annotated annotated) {
		return new TypeClosure(new CdiQualifierChecker(manager), annotated.getTypeClosure(), annotated.getAnnotations());
	}

	public static TypeClosure decorated(BeanManager manager, Decorator<?> decorator) {
		return new TypeClosure(new CdiQualifierChecker(manager), Collections.singleton(decorator.getDelegateType()), decorator.getDelegateQualifiers());
	}

	public static TypeClosure intercepted(BeanManager manager, Interceptor<?> interceptor) {
		return new TypeClosure(new CdiInterceptorChecker(manager), interceptor.getTypes(), interceptor.getInterceptorBindings());
	}

	public static TypeClosure qualified(BeanManager manager, Annotated annotated) {
		return new TypeClosure(new CdiQualifierChecker(manager), annotated.getTypeClosure(), annotated.getAnnotations());
	}

	public static TypeClosure qualified(BeanManager manager, BeanAttributes<?> attributes) {
		return new TypeClosure(new CdiQualifierChecker(manager), attributes.getTypes(), attributes.getQualifiers());
	}

	private final AnnotationChecker checker;
	private final Set<Type> types;
	private final Map<Class<? extends Annotation>, Annotation> annotations;

	public TypeClosure(AnnotationChecker checker, Set<Type> types, Collection<Annotation> annotations) {
		this.checker = requireNonNull(checker);
		if (types.isEmpty()) {
			this.types = Collections.emptySet();
		} else {
			this.types = Collections.unmodifiableSet(new HashSet<>(types));
		}
		if (annotations.isEmpty()) {
			this.annotations = Collections.emptyMap();
		} else {
			Map<Class<? extends Annotation>, Annotation> map = new HashMap<>(annotations.size());
			for (Annotation annotation : annotations) {
				Class<? extends Annotation> type = annotation.annotationType();
				if (checker.relevant(type) && map.put(type, annotation) != null) {
					throw new IllegalArgumentException("Annotation " + annotations + "contain duplicate type" + type);
				}
			}
			if (map.isEmpty()) {
				map.put(Default.class, new DefaultLiteral());
			}
			this.annotations = Collections.unmodifiableMap(map);
		}
	}

	public <A extends Annotation> A annotationOf(Class<A> annotationType) {
		Annotation annotation = annotations.get(annotationType);
		if (annotation != null) {
			return annotationType.cast(annotation);
		} else {
			throw new IllegalArgumentException("Event qualifiers " + annotations + " does not contain a " + annotationType);
		}
	}

	public List<Decorator<?>> resolveDecorators(BeanManager manager) {
		return manager.resolveDecorators(getTypes(), getAnnotationsAsArray());
	}

	public List<Interceptor<?>> resolveInterceptors(BeanManager manager, InterceptionType type) {
		return manager.resolveInterceptors(type, getAnnotationsAsArray());
	}

	public boolean containsAnnotation(Annotation annotation) {
		Annotation candidate = annotations.get(annotation.annotationType());
		return candidate != null ? checker.equivalent(annotation, candidate) : false;
	}

	public boolean containsAnnotationType(Class<? extends Annotation> annotationType) {
		return annotations.keySet().contains(annotationType);
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
		TypeClosure other = (TypeClosure) obj;
		if (annotations == null) {
			if (other.annotations != null) {
				return false;
			}
		} else if (!annotations.equals(other.annotations)) {
			return false;
		}
		if (types == null) {
			if (other.types != null) {
				return false;
			}
		} else if (!types.equals(other.types)) {
			return false;
		}
		return true;
	}

	public Annotation[] getAnnotationsAsArray() {
		return annotations.values().toArray(new Annotation[annotations.size()]);
	}

	public Collection<Annotation> getAnnotations() {
		return annotations.values();
	}

	public Set<Class<? extends Annotation>> getAnnotationTypes() {
		return annotations.keySet();
	}

	public Class<?> getBaseClass() {
		return toClassArray()[0];
	}

	public Set<Type> getTypes() {
		return types;
	}

	public Class<?>[] toClassArray() {
		return types.stream().map(t -> TypeUtils.getRawType(t, null)).sorted(CLASS_HIERARCHY_COMPARATOR).toArray(Class[]::new);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotations == null) ? 0 : annotations.hashCode());
		result = prime * result + ((types == null) ? 0 : types.hashCode());
		return result;
	}

	public boolean isAssignableFrom(TypeClosure closure) {
		for (Type toType : types) {
			if (!closure.isAssignableTo(toType)) {
				return false;
			}
		}
		for (Annotation toAnnotation : annotations.values()) {
			if (!closure.containsAnnotation(toAnnotation)) {
				return false;
			}
		}
		return true;
	}

	public boolean isAssignableTo(Type toType) {
		for (Type fromType : this.types) {
			if (TypeUtils.isAssignable(fromType, toType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<Type> iterator() {
		return types.iterator();
	}

	public TypeClosure replaceTyped(Annotation qualifier) {
		return replaceTyped(Collections.singleton(qualifier));
	}

	public TypeClosure replaceTyped(Collection<Annotation> annotations) {
		Set<Class<? extends Annotation>> annotationTypes = new TypeClosure(checker, Collections.<Type>emptySet(), annotations).getAnnotationTypes();
		return withoutAnnotations(annotationTypes).withAnnotations(annotations);
	}

	public TypeClosure withAnnotations(Collection<Annotation> annotations) {
		Set<Annotation> copy = new HashSet<>(this.annotations.values());
		return copy.addAll(annotations) ? new TypeClosure(checker, types, copy) : this;
	}

	public TypeClosure withoutAnnotations(Iterable<Class<? extends Annotation>> annotationTypes) {
		Set<Annotation> copy = new HashSet<>(this.annotations.values());
		Iterator<Annotation> iterator = copy.iterator();
		boolean changed = false;
		while (iterator.hasNext()) {
			Class<? extends Annotation> candidateType = iterator.next().annotationType();
			for (Class<? extends Annotation> annotationType : annotationTypes) {
				if (candidateType == annotationType) {
					iterator.remove();
					changed = true;
					break;
				}
			}
		}
		return changed ? new TypeClosure(checker, types, copy) : this;
	}

	public TypeClosure withoutDefaultQualifiers() {
		return withoutAnnotations(Arrays.asList(Default.class, New.class, Any.class));
	}

	public TypeClosure withoutTypes(Collection<? extends Type> types) {
		Set<Type> copy = new HashSet<>(this.types);
		return copy.removeAll(types) ? new TypeClosure(checker, copy, annotations.values()) : this;
	}

	public TypeClosure withTypes(Collection<? extends Type> types) {
		Set<Type> copy = new HashSet<>(this.types);
		return copy.addAll(types) ? new TypeClosure(checker, copy, annotations.values()) : this;
	}
}

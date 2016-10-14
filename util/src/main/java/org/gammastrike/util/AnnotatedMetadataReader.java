package org.gammastrike.util;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.deltaspike.core.api.literal.DependentScopeLiteral;
import org.apache.deltaspike.core.util.bean.BeanBuilder;

/**
 * Annotation reader for various annotated sources.
 *
 * @author sven.linstaedt
 *
 * @param <T>
 *            bean type
 */
public abstract class AnnotatedMetadataReader<T> {

	/**
	 * Special annotations types.
	 *
	 * @author sven.linstaedt
	 */
	public enum AnnotationMetaType {
		QUALIFIER() {

			@Override
			public boolean is(BeanManager beanManager, Class<? extends Annotation> annotationType) {
				return beanManager.isQualifier(annotationType);
			}
		},
		SCOPE() {

			@Override
			public boolean is(BeanManager beanManager, Class<? extends Annotation> annotationType) {
				return beanManager.isScope(annotationType);
			}
		},
		NORMAL_SCOPE() {

			@Override
			public boolean is(BeanManager beanManager, Class<? extends Annotation> annotationType) {
				return beanManager.isNormalScope(annotationType);
			}
		},
		PASSIVATING_SCOPE() {

			@Override
			public boolean is(BeanManager beanManager, Class<? extends Annotation> annotationType) {
				return beanManager.isPassivatingScope(annotationType);
			}
		},
		STEREOTYPE() {

			@Override
			public boolean is(BeanManager beanManager, Class<? extends Annotation> annotationType) {
				return beanManager.isStereotype(annotationType);
			}
		},
		INTERCEPTOR_BINDING() {

			@Override
			public boolean is(BeanManager beanManager, Class<? extends Annotation> annotationType) {
				return beanManager.isInterceptorBinding(annotationType);
			}
		};

		public abstract boolean is(BeanManager beanManager, Class<? extends Annotation> annotationType);
	}

	private static class CDIAnnotatedMetadataReader<T> extends AnnotatedMetadataReader<T> {

		private final Annotated annotated;

		protected CDIAnnotatedMetadataReader(Annotated annotated, Map<AnnotationMetaType, Set<Annotation>> metadata) {
			super(metadata);
			this.annotated = requireNonNull(annotated, "annotated");
		}

		@Override
		public Type getBaseType() {
			return annotated.getBaseType();
		}

		@Override
		public Set<Type> getTypeClosure() {
			return annotated.getTypeClosure();
		}
	}

	private static class JavaMemberMetadataReader<T> extends AnnotatedMetadataReader<T> {

		private final Type baseType;

		private Set<Type> typeClosure;

		protected JavaMemberMetadataReader(Type baseType, AccessibleObject member, Map<AnnotationMetaType, Set<Annotation>> metadata) {
			this(baseType, metadata);
		}

		protected JavaMemberMetadataReader(Type baseType, Map<AnnotationMetaType, Set<Annotation>> metadata) {
			super(metadata);
			this.baseType = requireNonNull(baseType, "baseType");
			this.typeClosure = collectTypes(baseType, new HashSet<Type>());
		}

		@Override
		public Type getBaseType() {
			return baseType;
		}

		@Override
		public Set<Type> getTypeClosure() {
			return Collections.unmodifiableSet(typeClosure);
		}
	}

	public static <T> AnnotatedMetadataReader<T> create(BeanManager manager, Annotated annotated) {
		Annotation[] annotations = annotated.getAnnotations().toArray(new Annotation[annotated.getAnnotations().size()]);
		return new CDIAnnotatedMetadataReader<>(annotated, resolve(manager, annotations));
	}

	public static <T> AnnotatedMetadataReader<T> create(BeanManager manager, Bean<T> bean) {
		Map<AnnotationMetaType, Set<Annotation>> metadata = Collections.<AnnotationMetaType, Set<Annotation>>singletonMap(AnnotationMetaType.QUALIFIER,
				bean.getQualifiers());
		return new JavaMemberMetadataReader<>(bean.getBeanClass(), metadata);
	}

	public static <T> AnnotatedMetadataReader<T> create(BeanManager manager, Class<T> type) {
		AnnotatedType<T> annotatedType = manager.createAnnotatedType(type);
		return create(manager, annotatedType);
	}

	public static <T> AnnotatedMetadataReader<T> create(BeanManager manager, InjectionPoint ip) {
		return create(manager, ip.getAnnotated());
	}

	public static <T> AnnotatedMetadataReader<T> create(BeanManager manager, Field field) {
		return new JavaMemberMetadataReader<>(field.getGenericType(), field, resolve(manager, field.getAnnotations()));
	}

	public static <T> AnnotatedMetadataReader<T> create(BeanManager manager, Method method) {
		return new JavaMemberMetadataReader<>(method.getGenericReturnType(), method, resolve(manager, method.getAnnotations()));
	}

	public static <T> AnnotatedMetadataReader<T> create(BeanManager manager, Constructor<T> c) {
		return new JavaMemberMetadataReader<>(c.getDeclaringClass(), c, resolve(manager, c.getAnnotations()));
	}

	public static <T> AnnotatedMetadataReader<T> create(Type type, Annotation... qualifiers) {
		Map<AnnotationMetaType, Set<Annotation>> metadata = Collections.<AnnotationMetaType, Set<Annotation>>singletonMap(AnnotationMetaType.QUALIFIER,
				new HashSet<>(Arrays.asList(qualifiers)));
		return new JavaMemberMetadataReader<>(type, metadata);
	}

	private static Map<AnnotationMetaType, Set<Annotation>> resolve(BeanManager manager, Annotation[] annotations) {
		Map<AnnotationMetaType, Set<Annotation>> result = new EnumMap<>(AnnotationMetaType.class);
		for (AnnotationMetaType metaType : AnnotationMetaType.values()) {
			result.put(metaType, new HashSet<Annotation>(1));
		}
		lookup(manager, result, annotations);
		Set<Annotation> scopes = result.get(AnnotationMetaType.SCOPE);
		if (scopes != null && scopes.isEmpty()) {
			scopes.add(new DependentScopeLiteral());
		}
		return result;
	}

	private static Set<Type> collectTypes(Type type, Set<Type> result) {
		if (type instanceof Class) {
			Class<?> clazz = (Class<?>) type;
			collectTypes(clazz.getGenericSuperclass(), result);
			for (Type genericInterface : clazz.getGenericInterfaces()) {
				collectTypes(genericInterface, result);
			}
		} else if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			collectTypes(parameterizedType.getRawType(), result);
		}
		return result;
	}

	private static void lookup(BeanManager beanManager, Map<AnnotationMetaType, Set<Annotation>> result, Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			for (AnnotationMetaType metaType : AnnotationMetaType.values()) {
				if (metaType.is(beanManager, annotation.annotationType())) {
					result.get(metaType).add(annotation);
				}
			}
			if (AnnotationMetaType.STEREOTYPE.is(beanManager, annotation.annotationType())) {
				lookup(beanManager, result, annotation.annotationType().getAnnotations());
			}
		}
	}

	private final Map<AnnotationMetaType, Set<Annotation>> annotationByMetaType;

	protected AnnotatedMetadataReader(Map<AnnotationMetaType, Set<Annotation>> metadata) {
		this.annotationByMetaType = new HashMap<>(metadata);
	}

	public abstract Type getBaseType();

	public abstract Set<Type> getTypeClosure();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getBaseType().hashCode();
		result = prime * result + getQualifiers().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AnnotatedMetadataReader)) {
			return false;
		}
		AnnotatedMetadataReader<?> other = (AnnotatedMetadataReader<?>) obj;
		if (!getBaseType().equals(other.getBaseType())) {
			return false;
		}
		if (!getQualifiers().equals(other.getQualifiers())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AnnotatedMetadataReader[type=" + getBaseType() + ", qualifiers=" + getQualifiers() + "]";
	}

	@SuppressWarnings("unchecked")
	public Class<? super T> getBaseClass() {
		Type baseType = getBaseType();
		if (baseType instanceof Class) {
			return (Class<? super T>) baseType;
		} else if (baseType instanceof ParameterizedType) {
			return (Class<? super T>) ((ParameterizedType) baseType).getRawType();
		} else {
			return Object.class;
		}
	}

	public Set<Annotation> annotations(AnnotationMetaType... metaTypes) {
		switch (metaTypes.length) {
		case 0:
			return Collections.emptySet();
		case 1:
			Set<Annotation> annotations = annotationByMetaType.get(metaTypes[0]);
			return annotations != null ? Collections.unmodifiableSet(annotations) : Collections.<Annotation>emptySet();
		default:
			Set<Annotation> result = new HashSet<>();
			for (AnnotationMetaType metaType : metaTypes) {
				result.addAll(annotationByMetaType.get(metaType));
			}
			return result;
		}
	}

	public Annotation annotation(AnnotationMetaType... metaTypes) {
		Set<Annotation> annotations = annotations(metaTypes);
		switch (annotations.size()) {
		case 0:
			throw new IllegalStateException("No annotation of type " + Arrays.asList(metaTypes) + " found");
		case 1:
			return annotations.iterator().next();
		default:
			throw new IllegalStateException("Multiple annotation of type " + Arrays.asList(metaTypes) + " found: " + annotations);
		}
	}

	public Set<Annotation> getQualifiers() {
		return annotations(AnnotationMetaType.QUALIFIER);
	}

	public Annotation[] getQualifierArray() {
		Set<Annotation> qualifiers = getQualifiers();
		return qualifiers.toArray(new Annotation[qualifiers.size()]);
	}

	public Annotation getScope() {
		return annotation(AnnotationMetaType.SCOPE, AnnotationMetaType.NORMAL_SCOPE, AnnotationMetaType.PASSIVATING_SCOPE);
	}

	public Set<Class<? extends Annotation>> getStereoTypes() {
		Set<Class<? extends Annotation>> result = new HashSet<>();
		for (Annotation stereoType : annotations(AnnotationMetaType.STEREOTYPE)) {
			result.add(stereoType.annotationType());
		}
		return result;
	}

	public Set<Annotation> getInterceptionBindings() {
		return annotations(AnnotationMetaType.INTERCEPTOR_BINDING);
	}

	public void writeTo(BeanBuilder<T> builder) {
		builder.beanClass(getBaseClass());
		builder.addTypes(getTypeClosure());
		builder.scope(getScope().annotationType());
		builder.addQualifiers(getQualifiers());
		builder.stereotypes(getStereoTypes());
	}

	public BeanBuilder<T> buildBean(BeanManager manager) {
		BeanBuilder<T> builder = new BeanBuilder<>(manager);
		builder.qualifiers(new HashSet<Annotation>()).types(new HashSet<Type>()); // fix NPE
		writeTo(builder);
		return builder;
	}

	@SuppressWarnings("unchecked")
	public Set<Bean<? extends T>> getBeans(BeanManager manager) {
		Set<Bean<? extends T>> result = new HashSet<>();
		Set<Bean<?>> beans = manager.getBeans(getBaseType(), getQualifierArray());
		for (Bean<?> bean : beans) {
			result.add((Bean<? extends T>) bean);
		}
		return result;
	}

	public Bean<? extends T> resolveBean(BeanManager manager) {
		Set<Bean<? extends T>> beans = getBeans(manager);
		Bean<? extends T> resolved = manager.resolve(beans);
		return resolved;
	}

	public Bean<? extends T> createOrResolveBean(BeanManager manager) {
		Bean<? extends T> bean = resolveBean(manager);
		if (bean == null) {
			bean = buildBean(manager).create();
		}
		return bean;
	}

	public T getReference(BeanManager manager, Bean<? extends T> bean) {
		CreationalContext<? extends T> context = manager.createCreationalContext(bean);
		T reference = getRerefence(manager, bean, context);
		return reference;
	}

	@SuppressWarnings("unchecked")
	public T getRerefence(BeanManager manager, Bean<? extends T> bean, CreationalContext<? extends T> context) {
		return (T) manager.getReference(bean, getBaseType(), context);
	}

	public T createOrResolveInstance(BeanManager manager) {
		return getReference(manager, createOrResolveBean(manager));
	}

	public T resolveInstance(BeanManager manager) {
		return getReference(manager, resolveBean(manager));
	}
}
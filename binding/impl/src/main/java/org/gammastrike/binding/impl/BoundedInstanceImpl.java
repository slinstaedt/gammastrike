package org.gammastrike.binding.impl;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;

import org.gammastrike.binding.api.BoundedInstance;

//@Dependent
@Vetoed
public class BoundedInstanceImpl<T> implements BoundedInstance<T>, Serializable {

	private static final long serialVersionUID = 1L;

	public static BoundedInstance<Object> current() {
		BeanManager manager = CDI.current().getBeanManager();
		Set<Annotation> qualifiers = Collections.emptySet();
		return new BoundedInstanceImpl<>(manager, Object.class, qualifiers);
	}

	private final BeanManager manager;
	private final Type type;
	private final Annotation[] qualifiers;

	private CreationalContext<Instance<T>> creationalContext;
	private Instance<T> delegate;

	BoundedInstanceImpl() {
		manager = null;
		type = null;
		qualifiers = null;
		creationalContext = null;
	}

	@Inject
	BoundedInstanceImpl(BeanManager manager, InjectionPoint ip) {
		this(manager, ip.getType(), ip.getQualifiers());
	}

	private BoundedInstanceImpl(BeanManager manager, Type type, Collection<Annotation> qualifiers) {
		this.manager = requireNonNull(manager);
		this.type = requireNonNull(type);
		this.qualifiers = qualifiers.toArray(new Annotation[qualifiers.size()]);
		this.creationalContext = manager.createCreationalContext(instanceBean());
	}

	@Override
	public void destroy(T instance) {
		instance().destroy(instance);
	}

	@Override
	public T get() {
		return instance().get();
	}

	@SuppressWarnings("unchecked")
	protected Instance<T> instance() {
		if (delegate == null) {
			Bean<Instance<T>> instanceBean = instanceBean();
			creationalContext = manager.createCreationalContext(instanceBean);
			delegate = (Instance<T>) manager.getReference(instanceBean, instanceType(), creationalContext);
		}
		return delegate;
	}

	@SuppressWarnings("unchecked")
	private Bean<Instance<T>> instanceBean() {
		ParameterizedType instanceType = instanceType();
		Set<Bean<?>> candidates = manager.getBeans(instanceType, qualifiers);
		return (Bean<Instance<T>>) manager.resolve(candidates);
	}

	private ParameterizedType instanceType() {
		return TypeBuilder.parameterizedType(Instance.class).withTypeArgument(type).build();
	}

	@Override
	public boolean isAmbiguous() {
		return instance().isAmbiguous();
	}

	@Override
	public boolean isUnsatisfied() {
		return instance().isUnsatisfied();
	}

	@Override
	public Iterator<T> iterator() {
		return instance().iterator();
	}

	@PreDestroy
	void onDestroy() {
		if (creationalContext != null) {
			creationalContext.release();
		}
	}

	@SuppressWarnings("unchecked")
	protected Bean<T> resolve() throws AmbiguousResolutionException, UnsatisfiedResolutionException {
		Set<Bean<?>> candidates = manager.getBeans(type, qualifiers);
		Bean<?> resolved = manager.resolve(candidates);
		if (resolved != null) {
			return (Bean<T>) resolved;
		} else {
			throw new UnsatisfiedResolutionException("No bean for type " + type + " and qualifers " + Arrays.asList(qualifiers));
		}
	}

	@Override
	public BoundedInstance<T> select(Annotation... qualifiers) {
		return selectInstance(type, qualifiers);
	}

	@Override
	public <U extends T> BoundedInstance<U> select(Class<U> subtype, Annotation... qualifiers) {
		return selectInstance(subtype, qualifiers);
	}

	@Override
	public <U extends T> BoundedInstance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
		return selectInstance(subtype.getType(), qualifiers);
	}

	private <U extends T> BoundedInstance<U> selectInstance(Type subtype, Annotation[] newQualifiers) {
		List<Annotation> annotations = new ArrayList<>(Arrays.asList(this.qualifiers));
		annotations.addAll(Arrays.asList(newQualifiers));
		return new BoundedInstanceImpl<>(manager, subtype, annotations);
	}
}

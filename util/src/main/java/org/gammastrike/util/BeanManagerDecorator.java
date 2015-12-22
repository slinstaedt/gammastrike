package org.gammastrike.util;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InjectionTargetFactory;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProducerFactory;

/**
 * Abstract {@link BeanManager} implementation, that delegates all invocations to the instance returned by {@link #delegate()}.
 *
 * @author sven.linstaedt
 */
public abstract class BeanManagerDecorator implements BeanManager, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean areInterceptorBindingsEquivalent(Annotation interceptorBinding1, Annotation interceptorBinding2) {
		return delegate().areInterceptorBindingsEquivalent(interceptorBinding1, interceptorBinding2);
	}

	@Override
	public boolean areQualifiersEquivalent(Annotation qualifier1, Annotation qualifier2) {
		return delegate().areQualifiersEquivalent(qualifier1, qualifier2);
	}

	@Override
	public <T> AnnotatedType<T> createAnnotatedType(Class<T> type) {
		return delegate().createAnnotatedType(type);
	}

	@Override
	public <T> Bean<T> createBean(BeanAttributes<T> attributes, Class<T> beanClass, InjectionTargetFactory<T> injectionTargetFactory) {
		return delegate().createBean(attributes, beanClass, injectionTargetFactory);
	}

	@Override
	public <T, X> Bean<T> createBean(BeanAttributes<T> attributes, Class<X> beanClass, ProducerFactory<X> producerFactory) {
		return delegate().createBean(attributes, beanClass, producerFactory);
	}

	@Override
	public BeanAttributes<?> createBeanAttributes(AnnotatedMember<?> type) {
		return delegate().createBeanAttributes(type);
	}

	@Override
	public <T> BeanAttributes<T> createBeanAttributes(AnnotatedType<T> type) {
		return delegate().createBeanAttributes(type);
	}

	@Override
	public <T> CreationalContext<T> createCreationalContext(Contextual<T> contextual) {
		return delegate().createCreationalContext(contextual);
	}

	@Override
	public InjectionPoint createInjectionPoint(AnnotatedField<?> field) {
		return delegate().createInjectionPoint(field);
	}

	@Override
	public InjectionPoint createInjectionPoint(AnnotatedParameter<?> parameter) {
		return delegate().createInjectionPoint(parameter);
	}

	@Override
	public <T> InjectionTarget<T> createInjectionTarget(AnnotatedType<T> type) {
		return delegate().createInjectionTarget(type);
	}

	/**
	 * Returns the delegation instance
	 * 
	 * @return the delegation instance
	 */
	public abstract BeanManager delegate();

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BeanManagerDecorator) {
			BeanManagerDecorator that = (BeanManagerDecorator) obj;
			return delegate().equals(that.delegate());
		}
		return delegate().equals(obj);
	}

	@Override
	public void fireEvent(Object event, Annotation... qualifiers) {
		delegate().fireEvent(event, qualifiers);
	}

	@Override
	public Set<Bean<?>> getBeans(String name) {
		return delegate().getBeans(name);
	}

	@Override
	public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
		return delegate().getBeans(beanType, qualifiers);
	}

	@Override
	public Context getContext(Class<? extends Annotation> scopeType) {
		return delegate().getContext(scopeType);
	}

	@Override
	public ELResolver getELResolver() {
		return delegate().getELResolver();
	}

	@Override
	public <T extends Extension> T getExtension(Class<T> extensionClass) {
		return delegate().getExtension(extensionClass);
	}

	@Override
	public Object getInjectableReference(InjectionPoint ij, CreationalContext<?> ctx) {
		return delegate().getInjectableReference(ij, ctx);
	}

	@Override
	public <T> InjectionTargetFactory<T> getInjectionTargetFactory(AnnotatedType<T> annotatedType) {
		return delegate().getInjectionTargetFactory(annotatedType);
	}

	@Override
	public Set<Annotation> getInterceptorBindingDefinition(Class<? extends Annotation> bindingType) {
		return delegate().getInterceptorBindingDefinition(bindingType);
	}

	@Override
	public int getInterceptorBindingHashCode(Annotation interceptorBinding) {
		return delegate().getInterceptorBindingHashCode(interceptorBinding);
	}

	@Override
	public Bean<?> getPassivationCapableBean(String id) {
		return delegate().getPassivationCapableBean(id);
	}

	@Override
	public <X> ProducerFactory<X> getProducerFactory(AnnotatedField<? super X> field, Bean<X> declaringBean) {
		return delegate().getProducerFactory(field, declaringBean);
	}

	@Override
	public <X> ProducerFactory<X> getProducerFactory(AnnotatedMethod<? super X> method, Bean<X> declaringBean) {
		return delegate().getProducerFactory(method, declaringBean);
	}

	@Override
	public int getQualifierHashCode(Annotation qualifier) {
		return delegate().getQualifierHashCode(qualifier);
	}

	@Override
	public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> ctx) {
		return delegate().getReference(bean, beanType, ctx);
	}

	@Override
	public Set<Annotation> getStereotypeDefinition(Class<? extends Annotation> stereotype) {
		return delegate().getStereotypeDefinition(stereotype);
	}

	@Override
	public int hashCode() {
		return delegate().hashCode();
	}

	@Override
	public boolean isInterceptorBinding(Class<? extends Annotation> annotationType) {
		return delegate().isInterceptorBinding(annotationType);
	}

	@Override
	public boolean isNormalScope(Class<? extends Annotation> annotationType) {
		return delegate().isNormalScope(annotationType);
	}

	@Override
	public boolean isPassivatingScope(Class<? extends Annotation> annotationType) {
		return delegate().isPassivatingScope(annotationType);
	}

	@Override
	public boolean isQualifier(Class<? extends Annotation> annotationType) {
		return delegate().isQualifier(annotationType);
	}

	@Override
	public boolean isScope(Class<? extends Annotation> annotationType) {
		return delegate().isScope(annotationType);
	}

	@Override
	public boolean isStereotype(Class<? extends Annotation> annotationType) {
		return delegate().isStereotype(annotationType);
	}

	@Override
	public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
		return delegate().resolve(beans);
	}

	@Override
	public List<Decorator<?>> resolveDecorators(Set<Type> types, Annotation... qualifiers) {
		return delegate().resolveDecorators(types, qualifiers);
	}

	@Override
	public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings) {
		return delegate().resolveInterceptors(type, interceptorBindings);
	}

	@Override
	public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(T event, Annotation... qualifiers) {
		return delegate().resolveObserverMethods(event, qualifiers);
	}

	@Override
	public String toString() {
		return delegate().toString();
	}

	@Override
	public void validate(InjectionPoint injectionPoint) {
		delegate().validate(injectionPoint);
	}

	@Override
	public ExpressionFactory wrapExpressionFactory(ExpressionFactory expressionFactory) {
		return delegate().wrapExpressionFactory(expressionFactory);
	}
}

package org.gammastrike.proxy.impl;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;

import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.gammastrike.binding.api.annotation.BoundWith;
import org.gammastrike.binding.api.event.ProcessBoundedTypes;
import org.gammastrike.proxy.api.BeanProxyFactory;
import org.gammastrike.proxy.api.Proxy;
import org.gammastrike.proxy.api.annotation.Proxied;
import org.gammastrike.util.BeanTypeRegistry;
import org.gammastrike.util.ProducerBasedInjectionTarget;
import org.gammastrike.value.TypeClosure;

public class ProxyingScopeExtension implements Extension {

	private static class ProxyingInjectionTarget<T> implements InjectionTarget<T> {

		public static <X> Producer<X> decorate(ProcessProducer<?, X> event, BeanTypeRegistry registry) {
			AnnotatedMember<?> annotatedMember = event.getAnnotatedMember();
			return new ProxyingInjectionTarget<>();
		}

		public static <X> InjectionTarget<X> decorate(ProcessInjectionTarget<X> event, BeanTypeRegistry registry) {
			AnnotatedType<X> annotatedType = event.getAnnotatedType();
			return new ProxyingInjectionTarget<>();
		}

		private InjectionTarget<T> delegate;
		private BeanTypeRegistry registry;
		private Annotated annotated;
		private BeanProxyFactory factory;
		private InjectionPoint injectionPoint;

		@Override
		public T produce(CreationalContext<T> ctx) {
			@SuppressWarnings("unchecked")
			Bean<T> bean = (Bean<T>) registry.beanOf(annotated);
			Handler<T> handler = new Handler<>();
			return factory.createBeanProxy(bean, handler);
		}

		@Override
		public void dispose(T instance) {
			if (instance instanceof Proxy) {
				UUID identity = ((Proxy) instance).getIdentity();
				// TODO trigger destruction event
			}
		}

		@Override
		public Set<InjectionPoint> getInjectionPoints() {
			return Collections.singleton(injectionPoint);
		}

		@Override
		public void inject(T instance, CreationalContext<T> ctx) {
			// only constructor injection supported for handler
		}

		@Override
		public void postConstruct(T instance) {
			// only constructor injection supported for handler
		}

		@Override
		public void preDestroy(T instance) {
			// only constructor injection supported for handler
		}
	}

	private static class Handler<T> implements InvocationHandler, Proxy, Serializable {

		private static final long serialVersionUID = 1L;

		private final InjectionPoint injectionPoint;
		private final UUID identity;

		public Handler() {
			this.injectionPoint = CDI.current().select(InjectionPoint.class).get();
			this.identity = UUID.randomUUID();
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (getClass().isAssignableFrom(method.getDeclaringClass())) {
				return method.invoke(this, args);
			} else {
				// TODO Auto-generated method stub
				return null;
			}
		}

		@Override
		public UUID getIdentity() {
			return identity;
		}
	}

	private final Set<Annotated> proxiedTypes;
	private final Map<Annotated, InjectionTarget<?>> injectionTargets;

	public ProxyingScopeExtension() {
		this.proxiedTypes = new HashSet<>();
		this.injectionTargets = new HashMap<>();
	}

	private InjectionPoint createInjectionPoint(BeanManager manager) {
		AnnotatedType<Handler> type = manager.createAnnotatedType(Handler.class);
		for (AnnotatedConstructor<Handler> constructor : type.getConstructors()) {
			for (AnnotatedParameter<Handler> parameter : constructor.getParameters()) {
				if (parameter.getBaseType() == InjectionPoint.class) {
					return manager.createInjectionPoint(parameter);
				}
			}
		}
		throw new AssertionError("Could not find field of " + InjectionPoint.class + " in " + Handler.class);
	}

	<X> void proxyProducer(@Observes ProcessProducer<?, X> event, BeanManager manager) {
		injectionTargets.put(event.getAnnotatedMember(), new ProducerBasedInjectionTarget<>(event.getProducer()));
		BeanTypeRegistry registry = manager.getExtension(BeanTypeRegistry.class);
		event.setProducer(ProxyingInjectionTarget.decorate(event, registry));
	}

	<X> void proxyInjectionTarget(@Observes ProcessInjectionTarget<X> event, BeanManager manager) {
		injectionTargets.put(event.getAnnotatedType(), event.getInjectionTarget());
		BeanTypeRegistry registry = manager.getExtension(BeanTypeRegistry.class);
		event.setInjectionTarget(ProxyingInjectionTarget.decorate(event, registry));
	}

	void proxyType(@Observes @BoundWith(Proxied.class) ProcessBoundedTypes event) {
		proxiedTypes.addAll(event.getAnnotateds());
	}

	void vetoProxiedBeans(@Observes ProcessBeanAttributes<?> event) {
		if (proxiedTypes.contains(event.getAnnotated())) {
			event.veto();
		}
	}

	void registeredProxyBeans(@Observes AfterBeanDiscovery event, BeanManager manager) {
		for (Annotated annotated : proxiedTypes) {
			TypeClosure closure = TypeClosure.from(annotated);
			Bean<Object> bean = new BeanBuilder<Object>(manager).beanClass(closure.getBaseClass()).types(closure.getTypes()).scope(Dependent.class).create();
			event.addBean(bean);
		}
	}

	void onShutdown(@Observes BeforeShutdown event) {
		proxiedTypes.clear();
		injectionTargets.clear();
	}
}

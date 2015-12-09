package org.gammastrike.proxy;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;

import javax.annotation.PreDestroy;
import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;

import org.gammastrike.literal.DestroyedLiteral;
import org.gammastrike.proxy.api.BeanProxyFactory;

public class ProxyingContext implements AlterableContext {

	private static final TypeLiteral<Event<Contextual<?>>> CONTEXTUAL_EVENT_TYPE = new TypeLiteral<Event<Contextual<?>>>() {

		private static final long serialVersionUID = 1L;
	};

	private final Class<? extends Annotation> scope;

	private BeanProxyFactory proxyFactory;
	private Event<Contextual<?>> destroyedTrigger;

	public ProxyingContext(Class<? extends Annotation> scope) {
		this.scope = requireNonNull(scope);
	}

	@Override
	public void destroy(Contextual<?> contextual) {
		if (contextual instanceof Bean) {
			destroyedTrigger.select(Bean.class).fire((Bean<?>) contextual);
		} else {
			destroyedTrigger.fire(contextual);
		}
	}

	@Override
	public <T> T get(Contextual<T> contextual) {
		return proxyFactory.createBeanProxy(contextual, null);
	}

	@Override
	public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
		return proxyFactory.createBeanProxy(contextual, creationalContext);
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return scope;
	}

	private void initIfNecessary() {
		if (proxyFactory == null) {
			initWith(CDI.current());
		}
	}

	@Inject
	void initWith(Instance<Object> instance) {
		this.proxyFactory = instance.select(BeanProxyFactory.class).get();
		this.destroyedTrigger = instance.select(CONTEXTUAL_EVENT_TYPE, DestroyedLiteral.of(scope)).get();
	}

	@Override
	public boolean isActive() {
		initIfNecessary();
		return proxyFactory != null;
	}

	@PreDestroy
	void onDestroy() {
		this.proxyFactory = null;
		this.destroyedTrigger = null;
	}
}

package org.gammastrike.store.impl;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.Unmanaged;
import javax.enterprise.inject.spi.Unmanaged.UnmanagedInstance;

import org.gammastrike.binding.api.event.ProcessBoundedTypes;
import org.gammastrike.binding.api.event.ProcessTypeBindings;

public class BeanStoreExtension implements Extension {

	private UnmanagedInstance<BeanProxyingContext> contextInstance;

	void checkBindingAnnotations(@Observes ProcessBoundedTypes event, BeanManager manager) {
		// TODO
	}

	void init(@Observes BeforeBeanDiscovery event, BeanManager manager) {
		contextInstance = new Unmanaged<>(manager, BeanProxyingContext.class).newInstance();
	}

	void initContextWithFactory(@Observes AfterDeploymentValidation event, BeanManager manager) {
		contextInstance.inject().postConstruct();
	}

	void initProxyContext(@Observes AfterBeanDiscovery event, BeanManager manager) {
		event.addContext(contextInstance.produce().get());
	}

	void onShutdown(@Observes BeforeShutdown event) {
		contextInstance.preDestroy().dispose();
	}

	<X> void readBindings(@Observes ProcessTypeBindings event, BeanManager manager) {
		// BeanBinding<X> binding = bindingReader.processObserved(event);
		// if (binding.isBound()) {
		// if (binding.getAttributes().getScope() != Dependent.class) {
		// event.addDefinitionError(new DefinitionException("Bean must have @Dependent scope: " + binding));
		// }
		// if (!binding.isAssignableTo(BeanStore.class)) {
		// MutableBeanAttributes<X> attributes = new MutableBeanAttributes<>(binding.getAttributes());
		// attributes.setScope(Proxied.class);
		// event.setBeanAttributes(attributes);
		// }
		// }
	}
}

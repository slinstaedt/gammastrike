package org.gammastrike.binding.impl;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.gammastrike.binding.api.BindingsScanner;
import org.gammastrike.binding.api.BoundedGroup;
import org.gammastrike.binding.api.event.BeforeBindingDiscovery;
import org.gammastrike.binding.api.event.ProcessBoundedGroup;
import org.gammastrike.binding.api.event.ProcessBoundedTypes;
import org.gammastrike.binding.api.event.ProcessTypeBindings;

public class BindingExtension implements Extension {

	private BindingsScanner scanner;

	void beforeBindingDiscovery(@Observes BeforeBeanDiscovery event, BeanManager manager) {
		scanner = new BindingsScanner(manager);
		BeforeBindingDiscovery.fire(manager, scanner);
	}

	void onShutdown(@Observes BeforeShutdown event) {
		scanner = null;
	}

	void processBoundedTypes(@Observes AfterTypeDiscovery event, BeanManager manager) {
		ProcessBoundedTypes.fire(manager, scanner);
		Iterable<BoundedGroup> boundedGroups = scanner.buildBoundedGroups();
		ProcessBoundedGroup.fire(manager, boundedGroups);
	}

	<X> void processTypeBindings(@Observes ProcessAnnotatedType<X> event, BeanManager manager) {
		ProcessTypeBindings.fire(manager, scanner, event);
	}

	void throwDefinitionErrors(@Observes AfterBeanDiscovery event) {
		for (Throwable error : scanner.getDefinitionErrors()) {
			event.addDefinitionError(error);
		}
	}
}

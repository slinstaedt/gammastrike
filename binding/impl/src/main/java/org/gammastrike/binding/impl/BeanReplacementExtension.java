package org.gammastrike.binding.impl;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBeanAttributes;

import org.gammastrike.binding.api.event.ProcessBoundedBean;
import org.gammastrike.binding.api.event.ProcessBoundedBean.Replacement;

public class BeanReplacementExtension implements Extension {

	private final Set<Replacement<?>> beanReplacements;

	public BeanReplacementExtension() {
		this.beanReplacements = new HashSet<>();
	}

	<X> void processBoundedBean(@Observes ProcessBeanAttributes<X> event, BeanManager manager) {
		Replacement<X> replacement = ProcessBoundedBean.fire(manager, event);

		if (replacement != null) {
			switch (replacement.getType()) {
			case ATTRIBUTES:
				event.setBeanAttributes(replacement.getAttributes());
				break;
			case BEAN:
				event.veto();
				beanReplacements.add(replacement);
				break;
			default:
				throw new IllegalStateException();
			}
		}
	}

	void registerReplacedBeans(@Observes AfterBeanDiscovery event) {
		for (Replacement<?> replacement : beanReplacements) {
			event.addBean(replacement.getBean());
		}
	}

	void onShutdown(@Observes BeforeShutdown event) {
		beanReplacements.clear();
	}
}

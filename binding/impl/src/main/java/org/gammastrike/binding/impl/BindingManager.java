package org.gammastrike.binding.impl;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;

import org.gammastrike.binding.api.BoundedAnnotated;
import org.gammastrike.binding.api.event.ProcessBoundedGroup;

public class BindingManager implements Extension {

	private final Map<Annotated, BoundedAnnotated> annotateds;

	public BindingManager() {
		this.annotateds = new HashMap<>();
	}

	public BoundedAnnotated boundedFor(Annotated annotated) {
		BoundedAnnotated boundedAnnotated = annotateds.get(annotated);
		if (boundedAnnotated == null) {
			boundedAnnotated = BoundedAnnotated.UNBOUND;
		}
		return boundedAnnotated;
	}

	<X> void processBean(@Observes ProcessBean<X> event) {
	}

	void processBoundedGroup(@Observes ProcessBoundedGroup event) {
		for (BoundedAnnotated annotated : event.getBoundedGroup().getBoundedAnnotateds()) {
			annotateds.put(annotated.getAnnotated(), annotated);
		}
	}

	void onShutdown(@Observes BeforeShutdown event) {
		annotateds.clear();
	}
}

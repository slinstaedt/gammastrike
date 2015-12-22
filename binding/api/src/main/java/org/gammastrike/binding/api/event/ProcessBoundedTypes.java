package org.gammastrike.binding.api.event;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.BeanManager;

import org.gammastrike.binding.api.Binding;
import org.gammastrike.binding.api.BindingsScanner;

public class ProcessBoundedTypes {

	public static void fire(BeanManager manager, BindingsScanner scanner) {
		for (Binding binding : scanner.getBindings()) {
			Set<Annotated> annotateds = scanner.annotatedsFor(binding);
			ProcessBoundedTypes event = new ProcessBoundedTypes(binding, annotateds);
			manager.fireEvent(event, binding.createBoundWithQualifier());
		}
	}

	private final Binding binding;
	private final Set<Annotated> annotateds;

	public ProcessBoundedTypes(Binding binding, Set<Annotated> annotateds) {
		this.binding = requireNonNull(binding);
		this.annotateds = requireNonNull(annotateds);
	}

	public Set<Annotated> getAnnotateds() {
		return annotateds;
	}

	public Binding getBinding() {
		return binding;
	}
}

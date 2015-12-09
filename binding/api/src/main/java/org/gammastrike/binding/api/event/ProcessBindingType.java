package org.gammastrike.binding.api.event;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.gammastrike.binding.api.BindingType;
import org.gammastrike.binding.api.BindingsScanner;

public class ProcessBindingType {

	public static boolean fire(BeanManager manager, BindingsScanner scanner, BindingType bindingType) {
		ProcessBindingType event = new ProcessBindingType(bindingType);
		manager.fireEvent(event, bindingType.createBoundWithQualifier());
		if (!event.vetoed) {
			scanner.addDefinitionErrors(event.errors);
		}
		return event.vetoed;
	}

	private final BindingType bindingType;
	private final Set<Throwable> errors;

	private boolean vetoed;

	public ProcessBindingType(BindingType bindingType) {
		this.bindingType = requireNonNull(bindingType);
		this.errors = new HashSet<>();

		vetoed = false;
	}

	public void addDefinitionError(Throwable error) {
		errors.add(error);
	}

	public BindingType getBindingType() {
		return bindingType;
	}

	public boolean isVetoed() {
		return vetoed;
	}

	public void veto() {
		vetoed = true;
	}
}

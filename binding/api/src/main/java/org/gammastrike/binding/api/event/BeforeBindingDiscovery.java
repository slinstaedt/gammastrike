package org.gammastrike.binding.api.event;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.gammastrike.binding.api.BindingsScanner;
import org.gammastrike.binding.api.MetaBindingType;
import org.gammastrike.binding.api.annotation.MetaBinding.AssignmentStrategy;

public class BeforeBindingDiscovery {

	public static void fire(BeanManager manager, BindingsScanner scanner) {
		BeforeBindingDiscovery event = new BeforeBindingDiscovery(scanner);
		manager.fireEvent(event);
	}

	private final BindingsScanner scanner;

	public BeforeBindingDiscovery(BindingsScanner scanner) {
		this.scanner = requireNonNull(scanner);
	}

	public void addBindingType(Class<? extends Annotation> metaBindingClass, Class<? extends Annotation> bindingClass, Annotation... bindingDefinitions) {
		Set<Annotation> definitions = new HashSet<>(Arrays.asList(bindingDefinitions));
		scanner.addCustomBindingType(metaBindingClass, bindingClass, definitions);
	}

	public void addMetaBindingType(Class<? extends Annotation> metaBindingClass, AssignmentStrategy assignmentStrategy) {
		scanner.addMetaBindingType(new MetaBindingType(metaBindingClass, assignmentStrategy));
	}

	public void addMetaBindingType(MetaBindingType metaBindingType) {
		scanner.addMetaBindingType(metaBindingType);
	}
}

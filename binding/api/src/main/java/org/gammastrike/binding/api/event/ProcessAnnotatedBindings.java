package org.gammastrike.binding.api.event;

import static java.util.Objects.requireNonNull;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;

import org.gammastrike.binding.api.Bindings;
import org.gammastrike.binding.api.BindingsScanner;
import org.gammastrike.binding.api.annotation.BoundWith;

public class ProcessAnnotatedBindings/* <A extends Annotated> */{

	private final BindingsScanner scanner;
	private final Bindings originalBindings;

	private Bindings finalBindings;
	private Annotated annotated;

	public ProcessAnnotatedBindings(BindingsScanner scanner, Annotated annotated) {
		this.scanner = requireNonNull(scanner);
		this.annotated = requireNonNull(annotated);

		Bindings bindings = scanner.scan(annotated);
		this.originalBindings = bindings;
		this.finalBindings = bindings;
	}

	protected void fire(BeanManager manager) {
		BoundWith[] qualifiers = originalBindings.createBoundWithQualifiers();
		manager.fireEvent(this, qualifiers);
		scanner.addAnnotatedBindings(annotated, finalBindings);

		if (annotated instanceof AnnotatedType) {
			AnnotatedType<?> annotatedType = (AnnotatedType<?>) annotated;
			for (AnnotatedField<?> annotatedField : annotatedType.getFields()) {
				new ProcessAnnotatedBindings(scanner, annotatedField).fire(manager);
			}
			for (AnnotatedConstructor<?> annotatedConstructor : annotatedType.getConstructors()) {
				new ProcessAnnotatedBindings(scanner, annotatedConstructor).fire(manager);
			}
			for (AnnotatedMethod<?> annotatedMethod : annotatedType.getMethods()) {
				new ProcessAnnotatedBindings(scanner, annotatedMethod).fire(manager);
			}
		} else if (annotated instanceof AnnotatedCallable) {
			AnnotatedCallable<?> annotatedCallable = (AnnotatedCallable<?>) annotated;
			for (AnnotatedParameter<?> annotatedParameter : annotatedCallable.getParameters()) {
				new ProcessAnnotatedBindings(scanner, annotatedParameter).fire(manager);
			}
		}
	}

	public Annotated getAnnotated() {
		return annotated;
	}

	public Bindings getBindings() {
		return finalBindings;
	}

	public void setAnnotated(Annotated annotated) {
		this.annotated = requireNonNull(annotated);
		Bindings bindings = scanner.scan(annotated);
		finalBindings = bindings.without(originalBindings).merge(finalBindings);
	}

	public void setBindings(Bindings bindings) {
		this.finalBindings = requireNonNull(bindings);
	}

	public void vetoBindings() {
		finalBindings = Bindings.NONE;
	}
}

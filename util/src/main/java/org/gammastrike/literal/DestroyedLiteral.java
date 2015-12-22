package org.gammastrike.literal;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;

import javax.enterprise.context.Destroyed;
import javax.enterprise.util.AnnotationLiteral;

/**
 * Literal for {@link Destroyed}.
 */
public class DestroyedLiteral extends AnnotationLiteral<Destroyed> implements Destroyed {

	private static final long serialVersionUID = 1L;

	public static Destroyed of(Class<? extends Annotation> value) {
		return new DestroyedLiteral(value);
	}

	private final Class<? extends Annotation> value;

	public DestroyedLiteral(Class<? extends Annotation> value) {
		this.value = requireNonNull(value);
	}

	@Override
	public Class<? extends Annotation> value() {
		return value;
	}
}
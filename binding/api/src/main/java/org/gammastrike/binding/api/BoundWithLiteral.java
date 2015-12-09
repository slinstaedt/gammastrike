package org.gammastrike.binding.api;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;

import javax.enterprise.util.AnnotationLiteral;

import org.gammastrike.binding.api.annotation.BoundWith;

/**
 * Literal for {@link BoundWith}.
 *
 * @author sven.linstaedt
 */
public class BoundWithLiteral extends AnnotationLiteral<BoundWith> implements BoundWith {

	private static final long serialVersionUID = 1L;

	private final Class<? extends Annotation> value;

	public BoundWithLiteral(Class<? extends Annotation> value) {
		this.value = requireNonNull(value);
	}

	@Override
	public Class<? extends Annotation> value() {
		return value;
	}
}
package org.gammastrike.binding.api.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ TYPE, METHOD, FIELD, PARAMETER })
public @interface BoundWith {

	class Literal extends AnnotationLiteral<BoundWith> implements BoundWith {

		private static final long serialVersionUID = 1L;

		public static BoundWith of(Class<? extends Annotation> value) {
			return new Literal(value);
		}

		private final Class<? extends Annotation> value;

		public Literal(Class<? extends Annotation> value) {
			this.value = requireNonNull(value);
		}

		@Override
		public Class<? extends Annotation> value() {
			return value;
		}
	}

	Class<? extends Annotation> value();
}
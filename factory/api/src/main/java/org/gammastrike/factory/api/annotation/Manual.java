package org.gammastrike.factory.api.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.util.Objects.requireNonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

/**
 * {@link Qualifier} that tags any {@link javax.enterprise.inject.spi.InjectionPoint} as being supplied by a {@link ManualFactoryBinding} annotated factory
 * method parameter.
 *
 * @author sven.linstaedt
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ TYPE, METHOD, FIELD, PARAMETER })
public @interface Manual {

	public class Literal extends AnnotationLiteral<Manual> implements Manual {

		private static final long serialVersionUID = 1L;

		public static final Manual DEFAULT = new Literal("");

		public static Manual of(String value) {
			return new Literal(value);
		}

		private final String value;

		public Literal(String value) {
			this.value = requireNonNull(value, "value");
		}

		@Override
		public String value() {
			return value;
		}
	}

	String value() default "";
}
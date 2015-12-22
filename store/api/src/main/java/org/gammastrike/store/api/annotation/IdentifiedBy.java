package org.gammastrike.store.api.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.util.Objects.requireNonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ TYPE, METHOD, FIELD, PARAMETER })
public @interface IdentifiedBy {

	public class Literal extends AnnotationLiteral<IdentifiedBy> implements IdentifiedBy {

		private static final long serialVersionUID = 1L;

		private final String value;

		public Literal(String value) {
			this.value = requireNonNull(value);
		}

		@Override
		public String value() {
			return value;
		}
	}

	/**
	 * @return the bean's identifier as String
	 */
	@Nonbinding
	String value();
}
package org.gammastrike.factory.api.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

/**
 * {@link Qualifier} that tags the given given {@link javax.enterprise.context.Dependent} scoped parameter, that supplied as method parameter for a
 * {@link ManualFactoryBinding} type, to be destroyed.
 *
 * @author sven.linstaedt
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ TYPE, METHOD, FIELD, PARAMETER })
public @interface Destroy {

	public class Literal extends AnnotationLiteral<Destroy> implements Destroy {

		private static final long serialVersionUID = 1L;

		public static final Destroy INSTANCE = new Literal();

		public Literal() {
		}
	}
}
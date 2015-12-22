package org.gammastrike.proxy.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ProxyTarget {

	class Literal extends AnnotationLiteral<ProxyTarget> implements ProxyTarget {

		private static final long serialVersionUID = 1L;
	}

	ProxyTarget INSTANCE = new Literal();
}

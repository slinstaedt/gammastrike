package org.gammastrike.literal;

import javax.enterprise.inject.Default;
import javax.enterprise.util.AnnotationLiteral;

public class DefaultLiteral extends AnnotationLiteral<Default> implements Default {

	private static final long serialVersionUID = 1L;

	public static final Default INSTANCE = new DefaultLiteral();
}

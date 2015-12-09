package org.gammastrike.literal;

import javax.enterprise.context.Dependent;
import javax.enterprise.util.AnnotationLiteral;

public class DependentLiteral extends AnnotationLiteral<Dependent> implements Dependent {

	private static final long serialVersionUID = 1L;

	public static final Dependent INSTANCE = new DependentLiteral();
}

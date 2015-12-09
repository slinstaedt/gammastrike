package org.gammastrike.factory.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.deltaspike.partialbean.api.PartialBeanBinding;

/**
 * Abstract types annotated with this annotation will have their abstract methods being handled as factory method. Any parameters will be available for
 * {@link Manual} injection points of beans, that have the type and qualifying annotations of the factory method return type.
 *
 * @author sven.linstaedt
 */
@PartialBeanBinding
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ManualFactory {
}
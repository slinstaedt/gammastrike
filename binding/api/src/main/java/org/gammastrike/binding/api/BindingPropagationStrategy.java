package org.gammastrike.binding.api;

import javax.enterprise.inject.spi.Annotated;

public interface BindingPropagationStrategy {

	Bindings bindingsOf(Annotated annotated);
}

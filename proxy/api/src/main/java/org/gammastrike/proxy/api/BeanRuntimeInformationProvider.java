package org.gammastrike.proxy.api;

import javax.enterprise.inject.spi.Bean;

import org.gammastrike.value.TypeClosure;

public interface BeanRuntimeInformationProvider {

	TypeClosure extractImplementingClasses(Bean<?> bean);

	ClassLoader provideClassLoader(Bean<?> bean);
}

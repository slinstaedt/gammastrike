package org.gammastrike.proxy.api;

import javax.enterprise.inject.spi.Bean;

public interface BeanRuntimeInformationProvider {

	Class<?>[] extractImplementingClasses(Bean<?> bean);

	ClassLoader provideClassLoader(Bean<?> bean);
}

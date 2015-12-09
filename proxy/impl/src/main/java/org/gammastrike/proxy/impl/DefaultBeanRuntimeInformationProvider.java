package org.gammastrike.proxy.impl;

import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import org.gammastrike.proxy.api.BeanRuntimeInformationProvider;

public class DefaultBeanRuntimeInformationProvider implements BeanRuntimeInformationProvider {

	@Override
	public Class<?>[] extractImplementingClasses(Bean<?> bean) {
		Set<Type> types = bean.getTypes();
		return bean.getTypes().toArray(new Class<?>[types.size()]);
	}

	@Override
	public ClassLoader provideClassLoader(Bean<?> bean) {
		return bean.getBeanClass().getClassLoader();
	}
}

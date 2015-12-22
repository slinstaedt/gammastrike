package org.gammastrike.proxy.impl;

import javax.enterprise.inject.spi.Bean;

import org.gammastrike.proxy.api.BeanRuntimeInformationProvider;
import org.gammastrike.value.TypeClosure;

public class DefaultBeanRuntimeInformationProvider implements BeanRuntimeInformationProvider {

	@Override
	public TypeClosure extractImplementingClasses(Bean<?> bean) {
		return TypeClosure.from(bean);
	}

	@Override
	public ClassLoader provideClassLoader(Bean<?> bean) {
		return bean.getBeanClass().getClassLoader();
	}
}

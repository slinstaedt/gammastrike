package org.gammastrike.proxy.impl;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.gammastrike.proxy.api.BeanRuntimeInformationProvider;
import org.gammastrike.value.TypeClosure;

public class DefaultBeanRuntimeInformationProvider implements BeanRuntimeInformationProvider {

	@Inject
	private BeanManager manager;

	@Override
	public TypeClosure extractImplementingClasses(Bean<?> bean) {
		return TypeClosure.qualified(manager, bean);
	}

	@Override
	public ClassLoader provideClassLoader(Bean<?> bean) {
		return bean.getBeanClass().getClassLoader();
	}
}

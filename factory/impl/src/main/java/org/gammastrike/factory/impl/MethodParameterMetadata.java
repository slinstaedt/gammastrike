package org.gammastrike.factory.impl;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Bean;

import org.gammastrike.factory.api.annotation.Destroy;

/**
 * Type that holds relevant data for factory methods.
 *
 * @author sven.linstaedt
 */
public class MethodParameterMetadata {

	private final AnnotatedMethod<?> method;
	private final Bean<?> returnBean;
	private final Map<Bean<?>, Integer> parameterBeans;
	private final int[] destroyParameterIndices;

	public MethodParameterMetadata(AnnotatedMethod<?> method, Bean<?> returnBean, List<Bean<?>> parameterBeans) {
		this.method = requireNonNull(method);
		this.returnBean = requireNonNull(returnBean);

		this.parameterBeans = new HashMap<>();
		Set<Integer> destroyParameterIndices = new HashSet<>();
		for (int index = 0; index < parameterBeans.size(); index++) {
			Bean<?> parameterBean = parameterBeans.get(index);
			this.parameterBeans.put(parameterBean, index);
			for (Annotation qualifier : parameterBean.getQualifiers()) {
				if (qualifier.annotationType() == Destroy.class) {
					destroyParameterIndices.add(index);
				}
			}
		}
		this.destroyParameterIndices = new int[destroyParameterIndices.size()];
		int c = 0;
		for (Integer destroyParameterIndex : destroyParameterIndices) {
			this.destroyParameterIndices[c++] = destroyParameterIndex.intValue();
		}
	}

	public int[] getDestroyParameterIndices() {
		return destroyParameterIndices;
	}

	public AnnotatedMethod<?> getMethod() {
		return method;
	}

	public Bean<?> getReturnBean() {
		return returnBean;
	}

	public int parameterIndexFor(Bean<?> bean) {
		Integer index = parameterBeans.get(bean);
		if (index != null) {
			return index.intValue();
		} else {
			throw new IllegalArgumentException("Unknown bean " + bean + " for method " + method);
		}
	}
}

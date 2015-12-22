package org.gammastrike.util;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;

public class BeanTypeRegistry implements Extension {

	private static <K, V> V retrieveChecked(Map<K, ? super V> map, K key) {
		@SuppressWarnings("unchecked")
		V value = (V) map.get(key);
		if (value == null) {
			throw new IllegalArgumentException("Unknown key: " + key);
		}
		return value;
	}

	private final Map<Class<?>, AnnotatedType<?>> types;
	private final Map<Annotated, Bean<?>> beans;

	public BeanTypeRegistry() {
		this.types = new HashMap<>();
		this.beans = new HashMap<>();
	}

	void collectType(@Observes ProcessAnnotatedType<?> event) {
		AnnotatedType<?> annotatedType = event.getAnnotatedType();
		types.put(annotatedType.getJavaClass(), annotatedType);
	}

	void collectBean(@Observes ProcessBean<?> event) {
		beans.put(event.getAnnotated(), event.getBean());
	}

	void destroy(@Observes BeforeShutdown event) {
		types.clear();
		beans.clear();
	}

	public <X> AnnotatedType<X> annotatedTypeOf(Class<X> type) {
		return retrieveChecked(types, type);
	}

	public Bean<?> beanOf(Annotated annotated) {
		return retrieveChecked(beans, annotated);
	}

	public <X> Bean<X> beanOf(AnnotatedType<X> type) {
		return retrieveChecked(beans, type);
	}

	public <X> Bean<X> beanOf(Class<X> type) {
		return beanOf(annotatedTypeOf(type));
	}
}

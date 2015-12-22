package org.gammastrike.store.impl;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.gammastrike.store.api.BeanIdentifier;
import org.gammastrike.store.api.BeanInstance;
import org.gammastrike.store.api.BeanStore;
import org.gammastrike.store.api.annotation.IdentifiedBy;

@ApplicationScoped
public class StoredBeanInstanceProvider {

	@Inject
	private Instance<BeanStore> beanstores;

	protected BeanIdentifier identifiedBy(InjectionPoint ip) {
		Set<Annotation> qualifiers = new HashSet<>();
		String identifier = null;
		for (Annotation qualifier : ip.getQualifiers()) {
			if (qualifier.annotationType() == IdentifiedBy.class) {
				identifier = ((IdentifiedBy) qualifier).value();
			} else {
				qualifiers.add(qualifier);
			}
		}
		return BeanIdentifier.buildFrom(identifier, qualifiers);
	}

	@Produces
	@IdentifiedBy("")
	public <T> BeanInstance<T> lookupStoredBean(InjectionPoint ip) {
		BeanIdentifier identifier = identifiedBy(ip);
		if (identifier.isUndefined()) {
			throw new IllegalArgumentException(identifier + " for injection at " + ip + " not defined");
		}

		BeanInstance<T> instance = null;
		for (BeanStore beanStore : beanstores) {
			instance = beanStore.get(identifier);
			if (instance != null) {
				return instance;
			}
		}

		throw new IllegalStateException(identifier + " not available for injection at " + ip);
	}
}

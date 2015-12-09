package org.gammastrike.store.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.gammastrike.store.api.BeanIdentifier;
import org.gammastrike.store.api.BeanInstance;
import org.gammastrike.store.api.BeanStore;

public class SimpleBeanStore implements BeanStore {

	private final Map<BeanIdentifier, Object> beans;

	public SimpleBeanStore() {
		beans = new ConcurrentHashMap<>();
	}

	@Override
	public void clear() {
		beans.clear();
	}

	@Override
	public boolean contains(BeanIdentifier id) {
		return beans.containsKey(id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> BeanInstance<T> get(BeanIdentifier id) {
		return (BeanInstance<T>) beans.get(id);
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public Iterator<BeanIdentifier> iterator() {
		return beans.keySet().iterator();
	}

	@Override
	public <T> void put(BeanIdentifier id, BeanInstance<T> instance) {
		beans.put(id, instance);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> BeanInstance<T> remove(BeanIdentifier id) {
		return (BeanInstance<T>) beans.remove(id);
	}
}

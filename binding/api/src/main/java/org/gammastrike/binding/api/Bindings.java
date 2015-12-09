package org.gammastrike.binding.api;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.gammastrike.binding.api.annotation.BoundWith;

public class Bindings implements Iterable<Binding>, Serializable {

	public static final Bindings NONE = new Bindings();

	private static final long serialVersionUID = 1L;

	public static Bindings of(Binding binding) {
		return new Bindings(Collections.<Binding> singleton(binding));
	}

	public static Bindings of(Collection<Binding> bindings) {
		if (bindings.isEmpty()) {
			return NONE;
		} else {
			return new Bindings(new HashSet<>(bindings));
		}
	}

	private final Set<Binding> values;

	private Bindings() {
		this.values = Collections.emptySet();
	}

	private Bindings(Set<Binding> bindings) {
		this.values = Collections.unmodifiableSet(bindings);
	}

	public Set<Binding> asSet() {
		return values;
	}

	public BoundWith[] createBoundWithQualifiers() {
		BoundWith[] qualifiers = new BoundWith[values.size()];
		int i = 0;
		for (Binding binding : values) {
			qualifiers[i++] = binding.createBoundWithQualifier();
		}
		return qualifiers;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Bindings other = (Bindings) obj;
		if (values == null) {
			if (other.values != null) {
				return false;
			}
		} else if (!values.equals(other.values)) {
			return false;
		}
		return true;
	}

	public Bindings findBindingsBy(MetaBindingType metaBindingType) {
		Set<Binding> result = new HashSet<>();
		for (Binding binding : values) {
			if (binding.hasMetaBindingType(metaBindingType)) {
				result.add(binding);
			}
		}
		return new Bindings(result);
	}

	public boolean hasBinding(Binding binding) {
		return values.contains(binding);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	public boolean hasMetaBinding(MetaBindingType metaBindingType) {
		return !findBindingsBy(metaBindingType).isEmpty();
	}

	public boolean isEmpty() {
		return values.isEmpty();
	}

	@Override
	public Iterator<Binding> iterator() {
		return values.iterator();
	}

	public Bindings merge(Binding... bindings) {
		return merge(Arrays.asList(bindings));
	}

	public Bindings merge(Iterable<Binding> bindings) {
		Set<Binding> result = new HashSet<>(this.values);
		for (Binding binding : bindings) {
			result.add(binding);
		}
		return new Bindings(result);
	}

	public int size() {
		return values.size();
	}

	@Override
	public String toString() {
		return "Bindings [" + values + "]";
	}

	public Bindings without(Binding... bindings) {
		return without(Arrays.asList(bindings));
	}

	public Bindings without(Iterable<Binding> bindings) {
		Set<Binding> result = new HashSet<>(this.values);
		for (Binding binding : bindings) {
			result.remove(binding);
		}
		return new Bindings(result);
	}
}

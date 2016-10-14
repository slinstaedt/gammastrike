package org.gammastrike.value;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.New;
import javax.enterprise.inject.spi.EventMetadata;

/**
 * Helper class e.g. for generic beans with observer methods, which trigger condition is based on some required qualifiers.
 *
 * @author sven.linstaedt
 */
public class Qualifiers implements Iterable<Annotation>, Serializable {

	/**
	 * Event matcher for {@link RequiredQualifiers}, that checks if the event has the required qualifiers.
	 */
	public static class EventQualifierMatcher implements Serializable {

		private static final long serialVersionUID = 1L;

		private final Qualifiers reference;
		private final Qualifiers event;

		public EventQualifierMatcher(Qualifiers reference, Qualifiers event) {
			this.reference = requireNonNull(reference);
			this.event = requireNonNull(event);
		}

		public Qualifiers getEventQualifiers() {
			return event;
		}

		public Qualifiers getMergedQualifiers() {
			return event.without(reference).with(reference);
		}

		public Qualifiers getReferenceQualifiers() {
			return reference;
		}

		public boolean matches(Annotation qualifier) {
			return event.contains(qualifier);
		}

		public boolean matchesAll(Annotation... qualifiers) {
			return matchesAll(Arrays.asList(qualifiers));
		}

		public boolean matchesAll(Iterable<Annotation> qualifiers) {
			for (Annotation qualifier : qualifiers) {
				if (!matches(qualifier)) {
					return false;
				}
			}
			return true;
		}

		public boolean matchesAny(Annotation... qualifiers) {
			return matchesAny(Arrays.asList(qualifiers));
		}

		public boolean matchesAny(Iterable<Annotation> qualifiers) {
			for (Annotation qualifier : qualifiers) {
				if (matches(qualifier)) {
					return true;
				}
			}
			return false;
		}

		public boolean matchesEvent() {
			return matchesAll(reference);
		}
	}

	public static Qualifiers from(Annotation... requiredQualifiers) {
		return new Qualifiers(Arrays.asList(requiredQualifiers));
	}

	public static Qualifiers from(Iterable<Annotation> requiredQualifiers) {
		if (requiredQualifiers instanceof Qualifiers) {
			return (Qualifiers) requiredQualifiers;
		} else {
			return new Qualifiers(requiredQualifiers);
		}
	}

	public static Qualifiers from(EventMetadata metadata) {
		return new Qualifiers(metadata.getQualifiers());
	}

	public static final Qualifiers ANY = new Qualifiers(Collections.<Annotation>emptySet());

	private static final long serialVersionUID = 1L;

	private final Map<Class<? extends Annotation>, Annotation> annotations;

	public Qualifiers(Iterable<Annotation> qualifiers) {
		Map<Class<? extends Annotation>, Annotation> map = new HashMap<>();
		for (Annotation qualifier : qualifiers) {
			if (map.put(qualifier.annotationType(), qualifier) != null) {
				throw new IllegalArgumentException("Qualifiers " + qualifiers + "contain duplicate type" + qualifier.annotationType());
			}
		}
		this.annotations = Collections.unmodifiableMap(map);
	}

	public <X> Event<X> applyTo(Event<X> event) {
		return event.select(asArray());
	}

	public <X> Instance<X> applyTo(Instance<X> instance) {
		return instance.select(asArray());
	}

	public Annotation[] asArray() {
		Collection<Annotation> values = annotations.values();
		return values.toArray(new Annotation[values.size()]);
	}

	public Collection<Annotation> asCollection() {
		return annotations.values();
	}

	public boolean contains(Annotation qualifier) {
		return qualifier.equals(annotations.get(qualifier.annotationType()));
	}

	public boolean containsType(Class<? extends Annotation> qualifierType) {
		return getTypes().contains(qualifierType);
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
		Qualifiers other = (Qualifiers) obj;
		if (annotations == null) {
			if (other.annotations != null) {
				return false;
			}
		} else if (!annotations.equals(other.annotations)) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public <A extends Annotation> A find(Class<A> qualifierType) {
		Annotation qualifier = annotations.get(qualifierType);
		if (qualifier != null) {
			return (A) qualifier;
		} else {
			throw new IllegalArgumentException("Event qualifiers " + annotations + " does not contain a " + qualifierType);
		}
	}

	public Set<Class<? extends Annotation>> getTypes() {
		return annotations.keySet();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotations == null) ? 0 : annotations.hashCode());
		return result;
	}

	public boolean isEmpty() {
		return annotations.isEmpty();
	}

	@Override
	public Iterator<Annotation> iterator() {
		return annotations.values().iterator();
	}

	public EventQualifierMatcher matcher(Annotation... qualifiers) {
		return new EventQualifierMatcher(this, Qualifiers.from(qualifiers));
	}

	public EventQualifierMatcher matcher(EventMetadata metadata) {
		return new EventQualifierMatcher(this, Qualifiers.from(metadata));
	}

	public EventQualifierMatcher matcher(Iterable<Annotation> qualifiers) {
		return new EventQualifierMatcher(this, Qualifiers.from(qualifiers));
	}

	public Qualifiers replaceTyped(Annotation qualifier) {
		return replaceTyped(Collections.singleton(qualifier));
	}

	public Qualifiers replaceTyped(Iterable<Annotation> qualifiers) {
		Set<Class<? extends Annotation>> qualifierTypes = from(qualifiers).getTypes();
		return withoutTyped(qualifierTypes).with(qualifiers);
	}

	public int size() {
		return annotations.size();
	}

	@Override
	public String toString() {
		return "Qualifiers" + annotations.values();
	}

	public Qualifiers with(Annotation qualifier) {
		return with(Collections.singleton(qualifier));
	}

	public Qualifiers with(Iterable<Annotation> qualifiers) {
		Set<Annotation> copy = new HashSet<>(annotations.values());
		boolean changed = false;
		for (Annotation qualifier : qualifiers) {
			changed |= copy.add(qualifier);
		}
		return changed ? new Qualifiers(copy) : this;
	}

	public Qualifiers without(Annotation qualifier) {
		return without(Collections.singleton(qualifier));
	}

	public Qualifiers without(Iterable<Annotation> qualifiers) {
		Set<Annotation> copy = new HashSet<>(annotations.values());
		boolean changed = false;
		for (Annotation qualifier : qualifiers) {
			changed |= copy.remove(qualifier);
		}
		return changed ? new Qualifiers(copy) : this;
	}

	public Qualifiers withoutDefaultQualifiers() {
		return withoutTyped(Arrays.asList(Default.class, New.class, Any.class));
	}

	public Qualifiers withoutTyped(Class<? extends Annotation> qualifierType) {
		return withoutTyped(Collections.<Class<? extends Annotation>>singleton(qualifierType));
	}

	public Qualifiers withoutTyped(Iterable<Class<? extends Annotation>> qualifierTypes) {
		Set<Annotation> copy = new HashSet<>(annotations.values());
		Iterator<Annotation> iterator = copy.iterator();
		boolean changed = false;
		while (iterator.hasNext()) {
			Class<? extends Annotation> candidateType = iterator.next().annotationType();
			for (Class<? extends Annotation> qualifierType : qualifierTypes) {
				if (candidateType == qualifierType) {
					iterator.remove();
					changed = true;
					break;
				}
			}
		}
		return changed ? new Qualifiers(copy) : this;
	}
}

package org.gammastrike.binding.api;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

public class BoundedGroup implements Serializable {

	public static final BoundedGroup NONE = new BoundedGroup();

	private static final long serialVersionUID = 1L;

	private final Binding binding;
	private final Map<Annotated, BoundedAnnotated> annotateds;

	private BoundedGroup() {
		this.binding = null;
		this.annotateds = Collections.emptyMap();
	}

	public BoundedGroup(Binding binding) {
		this.binding = requireNonNull(binding);
		this.annotateds = new HashMap<>();
	}

	void attach(BoundedAnnotated annotated) {
		annotateds.put(annotated.getAnnotated(), annotated);
	}

	public BoundedAnnotated boundedAnnotated(Annotated annotated) {
		BoundedAnnotated boundedAnnotated = annotateds.get(annotated);
		if (boundedAnnotated == null) {
			boundedAnnotated = BoundedAnnotated.UNBOUND;
		}
		return boundedAnnotated;
	}

	private Set<Annotated> findMembers(Annotated annotated) {
		Set<Annotated> members = new HashSet<>();
		members.add(annotated);
		if (annotated instanceof AnnotatedType) {
			AnnotatedType<?> annotatedType = (AnnotatedType<?>) annotated;
			for (AnnotatedField<?> annotatedField : annotatedType.getFields()) {
				members.addAll(findMembers(annotatedField));
			}
			for (AnnotatedConstructor<?> annotatedConstructor : annotatedType.getConstructors()) {
				members.addAll(findMembers(annotatedConstructor));
			}
			for (AnnotatedMethod<?> annotatedMethod : annotatedType.getMethods()) {
				members.addAll(findMembers(annotatedMethod));
			}
		} else if (annotated instanceof AnnotatedCallable) {
			AnnotatedCallable<?> annotatedCallable = (AnnotatedCallable<?>) annotated;
			members.addAll(annotatedCallable.getParameters());
		}
		return members;
	}

	public Binding getBinding() {
		return binding;
	}

	public Collection<BoundedAnnotated> getBoundedAnnotateds() {
		return Collections.unmodifiableCollection(annotateds.values());
	}

	public boolean isEmpty() {
		return annotateds.isEmpty();
	}

	public Map<Annotated, BoundedAnnotated> memberBindings(Annotated annotated) {
		Map<Annotated, BoundedAnnotated> result = new HashMap<>();
		for (Annotated member : findMembers(annotated)) {
			BoundedAnnotated boundedAnnotated = boundedAnnotated(annotated);
			if (boundedAnnotated.isBound()) {
				result.put(member, boundedAnnotated);
			}
		}
		return result;
	}

	public <T> Set<BoundedAnnotated> typedMatching(Class<T> type) {
		Set<BoundedAnnotated> result = new HashSet<>();
		for (BoundedAnnotated boundedType : getBoundedAnnotateds()) {
			if (boundedType.isAssignableTo(type)) {
				result.add(boundedType);
			}
		}
		return result;
	}

	public Set<BoundedAnnotated> typedNonMatching(Class<?> type) {
		HashSet<BoundedAnnotated> result = new HashSet<>(getBoundedAnnotateds());
		result.removeAll(typedMatching(type));
		return result;
	}
}
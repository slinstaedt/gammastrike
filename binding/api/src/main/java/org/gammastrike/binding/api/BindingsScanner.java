package org.gammastrike.binding.api;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.BeanManager;

import org.gammastrike.binding.api.annotation.MetaBinding;
import org.gammastrike.binding.api.event.ProcessBindingType;

public class BindingsScanner {

	private final BeanManager manager;
	private final Set<Throwable> definitionErrors;
	private final Map<Class<? extends Annotation>, MetaBindingType> metaBindingTypes;
	private final Map<Binding, Set<Annotated>> annotatedsByBinding;

	public BindingsScanner(BeanManager manager) {
		this.manager = requireNonNull(manager);
		this.definitionErrors = new CopyOnWriteArraySet<>();
		this.metaBindingTypes = new ConcurrentHashMap<>();
		this.annotatedsByBinding = new ConcurrentHashMap<>();
	}

	public BindingType addAnalyzedBindingType(Class<? extends Annotation> metaBindingClass, Class<? extends Annotation> bindingClass) {
		MetaBindingType metaBindingType;
		if (isMetaBindingType(metaBindingClass)) {
			metaBindingType = lookupMetaBindingType(metaBindingClass);
		} else {
			MetaBinding metaBindingAnnotation = metaBindingClass.getAnnotation(MetaBinding.class);
			metaBindingType = new MetaBindingType(metaBindingClass, metaBindingAnnotation.appliesTo());
			addMetaBindingType(metaBindingType);
		}

		if (metaBindingType.isMetaBindingTypeFor(bindingClass)) {
			return metaBindingType.bindingTypeFor(bindingClass);
		} else {
			return tryCreateBindingType(metaBindingType, bindingClass, null);
		}
	}

	public void addAnnotatedBindings(Annotated annotated, Bindings bindings) {
		for (Binding binding : bindings) {
			annotatedsFor(binding).add(annotated);
		}
	}

	public void addCustomBindingType(Class<? extends Annotation> metaBindingClass, Class<? extends Annotation> bindingClass, Set<Annotation> definitions) {
		MetaBindingType metaBindingType = lookupMetaBindingType(metaBindingClass);
		tryCreateBindingType(metaBindingType, bindingClass, definitions);
	}

	public void addDefinitionErrors(Collection<Throwable> errors) {
		definitionErrors.addAll(errors);
	}

	public void addMetaBindingType(MetaBindingType metaBindingType) {
		if (metaBindingTypes.containsKey(metaBindingType.getJavaType())) {
			throw new IllegalArgumentException(metaBindingType + " is already registered");
		}

		metaBindingTypes.put(metaBindingType.getJavaType(), metaBindingType);
	}

	private void analyse(Set<Binding> bindings, Annotated annotated, Set<Annotation> candidates) {
		for (Annotation bindingCandidate : candidates) {
			Class<? extends Annotation> bindingCandidateClass = bindingCandidate.annotationType();
			if (manager.isStereotype(bindingCandidateClass)) {
				analyse(bindings, annotated, manager.getStereotypeDefinition(bindingCandidateClass));
			} else {
				for (Annotation metaBindingCandidate : bindingCandidateClass.getAnnotations()) {
					Class<? extends Annotation> metaBindingCandidateClass = metaBindingCandidate.annotationType();
					if (metaBindingCandidateClass.isAnnotationPresent(MetaBinding.class) || isMetaBindingType(metaBindingCandidateClass)) {
						createBinding(bindings, bindingCandidate, metaBindingCandidateClass);
					}
				}
			}
		}
	}

	public Set<Annotated> annotatedsFor(Binding binding) {
		Set<Annotated> annotateds = annotatedsByBinding.get(binding);
		if (annotateds == null) {
			annotateds = new CopyOnWriteArraySet<>();
			annotatedsByBinding.put(binding, annotateds);
		}
		return annotateds;
	}

	public Iterable<BoundedGroup> buildBoundedGroups() {
		BoundedBuilder builder = new BoundedBuilder();
		for (Binding binding : getBindings()) {
			for (Annotated annotated : annotatedsFor(binding)) {
				builder.bind(binding, annotated);
			}
		}
		return builder.build();
	}

	private void createBinding(Set<Binding> bindings, Annotation bindingAnnotation, Class<? extends Annotation> metaBindingClass) {
		Class<? extends Annotation> bindingClass = bindingAnnotation.getClass();
		BindingType bindingType = addAnalyzedBindingType(metaBindingClass, bindingClass);
		if (bindingType != null) {
			bindings.add(new Binding(bindingType, bindingAnnotation));
		}
	}

	public Set<Binding> getBindings() {
		return annotatedsByBinding.keySet();
	}

	public Set<Throwable> getDefinitionErrors() {
		return definitionErrors;
	}

	public Collection<MetaBindingType> getMetaBindingTypes() {
		return Collections.unmodifiableCollection(metaBindingTypes.values());
	}

	public boolean isMetaBindingType(Class<? extends Annotation> metaBindingClass) {
		return metaBindingTypes.containsKey(metaBindingClass);
	}

	public MetaBindingType lookupMetaBindingType(Class<? extends Annotation> metaBindingClass) {
		MetaBindingType metaBindingType = metaBindingTypes.get(metaBindingClass);
		if (metaBindingType == null) {
			throw new IllegalArgumentException(metaBindingClass.getName() + " is not a MetaBindingType");
		}
		return metaBindingType;
	}

	public Bindings scan(Annotated annotated) {
		Set<Binding> bindings = new HashSet<>();
		analyse(bindings, annotated, annotated.getAnnotations());
		return Bindings.of(bindings);
	}

	private BindingType tryCreateBindingType(MetaBindingType metaBindingType, Class<? extends Annotation> bindingClass, Set<Annotation> definitions) {
		BindingType bindingType = metaBindingType.createBindingType(bindingClass, definitions);
		if (ProcessBindingType.fire(manager, this, bindingType)) {
			metaBindingType.remove(bindingType);
			return null;
		} else {
			return bindingType;
		}
	}
}

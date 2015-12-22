package org.gammastrike.binding.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.inject.spi.Annotated;

public class BoundedBuilder {

	private static class BoundedAnnotateds implements Iterable<BoundedAnnotated> {

		private final Map<Annotated, BoundedAnnotated> entries = new HashMap<>();

		public BoundedAnnotated boundedFor(Annotated key) {
			BoundedAnnotated annotated = entries.get(key);
			if (annotated == null) {
				annotated = new BoundedAnnotated(key);
				entries.put(key, annotated);
			}
			return annotated;
		}

		@Override
		public Iterator<BoundedAnnotated> iterator() {
			return entries.values().iterator();
		}
	}

	private static class BoundedGroups implements Iterable<BoundedGroup> {

		private final Map<Binding, BoundedGroup> entries = new HashMap<>();

		public BoundedGroup boundedFor(Binding key) {
			BoundedGroup group = entries.get(key);
			if (group == null) {
				group = new BoundedGroup(key);
				entries.put(key, group);
			}
			return group;
		}

		@Override
		public Iterator<BoundedGroup> iterator() {
			return entries.values().iterator();
		}
	}

	private final BoundedGroups groups;
	private final BoundedAnnotateds annotateds;

	public BoundedBuilder() {
		this.groups = new BoundedGroups();
		this.annotateds = new BoundedAnnotateds();
	}

	public void bind(Binding binding, Annotated annotated) {
		BoundedGroup boundedGroup = groups.boundedFor(binding);
		BoundedAnnotated boundedAnnotated = annotateds.boundedFor(annotated);
		boundedGroup.attach(boundedAnnotated);
		boundedAnnotated.attach(boundedGroup);
	}

	public Iterable<BoundedGroup> build() {
		return groups;
	}
}

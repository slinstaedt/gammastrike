package org.gammastrike.binding.api.event;

import static java.util.Objects.requireNonNull;

import javax.enterprise.inject.spi.BeanManager;

import org.gammastrike.binding.api.BoundedGroup;
import org.gammastrike.binding.api.annotation.BoundWith;

public class ProcessBoundedGroup {

	public static <X> void fire(BeanManager manager, Iterable<BoundedGroup> boundedGroups) {
		for (BoundedGroup boundedGroup : boundedGroups) {
			ProcessBoundedGroup event = new ProcessBoundedGroup(boundedGroup);
			BoundWith qualifier = boundedGroup.getBinding().createBoundWithQualifier();
			manager.fireEvent(event, qualifier);
		}
	}

	private final BoundedGroup boundedGroup;

	public ProcessBoundedGroup(BoundedGroup boundedGroup) {
		this.boundedGroup = requireNonNull(boundedGroup);
	}

	public BoundedGroup getBoundedGroup() {
		return boundedGroup;
	}
}

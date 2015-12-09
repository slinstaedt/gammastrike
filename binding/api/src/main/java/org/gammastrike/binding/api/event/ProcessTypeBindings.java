package org.gammastrike.binding.api.event;

import static java.util.Objects.requireNonNull;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessSyntheticAnnotatedType;

import org.gammastrike.binding.api.BindingsScanner;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ProcessTypeBindings/* <T> */extends ProcessAnnotatedBindings/* <AnnotatedType<T>> */{

	public static <X> void fire(BeanManager manager, BindingsScanner scanner, ProcessAnnotatedType<X> delegate) {
		new ProcessTypeBindings(scanner, delegate).fire(manager);
	}

	private final ProcessAnnotatedType delegate;

	public ProcessTypeBindings(BindingsScanner scanner, ProcessAnnotatedType delegate) {
		super(scanner, delegate.getAnnotatedType());
		this.delegate = requireNonNull(delegate);
	}

	public Extension getSource() {
		if (isSyntheticAnnotatedType()) {
			return ((ProcessSyntheticAnnotatedType<?>) delegate).getSource();
		} else {
			throw new IllegalStateException("AnnotatedType " + getAnnotated() + " is not synthetic");
		}
	}

	public boolean isSyntheticAnnotatedType() {
		return delegate instanceof ProcessSyntheticAnnotatedType;
	}

	public void setAnnotated(AnnotatedType annotatedType) {
		super.setAnnotated(annotatedType);
		delegate.setAnnotatedType(annotatedType);
	}

	public void vetoType() {
		vetoBindings();
		delegate.veto();
	}
}

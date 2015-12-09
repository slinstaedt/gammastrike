package org.gammastrike.factory.impl;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Provider;

import org.gammastrike.factory.api.annotation.ManualFactory;

/**
 * {@link InvocationHandler} implementatation, that handles abstract method invocations of factory methods.
 *
 * @author sven.linstaedt
 */
@ManualFactory
@Dependent
public class BindManualArgumentsInvocationHandler implements InvocationHandler, Serializable {

	private static final long serialVersionUID = 1L;

	private final BeanManager beanManager;
	private final Provider<MethodInvocationArgumentHolder> argumentHolder;
	private final Map<Object, CreationalContext<?>> dependentInstances;

	BindManualArgumentsInvocationHandler() {
		beanManager = null;
		argumentHolder = null;
		dependentInstances = null;
	}

	@Inject
	public BindManualArgumentsInvocationHandler(BeanManager beanManager, Provider<MethodInvocationArgumentHolder> argumentHolder) {
		this.beanManager = requireNonNull(beanManager);
		this.argumentHolder = requireNonNull(argumentHolder);
		this.dependentInstances = new ConcurrentHashMap<>();
	}

	@PreDestroy
	public void destroyAllDependencies() {
		for (CreationalContext<?> context : dependentInstances.values()) {
			context.release();
		}
		dependentInstances.clear();
	}

	public void destroyDependency(Object dependentInstance) {
		CreationalContext<?> context = dependentInstances.remove(dependentInstance);
		if (context != null) {
			context.release();
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MethodParameterMetadata metadata = argumentHolder.get().currentInvocation(method, args);
		for (int index : metadata.getDestroyParameterIndices()) {
			destroyDependency(args[index]);
		}

		Bean<?> bean = metadata.getReturnBean();
		CreationalContext<?> context = beanManager.createCreationalContext(bean);
		Object result = beanManager.getReference(bean, method.getGenericReturnType(), context);

		if (result != null && bean.getScope() == Dependent.class) {
			dependentInstances.put(result, context);
		}
		argumentHolder.get().reset();
		return result;
	}
}

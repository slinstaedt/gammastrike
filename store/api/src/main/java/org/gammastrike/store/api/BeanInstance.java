package org.gammastrike.store.api;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

public interface BeanInstance<T> {

    T getInstance();

    CreationalContext<T> getCreationalContext();

    Contextual<T> getContextual();
}

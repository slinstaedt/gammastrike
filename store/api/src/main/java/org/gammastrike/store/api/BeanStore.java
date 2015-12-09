package org.gammastrike.store.api;

public interface BeanStore extends Iterable<BeanIdentifier> {

    boolean isAvailable();

    /**
     * Gets an instance of a contextual from the store
     *
     * @param id The id of the contextual to return
     * @return The instance or null if not found
     */
    <T> BeanInstance<T> get(BeanIdentifier id);

    /**
     * Check if the store contains an instance
     *
     * @param id the id of the instance to check for
     * @return true if the instance is present, otherwise false
     */
    boolean contains(BeanIdentifier id);

    /**
     * Clears the store of contextual instances
     */
    void clear();

    /**
     * Adds a bean instance to the storage
     *
     * @param contextualInstance the contextual instance
     * @return the id for the instance
     */
    <T> void put(BeanIdentifier id, BeanInstance<T> instance);

    /**
     * Removes a bean instance identified by the given id.
     * 
     * @param id The bean id
     * @return the removed bean instance of null if there was no bean instance before
     */
    <T> BeanInstance<T> remove(BeanIdentifier id);
}

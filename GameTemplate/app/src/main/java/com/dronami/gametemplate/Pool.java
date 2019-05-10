package com.dronami.gametemplate;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

import java.util.ArrayList;
import java.util.List;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

public class Pool<T> {
    public interface PoolObjectFactory<T> {
        public T createObject();
    }

    private final List<T> freeObjects;
    private final PoolObjectFactory<T> factory;
    private final int maxSize;

    public Pool(PoolObjectFactory<T> factory, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
        this.freeObjects = new ArrayList<T>(maxSize);
    }

    // Return last element of freeObjects or create a new one if it's empty
    public T newObject() {
        T object = null;

        if (freeObjects.isEmpty()) {
            object = factory.createObject();
        } else {
            object = freeObjects.remove(freeObjects.size() - 1);
        }

        return object;
    }

    public void free(T object) {
        if (freeObjects.size() < maxSize) {
            freeObjects.add(object);
        }
    }
}

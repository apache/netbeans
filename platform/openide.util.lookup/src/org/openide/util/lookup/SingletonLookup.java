/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.util.lookup;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;

/**
 * Unmodifiable lookup that contains just one fixed object.
 *
 * @author Marian Petras
 */
class SingletonLookup extends Lookup {

    private final Object objectToLookup;
    private final String id;

    SingletonLookup(Object objectToLookup) {
        this(objectToLookup, null);
    }

    SingletonLookup(Object objectToLookup, String id) {
        if (objectToLookup == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        this.objectToLookup = objectToLookup;
        this.id = id;
    }

    @Override
    public <T> T lookup(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        return (clazz.isInstance(objectToLookup))
               ? clazz.cast(objectToLookup)
               : null;
    }

    @Override
    public <T> Result<T> lookup(Template<T> template) {
        if (template == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        Lookup.Item<T> item = lookupItem(template);
        if (item != null) {
            return new SingletonResult<T>(item);
        } else {
            return Lookup.EMPTY.lookup(template);
        }
    }

    @Override
    public <T> Collection<? extends T> lookupAll(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        return (clazz.isInstance(objectToLookup))
               ? Collections.singletonList(clazz.cast(objectToLookup))
               : Collections.<T>emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Item<T> lookupItem(Template<T> template) {
        if (template == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        String templateId = template.getId();
        if (templateId != null) {
            if (id == null) {
                if (!templateId.equals(objectToLookup.toString())) {
                    return null;
                }
            } else {
                if (!templateId.equals(id)) {
                    return null;
                }
            }
        }

        Object templateInst = template.getInstance();
        if ((templateInst != null) && (objectToLookup != templateInst)) {
            return null;
        }

        Class<T> clazz = template.getType();
        if ((clazz != null) && !clazz.isInstance(objectToLookup)) {
            return null;
        }

        Lookup.Item<T> item;
        if (clazz != null) {
            item = Lookups.lookupItem(clazz.cast(objectToLookup), id);
        } else {
            item = Lookups.lookupItem((T) objectToLookup, id);
        }
        return item;
    }

    @Override public String toString() {
        return "SingletonLookup[" + objectToLookup + "]";
    }

    static class SingletonResult<T> extends Lookup.Result<T> {

        private final Lookup.Item<T> item;

        SingletonResult(Lookup.Item<T> item) {
            this.item = item;
        }

        @Override
        public void addLookupListener(LookupListener l) {
            // this result never changes - no need to register a listener
        }

        @Override
        public void removeLookupListener(LookupListener l) {
            // this result never changes - no need to register a listener
        }

        @Override
        public Set<Class<? extends T>> allClasses() {
            return Collections.<Class<? extends T>>singleton(item.getType());
        }

        @Override
        public Collection<? extends Item<T>> allItems() {
            return Collections.singletonList(item);
        }

        @Override
        public Collection<? extends T> allInstances() {
            return Collections.singletonList(item.getInstance());
        }

    }

}

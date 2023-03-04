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
package org.netbeans.spi.java.classpath.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.FlaggedClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
final class MuxClassPathImplementation implements FlaggedClassPathImplementation, PropertyChangeListener {
    private final ClassPathSupport.Selector selector;
    private final PropertyChangeSupport listeners;
    //@GuardedBy("this")
    private List<PathResourceImplementation> cache;
    //@GuardedBy("this")
    private Set<ClassPath.Flag> flagsCache;
    //GuardedBy("this")
    private boolean activeClassPathValid;
    //GuardedBy("this")
    private ClassPath activeClassPath;
    //@GuardedBy("this")
    private PropertyChangeListener activeListener;

    MuxClassPathImplementation(@NonNull final ClassPathSupport.Selector selector) {
        Parameters.notNull("selector", selector);   //NOI18N
        this.listeners = new PropertyChangeSupport(this);
        this.selector = selector;
        this.selector.addPropertyChangeListener(WeakListeners.propertyChange(this, this.selector));
    }

    @Override
    @NonNull
    public Set<ClassPath.Flag> getFlags() {
        Set<ClassPath.Flag> res;
        synchronized (this) {
            res = flagsCache;
        }
        if (res == null) {
            res = getActiveClassPath().getFlags();
            assert res != null;
            synchronized (this) {
                if (flagsCache == null) {
                    flagsCache = res;
                } else {
                    res = flagsCache;
                }
            }
        }
        return res;
    }

    @Override
    @NonNull
    public List<? extends PathResourceImplementation> getResources() {
        List<PathResourceImplementation> res;
        synchronized (this) {
            res = cache;
        }
        if (res == null) {
            final ClassPath cp = getActiveClassPath();
            final List<ClassPath.Entry> entries = cp.entries();
            res = new ArrayList<>(entries.size());
            for (ClassPath.Entry entry : entries) {
                res.add(org.netbeans.spi.java.classpath.support.ClassPathSupport.createResource(entry.getURL()));
            }
            synchronized (this) {
                if (cache == null) {
                    cache = Collections.unmodifiableList(res);
                } else {
                    res = cache;
                }
            }
        }
        return res;
    }

    @Override
    public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.listeners.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.listeners.removePropertyChangeListener(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final String propName = evt.getPropertyName();
        if (ClassPathSupport.Selector.PROP_ACTIVE_CLASS_PATH.equals(propName)) {
            synchronized (this) {
                cache = null;
                flagsCache = null;
                activeClassPathValid = false;
            }
            this.listeners.firePropertyChange(PROP_RESOURCES, null, null);
            this.listeners.firePropertyChange(PROP_FLAGS, null, null);
        } else if (ClassPath.PROP_ENTRIES.equals(propName)) {
            synchronized (this) {
                cache = null;
            }
            this.listeners.firePropertyChange(PROP_RESOURCES, null, null);
        } else if (ClassPath.PROP_FLAGS.equals(propName)) {
            synchronized (this) {
                flagsCache = null;
            }
            this.listeners.firePropertyChange(PROP_FLAGS, null, null);
        }
    }

    @NonNull
    private ClassPath getActiveClassPath() {
        synchronized (this) {
            if (activeClassPathValid) {
                assert activeClassPath != null;
                return activeClassPath;
            }
            if (activeClassPath != null) {
                assert activeListener != null;
                activeClassPath.removePropertyChangeListener(activeListener);
                activeClassPath = null;
                activeListener = null;
            }
        }
        final ClassPath newCp = selector.getActiveClassPath();
        assert newCp != null : String.format(
                "Selector: %s (%s) returned null ClassPath",    //NOI18N
                selector,
                selector.getClass());
        synchronized (this) {
            if (activeClassPath == null) {
                assert activeListener == null;
                activeClassPath = newCp;
                activeListener = WeakListeners.propertyChange(this, activeClassPath);
                activeClassPath.addPropertyChangeListener(activeListener);
                activeClassPathValid = true;
            }
            return activeClassPath;
        }
    }
}

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

package org.netbeans.api.debugger.jpda;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

/**
 * Collector of threads running inside the debuggee.
 * Fires changes when threads change.
 * 
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this abstract class in client code. New methods can be added to
 * this class at any time to keep up with the JDI functionality.</pre>
 * 
 * @author Martin Entlicher
 * @since 2.16
 */
public abstract class ThreadsCollector {
    
    private final PropertyChangeSupport pch = new PropertyChangeSupport(this);

    /** Property name constant. */
    public static final String          PROP_THREAD_STARTED = "threadStarted";   // NOI18N
    /** Property name constant. */
    public static final String          PROP_THREAD_DIED = "threadDied";         // NOI18N
    /** Property name constant. */
    public static final String          PROP_THREAD_GROUP_ADDED = "threadGroupAdded";  // NOI18N
    
    /** Property name constant. */
    public static final String          PROP_THREAD_SUSPENDED = "threadSuspended";   // NOI18N
    /** Property name constant. */
    public static final String          PROP_THREAD_RESUMED = "threadResumed";   // NOI18N
    
    
    /**
     * Add a PropertyChangeListener to be notified about threads changes.
     * @param l The listener
     */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        pch.addPropertyChangeListener(l);
    }
    
    /**
     * Remove a PropertyChangeListener.
     * @param l The listener
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        pch.removePropertyChangeListener(l);
    }
    
    protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        pch.firePropertyChange(propertyName, oldValue, newValue);
    }
    
    /**
     * Returns all threads that exist in the debuggee.
     *
     * @return all threads
     */
    public abstract List<JPDAThread> getAllThreads();
    
    /**
     * Creates a deadlock detector.
     * @return deadlock detector with automatic detection of deadlock among suspended threads
     */
    public abstract DeadlockDetector getDeadlockDetector();
}

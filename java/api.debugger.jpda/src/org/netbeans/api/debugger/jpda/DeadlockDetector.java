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
import java.util.Collection;
import java.util.Set;

/**
 * Service that detects deadlocks and fires an event when the deadlock occurs.
 * 
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this abstract class in client code. New methods can be added to
 * this class at any time to keep up with the JDI functionality.</pre>
 * 
 * @author Martin Entlicher
 * @since 2.16
 */
public abstract class DeadlockDetector {
    
    /**
     * Name of property which is fired when deadlock occurs.
     */
    public static final String PROP_DEADLOCK = "deadlock"; // NOI18N
    
    private Set<Deadlock> deadlocks;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    /**
     * Get the set of detected deadlocks.
     * @return The set of deadlocks.
     */
    public final synchronized Set<Deadlock> getDeadlocks() {
        return deadlocks;
    }
    
    /**
     * Used by overriding class to set the deadlocks when found.
     * 
     * @param deadlocks The set of deadlocks.
     */
    protected final void setDeadlocks(Set<Deadlock> deadlocks) {
        synchronized (this) {
            this.deadlocks = deadlocks;
        }
        firePropertyChange(PROP_DEADLOCK, null, deadlocks);
    }
    
    /**
     * Utility method used by the implementing class to create deadlock instances.
     * @param threads The threads in deadlock
     * @return Deadlock instance
     */
    protected final Deadlock createDeadlock(Collection<JPDAThread> threads) {
        return new Deadlock(threads);
    }
    
    private void firePropertyChange(String name, Object oldValue, Object newValue) {
        pcs.firePropertyChange(name, oldValue, newValue);
    }
    
    /**
     * Add a PropertyChangeListener to this deadlock detector.
     * @param l The listener
     */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    /**
     * Remove a PropertyChangeListener from this deadlock detector.
     * @param l The listener
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * Representation of a deadlock - one set of mutually deadlocked threads.
     */
    public static final class Deadlock {

        private Collection<JPDAThread> threads;
        
        private Deadlock(Collection<JPDAThread> threads) {
            this.threads = threads;
        }
        
        /**
         * Get the threads in deadlock.
         * @return The threads in deadlock.
         */
        public Collection<JPDAThread> getThreads() {
            return threads;
        }
    }
    
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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

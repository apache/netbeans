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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.api.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.WeakSet;

/**
 *
 */
public final class NativeProjectRegistry {

    /**
     * Property representing open projects.
     * @see #getOpenProjects
     */
    public static final String PROPERTY_OPEN_NATIVE_PROJECT = "openNativeProject"; // NOI18N
    public static final String PROPERTY_CLOSE_NATIVE_PROJECT = "closeNativeProject"; // NOI18N
    public static final String PROPERTY_DELETE_NATIVE_PROJECT = "deleteNativeProject"; // NOI18N
    private static final NativeProjectRegistry INSTANCE = new NativeProjectRegistry();
    private static final Logger LOG = Logger.getLogger(NativeProjectRegistry.class.getName());

    private final Set<NativeProject> projects = new HashSet<>();
    private final ReentrantReadWriteLock projectsLock = new ReentrantReadWriteLock();
    private final Set<PropertyChangeListener> listeners = new WeakSet<>();
    private final ReentrantReadWriteLock listenersLock = new ReentrantReadWriteLock();

    private NativeProjectRegistry() {
    }

    /**
     * Get the default singleton instance of this class.
     * @return the default instance
     */
    public static NativeProjectRegistry getDefault() {
        return INSTANCE;
    }

    /**
     * Gets a list of currently open projects.
     * Projects are fully ready to use (configuration already read)
     *
     * @return list of projects currently opened in the IDE's GUI; order not specified
     */
    public Collection<NativeProject> getOpenProjects() {
        projectsLock.readLock().lock();
        try {
            return new HashSet<>(projects);
        } finally {
            projectsLock.readLock().unlock();
        }
    }

    /**
     * Adds a listener to changes in the set of open projects.
     * As this class is a singleton and is not subject to garbage collection,
     * it is recommended to add only weak listeners, or remove regular listeners reliably.
     * @param listener a listener to add
     * @see #PROPERTY_OPEN_NATIVE_PROJECT, PROPERTY_CLOSE_NATIVE_PROJECT, PROPERTY_DELETE_NATIVE_PROJECT
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listenersLock.writeLock().lock();
        try {
            listeners.add(listener);
        } finally {
            listenersLock.writeLock().unlock();
        }
    }

    /**
     * Removes a listener.
     * @param listener a listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listenersLock.writeLock().lock();
        try {
            listeners.remove(listener);
        } finally {
            listenersLock.writeLock().unlock();
        }
    }

    public void register(NativeProject project) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Open native project {0}", project); //NOI18N
        }
        Collection<NativeProject> oldProjects = getOpenProjects();
        projectsLock.writeLock().lock();
        try {
            projects.add(project);
        } finally {
            projectsLock.writeLock().unlock();
        }
        Collection<NativeProject> newProjects = getOpenProjects();
        notifyListeners(oldProjects, newProjects, project, PROPERTY_OPEN_NATIVE_PROJECT);
    }

    public void unregister(NativeProject project, boolean isDeleted) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Close native project {0}", project); //NOI18N
        }
        Collection<NativeProject> oldProjects = getOpenProjects();
        projectsLock.writeLock().lock();
        try {
            projects.remove(project);
        } finally {
            projectsLock.writeLock().unlock();
        }
        Collection<NativeProject> newProjects = getOpenProjects();
        if (isDeleted) {
            notifyListeners(oldProjects, newProjects, project, PROPERTY_DELETE_NATIVE_PROJECT);
        } else {
            notifyListeners(oldProjects, newProjects, project, PROPERTY_CLOSE_NATIVE_PROJECT);
        }
    }

    private void notifyListeners(Collection<NativeProject> oldProjects, Collection<NativeProject> newProjects, NativeProject project, String property) {
        PropertyChangeEvent ev = new PropertyChangeEvent(project, property, oldProjects, newProjects);
        List<PropertyChangeListener> listeners_copy;
        listenersLock.readLock().lock();
        try {
            listeners_copy = new ArrayList<>(listeners);
        } finally {
            listenersLock.readLock().unlock();
        }
        for (PropertyChangeListener listener : listeners_copy) {
            listener.propertyChange(ev);
        }
    }
}

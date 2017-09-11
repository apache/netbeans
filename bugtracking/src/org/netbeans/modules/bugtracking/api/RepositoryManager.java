/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.RepositoryRegistry;

/**
 * Manages registered {@link Repository}-s and related functionality.
 * 
 * @author Tomas Stupka
 * @since 1.85
 */
public final class RepositoryManager {

    /**
     * Name of the <code>PropertyChangeEvent</code> notifying that a repository was created or removed, where:<br>
     * <ul>
     *  <li><code>old value</code> - a Collection of all repositories before the change</li> 
     *  <li><code>new value</code> - a Collection of all repositories after the change</li> 
     * </ul>
     * either both, old value or new value, are <code>null</code> if unknown, or at least one of them is <code>not null</code> 
     * indicating the exact character of the notified change.
     * 
     * @since 1.85
     */
    public static final String EVENT_REPOSITORIES_CHANGED = "bugtracking.repositories.changed"; // NOI18N
    
    private static RepositoryManager instance;
    private static RepositoryRegistry registry;
    
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    private RepositoryManager () {
        registry = RepositoryRegistry.getInstance();
        RepositoryListener l = new RepositoryListener();
        registry.addPropertyChangeListener(l);
    }
    
    /**
     * Returns the only existing <code>RepositoryManager</code> instance.
     * 
     * @return a RepositoryManager
     * @since 1.85
     */
    public static synchronized RepositoryManager getInstance() {
        if(instance == null) {
            instance = new RepositoryManager();
        }
        return instance;
    }
    
    /**
     * Add a listener for repository related changes.
     * 
     * @param l the new listener
     * @since 1.85
     */
    public void addPropertChangeListener(PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }
    
    /**
     * Remove a listener for repository related changes.
     * 
     * @param l the new listener
     * @since 1.85
     */
    public void removePropertChangeListener(PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }
    
    /**
     * Returns all registered repositories, including those which are
     * currently opened in a logged in team sever dashboard.
     * 
     * @return all known repositories
     * @since 1.85
     */
    public Collection<Repository> getRepositories() {
        LinkedList<Repository> ret = new LinkedList<Repository>();
        ret.addAll(toRepositories(registry.getKnownRepositories(false, true)));
        return Collections.unmodifiableCollection(ret);
    }
    
    /**
     * Returns all registered repositories for a connector with the given id, 
     * including those which are currently opened in a logged in team sever dashboard.
     * 
     * @param connectorId
     * @return all known repositories for the given connector
     * @since 1.85
     */
    public Collection<Repository> getRepositories(String connectorId) {
        LinkedList<Repository> ret = new LinkedList<Repository>();
        ret.addAll(toRepositories(registry.getRepositories(connectorId, true)));
        return ret;
    }
    
    /**
     * Facility method to obtain an already registered {@link Repository} instance.
     * 
     * @param connectorId
     * @param repositoryId
     * @return a Repository with the given connector- and repository id or <code>null<code>.
     * @since 1.85
     */
    public Repository getRepository(String connectorId, String repositoryId) {
        RepositoryImpl impl = registry.getRepository(connectorId, repositoryId, true);
        return impl != null ? impl.getRepository() : null;
    }      
    
    private Collection<Repository> toRepositories(Collection<RepositoryImpl> impls) {
        if(impls == null) {
            return Collections.EMPTY_LIST;
        }
        Collection<Repository> ret = new ArrayList<Repository>(impls.size());
        for (RepositoryImpl repoImpl : impls) {
            ret.add(repoImpl.getRepository());
        }
        return ret;
    }
    
    private class RepositoryListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(EVENT_REPOSITORIES_CHANGED.equals(evt.getPropertyName())) {
                Collection<RepositoryImpl> newImpls = (Collection<RepositoryImpl>) evt.getNewValue();
                Collection<RepositoryImpl> oldImpls = (Collection<RepositoryImpl>) evt.getOldValue();
                changeSupport.firePropertyChange(EVENT_REPOSITORIES_CHANGED, oldImpls != null ? toRepositories(oldImpls) : null, newImpls != null ? toRepositories(newImpls) : null);
            }
        }
    }
}

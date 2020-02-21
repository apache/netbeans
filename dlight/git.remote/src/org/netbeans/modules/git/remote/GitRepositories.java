/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.git.remote;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class GitRepositories {
    private static GitRepositories instance;
    private final Set<VCSFileProxy> repositories = new HashSet<>();
    private final Set<VCSFileProxy> closed = new HashSet<>(5);
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    public static final String PROP_REPOSITORIES = "GitRemoteRepositories.repositories"; //NOI18N

    public static synchronized GitRepositories getInstance () {
        if (instance == null) {
            instance = new GitRepositories();
        }
        return instance;
    }

    public void add (VCSFileProxy repository, boolean byUser) {
        boolean added;
        if (!byUser && closed.contains(repository)) {
            // closed by user, so he must open it manually
            return;
        }
        try {
            if (VCSFileProxySupport.isAncestorOrEqual(VCSFileProxy.createFileProxy(VCSFileProxySupport.getFileSystem(repository).getTempFolder()), repository)) { //NOI18N
                // skip repositories in temp folder
                return;
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        Set<VCSFileProxy> oldValues = null;
        Set<VCSFileProxy> newValues = null;
        synchronized (repositories) {
            added = repositories.add(repository);
            if (added) {
                newValues = new HashSet<>(repositories);
            }
        }
        if (added) {
            closed.remove(repository);
            oldValues = new HashSet<>(newValues);
            oldValues.remove(repository);
            support.firePropertyChange(PROP_REPOSITORIES, oldValues, newValues);
        }
    }

    public void remove (VCSFileProxy repository, boolean byUser) {
        boolean removed;
        Set<VCSFileProxy> oldValues = null;
        Set<VCSFileProxy> newValues = null;
        synchronized (repositories) {
            removed = repositories.remove(repository);
            if (removed) {
                newValues = new HashSet<>(repositories);
                if (byUser) {
                    closed.add(repository);
                }
            }
        }
        if (removed) {
            oldValues = new HashSet<>(newValues);
            oldValues.add(repository);
            support.firePropertyChange(PROP_REPOSITORIES, oldValues, newValues);
        }
    }

    public Set<VCSFileProxy> getKnownRepositories () {
        synchronized (repositories) {
            return new HashSet<>(repositories);
        }
    }

    public void addPropertyChangeListener (PropertyChangeListener list) {
        support.addPropertyChangeListener(list);
    }

    public void removePropertyChangeListener (PropertyChangeListener list) {
        support.removePropertyChangeListener(list);
    }

}

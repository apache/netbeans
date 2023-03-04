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

package org.netbeans.modules.git;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.versioning.util.Utils;

/**
 *
 * @author ondra
 */
public class GitRepositories {
    private static GitRepositories instance;
    private final Set<File> repositories = new HashSet<>();
    private final Set<File> closed = new HashSet<>(5);
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    public static final String PROP_REPOSITORIES = "GitRepositories.repositories"; //NOI18N

    public static synchronized GitRepositories getInstance () {
        if (instance == null) {
            instance = new GitRepositories();
        }
        return instance;
    }

    public void add (File repository, boolean byUser) {
        boolean added;
        if (!byUser && closed.contains(repository)) {
            // closed by user, so he must open it manually
            return;
        }
        if (Utils.isAncestorOrEqual(new File(System.getProperty("java.io.tmpdir")), repository)) { //NOI18N
            // skip repositories in temp folder
            return;
        }
        Set<File> oldValues = null;
        Set<File> newValues = null;
        synchronized (repositories) {
            added = repositories.add(repository);
            if (added) {
                newValues = new HashSet<File>(repositories);
            }
        }
        if (added) {
            closed.remove(repository);
            oldValues = new HashSet<File>(newValues);
            oldValues.remove(repository);
            support.firePropertyChange(PROP_REPOSITORIES, oldValues, newValues);
        }
    }

    public void remove (File repository, boolean byUser) {
        boolean removed;
        Set<File> oldValues = null;
        Set<File> newValues = null;
        synchronized (repositories) {
            removed = repositories.remove(repository);
            if (removed) {
                newValues = new HashSet<File>(repositories);
                if (byUser) {
                    closed.add(repository);
                }
            }
        }
        if (removed) {
            oldValues = new HashSet<File>(newValues);
            oldValues.add(repository);
            support.firePropertyChange(PROP_REPOSITORIES, oldValues, newValues);
        }
    }

    public Set<File> getKnownRepositories () {
        synchronized (repositories) {
            return new HashSet<File>(repositories);
        }
    }

    public void addPropertyChangeListener (PropertyChangeListener list) {
        support.addPropertyChangeListener(list);
    }

    public void removePropertyChangeListener (PropertyChangeListener list) {
        support.removePropertyChangeListener(list);
    }

}

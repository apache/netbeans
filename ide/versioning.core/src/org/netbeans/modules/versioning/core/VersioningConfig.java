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
package org.netbeans.modules.versioning.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbPreferences;

import java.util.prefs.Preferences;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 * Stores Versioning manager configuration.
 *
 * @author Maros Sandor
 */
public class VersioningConfig {
    
    private static final VersioningConfig INSTANCE = new VersioningConfig();    
    private static final Logger LOG = Logger.getLogger(VersioningConfig.class.getName());
    private final Map<String, Set<String>> allDisconnectedRepositories;
    private static final String SEP = "###"; //NOI18N
    private static final String PREF_KEY = "disconnectedFolders"; //NOI18N

    private VersioningConfig () {
        allDisconnectedRepositories = initializeDisconnectedRepositories();
    }
    
    public static VersioningConfig getDefault() {
        return VersioningConfig.INSTANCE;
    }
    
    public Preferences getPreferences() {
        return getPrefs();
    }
    
    private static Preferences getPrefs () {
        return NbPreferences.root().node("org/netbeans/modules/versioning"); // NOI18N
    }

    /**
     * Tests whether the given repository is disconnected from the given versioning system. 
     * @param vs
     * @param repository
     * @return 
     */
    boolean isDisconnected (VersioningSystem vs, VCSFileProxy repository) {
        boolean disconnected = false;
        String className = vs.getDelegate().getClass().getName();
        synchronized (allDisconnectedRepositories) {
            Set<String> disconnectedRepositories = allDisconnectedRepositories.get(className);
            if (disconnectedRepositories != null) {
                for (String disconnectedRepository : disconnectedRepositories) {
                    if (disconnectedRepository.equals(repository.getPath())) {
                        disconnected = true;
                        LOG.log(Level.FINE, "isDisconnected: Folder is disconnected from {0}: {1}, disconnected root: {2}", new Object[] { className, repository, disconnectedRepository }); //NOI18N
                        break;
                    }
                }
            }
        }
        return disconnected;
    }

    /**
     * Reconnects the given repository to the given versioning system
     * @param vs
     * @param repository 
     */
    public void connectRepository (VersioningSystem vs, VCSFileProxy repository) {
        connectRepository(vs, repository.getPath());
    }
    
    public void connectRepository (VersioningSystem vs, String path) {
        String className = vs.getDelegate().getClass().getName();
        synchronized (allDisconnectedRepositories) {
            Set<String> disconnectedRepos = allDisconnectedRepositories.get(className);
            if (disconnectedRepos != null) {
                boolean changed = false;
                for (Iterator<String> it = disconnectedRepos.iterator(); it.hasNext(); ) {
                    String disconnectedRepository = it.next();
                    if (disconnectedRepository.equals(path)) {
                        LOG.log(Level.FINE, "connectRepository: Connecting repository to {0}: {1}", new Object[] { className, path }); //NOI18N
                        it.remove();
                        changed = true;
                        break;
                    }
                }
                if (changed) {
                    saveDisconnectedRepositories();
                }
            }
        }
    }

    /**
     * Disconnects the given repository from the given version control system
     * @param vs
     * @param repository 
     */
    public void disconnectRepository (VersioningSystem vs, VCSFileProxy repository) {
        disconnectRepository(vs, repository.getPath());
    }
    
    public void disconnectRepository (VersioningSystem vs, String path) {
        String className = vs.getDelegate().getClass().getName();
        synchronized (allDisconnectedRepositories) {
            Set<String> disconnectedRepos = allDisconnectedRepositories.get(className);
            if (disconnectedRepos == null) {
                disconnectedRepos = new HashSet<String>();
                allDisconnectedRepositories.put(className, disconnectedRepos);
            }
            boolean added = disconnectedRepos.add(path);
            if (!added) {
                LOG.log(Level.FINE, "disconnectRepository: Repository already disconnected for {0}: {1}", new Object[] { className, path }); //NOI18N
            } else {
                saveDisconnectedRepositories();
            }
        }
    }

    public String[] getDisconnectedRoots (VersioningSystem vs) {
        String className = vs.getDelegate().getClass().getName();
        String[] paths;
        synchronized (allDisconnectedRepositories) {
            Set<String> disconnectedRepos = allDisconnectedRepositories.get(className);
            if (disconnectedRepos == null) {
                paths = new String[0];
            } else {
                paths = disconnectedRepos.toArray(new String[0]);
            }
        }
        return paths;
    }

    private static Map<String, Set<String>> initializeDisconnectedRepositories () {
        Map<String, Set<String>> disconnectedFolders = new HashMap<String, Set<String>>(5);
        List<String> list = Utils.getStringList(getPrefs(), PREF_KEY);
        for (String s : list) {
            String[] disconnectedFolder = s.split(SEP);
            if (disconnectedFolder.length == 2) {
                Set<String> files = disconnectedFolders.get(disconnectedFolder[0]);
                if (files == null) {
                    files = new HashSet<String>();
                    disconnectedFolders.put(disconnectedFolder[0], files);
                }
                files.add(disconnectedFolder[1]);
            }
        }
        return disconnectedFolders;
    }

    private void saveDisconnectedRepositories () {
        List<String> list = new LinkedList<String>();
        synchronized (allDisconnectedRepositories) {
            for (Map.Entry<String, Set<String>> e : allDisconnectedRepositories.entrySet()) {
                String vsKey = e.getKey();
                for (String f : e.getValue()) {
                    list.add(vsKey + SEP + f);
                }
            }
        }
        Utils.put(getPreferences(), PREF_KEY, list);
    }
}

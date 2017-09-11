/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
                paths = disconnectedRepos.toArray(new String[disconnectedRepos.size()]);
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

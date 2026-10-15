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
package org.netbeans.modules.mercurial;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.ui.branch.HgBranch;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.RequestProcessor;

/**
 *
 * @author ondra
 */
public class WorkingCopyInfo {

    /**
     * Fired when a working copy parents change
     */
    public static final String PROPERTY_WORKING_COPY_PARENT = WorkingCopyInfo.class.getName() + ".workingCopyParents"; //NOI18N

    /**
     * Fired when the current branch name changes
     */
    public static final String PROPERTY_CURRENT_BRANCH = WorkingCopyInfo.class.getName() + ".currBranch"; //NOI18N

    private static final WeakHashMap<File, WorkingCopyInfo> cache = new WeakHashMap<File, WorkingCopyInfo>(5);
    private static final Logger LOG = Logger.getLogger(WorkingCopyInfo.class.getName());
    private static final RequestProcessor rp = new RequestProcessor("WorkingCopyInfo", 1, true); //NOI18N
    private static final RequestProcessor.Task refreshTask = rp.create(new RepositoryRefreshTask());
    private static final Set<WorkingCopyInfo> repositoriesToRefresh = new HashSet<WorkingCopyInfo>(2);
    private final WeakReference<File> rootRef;
    private final PropertyChangeSupport propertyChangeSupport;
    private HgLogMessage[] parents = new HgLogMessage[0];
    private String branch = HgBranch.DEFAULT_NAME;

    private WorkingCopyInfo (File root) {
        rootRef = new WeakReference<File>(root);
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Do NOT call from EDT
     * @param repositoryRoot existing repository root
     * @return null if repositoryRoot is not an existing git repository
     */
    public static WorkingCopyInfo getInstance (File repositoryRoot) {
        WorkingCopyInfo info = null;
        // this should return alwaus the same instance, so the cache can be implemented as a weak map.
        File repositoryRootSingleInstance = Mercurial.getInstance().getRepositoryRoot(repositoryRoot);
        if (repositoryRoot.equals(repositoryRootSingleInstance)) {
            synchronized (cache) {
                info = cache.get(repositoryRootSingleInstance);
                if (info == null) {
                    cache.put(repositoryRootSingleInstance, info = new WorkingCopyInfo(repositoryRootSingleInstance));
                    info.refresh();
                }
            }
        }
        return info;
    }

    public void addPropertyChangeListener (PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Do NOT call from EDT
     * @return
     */
    public void refresh () {
        assert !java.awt.EventQueue.isDispatchThread();
        File root = rootRef.get();
        try {
            if (root == null) {
                LOG.log(Level.WARNING, "refresh (): root is null, it has been collected in the meantime"); //NOI18N
            } else {
                LOG.log(Level.FINE, "refresh (): starting for {0}", root); //NOI18N
                List<HgLogMessage> parentInfo = HgCommand.getParents(root, null, null);
                setParents(parentInfo);
                String branch = HgCommand.getBranch(root);
                setBranch(branch);
            }
        } catch (HgException.HgCommandCanceledException ex) {
            // nothing
        } catch (HgException ex) {
            Level level = root.exists() ? Level.INFO : Level.FINE; // do not polute the message log with messages concerning temporary or deleted repositories
            LOG.log(level, null, ex);
        }
    }

    public static void refreshAsync (File repositoryRoot) {
        WorkingCopyInfo info = null;
        synchronized (cache) {
            info = cache.get(repositoryRoot);
        }
        if (info != null) {
            boolean start = false;
            synchronized (repositoriesToRefresh) {
                start = repositoriesToRefresh.add(info);
            }
            if (start) {
                LOG.log(Level.FINE, "Planning refresh for {0}", repositoryRoot); //NOI18N
                refreshTask.schedule(3000);
            }
        }
    }

    public HgLogMessage[] getWorkingCopyParents () {
        return parents;
    }

    public String getCurrentBranch () {
        return branch;
    }

    private void setParents (List<HgLogMessage> parents) {
        HgLogMessage[] oldParents = this.parents;
        boolean changed = oldParents.length != parents.size();
        if (!changed) {
            for (HgLogMessage newParent : parents) {
                boolean contains = false;
                for (HgLogMessage oldParent : oldParents) {
                    if (oldParent.getCSetShortID().equals(newParent.getCSetShortID())
                            && oldParent.getTags().length == newParent.getTags().length
                            && new HashSet<String>(Arrays.asList(oldParent.getTags())).equals(new HashSet<String>(Arrays.asList(newParent.getTags())))) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    changed = true;
                    break;
                }
            }
        }
        if (changed) {
            HgLogMessage[] newParents = parents.toArray(new HgLogMessage[0]);
            this.parents = newParents;
            propertyChangeSupport.firePropertyChange(PROPERTY_WORKING_COPY_PARENT, oldParents, newParents);
        }
    }
    
    private void setBranch (String branch) {
        if (branch == null) {
            branch = HgBranch.DEFAULT_NAME;
        }
        String oldBranch = this.branch;
        this.branch = branch;
        if (!oldBranch.equals(this.branch)) {
            propertyChangeSupport.firePropertyChange(PROPERTY_CURRENT_BRANCH, oldBranch, branch);
        }
    }

    private static class RepositoryRefreshTask implements Runnable {
        @Override
        public void run() {
            WorkingCopyInfo info;
            while ((info = getNextRepositoryInfo()) != null) {
                info.refresh();
            }
        }

        private WorkingCopyInfo getNextRepositoryInfo () {
            WorkingCopyInfo info = null;
            synchronized (repositoriesToRefresh) {
                Iterator<WorkingCopyInfo> it = repositoriesToRefresh.iterator();
                if (it.hasNext()) {
                    info = it.next();
                    it.remove();
                }
            }
            return info;
        }
    }
}

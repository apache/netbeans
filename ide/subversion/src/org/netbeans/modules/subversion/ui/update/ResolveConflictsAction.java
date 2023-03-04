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

package org.netbeans.modules.subversion.ui.update;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.ui.commit.ConflictResolvedAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 * Show basic conflict resolver UI (provided by the diff module) and resolves tree conflicts.
 *
 * @author Petr Kuzel
 */
@NbBundle.Messages({
    "ResolveConflicts=Resolve Con&flicts...",
    "ResolveConflicts_Context=Resolve Con&flicts...",
    "ResolveConflicts_Context_Multiple=Resolve Con&flicts...",
    "# {0} - number of selected projects",
    "ResolveConflicts_Projects=Resolve {0} Projects Con&flicts..."
})
public class ResolveConflictsAction extends ContextAction {

    private static final String ICON_RESOURCE = "org/netbeans/modules/subversion/resources/icons/conflict-resolve.png"; //NOI18N
    
    public ResolveConflictsAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }

    @Override
    protected String getBaseName(Node[] activatedNodes) {
        return "ResolveConflicts";  // NOI18N
    }

    @Override
    protected boolean enable(Node[] nodes) {
        return isCacheReady() && Subversion.getInstance().getStatusCache().containsFiles(getCachedContext(nodes),
                FileInformation.STATUS_VERSIONED_CONFLICT, true);
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        if(!Subversion.getInstance().checkClientAvailable()) {
            return;
        }
        Context ctx = getContext(nodes);
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File[] files = cache.listFiles(ctx, FileInformation.STATUS_VERSIONED_CONFLICT);

        resolveConflicts(files);
    }

    static void resolveConflicts(final File[] files) {
        Subversion.getInstance().getParallelRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                final Map<File, ISVNStatus> treeConflicts = getTreeConflicts(files);
                final Map<File, ISVNStatus> propertyConflicts = getPropertyConflicts(files);
                final List<File> filteredFiles = removeFolders(files, treeConflicts.keySet());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (filteredFiles.isEmpty() && treeConflicts.isEmpty() && propertyConflicts.isEmpty()) {
                            NotifyDescriptor nd = new NotifyDescriptor.Message(org.openide.util.NbBundle.getMessage(ResolveConflictsAction.class, "MSG_NoConflictsFound")); // NOI18N
                            DialogDisplayer.getDefault().notify(nd);
                        } else {
                            resolveTreeConflicts(treeConflicts);
                            resolvePropertyConflicts(propertyConflicts, filteredFiles);
                            for (File file : filteredFiles) {
                                ResolveConflictsExecutor executor = new ResolveConflictsExecutor(file);
                                executor.exec();
                            }
                        }
                    }
                });
            }

            private void resolveTreeConflicts (Map<File, ISVNStatus> treeConflicts) {
                for (Map.Entry<File, ISVNStatus> e : treeConflicts.entrySet()) {
                    File file = e.getKey();
                    ISVNStatus status = e.getValue();
                    if (acceptLocalChanges(status)) {
                        try {
                            ConflictResolvedAction.perform(file);
                        } catch (SVNClientException ex) {
                            Logger.getLogger(ResolveConflictsAction.class.getName()).log(Level.INFO, null, ex);
                        }
                    }
                }
            }

            private boolean acceptLocalChanges (ISVNStatus status) {
                File file = status.getFile();
                NotifyDescriptor nd = new NotifyDescriptor(NbBundle.getMessage(ResolveConflictsAction.class, "MSG_ResolveConflictsAction.ResolveTreeConflict.message", file.getName()), //NOI18N
                        NbBundle.getMessage(ResolveConflictsAction.class, "MSG_ResolveConflictsAction.ResolveTreeConflict.title"), NotifyDescriptor.YES_NO_OPTION, // NOI18N
                        NotifyDescriptor.QUESTION_MESSAGE, new Object[] { NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION}, NotifyDescriptor.NO_OPTION);
                return DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION;
            }

            private void resolvePropertyConflicts (Map<File, ISVNStatus> propertyConflicts, List<File> filesToResolve) {
                for (Map.Entry<File, ISVNStatus> e : propertyConflicts.entrySet()) {
                    File file = e.getKey();
                    ISVNStatus status = e.getValue();
                    if (acceptPropertyLocalChanges(status)) {
                        if (!filesToResolve.contains(file)) {
                            try {
                                ConflictResolvedAction.perform(file);
                            } catch (SVNClientException ex) {
                                Logger.getLogger(ResolveConflictsAction.class.getName()).log(Level.INFO, null, ex);
                            }
                        }
                    } else {
                        filesToResolve.remove(file);
                    }
                }
            }

            private boolean acceptPropertyLocalChanges (ISVNStatus status) {
                File file = status.getFile();
                NotifyDescriptor nd = new NotifyDescriptor(NbBundle.getMessage(ResolveConflictsAction.class, "MSG_ResolveConflictsAction.ResolvePropertyConflict.message", file.getName()), //NOI18N
                        NbBundle.getMessage(ResolveConflictsAction.class, "MSG_ResolveConflictsAction.ResolvePropertyConflict.title"), NotifyDescriptor.YES_NO_OPTION, // NOI18N
                        NotifyDescriptor.QUESTION_MESSAGE, new Object[] { NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION}, NotifyDescriptor.NO_OPTION);
                return DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION;
            }
        });
    }

    /**
     * Filters the array and returns only existing files, not folders.
     * I/O access
     * @param files
     * @param treeConflicts set of files that will not be included in the returned list
     * @return
     */
    private static List<File> removeFolders (File[] files, Set<File> treeConflicts) {
        LinkedList<File> filteredFiles = new LinkedList<File>();
        for (File file : files) {
            if (!treeConflicts.contains(file) && file.isFile()) {
                filteredFiles.add(file);
            }
        }
        return filteredFiles;
    }

    private static Map<File, ISVNStatus> getTreeConflicts (File[] files) {
        Map<File, ISVNStatus> treeConflicts = new HashMap<File, ISVNStatus>(files.length);
        if (files.length > 0) {
            try {
                SvnClient client = Subversion.getInstance().getClient(false);
                FileStatusCache cache = Subversion.getInstance().getStatusCache();
                for (File file : files) {
                    if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_VERSIONED_CONFLICT_TREE) != 0) {
                        ISVNStatus status = SvnUtils.getSingleStatus(client, file);
                        if (status.hasTreeConflict()) {
                            treeConflicts.put(file, status);
                        }
                    }
                }
            } catch (SVNClientException ex) {
                Subversion.LOG.log(Level.INFO, null, ex);
            }
        }
        return treeConflicts;
    }

    private static Map<File, ISVNStatus> getPropertyConflicts (File[] files) {
        Map<File, ISVNStatus> propertyConflicts = new HashMap<File, ISVNStatus>(files.length);
        if (files.length > 0) {
            try {
                SvnClient client = Subversion.getInstance().getClient(false);
                FileStatusCache cache = Subversion.getInstance().getStatusCache();
                for (File file : files) {
                    if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_VERSIONED_CONFLICT_CONTENT) != 0) {
                        ISVNStatus status = SvnUtils.getSingleStatus(client, file);
                        if (status.getPropStatus() == SVNStatusKind.CONFLICTED) {
                            propertyConflicts.put(file, status);
                        }
                    }
                }
            } catch (SVNClientException ex) {
                Subversion.LOG.log(Level.INFO, null, ex);
            }
        }
        return propertyConflicts;
    }

    @Override
    public boolean asynchronous() {
        return false;
    }

}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.subversion.remote.ui.update;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.subversion.remote.FileInformation;
import org.netbeans.modules.subversion.remote.FileStatusCache;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.ISVNStatus;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.ui.actions.ContextAction;
import org.netbeans.modules.subversion.remote.ui.commit.ConflictResolvedAction;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Show basic conflict resolver UI (provided by the diff module) and resolves tree conflicts.
 *
 * 
 */
@Messages({
    "ResolveConflicts=Resolve Con&flicts...",
    "ResolveConflicts_Context=Resolve Con&flicts...",
    "ResolveConflicts_Context_Multiple=Resolve Con&flicts...",
    "# {0} - number of selected projects",
    "ResolveConflicts_Projects=Resolve {0} Projects Con&flicts..."
})
public class ResolveConflictsAction extends ContextAction {

    private static final String ICON_RESOURCE = "org/netbeans/modules/subversion/remote/resources/icons/conflict-resolve.png"; //NOI18N
    
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
        Context ctx = getContext(nodes);
        if(!Subversion.getInstance().checkClientAvailable(ctx)) {
            return;
        }
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        VCSFileProxy[] files = cache.listFiles(ctx, FileInformation.STATUS_VERSIONED_CONFLICT);

        resolveConflicts(files);
    }

    static void resolveConflicts(final VCSFileProxy[] files) {
        Subversion.getInstance().getParallelRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                final Map<VCSFileProxy, ISVNStatus> treeConflicts = getTreeConflicts(files);
                final Map<VCSFileProxy, ISVNStatus> propertyConflicts = getPropertyConflicts(files);
                final List<VCSFileProxy> filteredFiles = removeFolders(files, treeConflicts.keySet());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (filteredFiles.isEmpty() && treeConflicts.isEmpty() && propertyConflicts.isEmpty()) {
                            NotifyDescriptor nd = new NotifyDescriptor.Message(org.openide.util.NbBundle.getMessage(ResolveConflictsAction.class, "MSG_NoConflictsFound")); // NOI18N
                            DialogDisplayer.getDefault().notify(nd);
                        } else {
                            resolveTreeConflicts(treeConflicts);
                            resolvePropertyConflicts(propertyConflicts, filteredFiles);
                            for (VCSFileProxy file : filteredFiles) {
                                ResolveConflictsExecutor executor = new ResolveConflictsExecutor(file);
                                executor.exec();
                            }
                        }
                    }
                });
            }

            private void resolveTreeConflicts (Map<VCSFileProxy, ISVNStatus> treeConflicts) {
                for (Map.Entry<VCSFileProxy, ISVNStatus> e : treeConflicts.entrySet()) {
                    VCSFileProxy file = e.getKey();
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
                VCSFileProxy file = status.getFile();
                NotifyDescriptor nd = new NotifyDescriptor(NbBundle.getMessage(ResolveConflictsAction.class, "MSG_ResolveConflictsAction.ResolveTreeConflict.message", file.getName()), //NOI18N
                        NbBundle.getMessage(ResolveConflictsAction.class, "MSG_ResolveConflictsAction.ResolveTreeConflict.title"), NotifyDescriptor.YES_NO_OPTION, // NOI18N
                        NotifyDescriptor.QUESTION_MESSAGE, new Object[] { NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION}, NotifyDescriptor.NO_OPTION);
                return DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION;
            }

            private void resolvePropertyConflicts (Map<VCSFileProxy, ISVNStatus> propertyConflicts, List<VCSFileProxy> filesToResolve) {
                for (Map.Entry<VCSFileProxy, ISVNStatus> e : propertyConflicts.entrySet()) {
                    VCSFileProxy file = e.getKey();
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
                VCSFileProxy file = status.getFile();
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
    private static List<VCSFileProxy> removeFolders (VCSFileProxy[] files, Set<VCSFileProxy> treeConflicts) {
        LinkedList<VCSFileProxy> filteredFiles = new LinkedList<>();
        for (VCSFileProxy file : files) {
            if (!treeConflicts.contains(file) && file.isFile()) {
                filteredFiles.add(file);
            }
        }
        return filteredFiles;
    }

    private static Map<VCSFileProxy, ISVNStatus> getTreeConflicts (VCSFileProxy[] files) {
        Map<VCSFileProxy, ISVNStatus> treeConflicts = new HashMap<>(files.length);
        if (files.length > 0) {
            try {
                SvnClient client = Subversion.getInstance().getClient(false, new Context(files));
                FileStatusCache cache = Subversion.getInstance().getStatusCache();
                for (VCSFileProxy file : files) {
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

    private static Map<VCSFileProxy, ISVNStatus> getPropertyConflicts (VCSFileProxy[] files) {
        Map<VCSFileProxy, ISVNStatus> propertyConflicts = new HashMap<>(files.length);
        if (files.length > 0) {
            try {
                SvnClient client = Subversion.getInstance().getClient(false, new Context(files));
                FileStatusCache cache = Subversion.getInstance().getStatusCache();
                for (VCSFileProxy file : files) {
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

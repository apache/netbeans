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

package org.netbeans.modules.subversion.ui.diff;

import java.awt.EventQueue;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.*;
import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.subversion.ui.actions.ActionUtils;
import org.netbeans.modules.subversion.ui.status.SyncFileNode;
import org.netbeans.modules.subversion.util.ClientCheckSupport;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Diff action shows local changes
 *
 * @author Petr Kuzel
 */
public class DiffAction extends ContextAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/subversion/resources/icons/diff.png"; //NOI18N

    public DiffAction () {
        super(ICON_RESOURCE);
    }
    
    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Diff";    // NOI18N
    }

    @Override
    protected int getFileEnabledStatus() {
        return getDirectoryEnabledStatus();
    }

    @Override
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED; 
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    public static void diff (final Context ctx, final int type, final String contextName,
            final boolean initialStatusRefreshDisabled) {
        Utils.post(new Runnable() {
            @Override
            public void run () {
                SVNUrl repositoryUrl = null, fileUrl = null;
                RepositoryFile left = null, right = null;
                File[] roots = SvnUtils.getActionRoots(ctx, false);
                if (roots != null && roots.length > 0) {
                    try {
                        File interestingFile;
                        if(roots.length == 1) {
                            interestingFile = roots[0];
                        } else {
                            interestingFile = SvnUtils.getPrimaryFile(roots[0]);
                        }
                        repositoryUrl = SvnUtils.getRepositoryRootUrl(interestingFile);
                        fileUrl = SvnUtils.getRepositoryUrl(interestingFile);
                    } catch (SVNClientException ex) {
                        Subversion.LOG.log(Level.INFO, null, ex);
                    }
                } else if (roots != null && roots.length == 0) {
                    Logger.getLogger(DiffAction.class.getName()).log(Level.WARNING, "No cation roots for context: {0}", Arrays.asList(ctx.getRootFiles()));
                }
                if (repositoryUrl != null && fileUrl != null) {
                    if (type == Setup.DIFFTYPE_LOCAL) {
                        left = new RepositoryFile(repositoryUrl, fileUrl, SVNRevision.BASE);
                        right = new RepositoryFile(repositoryUrl, fileUrl, SVNRevision.WORKING);
                    } else if (type == Setup.DIFFTYPE_REMOTE) {
                        left = new RepositoryFile(repositoryUrl, fileUrl, SVNRevision.HEAD);
                        right = new RepositoryFile(repositoryUrl, fileUrl, SVNRevision.BASE);
                    } else {
                        left = new RepositoryFile(repositoryUrl, fileUrl, SVNRevision.HEAD);
                        right = new RepositoryFile(repositoryUrl, fileUrl, SVNRevision.WORKING);
                    }
                }
                final SVNUrl fRepositoryUrl = repositoryUrl;
                final SVNUrl fFileUrl = fileUrl;
                final RepositoryFile fLeft = left;
                final RepositoryFile fRight = right;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        MultiDiffPanel panel = new MultiDiffPanel(ctx, type, contextName, initialStatusRefreshDisabled,
                                fRepositoryUrl, fFileUrl, fLeft, fRight);
                        DiffTopComponent tc = new DiffTopComponent(panel);
                        tc.setName(NbBundle.getMessage(DiffAction.class, "CTL_DiffPanel_Title", contextName)); // NOI18N
                        tc.open();
                        tc.requestActive();        
                    }
                });
            }
        });
    }

    public static void diff(File file, String rev1, String rev2) {
        MultiDiffPanel panel = new MultiDiffPanel(file, rev1, rev2, false); // spawns bacground DiffPrepareTask
        DiffTopComponent tc = new DiffTopComponent(panel);
        tc.setName(NbBundle.getMessage(DiffAction.class, "CTL_DiffPanel_Title", file.getName())); // NOI18N
        tc.open();
        tc.requestActive();
    }

    public static void diff(File file, ISVNStatus status) {
        MultiDiffPanel panel = new MultiDiffPanel(file, status);
        DiffTopComponent tc = new DiffTopComponent(panel);
        tc.setName(NbBundle.getMessage(DiffAction.class, "CTL_DiffPanel_Title", file.getName())); // NOI18N
        tc.open();
        tc.requestActive();
    }
    
    @Override
    protected void performContextAction(final Node[] nodes) {
        ClientCheckSupport.getInstance().runInAWTIfAvailable(ActionUtils.cutAmpersand(getRunningName(nodes)), new Runnable() {
            @Override
            public void run() {
                Context ctx = getContext(nodes);
                String contextName = getContextDisplayName(nodes);
                diff(ctx, SvnModuleConfig.getDefault().getLastUsedModificationContext(), contextName, isSvnNodes(nodes));
            }
        });
    }
    
    /**
     * Returns true if the given nodes are from the versioning view.
     * In such case the deep scan is not required because the files and their statuses should already be known
     * @param nodes
     * @return
     */
    private static boolean isSvnNodes (Node[] nodes) {
        boolean fromSubversionView = true;
        for (Node node : nodes) {
            if (!(node instanceof SyncFileNode)) {
                fromSubversionView = false;
                break;
            }
        }
        return fromSubversionView;
    }   
    
    public static class DiffToBaseAction extends ContextAction {
        @Override
        protected String getBaseName(Node[] nodes) {
            return "CTL_MenuItem_DiffToBase"; //NOI18N
        }

        @Override
        protected int getFileEnabledStatus() {
            return getDirectoryEnabledStatus();
        }

        @Override
        protected int getDirectoryEnabledStatus() {
            return FileInformation.STATUS_MANAGED 
                 & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED; 
        }

        @Override
        protected void performContextAction (final Node[] nodes) {
            ClientCheckSupport.getInstance().runInAWTIfAvailable(ActionUtils.cutAmpersand(getRunningName(nodes)), new Runnable() {
                @Override
                public void run() {
                    Context ctx = getContext(nodes);
                    String contextName = getContextDisplayName(nodes);
                    diff(ctx, Setup.DIFFTYPE_LOCAL, contextName, isSvnNodes(nodes));
                }
            });
        }
    }
    
    public static class DiffToRepositoryAction extends ContextAction {
        @Override
        protected String getBaseName(Node[] nodes) {
            return "CTL_MenuItem_DiffToRepository"; //NOI18N
        }

        @Override
        protected int getFileEnabledStatus() {
            return getDirectoryEnabledStatus();
        }

        @Override
        protected int getDirectoryEnabledStatus() {
            return FileInformation.STATUS_MANAGED 
                 & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED; 
        }

        @Override
        protected void performContextAction (final Node[] nodes) {
            ClientCheckSupport.getInstance().runInAWTIfAvailable(ActionUtils.cutAmpersand(getRunningName(nodes)), new Runnable() {
                @Override
                public void run() {
                    Context ctx = getContext(nodes);
                    String contextName = getContextDisplayName(nodes);
                    diff(ctx, Setup.DIFFTYPE_ALL, contextName, isSvnNodes(nodes));
                }
            });
        }
    }
}

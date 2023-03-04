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

package org.netbeans.modules.subversion.ui.wcadmin;

import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.util.NotifyHtmlPanel;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 *
 * @author Ondra Vrabec
 */
@ActionID(id = "org.netbeans.modules.subversion.ui.wcadmin.UpgradeAction", category = "Subversion")
@ActionRegistration(displayName = "CTL_Upgrade_Title")
public class UpgradeAction extends ContextAction {

    @Override
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    @Override
    protected int getFileEnabledStatus() {
        return 0;
    }

    @Override
    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_Upgrade_Title"; //NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        final Context ctx = getContext(nodes);
        final File[] roots = ctx.getRootFiles();
        if (roots == null || roots.length == 0) {
            Subversion.LOG.log(Level.FINE, "No versioned folder in the selected context for {0}", nodes); //NOI18N
            return;
        }

        upgrade(true, roots);
    }
    
    public void upgrade (final File root) {
        upgrade(false, root);
    }

    @NbBundle.Messages({
        "# {0} - path to a folder", "MSG_UpgradeAction_statusBar_upgraded=Working Copy at {0} upgraded successfully."
    })
    private void upgrade (boolean explicitelyInvoked, File... roots) {
        final Set<File> toUpgrade = new HashSet<>();
        for (File root : roots) {
            boolean needsUpgrade = false;
            try {
                SvnUtils.getRepositoryRootUrl(root);
            } catch (SVNClientException ex) {
                String msg = ex.getMessage().toLowerCase();
                if (SvnClientExceptionHandler.isTooOldWorkingCopy(msg) && (
                        msg.contains("upgrade") //NOI18N
                        || (msg.contains("working copy format") && msg.contains("is too old")))) { //NOI18N
                    needsUpgrade = true;
                }
            }
            boolean accept;
            if (!explicitelyInvoked) {
                accept = confirmPossibleUpgrade(root.getAbsolutePath());
            } else if (needsUpgrade) {
                accept = confirmUpgrade(root.getAbsolutePath());
            } else {
                accept = forceUpgrade(root.getAbsolutePath());
            }
            if (accept) {
                toUpgrade.add(root);
            }
        }
        if (toUpgrade.isEmpty()) {
            return;
        }
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor();
        SvnProgressSupport support = new SvnProgressSupport() {
            @Override
            protected void perform() {
                for (File root : toUpgrade) {
                    try {
                        SvnClient client = Subversion.getInstance().getClient(true);
                        setCancellableDelegate(client);
                        boolean cont = true;
                        File wcRoot = root;
                        while (cont) {
                            cont = false;
                            try {
                                client.upgrade(wcRoot);
                                Subversion.getInstance().getStatusCache().refreshAsync(Subversion.getInstance().getStatusCache().listFiles(
                                        new File[] { Subversion.getInstance().getTopmostManagedAncestor(wcRoot) }, FileInformation.STATUS_LOCAL_CHANGE));
                                StatusDisplayer.getDefault().setStatusText(Bundle.MSG_UpgradeAction_statusBar_upgraded(root.getAbsolutePath()));
                            } catch (SVNClientException ex) {
                                String msg = ex.getMessage().toLowerCase();
                                if (msg.contains("as it is not a pre-1.7 working copy root")) { //NOI18N
                                    // probably we don't have the working copy root yet
                                    for (String s : new String[] { ".*root is \'([^\']+)\'.*" }) { //NOI18N
                                        Pattern p = Pattern.compile(s, Pattern.DOTALL);
                                        Matcher m = p.matcher(ex.getMessage());
                                        if (m.matches()) {
                                            File rootCandidate = new File(m.group(1));
                                            if (!wcRoot.equals(rootCandidate)) {
                                                wcRoot = rootCandidate;
                                                cont = true;
                                            }
                                            break;
                                        }
                                    }
                                    if (!cont) {
                                        // if users selects folder without .svn folder
                                        File rootCandidate = wcRoot.getParentFile();
                                        if (rootCandidate != null && SvnUtils.isManaged(rootCandidate)) {
                                            wcRoot = rootCandidate;
                                            cont = true;
                                        }
                                    }
                                }
                                if (!cont) {
                                    throw ex;
                                }
                            }
                        }
                    } catch (SVNClientException ex) {
                        annotate(ex);
                    }
                }
            }
        };
        support.start(rp, null, NbBundle.getMessage(UpgradeAction.class, "LBL_Upgrade_Progress")); //NOI18N
    }

    private boolean confirmPossibleUpgrade (String path) {
        return confirm(NbBundle.getMessage(UpgradeAction.class, "LBL_Upgrade_title", path), //NOI18N
                NbBundle.getMessage(UpgradeAction.class, "MSG_Upgrade_possibleUpgrade", path)); //NOI18N
    }

    private boolean confirmUpgrade (String path) {
        return confirm(NbBundle.getMessage(UpgradeAction.class, "LBL_Upgrade_title", path), //NOI18N
                NbBundle.getMessage(UpgradeAction.class, "MSG_Upgrade_upgrade", path)); //NOI18N
    }

    private boolean forceUpgrade (String path) {
        return JOptionPane.showConfirmDialog(Utilities.findDialogParent(), NbBundle.getMessage(UpgradeAction.class, "MSG_Upgrade_forceUpgrade", path), //NOI18N
                NbBundle.getMessage(UpgradeAction.class, "LBL_Upgrade_title", path), //NOI18N
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
    
    private boolean confirm (String title, String message) {
        NotifyHtmlPanel p = new NotifyHtmlPanel();
        p.setText(message);
        NotifyDescriptor descriptor = new NotifyDescriptor(
                p, 
                title,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                new Object [] { NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION },
                NotifyDescriptor.YES_OPTION);
        return NotifyDescriptor.YES_OPTION == DialogDisplayer.getDefault().notify(descriptor);
    }
}

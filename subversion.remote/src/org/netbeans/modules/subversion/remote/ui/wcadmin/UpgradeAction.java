/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.remote.ui.wcadmin;

import java.util.Set;
import java.util.HashSet;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import org.netbeans.modules.subversion.remote.FileInformation;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.client.SvnProgressSupport;
import org.netbeans.modules.subversion.remote.ui.actions.ContextAction;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.NotifyHtmlPanel;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 * 
 */
@ActionID(id = "org.netbeans.modules.subversion.remote.ui.wcadmin.UpgradeAction", category = "SubversionRemote")
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
        final VCSFileProxy[] roots = ctx.getRootFiles();
        if (roots == null || roots.length == 0) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, "No versioned folder in the selected context for {0}", nodes); //NOI18N
            }
            return;
        }

        upgrade(true, roots);
    }
    
    public void upgrade (final VCSFileProxy root) {
        upgrade(false, root);
    }

    @Messages({
        "# {0} - path to a folder", "MSG_UpgradeAction_statusBar_upgraded=Working Copy at {0} upgraded successfully."
    })
    private void upgrade (boolean explicitelyInvoked, VCSFileProxy ... roots) {
        final Set<VCSFileProxy> toUpgrade = new HashSet<>();
        for (VCSFileProxy root : roots) {
            boolean needsUpgrade = false;
            try {
                ContextAction.getSvnUrl(new Context(root));
            } catch (SVNClientException ex) {
                String msg = ex.getMessage().toLowerCase(Locale.ENGLISH);
                if (SvnClientExceptionHandler.isTooOldWorkingCopy(msg) && (
                        msg.contains("upgrade") //NOI18N
                        || (msg.contains("working copy format") && msg.contains("is too old")))) { //NOI18N
                    needsUpgrade = true;
                }
            }
            boolean accept;
            if (!explicitelyInvoked) {
                accept = confirmPossibleUpgrade(root.getPath());
            } else if (needsUpgrade) {
                accept = confirmUpgrade(root.getPath());
            } else {
                accept = forceUpgrade(root.getPath());
            }
            if (accept) {
                toUpgrade.add(root);
            }
        }
        if (toUpgrade.isEmpty()) {
            return;
        }
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor();
        SvnProgressSupport support = new SvnProgressSupport(VCSFileProxySupport.getFileSystem(roots[0])) {
            @Override
            protected void perform() {
                for (VCSFileProxy root : toUpgrade) {
                    try {
                        SvnClient client = Subversion.getInstance().getClient(true, new Context(root));
                        setCancellableDelegate(client);
                        boolean cont = true;
                        VCSFileProxy wcRoot = root;
                        while (cont) {
                            cont = false;
                            try {
                                client.upgrade(wcRoot);
                                Subversion.getInstance().getStatusCache().refreshAsync(Subversion.getInstance().getStatusCache().listFiles(
                                        new VCSFileProxy[] { Subversion.getInstance().getTopmostManagedAncestor(wcRoot) }, FileInformation.STATUS_LOCAL_CHANGE));
                                StatusDisplayer.getDefault().setStatusText(Bundle.MSG_UpgradeAction_statusBar_upgraded(root.getPath()));
                                Subversion.getInstance().refreshTopmostRepositoryUrl(root);
                            } catch (SVNClientException ex) {
                                String msg = ex.getMessage().toLowerCase(Locale.ENGLISH);
                                if (msg.contains("as it is not a pre-1.7 working copy root")) { //NOI18N
                                    // probably we don't have the working copy root yet
                                    for (String s : new String[] { ".*root is \'([^\']+)\'.*" }) { //NOI18N
                                        Pattern p = Pattern.compile(s, Pattern.DOTALL);
                                        Matcher m = p.matcher(ex.getMessage());
                                        if (m.matches()) {
                                            VCSFileProxy rootCandidate = VCSFileProxySupport.getResource(root, m.group(1));
                                            if (!wcRoot.equals(rootCandidate)) {
                                                wcRoot = rootCandidate;
                                                cont = true;
                                            }
                                            break;
                                        }
                                    }
                                    if (!cont) {
                                        // if users selects folder without .svn folder
                                        VCSFileProxy rootCandidate = wcRoot.getParentFile();
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
        return JOptionPane.showConfirmDialog(null, NbBundle.getMessage(UpgradeAction.class, "MSG_Upgrade_forceUpgrade", path), //NOI18N
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

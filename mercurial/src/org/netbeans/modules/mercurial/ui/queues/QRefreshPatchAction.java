/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mercurial.ui.queues;

import java.io.File;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.queues.CreateRefreshAction.Cmd.CreateRefreshPatchCmd;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.mercurial.ui.queues.QRefreshPatchAction", category = "Mercurial/Queues")
@ActionRegistration(displayName = "#CTL_MenuItem_QRefreshPatch")
@NbBundle.Messages({
    "CTL_MenuItem_QRefreshPatch=&Refresh Patch...",
    "CTL_PopupMenuItem_QRefreshPatch=Refresh Patch..."
})
public class QRefreshPatchAction extends CreateRefreshAction {

    static final String KEY_CANCELED_MESSAGE = "qrefresh."; //NOI18N

    public QRefreshPatchAction () {
        super("refresh"); //NOI18N
    }

    @Override
    protected String getBaseName (Node[] nodes) {
        return "CTL_MenuItem_QRefreshPatch"; //NOI18N
    }

    @Override
    @NbBundle.Messages({
        "# {0} - repository name", "MSG_QRefreshPatchAction.err.noPatchApplied=Cannot refresh patch.\n"
            + "No patch applied in repository \"{0}\"."
    })
    QCommitPanel createPanel (final File root, final File[] roots) {
        QPatch currentPatch = null;
        try {
            for (QPatch p : HgCommand.qListSeries(root)) {
                if (p.isApplied()) {
                    currentPatch = p;
                } else {
                    break;
                }
            }
            if (currentPatch == null) {
                NotifyDescriptor.Message e = new NotifyDescriptor.Message(Bundle.MSG_QRefreshPatchAction_err_noPatchApplied(root.getName()));
                DialogDisplayer.getDefault().notifyLater(e);
            } else {
                final HgLogMessage.HgRevision parent = HgCommand.getParent(root, null, currentPatch.getId());
                String commitMessage = HgModuleConfig.getDefault().getLastCanceledCommitMessage(KEY_CANCELED_MESSAGE + currentPatch.getId());
                if (commitMessage.isEmpty()) {
                    commitMessage = HgModuleConfig.getDefault().getLastUsedQPatchMessage(currentPatch.getId());
                    if (commitMessage.isEmpty()) {
                        List<HgLogMessage> msgs = HgCommand.getParents(root, null, null);
                        if (!msgs.isEmpty()) {
                            commitMessage = msgs.get(0).getMessage();
                        }
                    }
                }
                return QCommitPanel.createRefreshPanel(roots, root, commitMessage, currentPatch, parent, QRefreshPatchAction.class.getName());
            }
        } catch (HgException.HgCommandCanceledException ex) {
            // canceled by user, do nothing
        } catch (HgException ex) {
            NotifyDescriptor.Message e = new NotifyDescriptor.Message(ex.getMessage());
            DialogDisplayer.getDefault().notifyLater(e);
        }
        return null;
    }

    @Override
    CreateRefreshPatchCmd createHgCommand (File root, List<File> candidates, OutputLogger logger,
            String message, String patchName, String user, String bundleKeyPostfix,
            List<File> roots, Set<File> excludedFiles, Set<File> filesToRefresh) {
        return new CreateRefreshPatchCmd(root, candidates, logger, message, patchName, user, bundleKeyPostfix,
                roots, excludedFiles, filesToRefresh) {
            @Override
            protected void runHgCommand (File repository, List<File> candidates, Set<File> excludedFiles,
                    String patchId, String msg, String user, OutputLogger logger) throws HgException {
                HgCommand.qRefreshPatch(repository, candidates, excludedFiles, msg, user, logger);
            }
        };
    }

    @Override
    void persistCanceledCommitMessage (QCreatePatchParameters params, String canceledCommitMessage) {
        HgModuleConfig.getDefault().setLastCanceledCommitMessage(KEY_CANCELED_MESSAGE + params.getPatch().getId(), canceledCommitMessage);
    }
    
}

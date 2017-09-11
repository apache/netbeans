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
import org.netbeans.modules.mercurial.ui.queues.CreateRefreshAction.Cmd.CreateRefreshPatchCmd;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.mercurial.ui.queues.QCreatePatchAction", category = "Mercurial/Queues")
@ActionRegistration(displayName = "#CTL_MenuItem_QCreatePatch")
@NbBundle.Messages({
    "CTL_MenuItem_QCreatePatch=&Create Patch...",
    "CTL_PopupMenuItem_QCreatePatch=Create Patch..."
})
public class QCreatePatchAction extends CreateRefreshAction {

    static final String KEY_CANCELED_MESSAGE = "qcreate"; //NOI18N

    public QCreatePatchAction () {
        super("create"); //NOI18N
    }
    
    @Override
    protected String getBaseName (Node[] nodes) {
        return "CTL_MenuItem_QCreatePatch"; //NOI18N
    }

    @Override
    CreateRefreshPatchCmd createHgCommand (File root, List<File> commitCandidates, OutputLogger logger, String message,
            String patchName, String user, String bundleKeyPostfix,
            List<File> roots, Set<File> excludedFiles, Set<File> filesToRefresh) {
        return new Cmd.CreateRefreshPatchCmd(root, commitCandidates, logger, message, patchName, user, bundleKeyPostfix,
                roots, excludedFiles, filesToRefresh) {
            @Override
            protected void runHgCommand (File repository, List<File> candidates, Set<File> excludedFiles,
                    String patchId, String msg, String user, OutputLogger logger) throws HgException {
                HgCommand.qCreatePatch(repository, candidates, excludedFiles, patchId, msg, user, logger);
            }
        };
    }

    @Override
    QCommitPanel createPanel (File root, File[] roots) {
        return QCommitPanel.createNewPanel(roots, root, HgModuleConfig.getDefault().getLastCanceledCommitMessage(KEY_CANCELED_MESSAGE), QCreatePatchAction.class.getName());
    }

    @Override
    void persistCanceledCommitMessage (QCreatePatchParameters params, String canceledCommitMessage) {
        HgModuleConfig.getDefault().setLastCanceledCommitMessage(KEY_CANCELED_MESSAGE, canceledCommitMessage);
    }
}

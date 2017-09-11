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

import java.awt.EventQueue;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.diff.DiffAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.mercurial.ui.queues.QDiffAction", category = "Mercurial/Queues")
@ActionRegistration(displayName = "#CTL_MenuItem_QDiff")
@NbBundle.Messages({
    "CTL_MenuItem_QDiff=&Diff",
    "CTL_PopupMenuItem_QDiff=Diff"
})
public class QDiffAction extends ContextAction {

    @Override
    protected boolean enable (Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName (Node[] nodes) {
        return "CTL_MenuItem_QDiff"; //NOI18N
    }

    @Override
    @NbBundle.Messages({
        "# {0} - number of selected files",
        "LBL_DiffView.name.files={0} files",
        "# {0} - label for the selected context",
        "LBL_DiffView.name=QDiff - {0}"
    })
    protected void performContextAction (Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        final File roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) return;
        final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);
        new HgProgressSupport() {

            @Override
            protected void perform () {
                if (!QUtils.isMQEnabledExtension(root)) {
                    return;
                }
                try {
                    List<HgLogMessage> parents = HgCommand.getParents(root, null, null);
                    if (parents.size() != 1 || !Arrays.asList(parents.get(0).getTags()).contains(QPatch.TAG_QTIP)) {
                        NotifyDescriptor.Message e = new NotifyDescriptor.Message(NbBundle.getMessage(QDiffAction.class, "MSG_DiffAction.error.notAtTip"), NotifyDescriptor.ERROR_MESSAGE); //NOI18N
                        DialogDisplayer.getDefault().notifyLater(e);
                    } else {
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                SystemAction.get(DiffAction.class).diff(roots, HgLogMessage.HgRevision.QDIFF_BASE,
                                        HgLogMessage.HgRevision.CURRENT,
                                        Bundle.LBL_DiffView_name(roots.length == 1 
                                        ? roots[0].getName() 
                                        : Bundle.LBL_DiffView_name_files(roots.length)),
                                        true, false);
                            }
                        });
                    }
                } catch (HgException.HgCommandCanceledException ex) {
                    // canceled by user, do nothing
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                }
            }
            
        }.start(Mercurial.getInstance().getRequestProcessor(root), root, NbBundle.getMessage(QDiffAction.class, "LBL_DiffAction.progress")); //NOI18N
    }

}

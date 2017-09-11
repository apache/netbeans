/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mercurial.ui.diff;

import java.awt.EventQueue;
import java.io.File;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Ondrej Vrabec
 */
@NbBundle.Messages({
    "CTL_MenuItem_DiffToRevision=Diff &To Revision...",
    "CTL_PopupMenuItem_DiffToRevision=Diff To Revision..."
})
public class DiffToRevisionAction extends ContextAction {
    
    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        return HgUtils.isFromHgRepository(context);
    }

    @Override
    protected String getBaseName (Node[] nodes) {
        return "CTL_MenuItem_DiffToRevision"; //NOI18N
    }

    @Override
    protected void performContextAction (Node[] nodes) {
        final VCSContext context = HgUtils.getCurrentContext(nodes);
        final File[] actionRoots = HgUtils.getActionRoots(context);
        if (actionRoots == null || actionRoots.length == 0) {
            return;
        }
        Utils.post(new Runnable() {
            @Override
            public void run () {
                final File repository = Mercurial.getInstance().getRepositoryRoot(actionRoots[0]);
                try {
                    final HgRevision parent = HgCommand.getParent(repository, null, null);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            String contextName = Utils.getContextDisplayName(context);
                            diff(repository, actionRoots, parent, contextName);
                        }
                    });
                } catch (HgException.HgCommandCanceledException ex) {
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                }
            }
        });
    }
    
    private void diff (File repository, File[] roots, HgRevision wcParent, String contextDisplayName) {
        DiffToRevision diffPanel = new DiffToRevision(repository, wcParent);
        if (diffPanel.showDialog()) {
            SystemAction.get(DiffAction.class).diff(roots, diffPanel.getSelectedTreeFirst(),
                    diffPanel.getSelectedTreeSecond(), contextDisplayName, false, true);
        }
    }
}

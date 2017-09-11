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

package org.netbeans.modules.git.ui.actions;

import java.awt.EventQueue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author ondra
 */
public abstract class GitAction extends NodeAction {
    
    // it's singleton
    // do not declare any instance data

    protected GitAction () {
        this(null);
    }

    protected GitAction (String iconResource) {
        if (iconResource == null) {
            setIcon(null);
            putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        } else {
            setIcon(ImageUtilities.loadImageIcon(iconResource, true));
        }
    }

    @Override
    protected boolean enable (Node[] activatedNodes) {
        Future<Project[]> projectOpenTask = OpenProjects.getDefault().openProjects();
        if (projectOpenTask.isDone()) {
            return enableFull(activatedNodes);
        } else {
            return true;
        }
    }

    @Override
    @NbBundle.Messages({
        "MSG_GitAction.actionDisabled=Action cannot be executed,\nit is disabled in the current context.",
        "MSG_GitAction.savingFiles.progress=Preparing Git action"
    })
    protected final void performAction(final Node[] nodes) {
        final AtomicBoolean canceled = new AtomicBoolean(false);
        Runnable run = new Runnable() {

            @Override
            public void run () {
                if (!enableFull(nodes)) {
                    Logger.getLogger(GitAction.class.getName()).log(Level.INFO, "Action got disabled, execution stopped");
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.MSG_GitAction_actionDisabled()));
                    return;
                }
                LifecycleManager.getDefault().saveAll();
                Utils.logVCSActionEvent("Git"); //NOI18N
                if (!canceled.get()) {
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run () {
                            performContextAction(nodes);
                        }
                    });
                }
            }
        };
        ProgressUtils.runOffEventDispatchThread(run, Bundle.MSG_GitAction_savingFiles_progress(), canceled, false);
    }

    protected abstract void performContextAction(Node[] nodes);

    @Override
    public String getName () {
        return NbBundle.getMessage(getClass(), "LBL_" + getClass().getSimpleName() + "_Name"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx(getClass());
    }

    protected final VCSContext getCurrentContext (Node[] nodes) {
        return GitUtils.getCurrentContext(nodes);
    }

    @Override
    protected final boolean asynchronous () {
        return false;
    }

    protected boolean enableFull (Node[] activatedNodes) {
        VCSContext context = getCurrentContext(activatedNodes);
        return GitUtils.isFromGitRepository(context);
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.git.remote.ui.actions;

import java.awt.EventQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.LifecycleManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
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
        VCSContext context = getCurrentContext(activatedNodes);
        return GitUtils.isFromGitRepository(context);
    }

    @Override
    @NbBundle.Messages({
        "MSG_GitAction.savingFiles.progress=Preparing Git action"
    })
    protected final void performAction(final Node[] nodes) {
        final AtomicBoolean canceled = new AtomicBoolean(false);
        Runnable run = new Runnable() {

            @Override
            public void run () {
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
}

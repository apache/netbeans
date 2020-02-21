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

package org.netbeans.modules.mercurial.remote.ui.update;

import org.netbeans.modules.mercurial.remote.FileStatusCache;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.mercurial.remote.FileInformation;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Show basic conflict resolver UI (provided by the diff module).
 *
 * 
 */
@Messages({
    "CTL_MenuItem_Resolve=Resolve Con&flicts"
})
public class ResolveConflictsAction extends ContextAction {

    private static final String ICON_RESOURCE = "org/netbeans/modules/mercurial/remote/resources/icons/conflict-resolve.png"; //NOI18N
    
    public ResolveConflictsAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        return cache.containsFileOfStatus(context, FileInformation.STATUS_VERSIONED_CONFLICT, false);
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Resolve";                                  //NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        resolve(context);
    }

    public static void resolve(VCSContext ctx) {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        VCSFileProxy[] files = cache.listFiles(ctx, FileInformation.STATUS_VERSIONED_CONFLICT);

        resolveConflicts(files);

    }

    static void resolveConflicts(final VCSFileProxy[] files) {
        if (files.length == 0) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    org.openide.util.NbBundle.getMessage(
                        ResolveConflictsAction.class, "MSG_NoConflictsFound")); // NOI18N
            DialogDisplayer.getDefault().notify(nd);
        } else {
            new HgProgressSupport() {
                @Override
                protected void perform() {
                    for (int i = 0; i < files.length; i++) {
                        VCSFileProxy file = files[i];
                        ResolveConflictsExecutor executor = new ResolveConflictsExecutor(file);
                        executor.exec();
                        if (isCanceled()) {
                            break;
                        }
                    }
                }
            }.start(Mercurial.getInstance().getRequestProcessor(), files[0], NbBundle.getMessage(ResolveConflictsAction.class, "MSG_PreparingMerge"));
        }        
    }
    
}

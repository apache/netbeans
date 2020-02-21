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

package org.netbeans.modules.mercurial.remote.ui.commit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 * 
 */
@Messages({
    "CTL_MenuItem_IncludeInCommit=In&clude in Commit",
    "CTL_MenuItem_ExcludeFromCommit=Ex&clude from Commit"
})
public final class ExcludeFromCommitAction extends ContextAction {

    public static final int UNDEFINED = -1;
    public static final int EXCLUDING = 1;
    public static final int INCLUDING = 2;

    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        Set<VCSFileProxy> ctxFiles = context != null? context.getRootFiles(): null;
        if (!HgUtils.isFromHgRepository(context) || ctxFiles == null || ctxFiles.size() == 0) {
            return false;
        }
        return !HgUtils.onlyProjects(nodes) && !HgUtils.onlyFolders(ctxFiles);
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        int actionStatus = getActionStatus(ctx);
        switch (actionStatus) {
        case UNDEFINED:
        case EXCLUDING:
            return "CTL_MenuItem_ExcludeFromCommit";                    //NOI18N
        case INCLUDING:
            return "CTL_MenuItem_IncludeInCommit";                      //NOI18N
        default:
            throw new RuntimeException("Invalid action status: " + actionStatus); // NOI18N
        }
    }
    
    public int getActionStatus(VCSContext ctx) {
        HgModuleConfig config = HgModuleConfig.getDefault(HgUtils.getRootFile(ctx));
        int status = UNDEFINED;
        if (ctx == null) {
            ctx = HgUtils.getCurrentContext(null);
        }
        Set<VCSFileProxy> files = ctx.getRootFiles();
        for (VCSFileProxy file : files) {
            if (config.isExcludedFromCommit(file.getPath())) {
                if (status == EXCLUDING) {
                    return UNDEFINED;
                }
                status = INCLUDING;
            } else {
                if (status == INCLUDING) {
                    return UNDEFINED;
                }
                status = EXCLUDING;
            }
        }
        return status;
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        final VCSContext ctx = HgUtils.getCurrentContext(nodes);
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor();
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                HgModuleConfig config = HgModuleConfig.getDefault(HgUtils.getRootFile(ctx));
                int status = getActionStatus(ctx);
                Set<VCSFileProxy> files = ctx.getRootFiles();
                List<String> paths = new ArrayList<>(files.size());
                for (VCSFileProxy file : files) {
                    paths.add(file.getPath());
                }
                if (isCanceled()) {
                    return;
                }
                if (status != INCLUDING) {
                    config.addExclusionPaths(paths);
                } else {
                    config.removeExclusionPaths(paths);
                }
            }
        };
        support.start(rp, HgUtils.getRootFile(ctx), "");                                          //NOI18N
    }
}

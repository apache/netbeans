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

package org.netbeans.modules.mercurial.ui.commit;

import java.io.*;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.nodes.*;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Kuzel
 */
@NbBundle.Messages({
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
        Set<File> ctxFiles = context != null? context.getRootFiles(): null;
        if (!HgUtils.isFromHgRepository(context) || ctxFiles == null || ctxFiles.size() == 0) {
            return false;
        }
        return !HgUtils.onlyProjects(nodes) && !HgUtils.onlyFolders(ctxFiles);
    }

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
        HgModuleConfig config = HgModuleConfig.getDefault();
        int status = UNDEFINED;
        if (ctx == null) {
            ctx = HgUtils.getCurrentContext(null);
        }
        Set<File> files = ctx.getRootFiles();
        for (File file : files) {
            if (config.isExcludedFromCommit(file.getAbsolutePath())) {
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
            public void perform() {
                HgModuleConfig config = HgModuleConfig.getDefault();
                int status = getActionStatus(ctx);
                Set<File> files = ctx.getRootFiles();
                List<String> paths = new ArrayList<String>(files.size());
                for (File file : files) {
                    paths.add(file.getAbsolutePath());
                }
                if (isCanceled()) return;
                if (status != INCLUDING) {
                    config.addExclusionPaths(paths);
                } else {
                    config.removeExclusionPaths(paths);
                }
            }
        };
        support.start(rp, "");                                          //NOI18N
    }
}

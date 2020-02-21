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

import java.util.logging.Level;
import org.netbeans.modules.mercurial.remote.FileStatusCache;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.FileInformation;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import  org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Allow files who have Conlfict Status to be manually resolved.
 *
 * 
 */
@Messages({
    "CTL_MenuItem_MarkResolved=&Mark as Resolved"
})
public class ConflictResolvedAction extends ContextAction {

    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        return cache.containsFileOfStatus(context, FileInformation.STATUS_VERSIONED_CONFLICT, false);
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_MarkResolved";                             //NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        resolved(context);
    }

    public static void resolved(VCSContext ctx) {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        VCSFileProxy[] files = cache.listFiles(ctx, FileInformation.STATUS_VERSIONED_CONFLICT);
        final VCSFileProxy root = HgUtils.getRootFile(ctx);
        if (root == null || files == null || files.length == 0) {
            return;
        }

        conflictResolved(root, files);
    }

    public static void conflictResolved(VCSFileProxy repository, final VCSFileProxy[] files) {
        if (repository == null || files == null || files.length == 0) {
            return;
        }
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {

            @Override
            public void perform() {
                OutputLogger logger = getLogger();
                for (VCSFileProxy file : files) {
                    if (isCanceled()) {
                        return;
                    }
                    VCSFileProxy repository = Mercurial.getInstance().getRepositoryRoot(file);
                    ConflictResolvedAction.perform(file, repository, logger);
                }
            }
        };
        support.start(rp, repository, NbBundle.getMessage(ConflictResolvedAction.class, "MSG_ConflictResolved_Progress")); // NOI18N
    }
    
    private static void perform(VCSFileProxy file, VCSFileProxy repository, OutputLogger logger) {
        if (file == null) {
            return;
        }
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        file = file.normalizeFile();
        try {
            HgCommand.markAsResolved(repository, file, logger);
        } catch (HgException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
        }
        HgCommand.deleteConflictFile(file);
        Mercurial.LOG.log(Level.FINE, "ConflictResolvedAction.perform(): DELETE CONFLICT File: {0}", // NOI18N
                new Object[] {file.getPath() + HgCommand.HG_STR_CONFLICT_EXT} );
        cache.refresh(file);
    }
    
    public static void resolved(final VCSFileProxy file) {
        if (file == null) {
            return;
        }
        final VCSFileProxy repository = Mercurial.getInstance().getRepositoryRoot(file);
        if (repository == null) {
            return;
        }
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                ConflictResolvedAction.perform(file, repository, getLogger());
            }
        };
        support.start(rp, repository, NbBundle.getMessage(ConflictResolvedAction.class, "MSG_ConflictResolved_Progress")); // NOI18N
    }

}

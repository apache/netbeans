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

package org.netbeans.modules.mercurial.ui.update;

import java.io.File;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import  org.openide.util.NbBundle;

/**
 * Allow files who have Conlfict Status to be manually resolved.
 *
 * @author Petr Kuzel
 */
@NbBundle.Messages({
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
        File[] files = cache.listFiles(ctx, FileInformation.STATUS_VERSIONED_CONFLICT);
        final File root = HgUtils.getRootFile(ctx);
        if (root == null || files == null || files.length == 0) return;

        conflictResolved(root, files);
    }

    public static void conflictResolved(File repository, final File[] files) {
        if (repository == null || files == null || files.length == 0) return;
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {

            @Override
            public void perform() {
                OutputLogger logger = getLogger();
                for (File file : files) {
                    if (isCanceled()) {
                        return;
                    }
                    File repository = Mercurial.getInstance().getRepositoryRoot(file);
                    ConflictResolvedAction.perform(file, repository, logger);
                }
            }
        };
        support.start(rp, repository, NbBundle.getMessage(ConflictResolvedAction.class, "MSG_ConflictResolved_Progress")); // NOI18N
    }
    
    private static void perform(File file, File repository, OutputLogger logger) {
        if (file == null) return;
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        file = FileUtil.normalizeFile(file);
        try {
            HgCommand.markAsResolved(repository, file, logger);
        } catch (HgException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
        }
        HgCommand.deleteConflictFile(file.getAbsolutePath());
        Mercurial.LOG.log(Level.FINE, "ConflictResolvedAction.perform(): DELETE CONFLICT File: {0}", // NOI18N
                new Object[] {file.getAbsolutePath() + HgCommand.HG_STR_CONFLICT_EXT} );
        cache.refresh(file);
    }
    
    public static void resolved(final File file) {
        if (file == null) return;
        final File repository = Mercurial.getInstance().getRepositoryRoot(file);
        if (repository == null) return;
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

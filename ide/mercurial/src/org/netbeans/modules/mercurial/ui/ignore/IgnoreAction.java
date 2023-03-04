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

package org.netbeans.modules.mercurial.ui.ignore;

import java.util.*;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.*;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.queries.SharabilityQuery;
import org.openide.nodes.Node;

/**
 * Adds/removes files to repository .hgignore.
 *
 * @author Maros Sandor
 */
@NbBundle.Messages({
    "CTL_MenuItem_Ignore=Toggle &Ignore"
})
public class IgnoreAction extends ContextAction {
    
    public static final int UNDEFINED  = 0;
    public static final int IGNORING   = 1;
    public static final int UNIGNORING = 2;
    
    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        Set<File> ctxFiles = context != null? context.getRootFiles(): null;
        if(!HgUtils.isFromHgRepository(context) || ctxFiles == null || ctxFiles.isEmpty()) {
            return false;
        }
        return !HgUtils.onlyProjects(nodes);
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Ignore";                                   //NOI18N
    }
   
    public int getActionStatus(File [] files) {
        int actionStatus = -1;
        if (files.length == 0) return UNDEFINED; 
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(".hg") || // NOI18N
                    SharabilityQuery.getSharability(files[i])== SharabilityQuery.NOT_SHARABLE) { 
                actionStatus = UNDEFINED;
                break;
            }
            FileInformation info = cache.getStatus(files[i]);
            if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY || info.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE && info.isDirectory()) {
                if (actionStatus == UNIGNORING) {
                    actionStatus = UNDEFINED;
                    break;
                }
                actionStatus = IGNORING;
            } else if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                if (actionStatus == IGNORING) {
                    actionStatus = UNDEFINED;
                    break;
                }
                actionStatus = UNIGNORING;
            } else {
                actionStatus = UNDEFINED;
                break;
            }
        }
        return actionStatus == -1 ? UNDEFINED : actionStatus;
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        final VCSContext context = HgUtils.getCurrentContext(nodes);
        final Set<File> repositories = HgUtils.getRepositoryRoots(context);
        if(repositories == null || repositories.isEmpty()) return;

        final Set<File> ctxFiles = context != null? context.getRootFiles(): null;
        if(ctxFiles == null || ctxFiles.isEmpty()) return;

        File repository = repositories.iterator().next();
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                for (File repository : repositories) {
                    final File[] files = HgUtils.filterForRepository(context, repository, true);
                    performIgnore(repository, files); // NOI18N
                }
            }

            private void performIgnore(final File repository, final File[] files) throws MissingResourceException {
                OutputLogger logger = getLogger();
                int mActionStatus = getActionStatus(files);
                try {
                    if (mActionStatus == UNDEFINED) {
                        logger.outputInRed(NbBundle.getMessage(IgnoreAction.class, "MSG_IGNORE_TITLE"));
                        logger.outputInRed(NbBundle.getMessage(IgnoreAction.class, "MSG_IGNORE_TITLE_SEP"));
                        logger.output(NbBundle.getMessage(IgnoreAction.class, "MSG_IGNORE_ONLY_LOCALLY_NEW"));
                        logger.outputInRed(NbBundle.getMessage(IgnoreAction.class, "MSG_IGNORE_DONE"));
                        logger.output("");
                        return;
                    }
                    if (mActionStatus == IGNORING) {
                        HgUtils.addIgnored(repository, files);
                        logger.outputInRed(NbBundle.getMessage(IgnoreAction.class, "MSG_IGNORE_TITLE"));
                        logger.outputInRed(NbBundle.getMessage(IgnoreAction.class, "MSG_IGNORE_TITLE_SEP"));
                        logger.output(NbBundle.getMessage(IgnoreAction.class, "MSG_IGNORE_INIT_SEP", repository.getName()));
                    } else {
                        HgUtils.removeIgnored(repository, files);
                        logger.outputInRed(NbBundle.getMessage(IgnoreAction.class, "MSG_UNIGNORE_TITLE"));
                        logger.outputInRed(NbBundle.getMessage(IgnoreAction.class, "MSG_UNIGNORE_TITLE_SEP"));
                        logger.output(NbBundle.getMessage(IgnoreAction.class, "MSG_UNIGNORE_INIT_SEP", repository.getName()));
                    }
                } catch (IOException ex) {
                    Mercurial.LOG.log(Level.FINE, "IgnoreAction(): File {0} - {1}", new Object[]{repository.getAbsolutePath(), ex.toString()});
                }
                for (File file : files) {
                    Mercurial.getInstance().getFileStatusCache().refreshIgnores(file);
                    logger.output("\t" + file.getAbsolutePath());
                }
                if (mActionStatus == IGNORING) {
                    logger.outputInRed(NbBundle.getMessage(IgnoreAction.class, "MSG_IGNORE_DONE"));
                } else {
                    logger.outputInRed(NbBundle.getMessage(IgnoreAction.class, "MSG_UNIGNORE_DONE"));
                }
                logger.output("");
            }
        };
        support.start(rp, repository, org.openide.util.NbBundle.getMessage(IgnoreAction.class, "LBL_Ignore_Progress"));

    }


}

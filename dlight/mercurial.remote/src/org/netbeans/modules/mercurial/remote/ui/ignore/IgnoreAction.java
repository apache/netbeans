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

package org.netbeans.modules.mercurial.remote.ui.ignore;

import java.util.*;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.remote.*;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import java.io.IOException;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;

/**
 * Adds/removes files to repository .hgignore.
 *
 * 
 */
@Messages({
    "CTL_MenuItem_Ignore=Toggle &Ignore"
})
public class IgnoreAction extends ContextAction {
    
    public static final int UNDEFINED  = 0;
    public static final int IGNORING   = 1;
    public static final int UNIGNORING = 2;
    
    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        Set<VCSFileProxy> ctxFiles = context != null? context.getRootFiles(): null;
        if(!HgUtils.isFromHgRepository(context) || ctxFiles == null || ctxFiles.isEmpty()) {
            return false;
        }
        return !HgUtils.onlyProjects(nodes);
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Ignore";                                   //NOI18N
    }
   
    public int getActionStatus(VCSFileProxy [] files) {
        int actionStatus = -1;
        if (files.length == 0) {
            return UNDEFINED;
        } 
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(".hg") || // NOI18N
                    SharabilityQuery.getSharability(VCSFileProxySupport.toURI(files[i])) == SharabilityQuery.Sharability.NOT_SHARABLE) { 
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
        if (context == null) {
            return;
        }
        final Set<VCSFileProxy> repositories = HgUtils.getRepositoryRoots(context);
        if(repositories.isEmpty()) {
            return;
        }

        final Set<VCSFileProxy> ctxFiles = context.getRootFiles();
        if(ctxFiles == null || ctxFiles.isEmpty()) {
            return;
        }

        VCSFileProxy repository = repositories.iterator().next();
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                for (VCSFileProxy repository : repositories) {
                    final VCSFileProxy[] files = HgUtils.filterForRepository(context, repository, true);
                    performIgnore(repository, files); // NOI18N
                }
            }

            private void performIgnore(final VCSFileProxy repository, final VCSFileProxy[] files) throws MissingResourceException {
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
                    Mercurial.LOG.log(Level.FINE, "IgnoreAction(): File {0} - {1}", new Object[]{repository.getPath(), ex.toString()});
                }
                for (VCSFileProxy file : files) {
                    Mercurial.getInstance().getFileStatusCache().refreshIgnores(file);
                    logger.output("\t" + file.getPath()); //NOI18N
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

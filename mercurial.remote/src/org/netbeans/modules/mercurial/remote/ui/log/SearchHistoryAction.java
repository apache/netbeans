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

package org.netbeans.modules.mercurial.remote.ui.log;

import org.openide.util.NbBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.remote.FileInformation;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.nodes.Node;

/**
 * Opens Search History Component.
 * 
 * 
 */
public abstract class SearchHistoryAction extends ContextAction {
    
    protected SearchHistoryAction () {
        
    }
    
    protected SearchHistoryAction (String menuIcon) {
        super(menuIcon);
    }
    
    static final int DIRECTORY_ENABLED_STATUS = FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    static final int FILE_ENABLED_STATUS = FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;

    @Override
    protected boolean enable(Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    protected static void outputSearchContextTab (VCSFileProxy repositoryRoot, String title) {
        OutputLogger logger = openLogger(repositoryRoot, title);
        logger.output(NbBundle.getMessage(SearchHistoryAction.class, "MSG_LOG_ROOT_CONTEXT_SEP")); // NOI18N
        logger.output(repositoryRoot.getPath());
        closeLog(logger);
    }

    protected static void outputSearchContextTab (VCSFileProxy repositoryRoot, VCSFileProxy[] files, String title) {
        OutputLogger logger = openLogger(repositoryRoot, title);
        logger.output(NbBundle.getMessage(SearchHistoryAction.class, "MSG_LOG_CONTEXT_SEP")); // NOI18N
        for (VCSFileProxy f : files) {
            logger.output(f.getPath());
        }
        closeLog(logger);
    }

    private static OutputLogger openLogger (VCSFileProxy repositoryRoot, String title) {
        OutputLogger logger = OutputLogger.getLogger(repositoryRoot);
        logger.outputInRed(
                NbBundle.getMessage(SearchHistoryAction.class,
                title));
        logger.outputInRed(
                NbBundle.getMessage(SearchHistoryAction.class,
                "MSG_Log_Title_Sep")); // NOI18N
        return logger;
    }

    private static void closeLog (OutputLogger logger) {
        logger.outputInRed(""); // NOI18N
        logger.closeLog();
    }

    protected VCSFileProxy getRepositoryRoot (VCSContext context) {
        VCSFileProxy repositoryRoot = null;
        if (context != null) {
            final VCSFileProxy roots[] = HgUtils.getActionRoots(context);
            if (roots != null && roots.length > 0) {
                repositoryRoot = Mercurial.getInstance().getRepositoryRoot(roots[0]);
            } else {
                VCSFileProxy repo = HgUtils.getRootFile(context);
                if (repo != null) {
                    Logger.getLogger(SearchHistoryAction.class.getName()).log(Level.INFO, "getActionRoots() returns empty, yet context contains {0} as root", repo); //NOI18N
                }
            }
        }
        return repositoryRoot;
    }

    protected VCSFileProxy[] getFiles (VCSContext context, VCSFileProxy repository) {
        VCSFileProxy[] files = null;
        if (repository != null) {
            files = HgUtils.filterForRepository(context, repository, false);
        }
        return files;
    }
}

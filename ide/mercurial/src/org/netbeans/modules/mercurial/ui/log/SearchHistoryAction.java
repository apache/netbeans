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

package org.netbeans.modules.mercurial.ui.log;

import org.openide.util.NbBundle;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.nodes.Node;

/**
 * Opens Search History Component.
 * 
 * @author Maros Sandor
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

    protected static void outputSearchContextTab (File repositoryRoot, String title) {
        OutputLogger logger = openLogger(repositoryRoot, title);
        logger.output(NbBundle.getMessage(SearchHistoryAction.class, "MSG_LOG_ROOT_CONTEXT_SEP")); // NOI18N
        logger.output(repositoryRoot.getAbsolutePath());
        closeLog(logger);
    }

    protected static void outputSearchContextTab (File repositoryRoot, File[] files, String title) {
        OutputLogger logger = openLogger(repositoryRoot, title);
        logger.output(NbBundle.getMessage(SearchHistoryAction.class, "MSG_LOG_CONTEXT_SEP")); // NOI18N
        for (File f : files) {
            logger.output(f.getAbsolutePath());
        }
        closeLog(logger);
    }

    private static OutputLogger openLogger (File repositoryRoot, String title) {
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

    protected File getRepositoryRoot (VCSContext context) {
        File repositoryRoot = null;
        if (context != null) {
            final File roots[] = HgUtils.getActionRoots(context);
            if (roots != null && roots.length > 0) {
                repositoryRoot = Mercurial.getInstance().getRepositoryRoot(roots[0]);
            } else {
                File repo = HgUtils.getRootFile(context);
                if (repo != null) {
                    Logger.getLogger(SearchHistoryAction.class.getName()).log(Level.INFO, "getActionRoots() returns empty, yet context contains {0} as root", repo); //NOI18N
                }
            }
        }
        return repositoryRoot;
    }

    protected File[] getFiles (VCSContext context, File repository) {
        File[] files = null;
        if (repository != null) {
            files = HgUtils.filterForRepository(context, repository, false);
        }
        return files;
    }
}

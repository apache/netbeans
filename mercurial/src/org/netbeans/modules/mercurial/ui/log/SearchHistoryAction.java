/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

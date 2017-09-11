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

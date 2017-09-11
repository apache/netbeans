/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.git.ui.tag;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitTag;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.tag.CreateTagAction", category = "Git")
@ActionRegistration(displayName = "#LBL_CreateTagAction_Name")
@NbBundle.Messages("LBL_CreateTagAction_Name=Crea&te Tag...")
public class CreateTagAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(CreateTagAction.class.getName());

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        createTag(repository, info.getActiveBranch().getName().equals(GitBranch.NO_BRANCH) ? GitUtils.HEAD : info.getActiveBranch().getName());
    }

    public void createTag (final File repository, String preselectedRevision) {
        final CreateTag createTag = new CreateTag(repository, preselectedRevision, "");
        if (createTag.show()) {
            GitProgressSupport supp = new GitProgressSupport() {
                @Override
                protected void perform () {
                    try {
                        new CreateTagProcess(createTag, this, getClient()).call();
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    }
                }
            };
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(CreateTagAction.class, "LBL_CreateTagAction.progressName")); //NOI18N
        }
    }
    
    static class CreateTagProcess implements Callable<GitTag> {
        
        private final GitProgressSupport supp;
        private final CreateTag createTag;
        private final GitClient client;

        public CreateTagProcess (CreateTag createTag, GitProgressSupport supp, GitClient client) {
            this.supp = supp;
            this.createTag = createTag;
            this.client = client;
        }
        
        @Override
        public GitTag call () {
            try {
                LOG.log(Level.FINE, "Creating a tag: {0}/{1}", new Object[] { createTag.getTagName(), createTag.getRevision() }); //NOI18N
                GitTag tag = client.createTag(createTag.getTagName(), createTag.getRevision(),
                        createTag.getTagMessage(), false, createTag.isForceUpdate(), supp.getProgressMonitor());
                log(tag);
                return tag;
            } catch (GitException ex) {
                GitClientExceptionHandler.notifyException(ex, true);
            }
            return null;
        }

        private void log (GitTag tag) {
            OutputLogger logger = supp.getLogger();
            logger.outputLine(NbBundle.getMessage(CreateTagAction.class, "MSG_CreateTagAction.tagCreated", new Object[] { tag.getTagName(), //NOI18N
                tag.getTaggedObjectId(),
                tag.getTagId(),
                tag.getTagger().toString(),
                tag.getMessage() }));
        }
        
    }
}

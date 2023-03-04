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
package org.netbeans.modules.mercurial.ui.tag;

import java.io.File;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.RequestProcessor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * 
 */
@ActionID(id = "org.netbeans.modules.mercurial.ui.tag.CreateTagAction", category = "Mercurial")
@ActionRegistration(displayName = "#CTL_MenuItem_CreateTag")
@NbBundle.Messages({
    "CTL_MenuItem_CreateTag=Create &Tag...",
    "CTL_PopupMenuItem_CreateTag=Create Tag..."
})
public class CreateTagAction extends ContextAction {
    
    @Override
    protected boolean enable(Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_CreateTag"; //NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        final File roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) return;
        final File repository = Mercurial.getInstance().getRepositoryRoot(roots[0]);

        CreateTag createTag = new CreateTag(repository);
        if (!createTag.showDialog()) {
            return;
        }
        final String tagName = createTag.getTagName();
        final String message = createTag.getMessage();
        final String revision = createTag.getRevision();
        final boolean local = createTag.isLocalTag();
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                OutputLogger logger = getLogger();
                try {
                    logger.outputInRed(NbBundle.getMessage(CreateTagAction.class, "MSG_CREATE_TITLE")); //NOI18N
                    logger.outputInRed(NbBundle.getMessage(CreateTagAction.class, "MSG_CREATE_TITLE_SEP")); //NOI18N
                    logger.output(NbBundle.getMessage(CreateTagAction.class, "MSG_CREATE_INFO_SEP", tagName, repository.getAbsolutePath())); //NOI18N
                    HgCommand.createTag(repository, tagName, message, revision, local, logger);
                    if (!local) {
                        HgUtils.logHgLog(HgCommand.doTip(repository, logger), logger);
                    }
                } catch (HgException.HgCommandCanceledException ex) {
                    // canceled by user, do nothing
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                }
                logger.outputInRed(NbBundle.getMessage(CreateTagAction.class, "MSG_CREATE_DONE")); //NOI18N
                logger.output(""); //NOI18N
            }
        };
        support.start(rp, repository, org.openide.util.NbBundle.getMessage(CreateTagAction.class, "MSG_CreateTag_Progress", tagName)); //NOI18N
    }
}

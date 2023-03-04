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
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Update action for mercurial: 
 * hg update - update or merge working directory
 * 
 * @author John Rice
 */
public class UpdateAction extends ContextAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/mercurial/resources/icons/update.png"; //NOI18N
    
    public UpdateAction () {
        super(ICON_RESOURCE);
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Update"; // NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        update(HgUtils.getCurrentContext(nodes), null);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }

    public void update (final VCSContext ctx, HgLogMessage rev){

        final File roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) return;
        File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);
        update(root, rev);
    }
    
    public void update (final File root, HgLogMessage rev) {
        final Update update = new Update(root, rev);
        if (!update.showDialog()) {
            return;
        }
        final String revStr = update.getSelectionRevision();
        final boolean doForcedUpdate = update.isForcedUpdateRequested();
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                boolean bNoUpdates = true;
                OutputLogger logger = getLogger();
                try {
                    logger.outputInRed(
                            NbBundle.getMessage(UpdateAction.class,
                            "MSG_UPDATE_TITLE")); // NOI18N
                    logger.outputInRed(
                            NbBundle.getMessage(UpdateAction.class,
                            "MSG_UPDATE_TITLE_SEP")); // NOI18N
                    logger.output(
                                NbBundle.getMessage(UpdateAction.class,
                                "MSG_UPDATE_INFO_SEP", revStr == null ? NbBundle.getMessage(UpdateAction.class, "MSG_UPDATE_REVISION_PARENT") //NOI18N
                            : revStr, root.getAbsolutePath())); // NOI18N
                    if (doForcedUpdate) {
                        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(UpdateAction.class, "MSG_UPDATE_CONFIRM_QUERY")); // NOI18N
                        descriptor.setTitle(NbBundle.getMessage(UpdateAction.class, "MSG_UPDATE_CONFIRM")); // NOI18N
                        descriptor.setMessageType(JOptionPane.WARNING_MESSAGE);
                        descriptor.setOptionType(NotifyDescriptor.YES_NO_OPTION);

                        Object res = DialogDisplayer.getDefault().notify(descriptor);
                        if (res == NotifyDescriptor.NO_OPTION) {
                            logger.outputInRed(
                                    NbBundle.getMessage(UpdateAction.class,
                                    "MSG_UPDATE_CANCELED", root.getAbsolutePath())); // NOI18N
                            logger.output(""); // NOI18N
                            return;
                        }
                        logger.output(
                                NbBundle.getMessage(UpdateAction.class,
                                "MSG_UPDATE_FORCED", root.getAbsolutePath())); // NOI18N  
                    }
                    
                    List<String> list = HgUtils.runWithoutIndexing(new Callable<List<String>>() {

                        @Override
                        public List<String> call () throws HgException {
                            return HgCommand.doUpdateAll(root, doForcedUpdate, revStr);
                        }

                    }, root);
                    if (list != null && !list.isEmpty()){
                        bNoUpdates = HgCommand.isNoUpdates(list.get(0));
                        // Force Status Refresh from this dir and below
                        if(!bNoUpdates) {
                            HgUtils.notifyUpdatedFiles(root, list);
                            HgUtils.forceStatusRefresh(root);
                        }
                        //logger.clearOutput();
                        logger.output(list);
                        logger.output(""); // NOI18N
                    }

                } catch (HgException.HgCommandCanceledException ex) {
                    // canceled by user, do nothing
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                }

                logger.outputInRed(
                        NbBundle.getMessage(UpdateAction.class,
                        "MSG_UPDATE_DONE")); // NOI18N
                logger.output(""); // NOI18N
            }
        };
        support.start(rp, root, org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_Update_Progress")); // NOI18N
    }
}

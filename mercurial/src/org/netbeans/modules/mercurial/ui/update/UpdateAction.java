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

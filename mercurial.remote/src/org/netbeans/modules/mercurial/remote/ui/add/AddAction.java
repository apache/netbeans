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
package org.netbeans.modules.mercurial.remote.ui.add;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.mercurial.remote.FileInformation;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.RequestProcessor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

@Messages({
    "CTL_MenuItem_Add=&Add",
    "CTL_PopupMenuItem_Add=Add"
})
public class AddAction extends ContextAction {
    
    private static final String ICON_RESOURCE = "org/netbeans/modules/mercurial/remote/resources/icons/add.png"; //NOI18N
    
    public AddAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected boolean enable (Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        return Mercurial.getInstance().getFileStatusCache().containsFileOfStatus(ctx, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, true);
    }

    @Override
    protected String getBaseName (Node[] nodes) {
        return "CTL_MenuItem_Add"; // NOI18N
    }

    @Override
    protected void performContextAction (final Node[] nodes) {
        final VCSContext ctx = HgUtils.getCurrentContext(nodes);
        new HgProgressSupport() {
            @Override
            public void perform () {
                VCSFileProxy[] allFilesToAdd = HgUtils.getModifiedFiles(ctx, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, true);
                Map<VCSFileProxy, List<VCSFileProxy>> candidates = filterUnderRepository(allFilesToAdd);
                addFiles(candidates);
            }

            private Map<VCSFileProxy, List<VCSFileProxy>> filterUnderRepository (VCSFileProxy[] allFilesToAdd) {
                Map<VCSFileProxy, List<VCSFileProxy>> filtered = new HashMap<>(5);
                Mercurial hg = Mercurial.getInstance();
                for (VCSFileProxy file : allFilesToAdd) {
                    VCSFileProxy repository = hg.getRepositoryRoot(file);
                    List<VCSFileProxy> repoFiles = filtered.get(repository);
                    if (repoFiles == null) {
                        repoFiles = new LinkedList<>();
                        filtered.put(repository, repoFiles);
                    }
                    repoFiles.add(file);
                }
                return filtered;
            }
        }.start(Mercurial.getInstance().getParallelRequestProcessor(), HgUtils.getRootFile(ctx), NbBundle.getMessage(AddAction.class, "MSG_Add_Progress_Init")); //NOI18N
    }

    private void addFiles (final Map<VCSFileProxy, List<VCSFileProxy>> candidates) {
        Mercurial.getInstance().getParallelRequestProcessor().post(new Runnable () {
            @Override
            public void run () {
                for (Map.Entry<VCSFileProxy, List<VCSFileProxy>> e : candidates.entrySet()) {
                    final VCSFileProxy root = e.getKey();
                    final List<VCSFileProxy> files = e.getValue();
                    RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
                    HgProgressSupport support = new HgProgressSupport() {
                        @Override
                        public void perform() {
                            OutputLogger logger = getLogger();
                            try {
                                logger.outputInRed(NbBundle.getMessage(AddAction.class, "MSG_ADD_TITLE")); //NOI18N
                                logger.outputInRed(NbBundle.getMessage(AddAction.class, "MSG_ADD_TITLE_SEP")); //NOI18N
                                HgCommand.doAdd(root, files, logger);
                                logger.output(NbBundle.getMessage(AddAction.class, "MSG_ADD_FILES")); //NOI18N
                                for (VCSFileProxy file : files) {
                                    logger.output(file.getPath());
                                }
                                Mercurial.getInstance().getFileStatusCache().refreshAllRoots(Collections.<VCSFileProxy, Set<VCSFileProxy>>singletonMap(root, new HashSet<>(files)));
                            } catch (HgException.HgCommandCanceledException ex) {
                                // canceled by user, do nothing
                            } catch (HgException ex) {
                                HgUtils.notifyException(ex);
                            }
                            logger.outputInRed(NbBundle.getMessage(AddAction.class, "MSG_ADD_DONE")); // NOI18N
                            logger.output(""); // NOI18N
                        }
                    };
                    support.start(rp, root, NbBundle.getMessage(AddAction.class, "MSG_Add_Progress", root.getName())).waitFinished(); //NOI18N
                }
            }
        });
    }
}

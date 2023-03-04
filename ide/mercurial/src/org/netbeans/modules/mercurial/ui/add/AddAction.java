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
package org.netbeans.modules.mercurial.ui.add;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.RequestProcessor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "CTL_MenuItem_Add=&Add",
    "CTL_PopupMenuItem_Add=Add"
})
public class AddAction extends ContextAction {
    
    private static final String ICON_RESOURCE = "org/netbeans/modules/mercurial/resources/icons/add.png"; //NOI18N
    
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
        new HgProgressSupport() {
            @Override
            public void perform () {
                VCSContext ctx = HgUtils.getCurrentContext(nodes);
                File[] allFilesToAdd = HgUtils.getModifiedFiles(ctx, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, true);
                Map<File, List<File>> candidates = filterUnderRepository(allFilesToAdd);
                addFiles(candidates);
            }

            private Map<File, List<File>> filterUnderRepository (File[] allFilesToAdd) {
                Map<File, List<File>> filtered = new HashMap<File, List<File>>(5);
                Mercurial hg = Mercurial.getInstance();
                for (File file : allFilesToAdd) {
                    File repository = hg.getRepositoryRoot(file);
                    List<File> repoFiles = filtered.get(repository);
                    if (repoFiles == null) {
                        repoFiles = new LinkedList<File>();
                        filtered.put(repository, repoFiles);
                    }
                    repoFiles.add(file);
                }
                return filtered;
            }
        }.start(Mercurial.getInstance().getParallelRequestProcessor(), NbBundle.getMessage(AddAction.class, "MSG_Add_Progress_Init")); //NOI18N
    }

    private void addFiles (final Map<File, List<File>> candidates) {
        Mercurial.getInstance().getParallelRequestProcessor().post(new Runnable () {
            @Override
            public void run () {
                for (Map.Entry<File, List<File>> e : candidates.entrySet()) {
                    final File root = e.getKey();
                    final List<File> files = e.getValue();
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
                                for (File file : files) {
                                    logger.output(file.getAbsolutePath());
                                }
                                Mercurial.getInstance().getFileStatusCache().refreshAllRoots(Collections.<File, Set<File>>singletonMap(root, new HashSet<File>(files)));
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

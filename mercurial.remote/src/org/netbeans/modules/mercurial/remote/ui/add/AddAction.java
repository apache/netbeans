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

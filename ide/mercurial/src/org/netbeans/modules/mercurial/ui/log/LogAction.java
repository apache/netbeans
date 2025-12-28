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
package org.netbeans.modules.mercurial.ui.log;

import java.awt.EventQueue;
import org.netbeans.modules.versioning.spi.VCSContext;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.mercurial.FileStatus;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.WorkingCopyInfo;
import org.netbeans.modules.mercurial.ui.branch.HgBranch;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Log action for mercurial:
 * hg log - show revision history of entire repository or files
 *
 * @author John Rice
 */
public class LogAction extends SearchHistoryAction {

    private static final String ICON_RESOURCE = "org/netbeans/modules/mercurial/resources/icons/search_history.png"; //NOI18N

    public LogAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Log";                                      //NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        openHistory(context, NbBundle.getMessage(LogAction.class, "MSG_Log_TabTitle", org.netbeans.modules.versioning.util.Utils.getContextDisplayName(context)));
    }

    private void openHistory (final VCSContext context, final String title) {
        File repositoryRoot = getRepositoryRoot(context);
        final File[] files = replaceCopiedFiles(getFiles(context, repositoryRoot));
        openHistory(repositoryRoot, files, title, null);
    }
    
    public static void openHistory (File repositoryRoot, File[] files) {
        openHistory(repositoryRoot, files, null);
    }
    
    public static void openHistory (File repositoryRoot, File[] files, String revision) {
        List<Node> nodes = new ArrayList<Node>(files.length);
        for (File file : files) {
            FileObject fo = FileUtil.toFileObject(file);
            if(fo == null) continue;
            DataObject dao;
            try {
                dao = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
                continue;
            }
            nodes.add(dao.getNodeDelegate());
        }
        if(nodes.isEmpty()) return;
        
        String title = NbBundle.getMessage(
                LogAction.class, 
                "MSG_Log_TabTitle", // NOI18N
                Utils.getContextDisplayName(VCSContext.forNodes(nodes.toArray(new Node[0]))));
        openHistory(repositoryRoot, files, title, revision);
    }
    
    private static void openHistory (final File repositoryRoot, final File[] files, final String title, final String revision) {
        Utils.postParallel(new Runnable() {
            @Override
            public void run () {
                if (files == null) {
                    return;
                }
                outputSearchContextTab(repositoryRoot, files, "MSG_Log_Title");
                final boolean startSearch = files != null && (files.length == 1 && !files[0].isDirectory() || files.length > 1 && Utils.shareCommonDataObject(files));
                final String branchName;
                if (revision != null && !revision.isEmpty()) {
                    branchName = ""; //NOI18N
                } else {
                    HgLogMessage[] parents = WorkingCopyInfo.getInstance(repositoryRoot).getWorkingCopyParents();
                    if (parents.length == 1) {
                        if (parents[0].getBranches().length > 0) {
                            branchName = parents[0].getBranches()[0];
                        } else {
                            branchName = HgBranch.DEFAULT_NAME;
                        }
                    } else {
                        branchName = ""; //NOI18N
                    }
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        SearchHistoryTopComponent tc = new SearchHistoryTopComponent(files, branchName, revision);
                        tc.setDisplayName(title);
                        tc.open();
                        tc.requestActive();
                        if (startSearch) {
                            tc.search(false);
                        }
                    }
                });
            }

            
        }, 0);
    }    

    private static File[] replaceCopiedFiles (File[] files) {
                if (files == null) {
                    return null;
                }
                Set<File> originalFiles = new LinkedHashSet<File>(files.length);
                FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
                for (File file : files) {
                    FileStatus st = cache.getStatus(file).getStatus(null);
                    if (st != null && st.isCopied() && st.getOriginalFile() != null) {
                        file = st.getOriginalFile();
                    }
                    originalFiles.add(file);
                }
                return originalFiles.toArray(new File[0]);
            }

    /**
     * Opens search panel with a diff view fixed on a line
     * @param file file to search history for
     * @param lineNumber number of a line to fix on
     */
    public static void openSearch(final File file, final int lineNumber) {
        SearchHistoryTopComponent tc = new SearchHistoryTopComponent(file, new SearchHistoryTopComponent.DiffResultsViewFactory() {
            @Override
            DiffResultsView createDiffResultsView(SearchHistoryPanel panel, List<RepositoryRevision> results) {
                return new DiffResultsViewForLine(panel, results, lineNumber);
            }
        });
        String tcTitle = NbBundle.getMessage(SearchHistoryAction.class, "CTL_SearchHistory_Title", file.getName()); // NOI18N
        tc.setDisplayName(tcTitle);
        tc.open();
        tc.requestActive();
        tc.search(true);
        tc.activateDiffView(true);
    }
}

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
package org.netbeans.modules.mercurial.remote.ui.diff;

import java.util.Set;
import org.netbeans.modules.versioning.core.spi.VCSContext;

import java.nio.charset.Charset;
import java.util.List;

import org.netbeans.modules.mercurial.remote.FileInformation;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.remote.ui.log.RepositoryRevision;
import org.netbeans.modules.mercurial.remote.ui.repository.ChangesetPickerPanel;
import org.netbeans.modules.remotefs.versioning.api.ExportDiffSupport;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;

/**
 * ExportDiff action for mercurial:
 * hg export
 *
 * 
 */
@Messages({
    "CTL_MenuItem_ExportDiff=Export &Diff...",
    "CTL_PopupMenuItem_ExportDiff=Export Diff..."
})
public class ExportDiffAction extends ContextAction {

    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        Set<VCSFileProxy> roots = context.getFiles();
        if(roots == null) {
            return false;
        }
        if(!HgUtils.isFromHgRepository(context)) {
            return false;
        }
        for (VCSFileProxy root : roots) {
            FileInformation info = Mercurial.getInstance().getFileStatusCache().getCachedStatus(root);
            if(info != null &&
               (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY ||
                info.getStatus() == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_ExportDiff"; // NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        exportDiff(context);
    }

    private static void exportDiff(VCSContext ctx) {
        final VCSFileProxy files[] = HgUtils.getActionRoots(ctx);
        if (files == null || files.length == 0) {
            return;
        }
        final VCSFileProxy repository = Mercurial.getInstance().getRepositoryRoot(files[0]);

        ExportDiffSupport exportDiffSupport = new ExportDiff(repository, files) {
            @Override
            public void writeDiffFile (final VCSFileProxy toFile) {
                final String revStr = getSelectionRevision();
                saveFolderToPrefs(toFile);
                RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
                HgProgressSupport support = new HgProgressSupport() {
                    @Override
                    public void perform() {
                        OutputLogger logger = getLogger();
                        performExport(repository, revStr, toFile.getPath(), logger);
                    }
                };
                support.start(rp, repository, org.openide.util.NbBundle.getMessage(ExportDiffAction.class, "LBL_ExportDiff_Progress")).waitFinished(); // NOI18N
            }
        };
        exportDiffSupport.export();
    }

    public static void exportDiffFileRevision(final RepositoryRevision.Event drev) {
        if(drev == null) {
            return;
        }
        final VCSFileProxy fileToDiff = drev.getFile();
        RepositoryRevision repoRev = drev.getLogInfoHeader();
        final VCSFileProxy root = repoRev.getRepositoryRoot();
        if ((root == null) || root.getPath().equals("")) {                //NOI18N
            return;
        }
        final String revStr = repoRev.getLog().getRevisionNumber();
        ExportDiff exportDiffSupport = new ExportDiff(root, repoRev.getLog(), null, fileToDiff) {
            @Override
            public void writeDiffFile (final VCSFileProxy toFile) {
                saveFolderToPrefs(toFile);
                RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
                HgProgressSupport support = new HgProgressSupport() {
                    @Override
                    public void perform() {
                        OutputLogger logger = getLogger();
                        performExportFile(root, revStr, fileToDiff, toFile.getPath(), logger);
                    }
                };
                support.start(rp, root, org.openide.util.NbBundle.getMessage(ExportDiffAction.class, "LBL_ExportDiff_Progress")).waitFinished(); // NOI18N
            }
        };
        exportDiffSupport.export();
    }

    public static void exportDiffRevision(final RepositoryRevision repoRev, final VCSFileProxy[] roots) {
        if (repoRev == null) {
            return;
        }
        final VCSFileProxy root = repoRev.getRepositoryRoot();
        if ((root == null) || root.getPath().equals("")) {                //NOI18N
            return;
        }
        ExportDiff exportDiffSupport = new ExportDiff(root, repoRev.getLog(), roots) {
            @Override
            public void writeDiffFile (final VCSFileProxy toFile) {
                final String revStr = repoRev.getLog().getRevisionNumber();
                saveFolderToPrefs(toFile);
                RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
                HgProgressSupport support = new HgProgressSupport() {
                    @Override
                    public void perform() {
                        OutputLogger logger = getLogger();
                        performExport(root, revStr, toFile.getPath(), logger);
                    }
                };
                support.start(rp, root, org.openide.util.NbBundle.getMessage(ExportDiffAction.class, "LBL_ExportDiff_Progress")).waitFinished(); // NOI18N
            }
        };
        exportDiffSupport.export();
    }

    private static void performExport(VCSFileProxy repository, String revStr, String outputFileName, OutputLogger logger) {
        try {
            logger.outputInRed(
                    NbBundle.getMessage(ExportDiffAction.class,
                    "MSG_EXPORT_TITLE")); // NOI18N
            logger.outputInRed(
                    NbBundle.getMessage(ExportDiffAction.class,
                    "MSG_EXPORT_TITLE_SEP")); // NOI18N

            if (revStr != null && NbBundle.getMessage(ChangesetPickerPanel.class,
                    "MSG_Revision_Default").startsWith(revStr)) { //NOI18N
                logger.output(
                        NbBundle.getMessage(ExportDiffAction.class,
                        "MSG_EXPORT_NOTHING")); // NOI18N
            } else {
                List<String> list = HgCommand.doExport(repository, revStr, outputFileName, logger);
                logger.output(list); // NOI18N
                if (!list.isEmpty() && list.size() > 1) {
                    VCSFileProxy outFile = VCSFileProxySupport.getResource(repository, list.get(1));
                    if (outFile != null && VCSFileProxySupport.canRead(outFile)) {
                        openFile(outFile);
                    }
                }
            }
        } catch (HgException ex) {
            HgUtils.notifyException(ex);
        } finally {
            logger.outputInRed(NbBundle.getMessage(ExportDiffAction.class, "MSG_EXPORT_DONE")); // NOI18N
            logger.output(""); // NOI18N
        }
    }
    private static void performExportFile(VCSFileProxy repository, String revStr, VCSFileProxy fileToDiff, String outputFileName, OutputLogger logger) {
    try {
        logger.outputInRed(
                NbBundle.getMessage(ExportDiffAction.class,
                "MSG_EXPORT_FILE_TITLE")); // NOI18N
        logger.outputInRed(
                NbBundle.getMessage(ExportDiffAction.class,
                "MSG_EXPORT_FILE_TITLE_SEP")); // NOI18N

        if (NbBundle.getMessage(ChangesetPickerPanel.class,
                "MSG_Revision_Default").startsWith(revStr)) { //NOI18N
            logger.output(
                    NbBundle.getMessage(ExportDiffAction.class,
                    "MSG_EXPORT_NOTHING")); // NOI18N
        } else {
            List<String> list = HgCommand.doExportFileDiff(repository, fileToDiff, revStr, outputFileName, logger);
            String repoPath = repository.getPath();
            String fileToDiffPath = fileToDiff.getPath();
            fileToDiffPath = fileToDiffPath.substring(repoPath.length()+1);

            logger.output(NbBundle.getMessage(ExportDiffAction.class, "MSG_EXPORT_FILE", fileToDiffPath, revStr, outputFileName)); // NOI18N
            if (!list.isEmpty() && list.size() > 1) {
                VCSFileProxy outFile = VCSFileProxySupport.getResource(repository, outputFileName);
                if (outFile != null && VCSFileProxySupport.canRead(outFile)) {
                        openFile(outFile);
                }
            }
        }
        } catch (HgException ex) {
            HgUtils.notifyException(ex);
        } finally {
            logger.outputInRed(NbBundle.getMessage(ExportDiffAction.class, "MSG_EXPORT_FILE_DONE")); // NOI18N
            logger.output(""); // NOI18N
        }
    }

    static void saveFolderToPrefs (final VCSFileProxy file) {
        if (file.getParentFile()!= null) {
            HgModuleConfig.getDefault(file).getPreferences().put("ExportDiff.saveFolder", file.getParentFile().getPath()); // NOI18N
        }
    }
    
    private static void openFile (VCSFileProxy outFile) {
        outFile = outFile.normalizeFile();
        if (HgCommand.ENCODING != null) {
            VCSFileProxySupport.associateEncoding(outFile, Charset.forName(HgCommand.ENCODING));
        }
        VCSFileProxySupport.openFile(outFile);
    }
}

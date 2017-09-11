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
package org.netbeans.modules.mercurial.ui.diff;

import java.util.Set;
import org.netbeans.modules.versioning.spi.VCSContext;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.log.RepositoryRevision;
import org.netbeans.modules.mercurial.ui.repository.ChangesetPickerPanel;
import org.netbeans.modules.versioning.util.ExportDiffSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 * ExportDiff action for mercurial:
 * hg export
 *
 * @author Padraig O'Briain
 */
@NbBundle.Messages({
    "CTL_MenuItem_ExportDiff=Export &Diff...",
    "CTL_PopupMenuItem_ExportDiff=Export Diff..."
})
public class ExportDiffAction extends ContextAction {

    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        Set<File> roots = context.getFiles();
        if(roots == null) return false;
        if(!HgUtils.isFromHgRepository(context)) return false;
        for (File root : roots) {
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
        final File files[] = HgUtils.getActionRoots(ctx);
        if (files == null || files.length == 0) return;
        final File repository = Mercurial.getInstance().getRepositoryRoot(files[0]);

        ExportDiffSupport exportDiffSupport = new ExportDiff(repository, files) {
            @Override
            public void writeDiffFile (final File toFile) {
                final String revStr = getSelectionRevision();
                saveFolderToPrefs(toFile);
                RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
                HgProgressSupport support = new HgProgressSupport() {
                    @Override
                    public void perform() {
                        OutputLogger logger = getLogger();
                        performExport(repository, revStr, toFile.getAbsolutePath(), logger);
                    }
                };
                support.start(rp, repository, org.openide.util.NbBundle.getMessage(ExportDiffAction.class, "LBL_ExportDiff_Progress")).waitFinished(); // NOI18N
            }
        };
        exportDiffSupport.export();
    }

    public static void exportDiffFileRevision(final RepositoryRevision.Event drev) {
        if(drev == null) return;
        final File fileToDiff = drev.getFile();
        RepositoryRevision repoRev = drev.getLogInfoHeader();
        final File root = repoRev.getRepositoryRoot();
        if ((root == null) || root.getPath().equals(""))                //NOI18N
            return;
        final String revStr = repoRev.getLog().getRevisionNumber();
        ExportDiff exportDiffSupport = new ExportDiff(root, repoRev.getLog(), null, fileToDiff) {
            @Override
            public void writeDiffFile (final File toFile) {
                saveFolderToPrefs(toFile);
                RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
                HgProgressSupport support = new HgProgressSupport() {
                    @Override
                    public void perform() {
                        OutputLogger logger = getLogger();
                        performExportFile(root, revStr, fileToDiff, toFile.getAbsolutePath(), logger);
                    }
                };
                support.start(rp, root, org.openide.util.NbBundle.getMessage(ExportDiffAction.class, "LBL_ExportDiff_Progress")).waitFinished(); // NOI18N
            }
        };
        exportDiffSupport.export();
    }

    public static void exportDiffRevision(final RepositoryRevision repoRev, final File[] roots) {
        if (repoRev == null)
            return;
        final File root = repoRev.getRepositoryRoot();
        if ((root == null) || root.getPath().equals(""))                //NOI18N
            return;
        ExportDiff exportDiffSupport = new ExportDiff(root, repoRev.getLog(), roots) {
            @Override
            public void writeDiffFile (final File toFile) {
                final String revStr = repoRev.getLog().getRevisionNumber();
                saveFolderToPrefs(toFile);
                RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
                HgProgressSupport support = new HgProgressSupport() {
                    @Override
                    public void perform() {
                        OutputLogger logger = getLogger();
                        performExport(root, revStr, toFile.getAbsolutePath(), logger);
                    }
                };
                support.start(rp, root, org.openide.util.NbBundle.getMessage(ExportDiffAction.class, "LBL_ExportDiff_Progress")).waitFinished(); // NOI18N
            }
        };
        exportDiffSupport.export();
    }

    private static void performExport(File repository, String revStr, String outputFileName, OutputLogger logger) {
        try {
            logger.outputInRed(
                    NbBundle.getMessage(ExportDiffAction.class,
                    "MSG_EXPORT_TITLE")); // NOI18N
            logger.outputInRed(
                    NbBundle.getMessage(ExportDiffAction.class,
                    "MSG_EXPORT_TITLE_SEP")); // NOI18N

            if (revStr != null && NbBundle.getMessage(ChangesetPickerPanel.class,
                    "MSG_Revision_Default").startsWith(revStr)) {
                logger.output(
                        NbBundle.getMessage(ExportDiffAction.class,
                        "MSG_EXPORT_NOTHING")); // NOI18N
            } else {
                List<String> list = HgCommand.doExport(repository, revStr, outputFileName, logger);
                logger.output(list); // NOI18N
                if (!list.isEmpty() && list.size() > 1) {
                    File outFile = new File(list.get(1));
                    if (outFile != null && outFile.canRead()) {
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
    private static void performExportFile(File repository, String revStr, File fileToDiff, String outputFileName, OutputLogger logger) {
    try {
        logger.outputInRed(
                NbBundle.getMessage(ExportDiffAction.class,
                "MSG_EXPORT_FILE_TITLE")); // NOI18N
        logger.outputInRed(
                NbBundle.getMessage(ExportDiffAction.class,
                "MSG_EXPORT_FILE_TITLE_SEP")); // NOI18N

        if (NbBundle.getMessage(ChangesetPickerPanel.class,
                "MSG_Revision_Default").startsWith(revStr)) {
            logger.output(
                    NbBundle.getMessage(ExportDiffAction.class,
                    "MSG_EXPORT_NOTHING")); // NOI18N
        } else {
            List<String> list = HgCommand.doExportFileDiff(repository, fileToDiff, revStr, outputFileName, logger);
            String repoPath = repository.getAbsolutePath();
            String fileToDiffPath = fileToDiff.getAbsolutePath();
            fileToDiffPath = fileToDiffPath.substring(repoPath.length()+1);

            logger.output(NbBundle.getMessage(ExportDiffAction.class, "MSG_EXPORT_FILE", fileToDiffPath, revStr, outputFileName)); // NOI18N
            if (!list.isEmpty() && list.size() > 1) {
                File outFile = new File(outputFileName);
                if (outFile != null && outFile.canRead()) {
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

    static void saveFolderToPrefs (final File file) {
        if (file.getParent() != null) {
            HgModuleConfig.getDefault().getPreferences().put("ExportDiff.saveFolder", file.getParent()); // NOI18N
        }
    }
    
    private static void openFile (File outFile) {
        outFile = FileUtil.normalizeFile(outFile);
        if (HgCommand.ENCODING != null) {
            Utils.associateEncoding(outFile, Charset.forName(HgCommand.ENCODING));
        }
        Utils.openFile(outFile);
    }
}

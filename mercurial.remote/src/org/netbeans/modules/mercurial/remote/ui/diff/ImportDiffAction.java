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
package org.netbeans.modules.mercurial.remote.ui.diff;

import org.netbeans.modules.versioning.core.spi.VCSContext;

import java.awt.event.ActionEvent;
import java.util.List;

import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import java.awt.event.ActionListener;
import java.awt.Dialog;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.remote.ui.merge.MergeAction;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;

/**
 * ImportDiff action for mercurial: 
 * hg export
 * 
 * 
 */
@Messages({
    "CTL_MenuItem_ImportDiff=&Import Changesets...",
    "CTL_PopupMenuItem_ImportDiff=Import Changesets..."
})
public class ImportDiffAction extends ContextAction {
    
    @Override
    protected boolean enable(Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_ImportDiff"; // NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        importDiff(context);
    }

    private static void importDiff(VCSContext ctx) {
        final VCSFileProxy roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) {
            return;
        }
        final VCSFileProxy root = Mercurial.getInstance().getRepositoryRoot(roots[0]);
        VCSFileProxy oldFolder = VCSFileProxySupport.getResource(root, HgModuleConfig.getDefault(root).getImportFolder());
        final JFileChooser fileChooser = VCSFileProxySupport.createFileChooser(oldFolder);
        fileChooser.setDialogTitle(NbBundle.getMessage(ImportDiffAction.class, "ImportBrowse_title"));                                            // NO I18N
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setApproveButtonMnemonic(NbBundle.getMessage(ImportDiffAction.class, "Import").charAt(0));                      // NO I18N
        fileChooser.setApproveButtonText(NbBundle.getMessage(ImportDiffAction.class, "Import"));                                        // NO I18N
        JPanel panel = new JPanel();
        final JRadioButton asPatch = new JRadioButton(NbBundle.getMessage(ImportDiffAction.class, "CTL_Import_PatchOption")); //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(asPatch, asPatch.getText()); // NOI18N
        final JRadioButton asBundle = new JRadioButton(NbBundle.getMessage(ImportDiffAction.class, "CTL_Import_BundleOption")); //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(asBundle, asBundle.getText()); // NOI18N
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(asBundle);
        buttonGroup.add(asPatch);
        asPatch.setSelected(true);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(asPatch);
        panel.add(asBundle);
        fileChooser.setAccessory(panel);

        DialogDescriptor dd = new DialogDescriptor(fileChooser, NbBundle.getMessage(ImportDiffAction.class, "ImportBrowse_title"));              // NO I18N
        dd.setOptions(new Object[0]);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        fileChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String state = e.getActionCommand();
                if (state.equals(JFileChooser.APPROVE_SELECTION)) {
                    final VCSFileProxy patchFile = VCSFileProxySupport.getSelectedFile(fileChooser);

                    HgModuleConfig.getDefault(root).setImportFolder(patchFile.getParentFile().getPath());
                    RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
                    ImportDiffProgressSupport.Kind kind;
                    if (asBundle.isSelected()) {
                        kind = ImportDiffProgressSupport.Kind.BUNDLE;
                    } else if (asPatch.isSelected()) {
                        kind = ImportDiffProgressSupport.Kind.PATCH;
                    } else {
                        kind = null;
                    }
                    HgProgressSupport support = new ImportDiffProgressSupport(root, patchFile, kind);
                    support.start(rp, root, org.openide.util.NbBundle.getMessage(ImportDiffAction.class, "LBL_ImportDiff_Progress")); // NOI18N
                }
                dialog.dispose();
            }
        });
        dialog.setVisible(true);
    }

    private static class ImportDiffProgressSupport extends HgProgressSupport {

        private final VCSFileProxy patchFile;
        private final VCSFileProxy repository;
        private final Kind kind;
        
        static enum Kind {
            PATCH,
            BUNDLE
        }

        public ImportDiffProgressSupport (VCSFileProxy repository, VCSFileProxy patchFile, Kind kind) {
            this.repository = repository;
            this.patchFile = patchFile;
            this.kind = kind;
        }
        
        @Override
        public void perform() {
            if (kind == Kind.BUNDLE) {
                performUnbundle();
            } else if (kind == Kind.PATCH) {
                performImport();
            }
        }

        private void performUnbundle () {
            final OutputLogger logger = getLogger();
            try {
                logger.outputInRed(NbBundle.getMessage(ImportDiffAction.class, "MSG_UNBUNDLE_TITLE")); // NOI18N
                logger.outputInRed(NbBundle.getMessage(ImportDiffAction.class, "MSG_UNBUNDLE_TITLE_SEP")); // NOI18N
                HgUtils.runWithoutIndexing(new Callable<Void>() {
                    @Override
                    public Void call () throws Exception {
                        List<String> list = HgCommand.doUnbundle(repository, patchFile, true, logger);
                        if (list != null && !list.isEmpty()) {
                            List<String> updatedFilesList = list;
                            logger.output(HgUtils.replaceHttpPassword(list));
                            // Handle Merge - both automatic and merge with conflicts
                            boolean bMergeNeededDueToPull = HgCommand.isMergeNeededMsg(list.get(list.size() - 1));
                            boolean bConfirmMerge = false;
                            boolean warnMoreHeads = true;
                            if (bMergeNeededDueToPull) {
                                bConfirmMerge = HgUtils.confirmDialog(
                                        ImportDiffAction.class, "MSG_UNBUNDLE_MERGE_CONFIRM_TITLE", "MSG_UNBUNDLE_MERGE_CONFIRM_QUERY"); // NOI18N
                                warnMoreHeads = false;
                            } else {
                                boolean bOutStandingUncommittedMerges = HgCommand.isMergeAbortUncommittedMsg(list.get(list.size() - 1));
                                if (bOutStandingUncommittedMerges) {
                                    bConfirmMerge = HgUtils.confirmDialog(
                                            ImportDiffAction.class, "MSG_UNBUNDLE_MERGE_CONFIRM_TITLE", "MSG_UNBUNDLE_MERGE_UNCOMMITTED_CONFIRM_QUERY"); // NOI18N
                                }
                            }
                            if (bConfirmMerge) {
                                logger.output(""); // NOI18N
                                logger.outputInRed(NbBundle.getMessage(ImportDiffAction.class, "MSG_UNBUNDLE_MERGE_DO")); // NOI18N
                                List<String> mergeResult = MergeAction.doMergeAction(repository, null, logger);
                                if (mergeResult != null) {
                                    updatedFilesList.addAll(mergeResult);
                                }
                            } else {
                                HgLogMessage[] heads = HgCommand.getHeadRevisionsInfo(repository, false, OutputLogger.getLogger(null));
                                Map<String, Collection<HgLogMessage>> branchHeads = HgUtils.sortByBranch(heads);
                                if (!branchHeads.isEmpty()) {
                                    MergeAction.displayMergeWarning(branchHeads, logger, warnMoreHeads);
                                }
                            }
                            boolean fileUpdated = isUpdated(updatedFilesList);
                            if (fileUpdated) {
                                HgUtils.notifyUpdatedFiles(repository, updatedFilesList);
                                HgUtils.forceStatusRefresh(repository);
                            }
                        }
                        return null;
                    }
                }, repository);
            } catch (HgException.HgCommandCanceledException ex) {
                // canceled by user, do nothing
            } catch (HgException ex) {
                HgUtils.notifyException(ex);
            } finally {
                logger.outputInRed(NbBundle.getMessage(ImportDiffAction.class, "MSG_UNBUNDLE_DONE")); // NOI18N
                logger.output(""); // NOI18N
            }
        }

        private void performImport () {
            final OutputLogger logger = getLogger();
            try {
                logger.outputInRed(
                        NbBundle.getMessage(ImportDiffAction.class,
                        "MSG_IMPORT_TITLE")); // NOI18N
                logger.outputInRed(
                        NbBundle.getMessage(ImportDiffAction.class,
                        "MSG_IMPORT_TITLE_SEP")); // NOI18N

                HgUtils.runWithoutIndexing(new Callable<Void>() {
                    @Override
                    public Void call () throws Exception {
                        List<String> list = HgCommand.doImport(repository, patchFile, logger);
                        Mercurial.getInstance().historyChanged(repository);
                        Mercurial.getInstance().changesetChanged(repository);
                        logger.output(list);
                        return null;
                    }
                }, repository);
            } catch (HgException.HgCommandCanceledException ex) {
                // canceled by user, do nothing
            } catch (HgException ex) {
                HgUtils.notifyException(ex);
            } finally {
                logger.outputInRed(NbBundle.getMessage(ImportDiffAction.class, "MSG_IMPORT_DONE")); // NOI18N
                logger.output(""); // NOI18N
            }
        }

        private static boolean isUpdated (List<String> list) {
            boolean updated = false;
            for (String s : list) {
                if (s.contains("getting ") || s.startsWith("merging ")) { //NOI18N
                    updated = true;
                    break;
                }
            }
            return updated;
        }
    }
}

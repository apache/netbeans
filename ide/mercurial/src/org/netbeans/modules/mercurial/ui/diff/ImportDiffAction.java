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
package org.netbeans.modules.mercurial.ui.diff;

import org.netbeans.modules.versioning.spi.VCSContext;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
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
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.merge.MergeAction;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.openide.nodes.Node;

/**
 * ImportDiff action for mercurial: 
 * hg export
 * 
 * @author Padraig O'Briain
 */
@NbBundle.Messages({
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
        final File roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) return;
        final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);

        final JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(ImportDiffAction.class, "ACSD_ImportBrowseFolder"), null);   // NO I18N
        fileChooser.setDialogTitle(NbBundle.getMessage(ImportDiffAction.class, "ImportBrowse_title"));                                            // NO I18N
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setApproveButtonMnemonic(NbBundle.getMessage(ImportDiffAction.class, "Import").charAt(0));                      // NO I18N
        fileChooser.setApproveButtonText(NbBundle.getMessage(ImportDiffAction.class, "Import"));                                        // NO I18N
        fileChooser.setCurrentDirectory(new File(HgModuleConfig.getDefault().getImportFolder()));
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
                    final File patchFile = fileChooser.getSelectedFile();

                    HgModuleConfig.getDefault().setImportFolder(patchFile.getParent());
                    RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
                    ImportDiffProgressSupport.Kind kind;
                    if (asBundle.isSelected()) {
                        kind = ImportDiffProgressSupport.Kind.BUNDLE;
                    } else if (asPatch.isSelected()) {
                        kind = ImportDiffProgressSupport.Kind.PATCH;
                    } else {
                        kind = null;
                    }
                    HgProgressSupport support = new ImportDiffProgressSupport(root, patchFile, true, kind);
                    support.start(rp, root, org.openide.util.NbBundle.getMessage(ImportDiffAction.class, "LBL_ImportDiff_Progress")); // NOI18N
                }
                dialog.dispose();
            }
        });
        dialog.setVisible(true);
    }

    static class ImportDiffProgressSupport extends HgProgressSupport {

        private final File patchFile;
        private final File repository;
        private final Kind kind;
        private final boolean commit;
        
        static enum Kind {
            PATCH,
            BUNDLE
        }

        public ImportDiffProgressSupport (File repository, File patchFile, boolean commit, Kind kind) {
            this.repository = repository;
            this.patchFile = patchFile;
            this.kind = kind;
            this.commit = commit;
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
                        List<String> list = HgCommand.doImport(repository, patchFile, commit, logger);
                        if (commit) {
                            Mercurial.getInstance().historyChanged(repository);
                            Mercurial.getInstance().changesetChanged(repository);
                        } else {
                            HgUtils.notifyUpdatedFiles(repository, list);
                            HgUtils.forceStatusRefresh(repository);
                        }
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

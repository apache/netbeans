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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JButton;
import org.openide.DialogDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.remote.ui.repository.ChangesetPickerPanel;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.remotefs.versioning.api.ExportDiffSupport;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * 
 */
class ExportBundle extends ExportDiffSupport implements ActionListener, PropertyChangeListener {

    private final ExportBundlePanel panel;
    private final JButton okButton;
    private final JButton cancelButton;
    private final VCSFileProxy repository;
    private ExportAsFilePanel targetPanel;
    private DialogDescriptor dialogDescriptor;
    private final String HG_TIP = "tip"; //NOI18N
    private final String HG_NULL_BASE = "null"; //NOI18N
    private final ChangesetPickerSimplePanel changesetPickerPanel;
    private static final HashMap<String, String> resourceNames = new HashMap<>();
    static {
        resourceNames.put("CTL_Attaching", "CTL_Attaching"); //NOI18N
        resourceNames.put("CTL_Export_Title", "CTL_ExportBundleDialog_Title"); //NOI18N
    }
    private static final String SEP = " "; //NOI18N
    private final JButton selectButton;

    public ExportBundle(VCSFileProxy repository) {
        super(new VCSFileProxy[]{repository}, HgModuleConfig.getDefault(repository).getPreferences());
        this.repository = repository;
        panel = new ExportBundlePanel(repository);
        okButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(okButton, org.openide.util.NbBundle.getMessage(ExportBundle.class, "CTL_ExportBundleDialog_Action_Export")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExportBundle.class, "ACSD_ExportBundleDialog_Action_Export")); // NOI18N
        okButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ExportBundle.class, "ACSN_ExportBundleDialog_Action_Export")); // NOI18N
        cancelButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(ExportBundle.class, "CTL_ExportBundleDialog_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExportBundle.class, "ACSD_ExportBundleDialog_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ExportBundle.class, "ACSN_ExportBundleDialog_Action_Cancel")); // NOI18N
        panel.baseRevision.setModel(new DefaultComboBoxModel(new String[] {HG_TIP, HG_NULL_BASE}));
        panel.baseRevision.setSelectedIndex(0);
        panel.txtTopRevision.setText(HG_TIP);
        panel.addActionListener(this);
        selectButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(selectButton, org.openide.util.NbBundle.getMessage(ExportBundle.class, "CTL_ExportBundle.ChangesetPicker_SelectButton")); //NOI18N
        this.changesetPickerPanel = new ChangesetPickerSimplePanel(repository);
    }

    @Override
    protected AbstractExportDiffPanel createSimpleDialog(String currentFilePath) {
        dialogDescriptor =
                new DialogDescriptor(panel,
                org.openide.util.NbBundle.getMessage(ExportBundle.class, "CTL_ExportBundleDialog_Title"), // NOI18N
                true,
                new Object[]{okButton, cancelButton},
                okButton,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(this.getClass()),
                null);
        targetPanel = new ExportAsFilePanel();
        targetPanel.addOutputFileTextDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                nameChange();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                nameChange();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                nameChange();
            }
        });
        panel.setInsidePanel(targetPanel);
        return targetPanel;
    }

    @Override
    protected void createComplexDialog(AbstractExportDiffPanel insidePanel) {
        panel.setInsidePanel(insidePanel);
        dialogDescriptor = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(ExportBundle.class,
                "CTL_ExportBundleDialog_Title")); // NOI18N
    }

    @Override
    protected VCSFileProxy createTempFile () throws IOException {
        return VCSFileProxySupport.createTempFile(repository, "hg-bundle", ".hg", false); // NOI18N
    }

    @Override
    protected String getMessage (String resourceName) {
        String translatedResourceName = resourceNames.get(resourceName);
        if (translatedResourceName == null) {
            return super.getMessage(resourceName);
        } else {
            return NbBundle.getMessage(ExportBundle.class, translatedResourceName);
        }
    }

    @Override
    protected javax.swing.filechooser.FileFilter getFileFilter () {
        return new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith("hg") || f.getName().endsWith("bundle") || f.isDirectory();  // NOI18N
            }
            @Override
            public String getDescription() {
                return NbBundle.getMessage(ExportBundle.class, "CTL_ExportBundle_FileMask");
            }
        };
    }

    @Override
    protected VCSFileProxy getTargetFile (VCSFileProxy target) {
        String name = target.getName();
        boolean requiredExt = false;
        requiredExt |= name.endsWith(".hg");  // NOI18N
        requiredExt |= name.endsWith(".bundle"); // NOI18N
        if (requiredExt == false) {
            VCSFileProxy parent = target.getParentFile();
            target = VCSFileProxy.createFileProxy(parent, name + ".hg"); // NOI18N
        }
        return target;
    }

    private void nameChange() {
        if (!targetPanel.getOutputFileText().trim().isEmpty()) {
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(false);
        }
    }

    private String getBaseRevision() {
        String rev = panel.getSelectedBaseRevision();
        if (rev == null) {
            rev = HG_NULL_BASE;
        }
        return parseRevision(rev);
    }

    private String getTopRevision() {
        String rev = panel.getSelectedRevision();
        if (rev == null) {
            rev = HG_TIP;
        }
        return parseRevision(rev);
    }

    private static String parseRevision (String revision)  {
        int pos = revision.indexOf(SEP);
        if (pos > 0) {
            revision = revision.substring(0, pos);
        }
        return revision;
    }

    @Override
    public void writeDiffFile(final VCSFileProxy toFile) {
        HgProgressSupport supp = new HgProgressSupport() {
            @Override
            protected void perform() {
                final String revBase = getBaseRevision();
                final String revTo = getTopRevision();
                OutputLogger logger = getLogger();
                if (toFile.getParentFile() != null) {
                    HgModuleConfig.getDefault(repository).getPreferences().put("ExportDiff.saveFolder", toFile.getParentFile().getPath()); // NOI18N
                }
                try {
                    logger.outputInRed(NbBundle.getMessage(ExportBundleAction.class, "MSG_EXPORT_BUNDLE_TITLE")); // NOI18N
                    logger.outputInRed(NbBundle.getMessage(ExportBundleAction.class, "MSG_EXPORT_BUNDLE_TITLE_SEP")); // NOI18N

                    logger.output(NbBundle.getMessage(ExportBundleAction.class, "MSG_EXPORT_BUNDLE_PROGRESS", new String[]{revTo, revBase, toFile.getPath()})); // NOI18N
                    List<String> list = HgCommand.doBundle(repository, revBase, revTo, toFile, logger);
                    logger.output(list); // NOI18N
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                } finally {
                    logger.outputInRed(NbBundle.getMessage(ExportBundleAction.class, "MSG_EXPORT_BUNDLE_DONE")); // NOI18N
                    logger.output(""); // NOI18N
                }
            }
        };
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        supp.start(rp, repository, org.openide.util.NbBundle.getMessage(ExportBundleAction.class, "LBL_ExportBundle_Progress")).waitFinished(); // NOI18N
    }

    @Override
    protected DialogDescriptor getDialogDescriptor() {
        return dialogDescriptor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        DialogDescriptor dd = new DialogDescriptor(changesetPickerPanel,
                org.openide.util.NbBundle.getMessage(ExportBundle.class, "CTL_ExportBundle.ChangesetPicker_Title"), // NOI18N
                true,
                new Object[]{selectButton, DialogDescriptor.CANCEL_OPTION},
                selectButton,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx("org.netbeans.modules.mercurial.remote.ui.repository.ChangesetPickerPanel"), //NOI18N
                null);
        selectButton.setEnabled(changesetPickerPanel.getSelectedRevision() != null);
        changesetPickerPanel.addPropertyChangeListener(this);
        changesetPickerPanel.initRevisions();
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        changesetPickerPanel.removePropertyChangeListener(this);
        if (dd.getValue() == selectButton) {
            HgLogMessage revisionWithChangeset = changesetPickerPanel.getSelectedRevision();
            String revision = ChangesetPickerPanel.HG_TIP.equals(revisionWithChangeset.getRevisionNumber()) ? ChangesetPickerPanel.HG_TIP
                    : new StringBuilder(revisionWithChangeset.getRevisionNumber()).append(SEP).append("(").append(revisionWithChangeset.getCSetShortID()).append(")").toString(); //NOI18N
            if (ExportBundlePanel.CMD_SELECT_BASE_REVISION.equals(command)) {
                panel.baseRevision.setModel(new DefaultComboBoxModel(new String[] {HG_NULL_BASE, revision})); //NOI18N
                panel.baseRevision.setSelectedItem(revision);
            } else if (ExportBundlePanel.CMD_SELECT_REVISION.equals(command)) {
                panel.txtTopRevision.setText(revision);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (selectButton != null && ChangesetPickerSimplePanel.PROP_VALID.equals(evt.getPropertyName())) {
            boolean valid = (Boolean) evt.getNewValue();
            selectButton.setEnabled(valid);
        }
    }

    private static class ChangesetPickerSimplePanel extends ChangesetPickerPanel {

        private boolean initialized;

        public ChangesetPickerSimplePanel(VCSFileProxy repository) {
            super(repository, null);
            initComponents();
        }

        @Override
        protected String getRefreshLabel() {
            return NbBundle.getMessage(ExportBundle.class, "MSG_Fetching_Revisions"); //NOI18N
        }

        private void initRevisions() {
            if (!initialized) {
                initialized = true;
                loadRevisions();
            }
        }

        private void initComponents() {
            org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ExportBundle.class, "ExportBundle.ChangesetPicker.jLabel1.text")); // NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ExportBundle.class, "LBL_EXPORT_INFO")); // NOI18N
            getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExportBundle.class, "ExportBundle.ChangesetPicker.AccessibleContext.accessibleDescription")); // NOI18N
        }
    }
}
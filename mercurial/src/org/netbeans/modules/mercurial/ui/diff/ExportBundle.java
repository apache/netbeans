/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mercurial.ui.diff;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import org.netbeans.modules.versioning.util.ExportDiffSupport;
import org.openide.DialogDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.repository.ChangesetPickerPanel;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author ondra
 */
class ExportBundle extends ExportDiffSupport implements ActionListener, PropertyChangeListener {

    private ExportBundlePanel panel;
    private JButton okButton;
    private JButton cancelButton;
    private final File repository;
    private ExportAsFilePanel targetPanel;
    private DialogDescriptor dialogDescriptor;
    private final String HG_TIP = "tip"; //NOI18N
    private final String HG_NULL_BASE = "null"; //NOI18N
    private final ChangesetPickerSimplePanel changesetPickerPanel;
    private static final HashMap<String, String> resourceNames = new HashMap<String, String>();
    static {
        resourceNames.put("CTL_Attaching", "CTL_Attaching");
        resourceNames.put("CTL_Export_Title", "CTL_ExportBundleDialog_Title");
    }
    private static final String SEP = " "; //NOI18N
    private final JButton selectButton;

    public ExportBundle(File repository) {
        super(new File[]{repository}, HgModuleConfig.getDefault().getPreferences());
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
    protected File createTempFile () throws IOException {
        return File.createTempFile("hg-bundle", ".hg"); // NOI18N
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
    protected File getTargetFile (File target) {
        String name = target.getName();
        boolean requiredExt = false;
        requiredExt |= name.endsWith(".hg");  // NOI18N
        requiredExt |= name.endsWith(".bundle"); // NOI18N
        if (requiredExt == false) {
            File parent = target.getParentFile();
            target = new File(parent, name + ".hg"); // NOI18N
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
    public void writeDiffFile(final File toFile) {
        HgProgressSupport supp = new HgProgressSupport() {
            @Override
            protected void perform() {
                final String revBase = getBaseRevision();
                final String revTo = getTopRevision();
                OutputLogger logger = getLogger();
                if (toFile.getParent() != null) {
                    HgModuleConfig.getDefault().getPreferences().put("ExportDiff.saveFolder", toFile.getParent()); // NOI18N
                }
                try {
                    logger.outputInRed(NbBundle.getMessage(ExportBundleAction.class, "MSG_EXPORT_BUNDLE_TITLE")); // NOI18N
                    logger.outputInRed(NbBundle.getMessage(ExportBundleAction.class, "MSG_EXPORT_BUNDLE_TITLE_SEP")); // NOI18N

                    logger.output(NbBundle.getMessage(ExportBundleAction.class, "MSG_EXPORT_BUNDLE_PROGRESS", new String[]{revTo, revBase, toFile.getAbsolutePath()})); // NOI18N
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
                new HelpCtx("org.netbeans.modules.mercurial.ui.repository.ChangesetPickerPanel"), //NOI18N
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
                    : new StringBuilder(revisionWithChangeset.getRevisionNumber()).append(SEP).append("(").append(revisionWithChangeset.getCSetShortID()).append(")").toString();
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

        public ChangesetPickerSimplePanel(File repository) {
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
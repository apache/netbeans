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

package org.netbeans.modules.mercurial.options;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JButton;
import java.util.ArrayList;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.MercurialAnnotator;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import static org.netbeans.modules.mercurial.util.HgCommand.HG_COMMAND;
import org.netbeans.modules.versioning.util.VCSOptionsKeywordsProvider;

final class MercurialOptionsPanelController extends OptionsPanelController implements ActionListener, VCSOptionsKeywordsProvider {
    
    private MercurialPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;
    
    public MercurialOptionsPanelController() {

        panel = new MercurialPanel(this);
        panel.execPathBrowseButton.addActionListener(this);
        panel.exportFilenameBrowseButton.addActionListener(this);

        String tooltip = NbBundle.getMessage(MercurialPanel.class, "MercurialPanel.annotationTextField.toolTipText", MercurialAnnotator.LABELS); // NOI18N

        panel.annotationTextField.setToolTipText(tooltip);
        panel.addButton.addActionListener(this);
        panel.manageButton.addActionListener(this);
    }

    public void update() {
        getPanel().load();
        changed = false;
    }
    
    public void applyChanges() {
        getPanel().store();
        // {folder} variable setting
        Mercurial.getInstance().getMercurialAnnotator().refresh();
        Mercurial.getInstance().refreshAllAnnotations();

        changed = false;
    }
    
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }
    
    public boolean isValid() {
        return validateFields();
    }
    
    public boolean isChanged() {
        return changed;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(MercurialOptionsPanelController.class);
    }
    
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == panel.execPathBrowseButton) {
            onExecPathBrowseClick();
        } else if (evt.getSource() == panel.exportFilenameBrowseButton) {
            onExportFilenameBrowseClick();
        } else if (evt.getSource() == panel.addButton) {
            onAddClick();
        } else if (evt.getSource() == panel.manageButton) {
            onManageClick();
        }
    }

    @Override
    public boolean acceptKeywords (List<String> keywords) {
        Set<String> allKeywords = new HashSet<String>(panel.getKeywords());
        allKeywords.retainAll(keywords);
        return !allKeywords.isEmpty();
    }

    private File getExportFile() {
        String execPath = panel.exportFilenameTextField.getText();
        return FileUtil.normalizeFile(new File(execPath));
    }

    private File getExecutableFile() {
        String execPath = panel.executablePathTextField.getText();
        return FileUtil.normalizeFile(new File(execPath));
    }

    private boolean validateFields() {
        getPanel().showError(null);
        String username = panel.userNameTextField.getText();
        if (!HgModuleConfig.getDefault().isUserNameValid(username)) {
            getPanel().showError(NbBundle.getMessage(MercurialPanel.class, "MSG_WARN_USER_NAME_TEXT")); //NOI18N
            return false;
        }
        String execpath = panel.executablePathTextField.getText();
        String hgExecutableParent = null;
        if (Utilities.isWindows()) {
            hgExecutableParent = getHgWindowsExecutableParent(execpath);
        }
        if ((hgExecutableParent == null) && execpath.endsWith(HG_COMMAND)) {
            hgExecutableParent = execpath.substring(0, execpath.length() - HG_COMMAND.length());
        }   
        if (hgExecutableParent == null) {
            hgExecutableParent = execpath;
        }
        if (!HgModuleConfig.getDefault().isExecPathValid(hgExecutableParent)) {
            getPanel().showError(NbBundle.getMessage(MercurialPanel.class, "MSG_WARN_EXEC_PATH_TEXT")); //NOI18N
            return false;
        }
        if (!HgUtils.isAnnotationFormatValid(HgUtils.createAnnotationFormat(panel.annotationTextField.getText()))) {
            getPanel().showError(NbBundle.getMessage(MercurialPanel.class, "MSG_WARN_ANNOTATION_FORMAT_TEXT")); //NOI18N
            return false;
        }
        return true;
    }

    private static String getHgWindowsExecutableParent(String pathToCheck) {
        for (String hgExecutable : HgCommand.HG_WINDOWS_EXECUTABLES) {
            if (pathToCheck.endsWith(hgExecutable)) {
                return pathToCheck.substring(0, pathToCheck.length()
                                                - hgExecutable.length());
            }
        }
        return null;
    }

    private void onExportFilenameBrowseClick() {
        File oldFile = getExecutableFile();
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(MercurialOptionsPanelController.class, "ACSD_ExportBrowseFolder"), oldFile);   // NOI18N
        fileChooser.setDialogTitle(NbBundle.getMessage(MercurialOptionsPanelController.class, "ExportBrowse_title"));                                            // NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        FileFilter[] old = fileChooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            FileFilter fileFilter = old[i];
            fileChooser.removeChoosableFileFilter(fileFilter);
        }
        fileChooser.showDialog(panel, NbBundle.getMessage(MercurialOptionsPanelController.class, "OK_Button"));                                            // NOI18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            panel.exportFilenameTextField.setText(f.getAbsolutePath());
        }
    }

    private void onExecPathBrowseClick() {
        File oldFile = getExportFile();
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(MercurialOptionsPanelController.class, "ACSD_BrowseFolder"), oldFile);   // NOI18N
        fileChooser.setDialogTitle(NbBundle.getMessage(MercurialOptionsPanelController.class, "Browse_title"));                                            // NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.showDialog(panel, NbBundle.getMessage(MercurialOptionsPanelController.class, "OK_Button"));                                            // NOI18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            panel.executablePathTextField.setText(f.getAbsolutePath());
        }
    }

    private MercurialPanel getPanel() {
        if (panel == null) {
            panel = new MercurialPanel(this);
        }
        return panel;
    }
    
    void changed(boolean isChanged) {
        boolean oldValue = changed;
        changed = isChanged;
        if (changed != oldValue) {
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, oldValue, changed);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    private class LabelVariable {
        private String description;
        private String variable;

        public LabelVariable(String variable, String description) {
            this.description = description;
            this.variable = variable;
        }

        public String toString() {
            return description;
        }

        public String getDescription() {
            return description;
        }

        public String getVariable() {
            return variable;
        }
    }

    private void onAddClick() {
        LabelsPanel labelsPanel = new LabelsPanel();
        List<LabelVariable> variables = new ArrayList<LabelVariable>(MercurialAnnotator.LABELS.length);
        for (int i = 0; i < MercurialAnnotator.LABELS.length; i++) {   
            LabelVariable variable = new LabelVariable(
                    MercurialAnnotator.LABELS[i], 
                    "{" + MercurialAnnotator.LABELS[i] + "} - " + NbBundle.getMessage(MercurialPanel.class, "MercurialPanel.label." + MercurialAnnotator.LABELS[i]) // NOI18N
            );
            variables.add(variable);   
        }       
        labelsPanel.labelsList.setListData(variables.toArray(new LabelVariable[0]));                
                
        String title = NbBundle.getMessage(MercurialPanel.class, "MercurialPanel.labelVariables.title"); // NOI18N
        String acsd = NbBundle.getMessage(MercurialPanel.class, "MercurialPanel.labelVariables.acsd"); // NOI18N

        DialogDescriptor dialogDescriptor = new DialogDescriptor(labelsPanel, title);
        dialogDescriptor.setModal(true);
        dialogDescriptor.setValid(true);
        
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(acsd);
        
        labelsPanel.labelsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    dialog.setVisible(false);
                }
            }        
        });                 
        
        dialog.setVisible(true);
        
        if(DialogDescriptor.OK_OPTION.equals(dialogDescriptor.getValue())) {
            
            Object[] selection = (Object[])labelsPanel.labelsList.getSelectedValues();
            
            String variable = ""; // NOI18N
            for (int i = 0; i < selection.length; i++) {
                variable += "{" + ((LabelVariable)selection[i]).getVariable() + "}"; // NOI18N
            }

            String annotation = panel.annotationTextField.getText();

            int pos = panel.annotationTextField.getCaretPosition();
            if(pos < 0) pos = annotation.length();

            StringBuffer sb = new StringBuffer(annotation.length() + variable.length());
            sb.append(annotation.substring(0, pos));
            sb.append(variable);
            if(pos < annotation.length()) {
                sb.append(annotation.substring(pos));
            }
            panel.annotationTextField.setText(sb.toString());
            panel.annotationTextField.requestFocus();
            panel.annotationTextField.setCaretPosition(pos + variable.length());
        }        
    }        
    
    private void onManageClick() {
        final PropertiesPanel panel = new PropertiesPanel();

        final PropertiesTable propTable;

        propTable = new PropertiesTable(panel.labelForTable, PropertiesTable.PROPERTIES_COLUMNS, new String[] { PropertiesTableModel.COLUMN_NAME_VALUE});

        panel.setPropertiesTable(propTable);

        JComponent component = propTable.getComponent();

        panel.propsPanel.setLayout(new BorderLayout());

        panel.propsPanel.add(component, BorderLayout.CENTER);

        HgExtProperties hgProperties = new HgExtProperties(panel, propTable, null) ;

        final JButton okButton =  new JButton(NbBundle.getMessage(MercurialOptionsPanelController.class, "CTL_Properties_Action_OK")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MercurialOptionsPanelController.class, "CTL_Properties_Action_OK")); // NOI18N
        final JButton cancelButton =  new JButton(NbBundle.getMessage(MercurialOptionsPanelController.class, "CTL_Properties_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MercurialOptionsPanelController.class, "CTL_Properties_Action_Cancel")); // NOI18N
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(MercurialOptionsPanelController.class, "CTL_PropertiesDialog_Title", null), // NOI18N
                true, new Object[] {okButton, cancelButton}, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(MercurialOptionsPanelController.class),
                null);
        
        panel.putClientProperty("contentTitle", null);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MercurialOptionsPanelController.class, "CTL_PropertiesDialog_Title")); // NOI18N

        dialog.pack();
        dialog.setVisible(true);
        if (dd.getValue() == okButton) {
            hgProperties.setProperties();
        }
    }
}

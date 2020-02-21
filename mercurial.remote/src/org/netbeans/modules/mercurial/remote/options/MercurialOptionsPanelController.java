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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.mercurial.remote.options;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JFileChooser;
import javax.swing.JButton;
import java.util.ArrayList;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.MercurialAnnotator;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import static org.netbeans.modules.mercurial.remote.util.HgCommand.HG_COMMAND;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.VCSOptionsKeywordsProvider;
import org.openide.filesystems.FileSystem;

final class MercurialOptionsPanelController extends OptionsPanelController implements ItemListener, ActionListener, VCSOptionsKeywordsProvider {
    
    private MercurialPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;
    private FileSystem fileSystem;
        
    public MercurialOptionsPanelController() {

        FileSystem[] fileSystems = VCSFileProxySupport.getConnectedFileSystems();
        if (fileSystems.length > 0){
            fileSystem = fileSystems[0];
        }
        panel = new MercurialPanel(this);
        panel.execPathBrowseButton.addActionListener(this);
        panel.exportFilenameBrowseButton.addActionListener(this);
        panel.cbBuildHost.addItemListener(this);
        panel.cbBuildHost.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof FileSystem) {
                    value = ((FileSystem)value).getDisplayName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        panel.cbBuildHost.setModel(new DefaultComboBoxModel(fileSystems));
        panel.fileSystemChanged(fileSystem);

        String tooltip = NbBundle.getMessage(MercurialPanel.class, "MercurialPanel.annotationTextField.toolTipText", MercurialAnnotator.LABELS.toArray(new String[MercurialAnnotator.LABELS.size()])); // NOI18N

        panel.annotationTextField.setToolTipText(tooltip);
        panel.addButton.addActionListener(this);
        panel.manageButton.addActionListener(this);
    }

    VCSFileProxy getRoot() {
        return VCSFileProxy.createFileProxy(getFS().getRoot());
    }

    FileSystem getFS() {
        fileSystem = (FileSystem) panel.cbBuildHost.getSelectedItem();
        return fileSystem;
    }
    
    @Override
    public void update() {
        FileSystem[] fileSystems = VCSFileProxySupport.getConnectedFileSystems();
        if (fileSystems.length > 0) {
            fileSystem = fileSystems[0];
        } else {
            fileSystem = null;
        }
        panel.cbBuildHost.setModel(new DefaultComboBoxModel(fileSystems));
        if (fileSystem == null) {
            return;
        }
        getPanel().load();
        changed = false;
    }
    
    @Override
    public void applyChanges() {
        if (getFS() == null) {
            return;
        }
        getPanel().store();
        // {folder} variable setting
        Mercurial.getInstance().getMercurialAnnotator().refresh();
        Mercurial.getInstance().refreshAllAnnotations();

        changed = false;
    }
    
    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }
    
    @Override
    public boolean isValid() {
        if (getFS() == null) {
            return true;
        }
        return validateFields();
    }
    
    @Override
    public boolean isChanged() {
        return changed;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(MercurialOptionsPanelController.class);
    }
    
    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    @Override
    public void itemStateChanged(ItemEvent ev) {
        if (ev.getStateChange() == ItemEvent.SELECTED) {
            Object item = ev.getItem();
            if (item instanceof FileSystem) {
                fileSystem = (FileSystem) item;
                panel.fileSystemChanged(fileSystem);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (getFS() == null) {
            return;
        }
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
        Set<String> allKeywords = new HashSet<>(panel.getKeywords());
        allKeywords.retainAll(keywords);
        return !allKeywords.isEmpty();
    }

    private VCSFileProxy getExportFile() {
        String exportPath = panel.exportFilenameTextField.getText();
        if (exportPath.startsWith("/")) { //NOI18N
            return VCSFileProxySupport.getResource(getRoot(), exportPath).normalizeFile();
        } else {
            return getRoot();
        }
    }

    private VCSFileProxy getExecutableFile() {
        String execPath = panel.executablePathTextField.getText();
        if (execPath.startsWith("/")) { //NOI18N
            return VCSFileProxySupport.getResource(getRoot(), execPath).normalizeFile();
        } else {
            return getRoot();
        }
    }

    private boolean validateFields() {
        getPanel().showError(null);
        String username = panel.userNameTextField.getText();
        if (!HgModuleConfig.getDefault(getRoot()).isUserNameValid(username)) {
            getPanel().showError(NbBundle.getMessage(MercurialPanel.class, "MSG_WARN_USER_NAME_TEXT")); //NOI18N
            return false;
        }
        String execpath = panel.executablePathTextField.getText();
        String hgExecutableParent = null;
        if (execpath.endsWith(HG_COMMAND)) {
            hgExecutableParent = execpath.substring(0, execpath.length() - HG_COMMAND.length());
        }   
        if (hgExecutableParent == null) {
            hgExecutableParent = execpath;
        }
        if (!HgModuleConfig.getDefault(getRoot()).isExecPathValid(hgExecutableParent)) {
            getPanel().showError(NbBundle.getMessage(MercurialPanel.class, "MSG_WARN_EXEC_PATH_TEXT")); //NOI18N
            return false;
        }
        if (!HgUtils.isAnnotationFormatValid(HgUtils.createAnnotationFormat(panel.annotationTextField.getText()))) {
            getPanel().showError(NbBundle.getMessage(MercurialPanel.class, "MSG_WARN_ANNOTATION_FORMAT_TEXT")); //NOI18N
            return false;
        }
        return true;
    }

    private void onExportFilenameBrowseClick() {
        VCSFileProxy oldFile = getExportFile();
        JFileChooser fileChooser = VCSFileProxySupport.createFileChooser(oldFile);
        fileChooser.setDialogTitle(NbBundle.getMessage(MercurialOptionsPanelController.class, "ExportBrowse_title"));                                            // NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        FileFilter[] old = fileChooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            FileFilter fileFilter = old[i];
            fileChooser.removeChoosableFileFilter(fileFilter);
        }
        fileChooser.showDialog(panel, NbBundle.getMessage(MercurialOptionsPanelController.class, "OK_Button"));                                            // NOI18N
        VCSFileProxy f = VCSFileProxySupport.getSelectedFile(fileChooser);
        if (f != null) {
            panel.exportFilenameTextField.setText(f.getPath());
        }
    }

    private void onExecPathBrowseClick() {
        if (getFS() == null) {
            return;
        }
        VCSFileProxy oldFile = getExecutableFile();
        JFileChooser fileChooser = VCSFileProxySupport.createFileChooser(oldFile);
        fileChooser.setDialogTitle(NbBundle.getMessage(MercurialOptionsPanelController.class, "Browse_title"));                                            // NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.showDialog(panel, NbBundle.getMessage(MercurialOptionsPanelController.class, "OK_Button"));                                            // NOI18N
        VCSFileProxy f = VCSFileProxySupport.getSelectedFile(fileChooser);
        if (f != null) {
            panel.executablePathTextField.setText(f.getPath());
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

    private static class LabelVariable {
        private final String description;
        private final String variable;

        public LabelVariable(String variable, String description) {
            this.description = description;
            this.variable = variable;
        }

        @Override
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
        List<LabelVariable> variables = new ArrayList<>(MercurialAnnotator.LABELS.size());
        for (int i = 0; i < MercurialAnnotator.LABELS.size(); i++) {
            LabelVariable variable = new LabelVariable(
                    MercurialAnnotator.LABELS.get(i),
                    "{" + MercurialAnnotator.LABELS.get(i) + "} - " + NbBundle.getMessage(MercurialPanel.class, "MercurialPanel.label." + MercurialAnnotator.LABELS.get(i)) // NOI18N
            );
            variables.add(variable);   
        }       
        labelsPanel.labelsList.setListData(variables.toArray(new LabelVariable[variables.size()]));                
                
        String title = NbBundle.getMessage(MercurialPanel.class, "MercurialPanel.labelVariables.title"); // NOI18N
        String acsd = NbBundle.getMessage(MercurialPanel.class, "MercurialPanel.labelVariables.acsd"); // NOI18N

        DialogDescriptor dialogDescriptor = new DialogDescriptor(labelsPanel, title);
        dialogDescriptor.setModal(true);
        dialogDescriptor.setValid(true);
        
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(acsd);
        
        labelsPanel.labelsList.addMouseListener(new MouseAdapter() {
            @Override
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
            if(pos < 0) {
                pos = annotation.length();
            }

            StringBuilder sb = new StringBuilder(annotation.length() + variable.length());
            sb.append(annotation.substring(0, pos));
            sb.append(variable);
            if(pos < annotation.length()) {
                sb.append(annotation.substring(pos, annotation.length()));
            }
            panel.annotationTextField.setText(sb.toString());
            panel.annotationTextField.requestFocus();
            panel.annotationTextField.setCaretPosition(pos + variable.length());
        }        
    }        
    
    private void onManageClick() {
        final PropertiesPanel panel = new PropertiesPanel(getFS());

        final PropertiesTable propTable;

        propTable = new PropertiesTable(panel.labelForTable, PropertiesTable.PROPERTIES_COLUMNS, new String[] { PropertiesTableModel.COLUMN_NAME_VALUE});

        panel.setPropertiesTable(propTable);

        JComponent component = propTable.getComponent();

        panel.propsPanel.setLayout(new BorderLayout());

        panel.propsPanel.add(component, BorderLayout.CENTER);

        HgExtProperties hgProperties = new HgExtProperties(panel, propTable, getFS()) ;

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

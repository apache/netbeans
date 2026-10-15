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
package org.netbeans.modules.subversion.options;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.subversion.Annotator;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.SvnClientFactory;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.repository.Repository;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.netbeans.modules.versioning.util.VCSOptionsKeywordsProvider;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public final class SvnOptionsController extends OptionsPanelController implements ActionListener, VCSOptionsKeywordsProvider {
    
    private final SvnOptionsPanel panel;
    private Repository repository;
    private final AnnotationSettings annotationSettings;
    private static final HashSet<String> allowedExecutables = new HashSet<String>(Arrays.asList(new String[] {"svn", "svn.exe"} )); //NOI18N
    private static final HashSet<String> allowedLibs = new HashSet<String>(Arrays.asList(new String[] {"libsvnjavahl-1.dll", "libsvnjavahl-1.so"} )); //NOI18N
    private Object currentClient;
        
    public SvnOptionsController() {        
        
        annotationSettings = new AnnotationSettings();
        
        panel = new SvnOptionsPanel();
        panel.browseButton.addActionListener(this);
        panel.browseJavahlButton.addActionListener(this);
        panel.manageConnSettingsButton.addActionListener(this);
        panel.manageLabelsButton.addActionListener(this);
        
        String tooltip = NbBundle.getMessage(AnnotationSettings.class, "AnnotationSettingsPanel.annotationTextField.toolTipText", Annotator.LABELS);               
        panel.annotationTextField.setToolTipText(tooltip);                
        panel.addButton.addActionListener(this);         
        panel.cmbPreferredClient.addActionListener(this);
        panel.cmbPreferredClient.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == panel.panelCLI) {
                    value = NbBundle.getMessage(SvnOptionsPanel.class, "LBL_PreferredClient.CLI"); //NOI18N
                } else if (value == panel.panelJavahl) {
                    value = NbBundle.getMessage(SvnOptionsPanel.class, "LBL_PreferredClient.JAVAHL"); //NOI18N
                } else if (value == panel.panelSvnkit) {
                    value = NbBundle.getMessage(SvnOptionsPanel.class, "LBL_PreferredClient.SVNKIT"); //NOI18N
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        List<JPanel> clients = new ArrayList<>(3);
        clients.add(panel.panelCLI);
        clients.add(panel.panelJavahl);
        if (SvnClientFactory.hasSvnKit()) {
            clients.add(panel.panelSvnkit);
        }
        panel.cmbPreferredClient.setModel(new DefaultComboBoxModel(clients.toArray()));
        panel.textPaneClient.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate (HyperlinkEvent e) {
                if(e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
                URL url = e.getURL();
                assert url != null;
                HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
                assert displayer != null : "HtmlBrowser.URLDisplayer found.";
                if (displayer != null) {
                    displayer.showURL(url);
                } else {
                    Subversion.LOG.info("No URLDisplayer found.");
                }
            }
        });
    }
    
    @Override
    public boolean acceptKeywords (List<String> keywords) {
        Set<String> allKeywords = new HashSet<String>(panel.getKeywords());
        allKeywords.retainAll(keywords);
        return !allKeywords.isEmpty();
    }

    private void createRepository() throws MissingResourceException {
        int repositoryModeMask = Repository.FLAG_URL_ENABLED | Repository.FLAG_SHOW_REMOVE;
        String title = org.openide.util.NbBundle.getMessage(SvnOptionsController.class, "CTL_Repository_Location");
        repository = new Repository(repositoryModeMask, title); // NOI18N
    }
    
    @Override
    public void update() {
        panel.executablePathTextField.setText(SvnModuleConfig.getDefault().getExecutableBinaryPath());
        panel.javahlPathTextField.setText(SvnModuleConfig.getDefault().getExecutableBinaryPath());
        panel.annotationTextField.setText(SvnModuleConfig.getDefault().getAnnotationFormat());
        panel.cbOpenOutputWindow.setSelected(SvnModuleConfig.getDefault().getAutoOpenOutput());
        panel.cbGetRemoteLocks.setSelected(SvnModuleConfig.getDefault().isGetRemoteLocks());
        panel.cbAutoLockFiles.setSelected(SvnModuleConfig.getDefault().isAutoLock());
        annotationSettings.update();
        if (repository != null) {
            repository.refreshUrlHistory();
        }
        panel.excludeNewFiles.setSelected(SvnModuleConfig.getDefault().getExludeNewFiles());
        panel.prefixRepositoryPath.setSelected(SvnModuleConfig.getDefault().isRepositoryPathPrefixed());
        panel.cbDetermineBranches.setSelected(SvnModuleConfig.getDefault().isDetermineBranchesEnabled());
        if (SvnClientFactory.isJavaHl()) {
            panel.cmbPreferredClient.setSelectedItem(panel.panelJavahl);
        } else if (SvnClientFactory.isSvnKit()) {
            panel.cmbPreferredClient.setSelectedItem(panel.panelSvnkit);
        } else {
            panel.cmbPreferredClient.setSelectedItem(panel.panelCLI);
        }
        currentClient = panel.cmbPreferredClient.getSelectedItem();
    }
    
    @Override
    public void applyChanges() {                                 
        // executable
        boolean clientChanged = isClientChanged();
        if (panel.cmbPreferredClient.getSelectedItem() == panel.panelCLI) {
            SvnModuleConfig.getDefault().setExecutableBinaryPath(panel.executablePathTextField.getText());
            SvnModuleConfig.getDefault().setPreferredFactoryType(SvnClientFactory.FACTORY_TYPE_COMMANDLINE);
        } else if (panel.cmbPreferredClient.getSelectedItem() == panel.panelJavahl) {
            SvnModuleConfig.getDefault().setExecutableBinaryPath(panel.javahlPathTextField.getText());
            SvnModuleConfig.getDefault().setPreferredFactoryType(SvnClientFactory.FACTORY_TYPE_JAVAHL);
        } else if (panel.cmbPreferredClient.getSelectedItem() == panel.panelSvnkit) {
            SvnModuleConfig.getDefault().setPreferredFactoryType(SvnClientFactory.FACTORY_TYPE_SVNKIT);
        }
        SvnModuleConfig.getDefault().setAnnotationFormat(panel.annotationTextField.getText());
        SvnModuleConfig.getDefault().setAutoOpenOutputo(panel.cbOpenOutputWindow.isSelected());
        SvnModuleConfig.getDefault().setGetRemoteLocks(panel.cbGetRemoteLocks.isSelected());
        SvnModuleConfig.getDefault().setAutoLock(panel.cbAutoLockFiles.isSelected());
        SvnModuleConfig.getDefault().setExcludeNewFiles(panel.excludeNewFiles.isSelected());
        SvnModuleConfig.getDefault().setRepositoryPathPrefixed(panel.prefixRepositoryPath.isSelected());
        SvnModuleConfig.getDefault().setDetermineBranchesEnabled(panel.cbDetermineBranches.isSelected());

        if (clientChanged) {
            SvnClientFactory.resetClient();
            repository = null;
        }
        // {folder} variable setting
        annotationSettings.applyChanges();
        Subversion.getInstance().getAnnotator().refresh();
        Subversion.getInstance().refreshAllAnnotations();        
        Subversion.getInstance().getStatusCache().getLabelsCache().flushFileLabels((File[])null);
    }
    
    @Override
    public void cancel() {
        if (repository != null) {
            repository.refreshUrlHistory();
        }
    }
    
    @Override
    public boolean isValid() {
        return true;
    }
    
    @Override
    public boolean isChanged() {        
        return !panel.executablePathTextField.getText().equals(SvnModuleConfig.getDefault().getExecutableBinaryPath()) ||
               !panel.javahlPathTextField.getText().equals(SvnModuleConfig.getDefault().getExecutableBinaryPath()) ||
               !panel.annotationTextField.getText().equals(SvnModuleConfig.getDefault().getAnnotationFormat()) || 
               panel.cbGetRemoteLocks.isSelected() != SvnModuleConfig.getDefault().isGetRemoteLocks() || 
               (repository != null && repository.isChanged()) || 
               annotationSettings.isChanged()
                || isClientChanged()
                || SvnModuleConfig.getDefault().getAutoOpenOutput() != panel.cbOpenOutputWindow.isSelected()
                || SvnModuleConfig.getDefault().isAutoLock() != panel.cbAutoLockFiles.isSelected()
                || SvnModuleConfig.getDefault().getExludeNewFiles() != panel.excludeNewFiles.isSelected()
                || SvnModuleConfig.getDefault().isDetermineBranchesEnabled() != panel.cbDetermineBranches.isSelected()
                || SvnModuleConfig.getDefault().isRepositoryPathPrefixed() != panel.prefixRepositoryPath.isSelected();
    }

    private boolean isClientChanged () {
        Object selectedPanel = panel.cmbPreferredClient.getSelectedItem();
        return selectedPanel != currentClient;
    }
        
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(getClass());
    }
    
    @Override
    public javax.swing.JComponent getComponent(org.openide.util.Lookup masterLookup) {
        return panel;
    }
    
    @Override
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        
    }
    
    @Override
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        
    }
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        if(evt.getSource() == panel.browseButton) {
            onBrowseClick();
        } else if(evt.getSource() == panel.browseJavahlButton) {
            onBrowseJavahlClick();
        } else if(evt.getSource() == panel.manageConnSettingsButton) {
            onManageConnClick();
        } else if(evt.getSource() == panel.manageLabelsButton) {
            onManageLabelsClick();
        } else if (evt.getSource() == panel.addButton) {
            onAddClick();
        } else if (evt.getSource() == panel.cmbPreferredClient) {
            onPreferredClientChanged();
        }
    }

    private void onPreferredClientChanged () {
        Object selectedClient = panel.cmbPreferredClient.getSelectedItem();
        panel.panelCLI.setVisible(false);
        panel.panelJavahl.setVisible(false);
        panel.panelSvnkit.setVisible(false);
        panel.lblRestartWarning.setVisible(false);
        if (selectedClient == panel.panelCLI) {
            panel.panelCLI.setVisible(true);
            // currently no need to restart, change works in the same session
//            panel.lblRestartWarning.setVisible(SvnClientFactory.isClientAvailable() && !SvnClientFactory.isCLI());
        } else if (selectedClient == panel.panelJavahl) {
            panel.panelJavahl.setVisible(true);
            // currently no need to restart, change works in the same session
//            panel.lblRestartWarning.setVisible(SvnClientFactory.isClientAvailable() && !SvnClientFactory.isJavaHl());
        } else if (selectedClient == panel.panelSvnkit) {
            panel.panelSvnkit.setVisible(true);
            // currently no need to restart, change works in the same session
//            panel.lblRestartWarning.setVisible(SvnClientFactory.isClientAvailable() && !SvnClientFactory.isSvnKit());
        }
    }
    
    private File getExecutableFile() {
        String execPath = panel.executablePathTextField.getText();
        return FileUtil.normalizeFile(new File(execPath));
    }

    private File getJavahlFolder () {
        String execPath = panel.javahlPathTextField.getText();
        return FileUtil.normalizeFile(new File(execPath));
    }
    
    private void onBrowseClick () {
        File oldFile = getExecutableFile();
        onBrowse(oldFile, allowedExecutables, panel.executablePathTextField,
                NbBundle.getMessage(SvnOptionsController.class, "ACSD_BrowseFolder"), //NOI18N
                NbBundle.getMessage(SvnOptionsController.class, "Browse_title"), //NOI18N
                NbBundle.getMessage(SvnOptionsController.class, "FileChooser.SvnExecutables.desc") //NOI18N
                );
    }
                
    private void onBrowse (File oldFile, final Set<String> allowedFileNames, JTextField textField, String acsd, String browseTitle, final String fileTypeDesc) {
        JFileChooser fileChooser = new AccessibleJFileChooser(acsd, oldFile);
        fileChooser.setDialogTitle(browseTitle);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || allowedFileNames.contains(f.getName());
            }
            @Override
            public String getDescription() {
                return fileTypeDesc;
            }
        });
        fileChooser.showDialog(panel, NbBundle.getMessage(SvnOptionsController.class, "OK_Button")); //NOI18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            while (!f.exists() || f.isFile()) {
                File parent = f.getParentFile();
                if (parent == null) {
                    break;
                } else {
                    f = parent;
                }
            }
            textField.setText(f.getAbsolutePath());
        }
    }

    private void onBrowseJavahlClick () {
        onBrowse(getJavahlFolder(), allowedLibs, panel.javahlPathTextField,
                NbBundle.getMessage(SvnOptionsController.class, "ACSD_BrowseJavahlFolder"), //NOI18N
                NbBundle.getMessage(SvnOptionsController.class, "Browse_Javahl_title"), //NOI18N
                NbBundle.getMessage(SvnOptionsController.class, "FileChooser.SvnLibs.desc") //NOI18N
            );
    }
    
    private void onManageConnClick() {
        if (repository == null) {
            panel.manageConnSettingsButton.setEnabled(false);
            new SvnProgressSupport() {
                @Override
                protected void perform () {
                    try {
                        createRepository();
                    } finally {
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                panel.manageConnSettingsButton.setEnabled(true);
                                if (repository != null) {
                                    onManageConnClick();
                                }
                            }
                        });
                    }
                }
            }.start(Subversion.getInstance().getParallelRequestProcessor(), null, NbBundle.getMessage(SvnOptionsController.class, "MSG_ManageConnections.initializing")); //NOI18N
        } else {
            boolean ok = repository.show(NbBundle.getMessage(SvnOptionsController.class, "CTL_ManageConnections"), new HelpCtx(Repository.class), true);
            if(ok) {            
                repository.storeRecentUrls();
            } else {    
                repository.refreshUrlHistory();
            }
        }
    }
    
    private void onManageLabelsClick() {     
        String labelFormat = panel.annotationTextField.getText().replace(" ", "");        
        annotationSettings.show(labelFormat != null && labelFormat.indexOf("{folder}") > -1);                
    }
    
    private class LabelVariable {
        private String description;
        private String variable;
         
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
        List<LabelVariable> variables = new ArrayList<LabelVariable>(Annotator.LABELS.length);
        for (int i = 0; i < Annotator.LABELS.length; i++) {   
            LabelVariable variable = new LabelVariable(
                    Annotator.LABELS[i], 
                    "{" + Annotator.LABELS[i] + "} - " + NbBundle.getMessage(AnnotationSettings.class, "AnnotationSettings.label." + Annotator.LABELS[i])
            );
            variables.add(variable);   
        }       
        labelsPanel.labelsList.setListData(variables.toArray(new LabelVariable[0]));                
                
        String title = NbBundle.getMessage(AnnotationSettings.class, "AnnotationSettings.labelVariables.title");
        String acsd = NbBundle.getMessage(AnnotationSettings.class, "AnnotationSettings.labelVariables.acsd");

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
            
            Object[] selection = labelsPanel.labelsList.getSelectedValues();
            
            String variable = "";
            for (int i = 0; i < selection.length; i++) {
                variable += "{" + ((LabelVariable)selection[i]).getVariable() + "}";
            }

            String annotation = panel.annotationTextField.getText();

            int pos = panel.annotationTextField.getCaretPosition();
            if(pos < 0) pos = annotation.length();

            StringBuilder sb = new StringBuilder(annotation.length() + variable.length());
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
    
}

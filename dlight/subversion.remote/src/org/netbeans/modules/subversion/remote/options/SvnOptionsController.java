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
package org.netbeans.modules.subversion.remote.options;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.subversion.remote.Annotator;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.SvnModuleConfig;
import org.netbeans.modules.subversion.remote.client.SvnClientFactory;
import org.netbeans.modules.subversion.remote.client.SvnProgressSupport;
import org.netbeans.modules.subversion.remote.ui.repository.Repository;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.VCSOptionsKeywordsProvider;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public final class SvnOptionsController extends OptionsPanelController implements ItemListener, ActionListener, VCSOptionsKeywordsProvider {
    
    private final SvnOptionsPanel panel;
    private Repository repository;
    private final AnnotationSettings annotationSettings;
    private static final HashSet<String> allowedExecutables = new HashSet<>(Arrays.asList(new String[] {"svn"} )); //NOI18N
    private FileSystem fileSystem;
        
    public SvnOptionsController() {    
        FileSystem[] fileSystems = VCSFileProxySupport.getConnectedFileSystems();
        if (fileSystems.length > 0) {
            fileSystem = fileSystems[0];
        }
        
        annotationSettings = new AnnotationSettings(fileSystem);
        
        panel = new SvnOptionsPanel();
        panel.browseButton.addActionListener(this);
        panel.manageConnSettingsButton.addActionListener(this);
        panel.manageLabelsButton.addActionListener(this);
        
        String tooltip = NbBundle.getMessage(AnnotationSettings.class, "AnnotationSettingsPanel.annotationTextField.toolTipText", Annotator.LABELS.toArray(new String[Annotator.LABELS.size()]));
        panel.annotationTextField.setToolTipText(tooltip);                
        panel.addButton.addActionListener(this);         
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
        panel.cbBuildHost.setModel(new DefaultComboBoxModel<FileSystem>(fileSystems));
        panel.fileSystemChanged(fileSystem);
        panel.textPaneClient.addHyperlinkListener(new HyperlinkListener() {
            @Override
            @org.netbeans.api.annotations.common.SuppressWarnings("RCN") // assert in release mode does not guarantee that "displayer != null"
            public void hyperlinkUpdate (HyperlinkEvent e) {
                if(e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
                    return;
                }
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
        Set<String> allKeywords = new HashSet<>(panel.getKeywords());
        allKeywords.retainAll(keywords);
        return !allKeywords.isEmpty();
    }

    private void createRepository() throws MissingResourceException {
        int repositoryModeMask = Repository.FLAG_URL_ENABLED | Repository.FLAG_SHOW_REMOVE;
        String title = org.openide.util.NbBundle.getMessage(SvnOptionsController.class, "CTL_Repository_Location");
        repository = new Repository(fileSystem, repositoryModeMask, title); // NOI18N
    }
    
    @Override
    public void update() {
        FileSystem[] fileSystems = VCSFileProxySupport.getConnectedFileSystems();
        if (fileSystems.length > 0) {
            fileSystem = fileSystems[0];
        } else {
            fileSystem = null;
        }
        panel.cbBuildHost.setModel(new DefaultComboBoxModel<FileSystem>(fileSystems));
        annotationSettings.update();
        if (fileSystem == null) {
            return;
        }
        panel.executablePathTextField.setText(SvnModuleConfig.getDefault(fileSystem).getExecutableBinaryPath());
        panel.annotationTextField.setText(SvnModuleConfig.getDefault(fileSystem).getAnnotationFormat());
        panel.cbOpenOutputWindow.setSelected(SvnModuleConfig.getDefault(fileSystem).getAutoOpenOutput());
        panel.cbGetRemoteLocks.setSelected(SvnModuleConfig.getDefault(fileSystem).isGetRemoteLocks());
        panel.cbAutoLockFiles.setSelected(SvnModuleConfig.getDefault(fileSystem).isAutoLock());
        if (repository != null) {
            repository.refreshUrlHistory();
        }
        panel.excludeNewFiles.setSelected(SvnModuleConfig.getDefault(fileSystem).getExludeNewFiles());
        panel.prefixRepositoryPath.setSelected(SvnModuleConfig.getDefault(fileSystem).isRepositoryPathPrefixed());
        panel.cbDetermineBranches.setSelected(SvnModuleConfig.getDefault(fileSystem).isDetermineBranchesEnabled());
    }
    
    @Override
    public void applyChanges() {                                 
        SvnModuleConfig.getDefault(fileSystem).setExecutableBinaryPath(panel.executablePathTextField.getText());
        SvnModuleConfig.getDefault(fileSystem).setPreferredFactoryType(SvnClientFactory.FACTORY_TYPE_COMMANDLINE);
        SvnModuleConfig.getDefault(fileSystem).setAnnotationFormat(panel.annotationTextField.getText());
        SvnModuleConfig.getDefault(fileSystem).setAutoOpenOutputo(panel.cbOpenOutputWindow.isSelected());
        SvnModuleConfig.getDefault(fileSystem).setGetRemoteLocks(panel.cbGetRemoteLocks.isSelected());
        SvnModuleConfig.getDefault(fileSystem).setAutoLock(panel.cbAutoLockFiles.isSelected());
        SvnModuleConfig.getDefault(fileSystem).setExcludeNewFiles(panel.excludeNewFiles.isSelected());
        SvnModuleConfig.getDefault(fileSystem).setRepositoryPathPrefixed(panel.prefixRepositoryPath.isSelected());
        SvnModuleConfig.getDefault(fileSystem).setDetermineBranchesEnabled(panel.cbDetermineBranches.isSelected());

        // {folder} variable setting
        annotationSettings.applyChanges();
        Subversion.getInstance().getAnnotator().refresh();
        Subversion.getInstance().refreshAllAnnotations();        
        Subversion.getInstance().getStatusCache().getLabelsCache().flushFileLabels((VCSFileProxy[])null);
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
        return !panel.executablePathTextField.getText().equals(SvnModuleConfig.getDefault(fileSystem).getExecutableBinaryPath()) ||
               !panel.annotationTextField.getText().equals(SvnModuleConfig.getDefault(fileSystem).getAnnotationFormat()) || 
               panel.cbGetRemoteLocks.isSelected() != SvnModuleConfig.getDefault(fileSystem).isGetRemoteLocks() || 
               (repository != null && repository.isChanged()) || 
               annotationSettings.isChanged()
                || SvnModuleConfig.getDefault(fileSystem).getAutoOpenOutput() != panel.cbOpenOutputWindow.isSelected()
                || SvnModuleConfig.getDefault(fileSystem).isAutoLock() != panel.cbAutoLockFiles.isSelected()
                || SvnModuleConfig.getDefault(fileSystem).getExludeNewFiles() != panel.excludeNewFiles.isSelected()
                || SvnModuleConfig.getDefault(fileSystem).isDetermineBranchesEnabled() != panel.cbDetermineBranches.isSelected()
                || SvnModuleConfig.getDefault(fileSystem).isRepositoryPathPrefixed() != panel.prefixRepositoryPath.isSelected();
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
        if(evt.getSource() == panel.browseButton) {
            onBrowseClick();
        } else if(evt.getSource() == panel.manageConnSettingsButton) {
            onManageConnClick();
        } else if(evt.getSource() == panel.manageLabelsButton) {
            onManageLabelsClick();
        } else if (evt.getSource() == panel.addButton) {
            onAddClick();
        }
    }

    private VCSFileProxy getExecutableFile() {
        if (fileSystem == null) {
            return null;
        }
        String execPath = panel.executablePathTextField.getText();
        if (execPath.isEmpty()) {
            return VCSFileProxy.createFileProxy(fileSystem.getRoot());
        } else {
            return VCSFileProxySupport.getResource(fileSystem, execPath);
        }
    }

    private void onBrowseClick () {
        VCSFileProxy oldFile = getExecutableFile();
        onBrowse(oldFile, allowedExecutables, panel.executablePathTextField,
                NbBundle.getMessage(SvnOptionsController.class, "ACSD_BrowseFolder"), //NOI18N
                NbBundle.getMessage(SvnOptionsController.class, "Browse_title"), //NOI18N
                NbBundle.getMessage(SvnOptionsController.class, "FileChooser.SvnExecutables.desc") //NOI18N
                );
    }
                
    private void onBrowse (VCSFileProxy oldFile, final Set<String> allowedFileNames, JTextField textField, String acsd, String browseTitle, final String fileTypeDesc) {
        if (oldFile == null) {
            return;
        }
        JFileChooser fileChooser = VCSFileProxySupport.createFileChooser(oldFile);
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
        int showDialog = fileChooser.showDialog(panel, NbBundle.getMessage(SvnOptionsController.class, "OK_Button")); //NOI18N
        if (showDialog == JFileChooser.APPROVE_OPTION) {
            VCSFileProxy f = VCSFileProxySupport.getSelectedFile(fileChooser);
            if (f != null) {
                while (!f.exists() || f.isFile()) {
                    VCSFileProxy parent = f.getParentFile();
                    if (parent == null) {
                        break;
                    } else {
                        f = parent;
                    }
                }
                textField.setText(f.getPath());
            }
        }
    }

    private void onManageConnClick() {
        fileSystem = (FileSystem) panel.cbBuildHost.getSelectedItem();
        if (fileSystem == null) {
            return;
        }
        if (repository == null) {
            panel.manageConnSettingsButton.setEnabled(false);
            new SvnProgressSupport(fileSystem) {
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
        String labelFormat = panel.annotationTextField.getText().replaceAll(" ", ""); //NOI18N  
        annotationSettings.show(labelFormat.indexOf("{folder}") > -1); //NOI18N         
    }

    private static class LabelVariable {
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
        List<LabelVariable> variables = new ArrayList<>(Annotator.LABELS.size());
        for (int i = 0; i < Annotator.LABELS.size(); i++) {
            LabelVariable variable = new LabelVariable(
                    Annotator.LABELS.get(i),
                    "{" + Annotator.LABELS.get(i) + "} - " + NbBundle.getMessage(AnnotationSettings.class, "AnnotationSettings.label." + Annotator.LABELS.get(i))
            );
            variables.add(variable);   
        }       
        labelsPanel.labelsList.setListData(variables.toArray(new LabelVariable[variables.size()]));                
                
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
                variable += "{" + ((LabelVariable)selection[i]).getVariable() + "}"; //NOI18N
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
    
}

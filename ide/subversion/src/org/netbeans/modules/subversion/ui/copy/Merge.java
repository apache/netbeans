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
package org.netbeans.modules.subversion.ui.copy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.ui.browser.Browser;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.ui.search.SvnSearch;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class Merge extends CopyDialog implements ItemListener {
        
    private String lastSelectedUrl;
    private final RepositoryFile repositoryFile;

    public Merge(RepositoryFile repositoryRoot, File root) {
        super(new MergePanel(), NbBundle.getMessage(Merge.class, "CTL_Merge_Prompt", root.getName()), NbBundle.getMessage(Merge.class, "CTL_Merge_Title")); // NOI18N

        MergePanel panel = getMergePanel();

        panel.typeComboBox.setModel(new DefaultComboBoxModel(
                new MergeType[] {
                       new MergeBranchType(repositoryRoot, root),
                       new MergeSinceOriginType(repositoryRoot, root),
                       new MergeOneFolderType(repositoryRoot, root),
                       new MergeTwoFoldersType(repositoryRoot, root)                       
                }
        ));
        this.repositoryFile = repositoryRoot;
        panel.typeComboBox.setRenderer(createTypeRenderer());
        panel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Merge.class, "CTL_Merge_Title"));
        panel.typeComboBox.addItemListener(this);
        mergeTypeSelected(((MergeType) panel.typeComboBox.getSelectedItem()));
    }            
    
    RepositoryFile getMergeStartRepositoryFile() {
        return getSelectedType().getMergeStartRepositoryFile();
    }

    RepositoryFile getMergeEndRepositoryFile() {
        return getSelectedType().getMergeEndRepositoryFile();
    }    

    SVNRevision getMergeStartRevision() {
        return getSelectedType().getMergeStartRevision();
    }

    SVNRevision getMergeEndRevision() {
        return getSelectedType().getMergeEndRevision();
    }

    boolean isStartRevisionIncluded () {
        return getSelectedType().isStartRevisionIncluded();
    }

    boolean isIgnoreAncestry () {
        return getSelectedType().isIgnoreAncestry();
    }

    private MergeType getSelectedType() {
        return (MergeType) getMergePanel().typeComboBox.getSelectedItem();
    }

    private MergePanel getMergePanel() {
        return (MergePanel) getPanel();
    }

    private ListCellRenderer createTypeRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if(value instanceof MergeType) {
                    setText(((MergeType)value).getDisplayName());
                }
                return this;
            }
        };
    }
    
    @Override
    public void itemStateChanged(ItemEvent e) {        
        final MergeType type = (MergeType) e.getItem();                
        
        JTextComponent text;
        if(type instanceof MergeSinceOriginType || type instanceof MergeBranchType) {
            text = (JTextComponent) type.getEndUrlComboBox().getEditor().getEditorComponent();    
        } else {
            text = (JTextComponent) type.getStartUrlComboBox().getEditor().getEditorComponent();    
        }
        
        if(e.getStateChange() == ItemEvent.SELECTED) {            
            
            mergeTypeSelected(type);
            
            if(text.getText().trim().equals("") && lastSelectedUrl != null) {
                text.setText(lastSelectedUrl);
            }
            
        } else if(e.getStateChange() == ItemEvent.DESELECTED) {
            if(!text.getText().trim().equals("")) {
                lastSelectedUrl = text.getText();
            }
        }
    }

    private void mergeTypeSelected(MergeType type) {                        
        MergePanel panel = getMergePanel();
        panel.typeDescriptionLabel.setText(type.getDescription());

        panel.previewPanel.removeAll();        
        panel.previewPanel.setLayout(new BorderLayout());
        panel.previewPanel.add(type.getPreviewPanel(), BorderLayout.CENTER);        
        
        panel.mergeFieldsPanel.removeAll();
        panel.mergeFieldsPanel.setLayout(new BorderLayout());
        panel.mergeFieldsPanel.add(type.getFieldsPanel(), BorderLayout.CENTER);

        type.setPreviewLabels();
        panel.repaint();
        
        setupUrlComboBox(repositoryFile, type.getStartUrlComboBox(), false);
        setupUrlComboBox(repositoryFile, type.getEndUrlComboBox(), false);
    }    

    private abstract class MergeType implements DocumentListener, PropertyChangeListener {

        private RepositoryPaths mergeStartRepositoryPaths;
        private RepositoryPaths mergeEndRepositoryPaths;
        private RepositoryFile repositoryFile;

        private boolean startPathValid = false;
        private boolean endPathValid = false;
    
        MergeType (RepositoryFile repositoryFile) {
            this.repositoryFile = repositoryFile;
        }

        protected void init(RepositoryPaths mergeStartRepositoryPaths, JLabel mergeStartRepositoryFolderLabel, RepositoryPaths mergeEndRepositoryPaths, JLabel mergeEndRepositoryFolderLabel, File root) {            
            if(mergeStartRepositoryPaths != null) {
                this.mergeStartRepositoryPaths = mergeStartRepositoryPaths;
                init(mergeStartRepositoryPaths, mergeStartRepositoryFolderLabel, root);   
            } else {
                // won't ever chage -> will always be valid
                startPathValid = true;
            }          
            if(mergeEndRepositoryPaths != null) {
                this.mergeEndRepositoryPaths = mergeEndRepositoryPaths;
                init(mergeEndRepositoryPaths, mergeEndRepositoryFolderLabel, root);
            } else {
                // won't ever chage -> will always be valid
                endPathValid = true;
            } 
        }

        private void init(RepositoryPaths paths, JLabel label, File root) {
            String browserPurposeMessage;
            int browserMode;
            if(root.isFile()) {
                if(label!=null) {
                    label.setText(org.openide.util.NbBundle.getMessage(Merge.class, "CTL_Merge_RepositoryFile")); // NOI18N
                }
                browserPurposeMessage = org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_BrowserMessageMergeFile");
                browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY | Browser.BROWSER_SHOW_FILES | Browser.BROWSER_FILES_SELECTION_ONLY;
            } else {
                browserPurposeMessage = org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_BrowserMessageMergeFolder");
                browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY;                
            }
            paths.setupBehavior(browserPurposeMessage, browserMode, Browser.BROWSER_HELP_ID_MERGE, SvnSearch.SEACRH_HELP_ID_MERGE);
            paths.addPropertyChangeListener(this);
        }

        protected abstract JPanel getFieldsPanel();
        protected abstract JPanel getPreviewPanel();
        protected abstract String getDisplayName();
        protected abstract String getDescription();        
        protected abstract JComboBox getStartUrlComboBox();
        protected abstract JComboBox getEndUrlComboBox();
        protected abstract void setPreviewLabels();

        public RepositoryFile getMergeStartRepositoryFile() {
            try {
                return mergeStartRepositoryPaths.getRepositoryFiles()[0];
            } catch (MalformedURLException ex) {
                // should be already checked and
                // not happen at this place anymore
                Subversion.LOG.log(Level.INFO, null, ex);
            }
            return null;
        }

        public RepositoryFile getMergeEndRepositoryFile() {
            try {
                return mergeEndRepositoryPaths.getRepositoryFiles()[0];
            } catch (MalformedURLException ex) {
                // should be already checked and
                // not happen at this place anymore
                Subversion.LOG.log(Level.INFO, null, ex);
            }
            return null;
        }

        public SVNRevision getMergeStartRevision() {
            try {
                return mergeStartRepositoryPaths.getRepositoryFiles()[0].getRevision();
            } catch (MalformedURLException ex) {
                // should be already checked and
                // not happen at this place anymore
                Subversion.LOG.log(Level.INFO, null, ex);
            }
            return null;
        }

        public SVNRevision getMergeEndRevision() {
            try {
                return mergeEndRepositoryPaths.getRepositoryFiles()[0].getRevision();
            } catch (MalformedURLException ex) {
                // should be already checked and
                // not happen at this place anymore
                Subversion.LOG.log(Level.INFO, null, ex);
            }
            return null;
        }

        public boolean isStartRevisionIncluded () {
            return false;
        }

        RepositoryPaths getMergeStartRepositoryPath() {
            return mergeStartRepositoryPaths;
        }

        RepositoryPaths getMergeEndRepositoryPath() {
            return mergeEndRepositoryPaths;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            setPreviewLabels();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            setPreviewLabels();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            setPreviewLabels();
        }

        protected RepositoryFile getRepositoryFile() {
            return repositoryFile;
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if( evt.getPropertyName().equals(RepositoryPaths.PROP_VALID) ) {                                        
                boolean valid = ((Boolean) evt.getNewValue()).booleanValue();
                if(evt.getSource() == getMergeStartRepositoryPath()) {
                    startPathValid = valid;
                } else if(evt.getSource() == getMergeEndRepositoryPath()) {
                    endPathValid = valid;
                }                                    
                getOKButton().setEnabled(startPathValid && endPathValid);
            }        
        }        

        public boolean isIgnoreAncestry () {
            return false;
        }
    }

    private class MergeTwoFoldersType extends MergeType {

        private MergeTwoFoldersPanel panel;
        private TwoFoldersPreviewPanel previewPanel;

        public MergeTwoFoldersType(RepositoryFile repositoryRoot, File root) {
            super(repositoryRoot);

            panel = new MergeTwoFoldersPanel();
            previewPanel = new TwoFoldersPreviewPanel();

            RepositoryPaths mergeStartRepositoryPaths =
                new RepositoryPaths(
                    repositoryRoot,
                    (JTextComponent) panel.mergeStartUrlComboBox.getEditor().getEditorComponent(),
                    panel.mergeStartBrowseButton,
                    panel.mergeStartRevisionTextField,
                    panel.mergeStartSearchButton,
                    panel.mergeStartBrowseRevisionButton
                );

            RepositoryPaths mergeEndRepositoryPaths =
                new RepositoryPaths(
                    repositoryRoot,
                    (JTextComponent) panel.mergeEndUrlComboBox.getEditor().getEditorComponent(),
                    panel.mergeEndBrowseButton,
                    panel.mergeEndRevisionTextField,
                    panel.mergeEndSearchButton,
                    panel.mergeEndBrowseRevisionButton
                );

            init(mergeStartRepositoryPaths,
                 panel.mergeStartRepositoryFolderLabel,
                 mergeEndRepositoryPaths,
                 panel.mergeEndRepositoryFolderLabel,
                 root);

            previewPanel.localFolderTextField.setText(root.getAbsolutePath());
            ((JTextComponent) panel.mergeStartUrlComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
            ((JTextComponent) panel.mergeEndUrlComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        }

        @Override
        public boolean isStartRevisionIncluded () {
            return panel.cbIncludeStartRevision.isSelected();
        }

        @Override
        public String getDisplayName() {
            return org.openide.util.NbBundle.getMessage(Merge.class, "CTL_Merge_TwoRepositoryFolders"); // NOI18N
        }

        @Override
        public String getDescription() {
            return org.openide.util.NbBundle.getMessage(Merge.class, "CTL_Merge_TwoFoldersDesc"); // NOI18N
        }

        @Override
        public JPanel getFieldsPanel() {
            return panel;
        }

        @Override
        public JPanel getPreviewPanel() {
            return previewPanel;
        }

        @Override
        public JComboBox getStartUrlComboBox() {
            return panel.mergeStartUrlComboBox;
        }

        @Override
        public JComboBox getEndUrlComboBox() {
            return panel.mergeEndUrlComboBox;
        }

        @Override
        protected void setPreviewLabels() {
            previewPanel.repositoryFolderTextField1.setText(getRepositoryFile().getRepositoryUrl() + "/" + panel.mergeEndUrlComboBox.getEditor().getItem().toString()); // NOI18N
            previewPanel.repositoryFolderTextField2.setText(getRepositoryFile().getRepositoryUrl() + "/" + panel.mergeStartUrlComboBox.getEditor().getItem().toString()); // NOI18N
        }

        @Override
        public boolean isIgnoreAncestry () {
            return panel.cbIgnoreAncestry.isSelected();
        }
        
    }

    private class MergeOneFolderType extends MergeType {

        private RepositoryPaths mergeEndRepositoryPaths;
        private MergeOneFolderPanel panel;
        private OneFolderPreviewPanel previewPanel;

        /** Creates a new instance of MergeOneFolderType */
        public MergeOneFolderType(RepositoryFile repositoryRoot, File root) {
            super(repositoryRoot);
            
            panel = new MergeOneFolderPanel();
            previewPanel = new OneFolderPreviewPanel();

            RepositoryPaths mergeStartRepositoryPaths =
                new RepositoryPaths(
                    repositoryRoot,
                    (JTextComponent) panel.mergeStartUrlComboBox.getEditor().getEditorComponent(),
                    null,
                    panel.mergeStartRevisionTextField,
                    panel.mergeStartSearchButton,
                    panel.mergeStartBrowseRevisionButton
                );

            mergeEndRepositoryPaths =
                new RepositoryPaths(
                    repositoryRoot,
                    (JTextComponent) panel.mergeStartUrlComboBox.getEditor().getEditorComponent(),
                    panel.mergeStartBrowseButton,
                    panel.mergeEndRevisionTextField,
                    panel.mergeEndSearchButton,
                    panel.mergeEndBrowseRevisionButton
                );

            init(mergeStartRepositoryPaths,
                 panel.mergeStartRepositoryFolderLabel,
                 mergeEndRepositoryPaths,
                 null,
                 root);
            
            previewPanel.localFolderTextField.setText(root.getAbsolutePath());
            ((JTextComponent) panel.mergeStartUrlComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        }

        
        @Override
        public RepositoryFile getMergeStartRepositoryFile() {
            // in this case its the same folder url
            return getMergeEndRepositoryFile();
        }

        @Override
        public boolean isStartRevisionIncluded () {
            return panel.cbIncludeStartRevision.isSelected();
        }

        @Override
        public String getDisplayName() {
            return org.openide.util.NbBundle.getMessage(Merge.class, "CTL_Merge_OneRepositoryFolder"); // NOI18N
        }

        @Override
        public String getDescription() {
            return org.openide.util.NbBundle.getMessage(Merge.class, "CTL_Merge_OneRepositoryFolderDesc"); // NOI18N
        }

        @Override
        public JPanel getFieldsPanel() {
            return panel;
        }

        @Override
        public JPanel getPreviewPanel() {
            return previewPanel;
        }
        
        @Override
        public JComboBox getStartUrlComboBox() {
            return panel.mergeStartUrlComboBox;
        }

        @Override
        public JComboBox getEndUrlComboBox() {
            return null;
        }    

        @Override
        protected void setPreviewLabels() {            
            previewPanel.repositoryFolderTextField.setText(getRepositoryFile().getRepositoryUrl() + org.openide.util.NbBundle.getMessage(Merge.class, "/") + panel.mergeStartUrlComboBox.getEditor().getItem().toString()); // NOI18N
        }

        @Override
        public boolean isIgnoreAncestry () {
            return panel.cbIgnoreAncestry.isSelected();
        }

    }

    private class MergeSinceOriginType extends MergeType {

        private MergeSinceOriginPanel panel;
        private RepositoryPaths mergeEndRepositoryPaths;
        private SinceOriginPreviewPanel previewPanel;

        public MergeSinceOriginType(RepositoryFile repositoryRoot, File root) {
            super(repositoryRoot);
            
            panel = new MergeSinceOriginPanel();
            previewPanel = new SinceOriginPreviewPanel();

            mergeEndRepositoryPaths =
                new RepositoryPaths(
                    repositoryRoot,
                    (JTextComponent) panel.mergeEndUrlComboBox.getEditor().getEditorComponent(),
                    panel.mergeEndBrowseButton,
                    panel.mergeEndRevisionTextField,
                    panel.mergeEndSearchButton,
                    panel.mergeEndBrowseRevisionButton
                );      
            
            init(null, null, mergeEndRepositoryPaths, panel.mergeEndRepositoryFolderLabel, root);
            previewPanel.localFolderTextField.setText(root.getAbsolutePath());
            ((JTextComponent) panel.mergeEndUrlComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);                     
        }

        @Override
        public String getDisplayName() {
            return org.openide.util.NbBundle.getMessage(Merge.class, "CTL_Merge_OneRepositoryFolderSinceOrigin"); // NOI18N
        }

        @Override
        public String getDescription() {
            return org.openide.util.NbBundle.getMessage(Merge.class, "CTL_Merge_OneRepositoryFolderSinceOriginDesc"); // NOI18N
        }

        @Override
        public JPanel getFieldsPanel() {
            return panel;
        }

        @Override
        public JPanel getPreviewPanel() {
            return previewPanel;
        }

        @Override
        public RepositoryFile getMergeStartRepositoryFile() {
            return null;
        }

        @Override
        public SVNRevision getMergeStartRevision() {
            return null;
        }

        public SVNUrl getMergeEndUrl() {
            try {
                return mergeEndRepositoryPaths.getRepositoryFiles()[0].getFileUrl();
            } catch (MalformedURLException ex) {
                // should be already checked and
                // not happen at this place anymore
                Subversion.LOG.log(Level.INFO, null, ex);
            }
            return null;
        }

        @Override
        public SVNRevision getMergeEndRevision() {
            try {
                return mergeEndRepositoryPaths.getRepositoryFiles()[0].getRevision();
            } catch (MalformedURLException ex) {
                // should be already checked and
                // not happen at this place anymore
                Subversion.LOG.log(Level.INFO, null, ex);
            }
            return null;
        }

        @Override
        RepositoryPaths getMergeStartRepositoryPath() {
            return null;
        }

        @Override
        RepositoryPaths getMergeEndRepositoryPath() {
            return mergeEndRepositoryPaths;
        }

        @Override
        public JComboBox getStartUrlComboBox() {
            return null;
        }

        @Override
        public JComboBox getEndUrlComboBox() {
            return panel.mergeEndUrlComboBox;
        }

        @Override
        protected void setPreviewLabels() {            
            previewPanel.repositoryFolderTextField.setText(getRepositoryFile().getRepositoryUrl() + "/" + panel.mergeEndUrlComboBox.getEditor().getItem().toString()); // NOI18N
        }

        @Override
        public boolean isIgnoreAncestry () {
            return panel.cbIgnoreAncestry.isSelected();
        }

    }               

    @NbBundle.Messages({
        "CTL_Merge_ReintegrateBranch=Reintegrate Feature Branch",
        "CTL_Merge_ReintegrateBranchDesc=Merge all changes from a feature branch not yet present in the working copy."
    })
    private class MergeBranchType extends MergeType {

        private final MergeBranchPanel panel;
        private final RepositoryPaths mergeEndRepositoryPaths;
        private final ReintegrateBranchPreviewPanel previewPanel;

        public MergeBranchType (RepositoryFile repositoryRoot, File root) {
            super(repositoryRoot);
            
            panel = new MergeBranchPanel();
            previewPanel = new ReintegrateBranchPreviewPanel();

            mergeEndRepositoryPaths =
                new RepositoryPaths(
                    repositoryRoot,
                    (JTextComponent) panel.mergeEndUrlComboBox.getEditor().getEditorComponent(),
                    panel.mergeEndBrowseButton,
                    null,
                    null
                );      
            
            init(null, null, mergeEndRepositoryPaths, panel.mergeEndRepositoryFolderLabel, root);
            previewPanel.localFolderTextField.setText(root.getAbsolutePath());
            ((JTextComponent) panel.mergeEndUrlComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);                     
        }

        @Override
        public String getDisplayName() {
            return Bundle.CTL_Merge_ReintegrateBranch();
        }

        @Override
        public String getDescription() {
            return Bundle.CTL_Merge_ReintegrateBranchDesc();
        }

        @Override
        public JPanel getFieldsPanel() {
            return panel;
        }

        @Override
        public JPanel getPreviewPanel() {
            return previewPanel;
        }

        @Override
        public RepositoryFile getMergeStartRepositoryFile() {
            return null;
        }

        @Override
        public SVNRevision getMergeStartRevision() {
            return null;
        }

        public SVNUrl getMergeEndUrl() {
            try {
                return mergeEndRepositoryPaths.getRepositoryFiles()[0].getFileUrl();
            } catch (MalformedURLException ex) {
                // should be already checked and
                // not happen at this place anymore
                Subversion.LOG.log(Level.INFO, null, ex);
            }
            return null;
        }

        @Override
        public SVNRevision getMergeEndRevision() {
            // keep null, because actually whole branch is integrated, revision makes no difference
            return null;
        }

        @Override
        RepositoryPaths getMergeStartRepositoryPath() {
            return null;
        }

        @Override
        RepositoryPaths getMergeEndRepositoryPath() {
            return mergeEndRepositoryPaths;
        }

        @Override
        public JComboBox getStartUrlComboBox() {
            return null;
        }

        @Override
        public JComboBox getEndUrlComboBox() {
            return panel.mergeEndUrlComboBox;
        }

        @Override
        protected void setPreviewLabels() {            
            previewPanel.repositoryFolderTextField.setText(getRepositoryFile().getRepositoryUrl() + "/" + panel.mergeEndUrlComboBox.getEditor().getItem().toString()); // NOI18N
        }

        @Override
        public boolean isIgnoreAncestry () {
            return false;
        }

    }
}

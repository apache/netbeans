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
package org.netbeans.modules.web.jsf.wizards;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.wizards.BrowseFolders;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

// XXX I18N
/**
 *
 * @author alexeybutenko
 */
public class CompositeComponentVisualPanel extends javax.swing.JPanel implements ActionListener, DocumentListener {

    private static final Logger LOG = Logger.getLogger(CompositeComponentVisualPanel.class.getName());
    private Project project;
    private SourceGroup[] folders;
    private final WebModule wm;
    private static final String RESOURCES_FOLDER = "resources"; //NOI18N
    private static final String COMPONENT_FOLDER = "ezcomp";    //NOI18N
    private String expectedExtension;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final ListCellRenderer CELL_RENDERER = new GroupCellRenderer();
    private final Pattern FOLDER_NAME_PATTERN = Pattern.compile(".*[\\\\/](.*)");//NOI18N
    private boolean indirectModification, prefixLocked;
    private static final String COMPOSITE_LIBRARY_NS = "http://java.sun.com/jsf/composite"; //NOI18N

    public CompositeComponentVisualPanel(Project project, SourceGroup[] folders, String selectedText) {
        this.project = project;
        this.folders = folders;
        this.wm = WebModule.getWebModule(project.getProjectDirectory());
        initComponents();
        locationCB.setRenderer(CELL_RENDERER);

        if (selectedText != null) {
            try {
                EditorKit kit = MimeLookup.getLookup(MimePath.parse("text/xhtml")).lookup(EditorKit.class); //NOI18N
                Document doc = kit.createDefaultDocument();
                doc.insertString(0, selectedText, null);
                selectedTextPane.setEditorKit(kit);
                selectedTextPane.setDocument(doc);

            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            //disabled the implementation section panel
            selectedTextPane.setEnabled(false);
            implSectionLabel.setEnabled(false);
        }
        super.validate();

        initValues(null, null, null, false);

        browseButton.addActionListener(this);
        locationCB.addActionListener(this);
        documentNameTextField.getDocument().addDocumentListener(this);
        folderTextField.getDocument().addDocumentListener(this);
        prefixTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                prefixTextFieldModified();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                prefixTextFieldModified();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                prefixTextFieldModified();
            }
        });
    }

    void initValues(FileObject template, FileObject preselectedFolder, String documentName, boolean fromEditor) {
        assert project != null;

        //disable the prefix field and label when is not wizard invoked from editor
        prefixLabel.setEnabled(fromEditor);
        prefixTextField.setEnabled(fromEditor);
        prefixLocked = !fromEditor; //prevent the prefix computation if the prefix field is disabled

        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());

        locationCB.setModel(new DefaultComboBoxModel(folders));
        // Guess the group we want to create the file in
        SourceGroup preselectedGroup = getPreselectedGroup(folders, preselectedFolder);
        // Create OS dependent relative name
        if (preselectedGroup != null) {
            String componentPath = (wm != null) ? CompositeComponentWizardPanel.getResourceFolderPath(wm) + File.separatorChar + COMPONENT_FOLDER : RESOURCES_FOLDER + File.separatorChar + COMPONENT_FOLDER;
            locationCB.setSelectedItem(preselectedGroup);
            if (preselectedFolder != null && preselectedFolder.getName().endsWith(RESOURCES_FOLDER)) {
                folderTextField.setText(getRelativeNativeName(preselectedGroup.getRootFolder(), preselectedFolder) + File.separatorChar + COMPONENT_FOLDER);
            } else {
                folderTextField.setText(componentPath);
            }
        }

        String ext = template == null ? "" : template.getExt(); // NOI18N
        expectedExtension = ext.length() == 0 ? "" : "." + ext; // NOI18N

        String displayName = null;
        try {
            if (template != null) {
                DataObject templateDo = DataObject.find(template);
                displayName = templateDo.getNodeDelegate().getDisplayName();
            }
        } catch (DataObjectNotFoundException ex) {
            displayName = template.getName();
        }
        putClientProperty("NewFileWizard_Title", displayName);// NOI18N
        putClientProperty(TemplateWizard.PROP_CONTENT_DATA, new String[]{NbBundle.getMessage(CompositeComponentWizardPanel.class, "LBL_SimpleTargetChooserPanel_Name")}); // NOI18N);
        if (template != null) {
            final String baseName = template.getName();
            if (documentName == null) {
                documentName = baseName;
            }
            if (preselectedFolder != null) {
                int index = 0;
                while (true) {
                    FileObject _tmp = preselectedFolder.getFileObject(documentName, template.getExt());
                    if (_tmp == null) {
                        break;
                    }
                    documentName = baseName + ++index;
                }
            }

            documentNameTextField.setText(documentName);
            documentNameTextField.selectAll();
        }
    }

    private SourceGroup getPreselectedGroup(SourceGroup[] groups, FileObject folder) {
        for (int i = 0; folder != null && i < groups.length; i++) {
            if (FileUtil.isParentOf(groups[i].getRootFolder(), folder)
                    || groups[i].getRootFolder().equals(folder)) {
                return groups[i];
            }
        }
        if (groups.length > 0) {
            return groups[0];
        }
        return null;
    }

    private String getRelativeNativeName(FileObject root, FileObject folder) {
        if (root == null) {
            throw new NullPointerException("null root passed to getRelativeNativeName"); // NOI18N
        }

        String path;

        if (folder == null) {
            path = ""; // NOI18N
        } else {
            path = FileUtil.getRelativePath(root, folder);
        }

        return path == null ? "" : path.replace('/', File.separatorChar); // NOI18N
    }

    public SourceGroup getTargetGroup() {
        return (SourceGroup) locationCB.getSelectedItem();
    }

    public String getCompositeComponentURI() {
        String folder = getTargetFolder();
        String resfslash = RESOURCES_FOLDER + "/";//NOI18N
        if (folder.startsWith(resfslash)) {
            return COMPOSITE_LIBRARY_NS + "/" + folder.substring(resfslash.length()); //NOI18N //copied from JsfUtils from web.jsf.editor module
        } else {
            return null; //messed, must start with resources/
        }
    }

    public String getTargetFolder() {
        String folderName = folderTextField.getText().trim();

        if (folderName.length() == 0) {
            return "";
        } else {
            return folderName.replace(File.separatorChar, '/'); // NOI18N
        }
    }

    public String getTargetName() {

        String text = documentNameTextField.getText().trim();

        if (text.length() == 0) {
            return "";
        } else {
            return text;
        }
    }

    public String getPrefix() {
        try {
            Document doc = prefixTextField.getDocument();
            return doc.getText(0, doc.getLength()).trim();
        } catch (BadLocationException ex) {
            //ignore
            return "";
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        documentNameTextField = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationCB = new javax.swing.JComboBox();
        folderLabel = new javax.swing.JLabel();
        folderTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        pathLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        targetSeparator = new javax.swing.JSeparator();
        customPanel = new javax.swing.JPanel();
        implSectionLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        selectedTextPane = new javax.swing.JEditorPane();
        fillerPanel = new javax.swing.JPanel();
        prefixLabel = new javax.swing.JLabel();
        prefixTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "A11Y_FileName_mnem").charAt(0));
        nameLabel.setLabelFor(documentNameTextField);
        nameLabel.setText(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "LBL_JspName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(nameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(documentNameTextField, gridBagConstraints);
        documentNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "A11Y_DESC_FileName")); // NOI18N

        projectLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "A11Y_Project_mnem").charAt(0));
        projectLabel.setLabelFor(projectTextField);
        projectLabel.setText(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "LBL_Project")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(projectLabel, gridBagConstraints);

        projectTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(projectTextField, gridBagConstraints);
        projectTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "A11Y_DESC_Project")); // NOI18N

        locationLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "A11Y_Location_mnem").charAt(0));
        locationLabel.setLabelFor(locationCB);
        locationLabel.setText(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "LBL_Location")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(locationLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(locationCB, gridBagConstraints);
        locationCB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "A11Y_DESC_Location")); // NOI18N

        folderLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "A11Y_Folder_mnem").charAt(0));
        folderLabel.setLabelFor(folderTextField);
        folderLabel.setText(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "LBL_Folder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(folderLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(folderTextField, gridBagConstraints);
        folderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "A11Y_DESC_Folder")); // NOI18N

        browseButton.setMnemonic(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "LBL_Browse_Mnemonic").charAt(0));
        browseButton.setText(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "LBL_Browse")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "ACSD_Browse")); // NOI18N

        pathLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "A11Y_CreatedFile_mnem").charAt(0));
        pathLabel.setLabelFor(fileTextField);
        pathLabel.setText(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "LBL_CreatedFile")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(pathLabel, gridBagConstraints);

        fileTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(fileTextField, gridBagConstraints);
        fileTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "A11Y_DESC_CreatedFile")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(targetSeparator, gridBagConstraints);

        customPanel.setPreferredSize(new java.awt.Dimension(400, 180));
        customPanel.setLayout(new java.awt.GridBagLayout());

        implSectionLabel.setText(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "LBL_IMPLEMENTATION")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        customPanel.add(implSectionLabel, gridBagConstraints);

        selectedTextPane.setEditable(false);
        selectedTextPane.setEnabled(false);
        selectedTextPane.setPreferredSize(null);
        jScrollPane1.setViewportView(selectedTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        customPanel.add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(customPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        add(fillerPanel, gridBagConstraints);

        prefixLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("A11Y_Prefix_mnem").charAt(0));
        prefixLabel.setText(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "LBL_Prefix")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(prefixLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(prefixTextField, gridBagConstraints);
        prefixTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CompositeComponentVisualPanel.class, "A11Y_Library_Prefix")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JPanel customPanel;
    private javax.swing.JTextField documentNameTextField;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JLabel folderLabel;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JLabel implSectionLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox locationCB;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JLabel prefixLabel;
    private javax.swing.JTextField prefixTextField;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JEditorPane selectedTextPane;
    private javax.swing.JSeparator targetSeparator;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (browseButton == e.getSource()) {
            FileObject fo = null;
            // Show the browse dialog

            SourceGroup group = (SourceGroup) locationCB.getSelectedItem();
            if (group == null) { // #161478
                return;
            }

            fo = BrowseFolders.showDialog(new SourceGroup[]{group}, org.openide.loaders.DataFolder.class,
                    folderTextField.getText().replace(File.separatorChar, '/')); // NOI18N

            if (fo != null && fo.isFolder()) {
                String relPath = FileUtil.getRelativePath(group.getRootFolder(), fo);
                folderTextField.setText(relPath.replace('/', File.separatorChar)); // NOI18N
            }
        } else if (locationCB == e.getSource()) {
            updateCreatedFolder();
        }
    }

    private void updateCreatedFolder() {
        SourceGroup sg = (SourceGroup) locationCB.getSelectedItem();
        if (sg == null) {
            return;
        }
        FileObject root = sg.getRootFolder();
        if (root == null) {
            return;
        }

        String folderName = folderTextField.getText().trim();
        String documentName = documentNameTextField.getText().trim();

        String createdFileName = FileUtil.getFileDisplayName(root)
                + (folderName.startsWith("/") || folderName.startsWith(File.separator) ? "" : "/") + // NOI18N
                folderName
                + (folderName.endsWith("/") || folderName.endsWith(File.separator) || folderName.length() == 0 ? "" : "/") + // NOI18N
                documentName + expectedExtension;

        fileTextField.setText(createdFileName.replace('/', File.separatorChar)); // NOI18N

        try {
            indirectModification = false; //uff, ugly, just hacking the existing code...
            updatePrefix();
        } finally {
            indirectModification = true;
        }

        changeSupport.fireChange();
    }

    private void prefixTextFieldModified() {
        if (indirectModification) {
            prefixLocked = true;
        }
        changeSupport.fireChange();
    }

    private void updatePrefix() {
        if (!prefixLocked) {
            //compute the library prefix according to the folder
            Matcher matcher = FOLDER_NAME_PATTERN.matcher(folderTextField.getText());
            if (matcher.matches() && matcher.groupCount() == 1) {
                String lastFolderName = matcher.group(1); //first group
                prefixTextField.setText(lastFolderName.substring(0, lastFolderName.length() < 2 ? lastFolderName.length() : 2));
            }
        }
    }

    // DocumentListener implementation -----------------------------------------
    @Override
    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        updateCreatedFolder();
    }

    @Override
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        updateCreatedFolder();
    }

    @Override
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        updateCreatedFolder();
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public String getCreatedFilePath() {
        return fileTextField.getText();
    }

    // Rendering of the location combo box -------------------------------------
    private class GroupCellRenderer extends JLabel implements ListCellRenderer {

        public GroupCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof SourceGroup) {
                SourceGroup group = (SourceGroup) value;
                String projectDisplayName = ProjectUtils.getInformation(project).getDisplayName();
                String groupDisplayName = group.getDisplayName();
                if (projectDisplayName.equals(groupDisplayName)) {
                    setText(groupDisplayName);
                } else {
                    setText(MessageFormat.format("{1} - {0}", //NOI18N
                            new Object[]{groupDisplayName, projectDisplayName, group.getRootFolder().getName()}));
                }

                setIcon(group.getIcon(false));
            } else {
                setText(value.toString());
                setIcon(null);
            }
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());

            }
            return this;
        }
    }
}

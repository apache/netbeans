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

package org.netbeans.modules.java.project.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.project.ui.JavaTargetChooserPanel;
import org.netbeans.modules.java.project.ui.NewJavaFileWizardIterator.Type;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.awt.Mnemonics;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Permits user to select a package to place a Java class (or other resource) into.
 * @author Petr Hrebejk, Jesse Glick
 */
public class JavaTargetChooserPanelGUI extends javax.swing.JPanel implements ActionListener, DocumentListener {
  
    private static final String DEFAULT_NEW_PACKAGE_NAME = 
        NbBundle.getMessage( JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_DefaultNewPackageName" ); // NOI18N
    private static final String NEW_CLASS_PREFIX = 
        NbBundle.getMessage( JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_NewJavaClassPrefix" ); // NOI18N
    
    /** preferred dimension of the panel */
    private static final Dimension PREF_DIM = new Dimension(500, 340);
    
    private final Project project;
    private final SourceGroup groups[];
    private final List<ChangeListener> listeners = new ArrayList<>();
    private final Type type;
    private final ThreadLocal<Boolean> ignoreChange;

    private String expectedExtension;
    private boolean ignoreRootCombo;
    private boolean wasPreviouslyFQN = false;
    private String originalPackageName;
    
    /** Creates new form SimpleTargetChooserGUI */
    public JavaTargetChooserPanelGUI(Project p, SourceGroup[] groups, Component bottomPanel, Type type) {
        this.type = type;
        this.project = p;
        this.groups = groups;
        this.ignoreChange = new ThreadLocal<>();
        for (SourceGroup sourceGroup : groups)
            if (sourceGroup == null)
                throw new NullPointerException ();
        
        initComponents();        
                
        if (type == Type.PACKAGE) {
            packageComboBox.setVisible( false );
            packageLabel.setVisible( false );
            Mnemonics.setLocalizedText (fileLabel, NbBundle.getMessage (JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_CreatedFolder_Label")); // NOI18N
            Mnemonics.setLocalizedText (documentNameLabel, NbBundle.getMessage (JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_PackageName_Label")); // NOI18N
            documentNameTextField.getDocument().addDocumentListener( this );
        } else if (type == Type.PKG_INFO) {
            documentNameTextField.setEditable (false);
        } else if (type == Type.MODULE_INFO) {
            documentNameTextField.setEditable (false);
            packageLabel.setVisible(false);
            packageComboBox.setVisible(false);
        }
        else {
            packageComboBox.getEditor().addActionListener( this );
            documentNameTextField.getDocument().addDocumentListener( this );
        }
        
                
        if ( bottomPanel != null ) {
            bottomPanelContainer.add( bottomPanel, java.awt.BorderLayout.CENTER );
        }
                
        //initValues( project, null, null );
        

        // Not very nice
        Component packageEditor = packageComboBox.getEditor().getEditorComponent();
        if ( packageEditor instanceof javax.swing.JTextField ) {
            ((javax.swing.JTextField)packageEditor).getDocument().addDocumentListener( this );
        }
        else {
            packageComboBox.addActionListener( this );
        }
        
        rootComboBox.setRenderer(new GroupListCellRenderer());        
        packageComboBox.setRenderer(PackageView.listRenderer());
        rootComboBox.addActionListener( this );
        
        setPreferredSize( PREF_DIM );
        setName( NbBundle.getBundle (JavaTargetChooserPanelGUI.class).getString ("LBL_JavaTargetChooserPanelGUI_Name") ); // NOI18N
    }
            
    @Override
    public void addNotify () {
        Dimension panel2Size = this.jPanel2.getPreferredSize();
        Dimension bottomPanelSize = this.bottomPanelContainer.getPreferredSize ();
        Dimension splitterSize = this.targetSeparator.getPreferredSize();        
        int vmax = panel2Size.height + bottomPanelSize.height + splitterSize.height + 12;   //Insets=12
        //Update only height, keep the wizard width
        if (vmax > PREF_DIM.height) {
            this.setPreferredSize (new Dimension (PREF_DIM.width,vmax));
        }
        super.addNotify();
    }
    
    public void initValues( FileObject template, FileObject preselectedFolder ) {
        assert project != null : "Project must be specified."; // NOI18N
        // Show name of the project
        projectTextField.setText( ProjectUtils.getInformation(project).getDisplayName() );
        assert template != null;
        
        String displayName;
        try {
            DataObject templateDo = DataObject.find (template);
            displayName = templateDo.getNodeDelegate ().getDisplayName ();
        } catch (DataObjectNotFoundException ex) {
            displayName = template.getName ();
        }
        
        putClientProperty ("NewFileWizard_Title", displayName);// NOI18N        
        // Setup comboboxes 
        rootComboBox.setModel(new DefaultComboBoxModel(groups));
        SourceGroup preselectedGroup = getPreselectedGroup( preselectedFolder );
        ignoreRootCombo = true;
        rootComboBox.setSelectedItem( preselectedGroup );                       
        ignoreRootCombo = false;
        final Object preselectedPackage = getPreselectedPackage(preselectedGroup, preselectedFolder);
        if (type == Type.PACKAGE) {
            final String baseName = DEFAULT_NEW_PACKAGE_NAME;
            String activeName = baseName;
            if (preselectedFolder != null) {
                int index = 0;
                while (true) {
                    FileObject _tmp = preselectedFolder.getFileObject(activeName, null);
                    if (_tmp == null) {
                        break;
                    }
                    activeName = baseName + ++index;
                }
            }
            String docName = preselectedPackage == null || preselectedPackage.toString().length() == 0 ? 
                activeName : 
                preselectedPackage.toString() + "." + activeName;

            documentNameTextField.setText( docName );                    
            int docNameLen = docName.length();
            int defPackageNameLen = activeName.length();

            documentNameTextField.setSelectionEnd( docNameLen - 1 );
            documentNameTextField.setSelectionStart( docNameLen - defPackageNameLen );                
        } else {
            if (preselectedPackage != null) {
                // packageComboBox.setSelectedItem( preselectedPackage );
                packageComboBox.getEditor().setItem( preselectedPackage );
            }
            if (template != null) {
            	if ( documentNameTextField.getText().trim().length() == 0 ) { // To preserve the class name on back in the wiazard
                    if (type == Type.PKG_INFO) {
                        documentNameTextField.setText (template.getName ());
                    }
                    else if (type == Type.MODULE_INFO) {
                        documentNameTextField.setText (template.getName ());
                    }
                    else {
                        //Ordinary file
                        final String baseName = NEW_CLASS_PREFIX + template.getName ();
                        String activeName = baseName;
                        if (preselectedFolder != null) {
                            int index = 0;                            
                            while (true) {
                                FileObject _tmp = preselectedFolder.getFileObject(activeName, template.getExt());    //NOI18N
                                if (_tmp == null) {
                                    break;
                                }
                                activeName = baseName + ++index;
                            }
                        }
                        documentNameTextField.setText (activeName);
                        documentNameTextField.selectAll ();
                    }
                }
            }
            updatePackages();
        }
        // Determine the extension
        String ext = template == null ? "" : template.getExt(); // NOI18N
        expectedExtension = ext.length() == 0 ? "" : "." + ext; // NOI18N
        
        updateText();
        
    }
        
    public FileObject getRootFolder() {
        final Object selectedItem  = rootComboBox.getSelectedItem();
        return (selectedItem instanceof SourceGroup) ? ((SourceGroup)selectedItem).getRootFolder() : null;
    }
    
    public String getPackageFileName() {
        
        if (type == Type.PACKAGE) {
            return ""; // NOI18N
        }
        
        String packageName = packageComboBox.getEditor().getItem().toString();        
        return  packageName.replace( '.', '/' ); // NOI18N        
    }
    
    /**
     * Name of selected package, or "" for default package.
     */
    String getPackageName() {
        if (type == Type.PACKAGE) {
            return ""; // NOI18N
        }
        return packageComboBox.getEditor().getItem().toString();
    }    
    
    public String getTargetName() {
        String text;
        final String rawName = documentNameTextField.getText().trim();
        if (type == Type.PACKAGE) {
            text = rawName;
        } else {
            final String[] pkgNamePair = JavaTargetChooserPanel.getPackageAndSimpleName(rawName);
            if (pkgNamePair[0].length() == 0) {
                text = rawName;
            } else {
                text = pkgNamePair[1];
            }
        }
        if ( text.length() == 0 ) {
            return null;
        } else {
            return text;
        }
    }
    
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(e);
        }
    }
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        targetSeparator = new javax.swing.JSeparator();
        bottomPanelContainer = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        documentNameLabel = new javax.swing.JLabel();
        documentNameTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        rootComboBox = new javax.swing.JComboBox();
        packageLabel = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        fileLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(targetSeparator, gridBagConstraints);

        bottomPanelContainer.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(bottomPanelContainer, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        documentNameLabel.setLabelFor(documentNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(documentNameLabel, org.openide.util.NbBundle.getMessage(JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_ClassName_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(documentNameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(documentNameTextField, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/java/project/ui/Bundle"); // NOI18N
        documentNameTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_documentNameTextField")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 24, 0);
        jPanel2.add(jPanel1, gridBagConstraints);

        jLabel5.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_jLabel5")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanel2.add(jLabel5, gridBagConstraints);

        projectTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        jPanel2.add(projectTextField, gridBagConstraints);
        projectTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_projectTextField")); // NOI18N

        jLabel1.setLabelFor(rootComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_jLabel1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanel2.add(jLabel1, gridBagConstraints);

        rootComboBox.setMinimumSize(new java.awt.Dimension(154, 27));
        rootComboBox.setPreferredSize(new java.awt.Dimension(154, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        jPanel2.add(rootComboBox, gridBagConstraints);
        rootComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_rootComboBox")); // NOI18N

        packageLabel.setLabelFor(packageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(packageLabel, org.openide.util.NbBundle.getMessage(JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_jLabel2")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanel2.add(packageLabel, gridBagConstraints);

        packageComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        jPanel2.add(packageComboBox, gridBagConstraints);
        packageComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_packageComboBox")); // NOI18N

        fileLabel.setLabelFor(fileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fileLabel, org.openide.util.NbBundle.getMessage(JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_CreatedFile_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 12, 0);
        jPanel2.add(fileLabel, gridBagConstraints);

        fileTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 12, 0);
        jPanel2.add(fileTextField, gridBagConstraints);
        fileTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_fileTextField")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(jPanel2, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(bundle.getString("AD_JavaTargetChooserPanelGUI")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanelContainer;
    private javax.swing.JLabel documentNameLabel;
    private javax.swing.JTextField documentNameTextField;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JComboBox rootComboBox;
    private javax.swing.JSeparator targetSeparator;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
        
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (ignoreChange.get() == Boolean.TRUE) {
            return;
        }
        if ( rootComboBox == e.getSource() ) {            
            if (!ignoreRootCombo && type != Type.PACKAGE) {
                updatePackages();
            }
            updateText();
            fireChange();
        }
        else if ( packageComboBox == e.getSource() ) {
            updateText();
            fireChange();
        }
        else if ( packageComboBox.getEditor()  == e.getSource() ) {
            updateText();
            fireChange();
        }
    }    
    
    // DocumentListener implementation -----------------------------------------
    
    @Override
    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        updateText();
        fireChange();        
    }    
    
    @Override
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        changedUpdate( e );
    }
    
    @Override
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        changedUpdate( e );
    }
    
    // Private methods ---------------------------------------------------------
        
    private RequestProcessor.Task updatePackagesTask = null;
    
    private static final ComboBoxModel WAIT_MODEL = new DefaultComboBoxModel( 
        new String[] {
            NbBundle.getMessage( JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_PackageName_PleaseWait" ) // NOI18N
        } 
    ); 
    
    private void updatePackages() {
        final Object item = rootComboBox.getSelectedItem();
        if (! (item instanceof SourceGroup)) {
            return;
        }
        WAIT_MODEL.setSelectedItem( packageComboBox.getEditor().getItem() );
        packageComboBox.setModel( WAIT_MODEL );
        
        if ( updatePackagesTask != null ) {
            updatePackagesTask.cancel();
        }
        
        updatePackagesTask = new RequestProcessor( "ComboUpdatePackages" ).post(new Runnable() {                               
            @Override
            public void run() {
                final ComboBoxModel model = PackageView.createListView((SourceGroup)item);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        model.setSelectedItem(packageComboBox.getEditor().getItem());
                        packageComboBox.setModel( model );
                    }
                });
            }
        });                
    }
            
    private void updateText() {
        final Object selectedItem =  rootComboBox.getSelectedItem();
        String createdFileName;
        if (selectedItem instanceof SourceGroup) {
            SourceGroup g = (SourceGroup) selectedItem;
            FileObject rootFolder = g.getRootFolder();
            String packageName = getPackageFileName();
            String documentName = documentNameTextField.getText().trim();
            if (Type.FILE.equals(type)) {
                final String[] pkgNamePair = JavaTargetChooserPanel.getPackageAndSimpleName(documentName);
                final boolean fqn = !pkgNamePair[0].isEmpty();
                if (fqn) {
                    if (!wasPreviouslyFQN) {
                        //backup the original package name
                        originalPackageName = getPackageName();
                    }
                    //set the textfield from the parsed FQN text
                    packageName = pkgNamePair[0];
                    documentName = pkgNamePair[1];
                    setPackageIgnoreEvents(packageName);
                    packageName = packageName.replace('.', '/');    //NOI18N
                    wasPreviouslyFQN = true;
                } else {
                    if (wasPreviouslyFQN) {
                        //reset the package name, if the user reverts his previously entered FQN
                        setPackageIgnoreEvents(originalPackageName);
                        wasPreviouslyFQN = false;
                    }
                }
                packageComboBox.setEnabled(!fqn);
            }
            if (type == Type.PACKAGE) {
                documentName = documentName.replace( '.', '/' ); // NOI18N
            }
            else if ( documentName.length() > 0 ) {
                documentName = documentName + expectedExtension;
            }
            createdFileName = FileUtil.getFileDisplayName( rootFolder ) +
                ( packageName.startsWith("/") || packageName.startsWith( File.separator ) ? "" : "/" ) + // NOI18N
                packageName +
                ( packageName.endsWith("/") || packageName.endsWith( File.separator ) || packageName.length() == 0 ? "" : "/" ) + // NOI18N
                documentName;
        } else {
            //May be null iff nothing selected
            createdFileName = "";   //NOI18N
        }
        fileTextField.setText( createdFileName.replace( '/', File.separatorChar ) ); // NOI18N
    }
    
    private SourceGroup getPreselectedGroup(FileObject folder) {
        for(int i = 0; folder != null && i < groups.length; i++) {
            FileObject root = groups[i].getRootFolder();
            if (root.equals(folder) || FileUtil.isParentOf(root, folder)) {
                return groups[i];
            }
        }
        return groups[0];
    }
    
    /**
     * Get a package combo model item for the package the user selected before opening the wizard.
     * May return null if it cannot find it; or a String instance if there is a well-defined
     * package but it is not listed among the packages shown in the list model.
     */
    @CheckForNull
    private Object getPreselectedPackage(
            @NonNull final SourceGroup group,
            @NullAllowed final FileObject folder) {
        if ( folder == null ) {
            return null;
        }
        FileObject root = group.getRootFolder();
        String relPath = FileUtil.getRelativePath( root, folder );
        if ( relPath == null ) {
            // Group Root folder is no a parent of the preselected folder
            // No package should be selected
            return null;
        }
        if (type == Type.MODULE_INFO) {
            return "";
        }
        // Find the right item.
        final String name = relPath.replace('/', '.');
        /*
        int max = model.getSize();
        for (int i = 0; i < max; i++) {
            Object item = model.getElementAt(i);
            if (item.toString().equals(name)) {
                return item;
            }
        }
         */
        // Didn't find it.
        // #49954: should nonetheless show something in the combo box.
        return name;
    }

    private void setPackageIgnoreEvents(@NonNull final String text) {
        ignoreChange.set(true);
        try {
            packageComboBox.setSelectedItem(text);
        } finally {
            ignoreChange.remove();
        }
    }
    
    // Private innerclasses ----------------------------------------------------

    /**
     * Displays a {@link SourceGroup} in {@link #rootComboBox}.
     */
    private static final class GroupListCellRenderer extends DefaultListCellRenderer/*<SourceGroup>*/ {
        
        public GroupListCellRenderer() {}
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String name;
            Icon icon;
            if (value == null) {
                name = ""; //NOI18N
                icon = null;
            }
            else {
                assert value instanceof SourceGroup;
                SourceGroup g = (SourceGroup) value;
                name = g.getDisplayName();
                icon = g.getIcon(false);
            }
            super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
            setIcon(icon);
            return this;
        }
        
    }
    
}

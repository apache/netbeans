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

package org.netbeans.modules.java.api.common.project.ui.customizer;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.text.MessageFormat;
import javax.swing.CellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.api.common.ModuleRoots;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.impl.RootsAccessor;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Handles adding, removing, reordering of source roots.
 *
 * @author Tomas Zezula
 * @since org.netbeans.modules.java.api.common/1 1.5
 */
public final class SourceRootsUi {
    
    private SourceRootsUi() {
    }
  
    public static DefaultTableModel createModel( SourceRoots roots ) {
        
        String[] rootLabels = roots.getRootNames();
        String[] rootProps = roots.getRootProperties();
        URL[] rootURLs = roots.getRootURLs(false);
        Object[][] data = new Object[rootURLs.length] [2];
        for (int i=0; i< rootURLs.length; i++) {
            data[i][0] = Utilities.toFile(URI.create (rootURLs[i].toExternalForm()));
            data[i][1] = roots.getRootDisplayName(rootLabels[i], rootProps[i]);
        }
        return new SourceRootsModel(data);
                
    }
    
    public static DefaultTableModel createModel( ModuleRoots roots ) {
        
        URL[] rootURLs = roots.getRootURLs(false);
        String[] rootPaths = roots.getRootPathProperties();
        Object[][] data = new Object[rootURLs.length] [2];
        for (int i = 0; i < rootURLs.length; i++) {
            data[i][0] = Utilities.toFile(URI.create (rootURLs[i].toExternalForm()));
            data[i][1] = roots.getRootPath(rootPaths[i]);
        }
        return new SourceRootsModel(data);
 
    }
    
    public static EditMediator registerEditMediator( Project master,
                                             SourceRoots sourceRoots,
                                             JTable rootsList,
                                             JButton addFolderButton,
                                             JButton removeButton,
                                             JButton upButton,
                                             JButton downButton,
                                             CellEditor rootsListEditor,
                                             boolean emptyTableIsValid) {
        
        EditMediator em = new EditMediator( master,
                                            sourceRoots,
                                            rootsList,
                                            addFolderButton,
                                            removeButton,
                                            upButton,
                                            downButton,
                                            emptyTableIsValid);
        
        // Register the listeners        
        // On all buttons
        addFolderButton.addActionListener( em ); 
        removeButton.addActionListener( em );
        upButton.addActionListener( em );
        downButton.addActionListener( em );
        // On list selection
        rootsList.getSelectionModel().addListSelectionListener( em );
        DefaultCellEditor editor = (DefaultCellEditor) rootsListEditor;
        if (editor == null) {
            editor = new DefaultCellEditor(new JTextField());
        }
        editor.addCellEditorListener (em);
        rootsList.setDefaultRenderer( File.class, new FileRenderer (FileUtil.toFile(master.getProjectDirectory())));
        rootsList.setDefaultEditor(String.class, editor);
        // Set the initial state of the buttons
        em.valueChanged( null );
        
        DefaultTableModel model = (DefaultTableModel)rootsList.getModel();
        String type = RootsAccessor.getInstance().getType(sourceRoots);
        boolean isModule = JavaProjectConstants.SOURCES_TYPE_MODULES.equals(type);
        String[] columnNames = new String[2];
        columnNames[0]  = NbBundle.getMessage( SourceRootsUi.class, isModule ? "CTL_ModuleFolders" : "CTL_PackageFolders");
        columnNames[1]  = NbBundle.getMessage( SourceRootsUi.class, isModule ? "CTL_ModulePaths" : "CTL_PackageLabels");
        model.setColumnIdentifiers(columnNames);
        rootsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        return em;
    }
    
    public static EditMediator registerEditMediator( Project master,
                                             SourceRoots sourceRoots,
                                             JTable rootsList,
                                             JButton addFolderButton,
                                             JButton removeButton,
                                             JButton upButton,
                                             JButton downButton ) {
        return registerEditMediator(master, sourceRoots, rootsList, addFolderButton, 
                removeButton, upButton, downButton, null, true);
    }
    
    /**
     * Opens the standard dialog for warning an user about illegal source roots.
     * @param roots the set of illegal source/test roots
     */
    public static void showIllegalRootsDialog (Set/*<File>*/ roots) {
        JButton closeOption = new JButton (NbBundle.getMessage(SourceRootsUi.class,"CTL_SourceRootsUi_Close"));
        closeOption.getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(SourceRootsUi.class,"AD_SourceRootsUi_Close"));
        JPanel warning = new WarningDlg (roots);
        String message = NbBundle.getMessage(SourceRootsUi.class,"MSG_InvalidRoot");
        JOptionPane optionPane = new JOptionPane (new Object[] {message, warning},
            JOptionPane.WARNING_MESSAGE,
            0,
            null,
            new Object[0],
            null);
        optionPane.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(SourceRootsUi.class,"AD_InvalidRootDlg"));
        DialogDescriptor dd = new DialogDescriptor (optionPane,
            NbBundle.getMessage(SourceRootsUi.class,"TITLE_InvalidRoot"),
            true,
            new Object[] {
                closeOption,
            },
            closeOption,
            DialogDescriptor.DEFAULT_ALIGN,
            null,
            null);
        DialogDisplayer.getDefault().notify(dd);
    }

    // Private innerclasses ----------------------------------------------------

    public static class EditMediator implements ActionListener, ListSelectionListener, CellEditorListener {

        
        final JTable rootsList;
        final JButton addFolderButton;
        final JButton removeButton;
        final JButton upButton;
        final JButton downButton;
        private final Project project;
        private final SourceRoots sourceRoots;
        private final Set<File> ownedFolders;
        private DefaultTableModel rootsModel;
        private EditMediator relatedEditMediator;
        
        private boolean emptyTableIsValid;
        
        public EditMediator( Project master,
                             SourceRoots sourceRoots,
                             JTable rootsList,
                             JButton addFolderButton,
                             JButton removeButton,
                             JButton upButton,
                             JButton downButton,
                             boolean emptyTableIsValid) {
            
            this.emptyTableIsValid = emptyTableIsValid;

            if ( !( rootsList.getModel() instanceof DefaultTableModel ) ) {
                throw new IllegalArgumentException( "Jtable's model has to be of class DefaultTableModel" ); // NOI18N
            }
                    
            this.rootsList = rootsList;
            this.addFolderButton = addFolderButton;
            this.removeButton = removeButton;
            this.upButton = upButton;
            this.downButton = downButton;
            this.ownedFolders = new HashSet<File>();

            this.project = master;
            this.sourceRoots = sourceRoots;

            this.ownedFolders.clear();
            this.rootsModel = (DefaultTableModel)rootsList.getModel();
            Vector data = rootsModel.getDataVector();
            for (Iterator<Vector<File>> it = data.iterator(); it.hasNext();) {
                Vector row = (Vector) it.next ();
                File f = (File) row.elementAt(0);
                this.ownedFolders.add (f);
            }
        }
        
        public void setRelatedEditMediator(EditMediator rem) {
            this.relatedEditMediator = rem;
        }
        
        // Implementation of ActionListener ------------------------------------
        
        /** Handles button events
         */        
        public void actionPerformed( ActionEvent e ) {
            
            Object source = e.getSource();
            
            if ( source == addFolderButton ) { 
                
                // Let user search for the Jar file
                FileChooserBuilder builder = new FileChooserBuilder(SourceRootsUi.class).setDirectoriesOnly(true);
                builder.setDefaultWorkingDirectory(FileUtil.toFile(this.project.getProjectDirectory()));
                String type = RootsAccessor.getInstance().getType(sourceRoots);
                boolean isModule = JavaProjectConstants.SOURCES_TYPE_MODULES.equals(type);
                if (sourceRoots.isTest()) {
                    builder.setTitle(NbBundle.getMessage(SourceRootsUi.class, isModule ? "LBL_TestModuleFolder_DialogTitle" : "LBL_TestFolder_DialogTitle"));  // NOI18N
                } else {
                    builder.setTitle(NbBundle.getMessage(SourceRootsUi.class, isModule ? "LBL_ModuleFolder_DialogTitle" : "LBL_SourceFolder_DialogTitle")); // NOI18N
                }
                File files[] = builder.showMultiOpenDialog();
                if ( files != null ) {
                    addFolders( files );
                }
            }
            else if ( source == removeButton ) { 
                removeElements();
            }
            else if ( source == upButton ) {
                moveUp();
            }
            else if ( source == downButton ) {
                moveDown();
            }
        }
        
        // Selection listener implementation  ----------------------------------
        
        /** Handles changes in the selection
         */        
        public void valueChanged( ListSelectionEvent e ) {
            
            int[] si = rootsList.getSelectedRows();
            
            // addJar allways enabled
            
            // addLibrary allways enabled
            
            // addArtifact allways enabled
            
            // remove enabled only if selection is not empty
            boolean remove = si != null && si.length > 0;
            if (si != null && !emptyTableIsValid && remove) {
                remove = si.length < rootsList.getRowCount();
            }
            // and when the selection does not contain unremovable item

            // up button enabled if selection is not empty
            // and the first selected index is not the first row
            boolean up = si != null && si.length > 0 && si[0] != 0;
            
            // up button enabled if selection is not empty
            // and the laset selected index is not the last row
            boolean down = si != null && si.length > 0 && si[si.length-1] !=rootsList.getRowCount() - 1;

            removeButton.setEnabled( remove );
            upButton.setEnabled( up );
            downButton.setEnabled( down );       
        }

        public void editingCanceled(ChangeEvent e) {

        }

        public void editingStopped(ChangeEvent e) {
            // fireActionPerformed(); 
        }
        
        private void addFolders( File files[] ) {
            int[] si = rootsList.getSelectedRows();
            int lastIndex = si == null || si.length == 0 ? -1 : si[si.length - 1];
            ListSelectionModel selectionModel = this.rootsList.getSelectionModel();
            selectionModel.clearSelection();
            Set<File> rootsFromOtherProjects = new HashSet<File>();
            Set<File> rootsFromRelatedSourceRoots = new HashSet<File>();
            String type = RootsAccessor.getInstance().getType(sourceRoots);
            boolean isModule = JavaProjectConstants.SOURCES_TYPE_MODULES.equals(type);
out:        for( int i = 0; i < files.length; i++ ) {
                File normalizedFile = FileUtil.normalizeFile(files[i]);
                Project p;
                if (ownedFolders.contains(normalizedFile)) {
                    Vector dataVector = rootsModel.getDataVector();
                    for (int j=0; j<dataVector.size();j++) {
                        //Sequential search in this minor case is faster than update of positions during each modification
                        File f = (File )((Vector)dataVector.elementAt(j)).elementAt(0);
                        if (f.equals(normalizedFile)) {
                            selectionModel.addSelectionInterval(j,j);
                        }
                    }
                }
                else if (this.relatedEditMediator != null && this.relatedEditMediator.ownedFolders.contains(normalizedFile)) {
                    rootsFromRelatedSourceRoots.add (normalizedFile);
                    continue;
                }
                if ((p=FileOwnerQuery.getOwner(Utilities.toURI(normalizedFile)))!=null && !p.getProjectDirectory().equals(project.getProjectDirectory())) {
                    final Sources sources = p.getLookup().lookup(Sources.class);
                    if (sources == null) {
                        rootsFromOtherProjects.add (normalizedFile);
                        continue;
                    }
                    final SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
                    final SourceGroup[] javaGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                    final SourceGroup[] groups = new SourceGroup [sourceGroups.length + javaGroups.length];
                    System.arraycopy(sourceGroups,0,groups,0,sourceGroups.length);
                    System.arraycopy(javaGroups,0,groups,sourceGroups.length,javaGroups.length);
                    final FileObject projectDirectory = p.getProjectDirectory();
                    final FileObject fileObject = FileUtil.toFileObject(normalizedFile);
                    if (projectDirectory == null || fileObject == null) {
                        rootsFromOtherProjects.add (normalizedFile);
                        continue;
                    }
                    for (int j=0; j<groups.length; j++) {
                        final FileObject sgRoot = groups[j].getRootFolder();
                        if (fileObject.equals(sgRoot)) {
                            rootsFromOtherProjects.add (normalizedFile);
                            continue out;
                        }
                        if (!projectDirectory.equals(sgRoot) && FileUtil.isParentOf(sgRoot, fileObject)) {
                            rootsFromOtherProjects.add (normalizedFile);
                            continue out;
                        }
                    }
                }
                int current = lastIndex + 1 + i;
                rootsModel.insertRow( current, new Object[] {normalizedFile, isModule
                            ? ((ModuleRoots)sourceRoots).createInitialPath()
                            : sourceRoots.createInitialDisplayName(normalizedFile)});
                selectionModel.addSelectionInterval(current,current);
                this.ownedFolders.add (normalizedFile);
            }
            if (rootsFromOtherProjects.size() > 0 || rootsFromRelatedSourceRoots.size() > 0) {
                rootsFromOtherProjects.addAll(rootsFromRelatedSourceRoots);
                showIllegalRootsDialog (rootsFromOtherProjects);
            }
            // fireActionPerformed();
        }    

        private void removeElements() {

            int[] si = rootsList.getSelectedRows();

            if(  si == null || si.length == 0 ) {
                assert false : "Remove button should be disabled"; // NOI18N
            }

            // Remove the items
            for( int i = si.length - 1 ; i >= 0 ; i-- ) {
                this.ownedFolders.remove(rootsModel.getDataVector().elementAt(si[i]).elementAt(0));
                rootsModel.removeRow( si[i] );
            }


            if ( rootsModel.getRowCount() != 0) {
                // Select reasonable item
                int selectedIndex = si[si.length - 1] - si.length  + 1; 
                if ( selectedIndex > rootsModel.getRowCount() - 1) {
                    selectedIndex = rootsModel.getRowCount() - 1;
                }
                rootsList.setRowSelectionInterval( selectedIndex, selectedIndex );
            }

            // fireActionPerformed();

        }

        private void moveUp() {

            int[] si = rootsList.getSelectedRows();

            if(  si == null || si.length == 0 ) {
                assert false : "MoveUp button should be disabled"; // NOI18N
            }

            // Move the items up
            ListSelectionModel selectionModel = this.rootsList.getSelectionModel();
            selectionModel.clearSelection();
            for( int i = 0; i < si.length; i++ ) {
                Vector item = rootsModel.getDataVector().elementAt(si[i]);
                int newIndex = si[i]-1;
                rootsModel.removeRow( si[i] );
                rootsModel.insertRow( newIndex, item );
                selectionModel.addSelectionInterval(newIndex,newIndex);
            }
            // fireActionPerformed();
        } 

        private void moveDown() {

            int[] si = rootsList.getSelectedRows();

            if(  si == null || si.length == 0 ) {
                assert false : "MoveDown button should be disabled"; // NOI18N
            }

            // Move the items up
            ListSelectionModel selectionModel = this.rootsList.getSelectionModel();
            selectionModel.clearSelection();
            for( int i = si.length -1 ; i >= 0 ; i-- ) {
                Vector item = (Vector)rootsModel.getDataVector().elementAt(si[i]);
                int newIndex = si[i] + 1;
                rootsModel.removeRow( si[i] );
                rootsModel.insertRow( newIndex, item );
                selectionModel.addSelectionInterval(newIndex,newIndex);
            }
            // fireActionPerformed();
        }    
        

    }

    private static class SourceRootsModel extends DefaultTableModel {
        private static final long serialVersionUID = 139645020171023706L;

        public SourceRootsModel (Object[][] data) {
            super (data,new Object[]{"location","label"});//NOI18N
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return File.class;
                case 1:
                    return String.class;
                default:
                    return super.getColumnClass (columnIndex);
            }
        }
    }
    
    private static class FileRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 19773973042472054L;
        
        private File projectFolder;
        
        public FileRenderer (File projectFolder) {
            this.projectFolder = projectFolder;
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row, int column) {
            String displayName;
            if (value instanceof File) {
                File root = (File) value;
                String pfPath = projectFolder.getAbsolutePath() + File.separatorChar;
                String srPath = root.getAbsolutePath();            
                if (srPath.startsWith(pfPath)) {
                    displayName = srPath.substring(pfPath.length());
                }
                else {
                    displayName = srPath;
                }
            }
            else {
                displayName = null;
            }
            Component c = super.getTableCellRendererComponent(table, displayName, isSelected, hasFocus, row, column);
            if (c instanceof JComponent) {
                ((JComponent) c).setToolTipText (displayName);
            }
            return c;
        }                                
    }

    private static class WarningDlg extends JPanel {
        private static final long serialVersionUID = 178428385251850014L;

        public WarningDlg (Set invalidRoots) {            
            this.initGui (invalidRoots);
        }

        private void initGui (Set invalidRoots) {
            setLayout( new GridBagLayout ());                        
            JLabel label = new JLabel ();
            label.setText (NbBundle.getMessage(SourceRootsUi.class,"LBL_InvalidRoot"));
            label.setDisplayedMnemonic(NbBundle.getMessage(SourceRootsUi.class,"MNE_InvalidRoot").charAt(0));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 1.0;
            c.insets = new Insets (12,0,6,0);
            ((GridBagLayout)this.getLayout()).setConstraints(label,c);
            this.add (label);            
            JList roots = new JList (invalidRoots.toArray());
            roots.setCellRenderer (new InvalidRootRenderer(true));
            JScrollPane p = new JScrollPane (roots);
            c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.BOTH;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = c.weighty = 1.0;
            c.insets = new Insets (0,0,12,0);
            ((GridBagLayout)this.getLayout()).setConstraints(p,c);
            this.add (p);
            label.setLabelFor(roots);
            roots.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(SourceRootsUi.class,"AD_InvalidRoot"));
            JLabel label2 = new JLabel ();
            label2.setText (NbBundle.getMessage(SourceRootsUi.class,"MSG_InvalidRoot2"));
            c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 1.0;
            c.insets = new Insets (0,0,0,0);
            ((GridBagLayout)this.getLayout()).setConstraints(label2,c);
            this.add (label2);            
        }

        private static class InvalidRootRenderer extends DefaultListCellRenderer {
            private static final long serialVersionUID = 194496879246810209L;

            private boolean projectConflict;

            public InvalidRootRenderer (boolean projectConflict) {
                this.projectConflict = projectConflict;
            }

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (" ".equals(value)) { // NOI18N
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
                File f = (File) value;
                String message = f.getAbsolutePath();
                if (projectConflict) {
                    Project p = FileOwnerQuery.getOwner(Utilities.toURI(f));
                    if (p!=null) {
                        ProjectInformation pi = ProjectUtils.getInformation(p);
                        String projectName = pi.getDisplayName();
                        message = MessageFormat.format (NbBundle.getMessage(SourceRootsUi.class,"TXT_RootOwnedByProject"), new Object[] {
                            message,
                            projectName});
                    }
                }
                return super.getListCellRendererComponent(list, message, index, isSelected, cellHasFocus);
            }
        }
    }

}

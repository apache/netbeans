/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.project.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.CellEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.netbeans.modules.python.project.PythonProject;
import org.netbeans.modules.python.project.PythonProjectType;
import org.netbeans.modules.python.project.SourceRoots;
import org.netbeans.modules.python.project.util.Pair;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.HtmlRenderer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class Utils {
    
    public static String browseLocationAction(final Component parent, String path, String title) {
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (path != null && path.length() > 0) {
            File f = new File(path);
            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(parent)) {
            return FileUtil.normalizeFile(chooser.getSelectedFile()).getAbsolutePath();
        }
        return null;
    }
    
    public static ComboBoxModel createPlatformModel () {
        return new PlatformModel ();
    }
    
    public static ListCellRenderer createPlatformRenderer () {
        return new PlatformRenderer();
    }
    
   
    public static TableModel createSourceRootsModel (final List<? extends Pair<File,String>> roots) {
        Object[][] data = new Object[roots.size()][2];
        final Iterator<? extends Pair<File,String>>  it = roots.iterator();
        for (int i=0; it.hasNext(); i++) {
            Pair<File,String> e = it.next();
            data[i][0] = e.first;
            data[i][1] = e.second;
        }
        return new SourceRootsModel (data);
    }
    
    
    public static SourceRootsMediator registerEditMediator(final PythonProject project,
            final JTable rootsList,
            final JButton addFolderButton,
            final JButton removeButton,
            final JButton upButton,
            final JButton downButton,
            final CellEditor rootsListEditor,
            final ChangeListener listener,
            final boolean isTest) {
        assert rootsList != null;
        assert addFolderButton != null;
        assert removeButton != null;
        assert upButton != null;
        assert downButton != null;
        
        final EditMediator em = new EditMediator(project, rootsList, addFolderButton, removeButton, upButton, downButton, listener, isTest);
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
        rootsList.setDefaultRenderer( File.class, new FileRenderer (FileUtil.toFile(project.getProjectDirectory())));
        rootsList.setDefaultEditor(String.class, editor);
        // Set the initial state of the buttons
        em.valueChanged( null );
        
        DefaultTableModel model = (DefaultTableModel)rootsList.getModel();
        String[] columnNames = new String[2];
        columnNames[0]  = NbBundle.getMessage( Utils.class,"CTL_SourceFolders");
        columnNames[1]  = NbBundle.getMessage( Utils.class,"CTL_SourceLabels");
        model.setColumnIdentifiers(columnNames);
        rootsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        return em;
    }
    
    
    public static String chooseMainModule (final FileObject[] roots) {        
        final JButton okButton = new JButton (NbBundle.getMessage(Utils.class, "LBL_SelectMainModule"));        
        final MainModuleChooser mcc = new MainModuleChooser(roots, okButton);
        final Object[] options = new Object[] {okButton, DialogDescriptor.CANCEL_OPTION};
        final DialogDescriptor dd = new DialogDescriptor (mcc, NbBundle.getMessage(Utils.class, "LBL_BrowseMainModules"), true, options,
        okButton,DialogDescriptor.RIGHT_ALIGN,HelpCtx.DEFAULT_HELP,null);
        dd.setClosingOptions(options);
        if (DialogDisplayer.getDefault().notify(dd) == okButton) {
            return mcc.getMainModule();
        }
        return null;
    }
    
    
    public static interface SourceRootsMediator {
        public void setRelatedEditMediator(SourceRootsMediator rem);
    }
    
    
    private static class PlatformModel extends DefaultComboBoxModel {
        
        private final PythonPlatformManager manager;
        
        public PlatformModel () {
            manager = PythonPlatformManager.getInstance();
            init ();
        }
        
        private void init () {
            this.removeAllElements();   //init will be used also in case of chnge of installed plaforms
            final List<String> ids = manager.getPlatformList();
            for (String id : ids) {
                PythonPlatform platform = manager.getPlatform(id);
                this.addElement(platform);
            }
        }
    }
    
    private static class PlatformRenderer implements ListCellRenderer {
        
        private final ListCellRenderer delegate;
        
        public PlatformRenderer () {
            delegate = HtmlRenderer.createRenderer();
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String name;
            if (value instanceof PythonPlatform) {
                PythonPlatform key = (PythonPlatform) value;
                name = key.getName();
            }
            else if (value instanceof String) {
                //hndles broken platform for customizer
                name = "<html><font color=\"#A40000\">" //NOI18N
                            + NbBundle.getMessage(
                                    Utils.class, "TXT_BrokenPlatformFmt", (String)value);
            }
            else {
                name = "";
            }            
            return delegate.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
        }
        
    }
    
    private static class SourceRootsModel extends DefaultTableModel {

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
    
    
    private static class EditMediator implements ActionListener, ListSelectionListener, CellEditorListener, SourceRootsMediator {

        
        final JTable rootsList;
        final JButton addFolderButton;
        final JButton removeButton;
        final JButton upButton;
        final JButton downButton;
        private final Project project;
        private final Set<File> ownedFolders;
        private final boolean isTest;
        private final ChangeListener listener;
        private DefaultTableModel rootsModel;
        private EditMediator relatedEditMediator;
        private File lastUsedDir;       //Last used current folder in JFileChooser  

        
        public EditMediator( Project master,
                             JTable rootsList,
                             JButton addFolderButton,
                             JButton removeButton,
                             JButton upButton,
                             JButton downButton,
                             ChangeListener listener,
                             boolean isTest) {

            if ( !( rootsList.getModel() instanceof DefaultTableModel ) ) {
                throw new IllegalArgumentException( "Jtable's model has to be of class DefaultTableModel" ); // NOI18N
            }
                    
            this.rootsList = rootsList;
            this.addFolderButton = addFolderButton;
            this.removeButton = removeButton;
            this.upButton = upButton;
            this.downButton = downButton;
            this.ownedFolders = new HashSet<>();
            this.project = master;
            this.listener = listener;
            this.isTest = isTest;

            this.ownedFolders.clear();
            this.rootsModel = (DefaultTableModel)rootsList.getModel();
            Vector data = rootsModel.getDataVector();
            for (Iterator it = data.iterator(); it.hasNext();) {
                Vector row = (Vector) it.next ();
                File f = (File) row.elementAt(0);
                this.ownedFolders.add (f);
            }
        }
        
        @Override
        public void setRelatedEditMediator(final SourceRootsMediator rem) {
            this.relatedEditMediator = (EditMediator) rem;
        }
        
        // Implementation of ActionListener ------------------------------------
        
        /** Handles button events
         */        
        @Override
        public void actionPerformed(ActionEvent e ) {
            
            Object source = e.getSource();
            
            if ( source == addFolderButton ) { 
                
                // Let user search for the Jar file
                JFileChooser chooser = new JFileChooser();
                FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
                chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
                chooser.setMultiSelectionEnabled( true );
                if (isTest) {
                    chooser.setDialogTitle( NbBundle.getMessage( Utils.class, "LBL_TestFolder_DialogTitle" )); // NOI18N
                }
                else {
                    chooser.setDialogTitle( NbBundle.getMessage( Utils.class, "LBL_SourceFolder_DialogTitle" )); // NOI18N
                }    
                File curDir = this.lastUsedDir;
                if (curDir == null) {
                    curDir = FileUtil.toFile(this.project.getProjectDirectory());
                }
                if (curDir != null) {
                    chooser.setCurrentDirectory (curDir);
                }
                int option = chooser.showOpenDialog( SwingUtilities.getWindowAncestor( addFolderButton ) ); // Sow the chooser
                
                if ( option == JFileChooser.APPROVE_OPTION ) {
                    curDir = chooser.getCurrentDirectory();
                    if (curDir != null) {
                        this.lastUsedDir = curDir;
                        if (this.relatedEditMediator != null) {
                            this.relatedEditMediator.lastUsedDir = curDir;
                        }
                    }
                    File files[] = chooser.getSelectedFiles();
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
            
            fireChange();
        }
        
        // Selection listener implementation  ----------------------------------
        
        /** Handles changes in the selection
         */        
        @Override
        public void valueChanged( ListSelectionEvent e ) {
            
            int[] si = rootsList.getSelectedRows();
                        
            // edit enabled only if selection is not empty
            boolean edit = si != null && si.length > 0;            

            // remove enabled only if selection is not empty
            boolean remove = si != null && si.length > 0;
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
                        
            //System.out.println("Selection changed " + edit + ", " + remove + ", " +  + ", " + + ", ");
            
        }

        @Override
        public void editingCanceled(ChangeEvent e) {
        }

        @Override
        public void editingStopped(ChangeEvent e) {            
            fireChange();
        }
        
        private void addFolders( File files[] ) {
            int[] si = rootsList.getSelectedRows();
            int lastIndex = si == null || si.length == 0 ? -1 : si[si.length - 1];
            ListSelectionModel selectionModel = this.rootsList.getSelectionModel();
            selectionModel.clearSelection();
            Set<File> rootsFromOtherProjects = new HashSet<> ();
            Set<File> rootsFromRelatedSourceRoots = new HashSet<>();
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
                if ((p=FileOwnerQuery.getOwner(normalizedFile.toURI()))!=null && !p.getProjectDirectory().equals(project.getProjectDirectory())) {
                    final Sources sources = (Sources) p.getLookup().lookup (Sources.class);
                    if (sources == null) {
                        rootsFromOtherProjects.add (normalizedFile);
                        continue;
                    }
                    final SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
                    final SourceGroup[] javaGroups = sources.getSourceGroups(PythonProjectType.SOURCES_TYPE_PYTHON);
                    final SourceGroup[] groups = new SourceGroup [sourceGroups.length + javaGroups.length];
                    System.arraycopy(sourceGroups,0,groups,0,sourceGroups.length);
                    System.arraycopy(javaGroups,0,groups,sourceGroups.length,javaGroups.length);
                    final FileObject projectDirectory = p.getProjectDirectory();
                    final FileObject fileObject = FileUtil.toFileObject(normalizedFile);
                    if (projectDirectory == null || fileObject == null) {
                        rootsFromOtherProjects.add (normalizedFile);
                        continue;
                    }
                    for (SourceGroup group : groups) {
                        final FileObject sgRoot = group.getRootFolder();
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
                rootsModel.insertRow( current, new Object[] {normalizedFile, SourceRoots.createInitialDisplayName(normalizedFile,FileUtil.toFile(this.project.getProjectDirectory()),isTest)});
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
                this.ownedFolders.remove(((Vector)rootsModel.getDataVector().elementAt(si[i])).elementAt(0));
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
                Vector item = (Vector) rootsModel.getDataVector().elementAt(si[i]);
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
                Vector item = (Vector) rootsModel.getDataVector().elementAt(si[i]);
                int newIndex = si[i] + 1;
                rootsModel.removeRow( si[i] );
                rootsModel.insertRow( newIndex, item );
                selectionModel.addSelectionInterval(newIndex,newIndex);
            }
            // fireActionPerformed();
        }            
        
        private void fireChange () {
            if (listener != null) {
                final ChangeEvent event = new ChangeEvent(this);
                listener.stateChanged(event);
            }
        }
    }        
    
    /**
     * Opens the standard dialog for warning an user about illegal source roots.
     * @param roots the set of illegal source/test roots
     */
    private static void showIllegalRootsDialog (Set<File> roots) {
        JButton closeOption = new JButton (NbBundle.getMessage(Utils.class,"CTL_J2SESourceRootsUi_Close"));
        closeOption.getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(Utils.class,"AD_J2SESourceRootsUi_Close"));        
        JPanel warning = new WarningDlg (roots);                
        String message = NbBundle.getMessage(Utils.class,"MSG_InvalidRoot");
        JOptionPane optionPane = new JOptionPane (new Object[] {message, warning},
            JOptionPane.WARNING_MESSAGE,
            0, 
            null, 
            new Object[0], 
            null);
        optionPane.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(Utils.class,"AD_InvalidRootDlg"));
        DialogDescriptor dd = new DialogDescriptor (optionPane,
            NbBundle.getMessage(Utils.class,"TITLE_InvalidRoot"),
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
    
    private static class WarningDlg extends JPanel {

        public WarningDlg (Set invalidRoots) {            
            this.initGui (invalidRoots);
        }

        private void initGui (Set invalidRoots) {
            setLayout( new GridBagLayout ());                        
            JLabel label = new JLabel ();
            label.setText (NbBundle.getMessage(Utils.class,"LBL_InvalidRoot"));
            label.setDisplayedMnemonic(NbBundle.getMessage(Utils.class,"MNE_InvalidRoot").charAt(0));            
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
            roots.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(Utils.class,"AD_InvalidRoot"));
            JLabel label2 = new JLabel ();
            label2.setText (NbBundle.getMessage(Utils.class,"MSG_InvalidRoot2"));
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

            private boolean projectConflict;

            public InvalidRootRenderer (boolean projectConflict) {
                this.projectConflict = projectConflict;
            }

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                File f = (File) value;
                String message = f.getAbsolutePath();
                if (projectConflict) {
                    Project p = FileOwnerQuery.getOwner(f.toURI());
                    if (p!=null) {
                        ProjectInformation pi = ProjectUtils.getInformation(p);
                        String projectName = pi.getDisplayName();
                        message = MessageFormat.format (NbBundle.getMessage(Utils.class,"TXT_RootOwnedByProject"), new Object[] {
                            message,
                            projectName});
                    }
                }
                return super.getListCellRendererComponent(list, message, index, isSelected, cellHasFocus);
            }
        }
    }
    
    private static class FileRenderer extends DefaultTableCellRenderer {
        
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

}

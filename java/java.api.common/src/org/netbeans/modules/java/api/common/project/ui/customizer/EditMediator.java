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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.prefs.Preferences;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.libraries.LibraryChooser.Filter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.FileChooser;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ui.CustomizerUtilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Visual classpath customizer support.
 * 
 * @since org.netbeans.modules.java.api.common/1 1.5
 */
public final class EditMediator implements ActionListener, ListSelectionListener {

    private static String[] DEFAULT_ANT_ARTIFACT_TYPES = new String[] {
        JavaProjectConstants.ARTIFACT_TYPE_JAR, JavaProjectConstants.ARTIFACT_TYPE_FOLDER};

    public static final FileFilter JAR_ZIP_FILTER = new SimpleFileFilter( 
        NbBundle.getMessage( EditMediator.class, "LBL_ZipJarFolderFilter" ), // NOI18N
        new String[] {"ZIP","JAR"} ); // NOI18N
    
    private static final RequestProcessor RP = new RequestProcessor(EditMediator.class);
    
    private final ListComponent list;
    private final DefaultListModel listModel;
    private final ListSelectionModel selectionModel;
    private final ButtonModel addJar;
    private final ButtonModel addLibrary;
    private final ButtonModel addAntArtifact;
    private final ButtonModel remove;
    private final ButtonModel moveUp;
    private final ButtonModel moveDown;
    private final ButtonModel edit;
    private final boolean allowRemoveClassPath;
    private Document libraryPath;
    private ClassPathUiSupport.Callback callback;
    private AntProjectHelper helper;
    private ReferenceHelper refHelper;
    private Project project;
    private FileFilter filter;
    private String[] antArtifactTypes;
    private int fileSelectionMode;

    private EditMediator( Project project,
                         AntProjectHelper helper,
                         ReferenceHelper refHelper,
                         ListComponent list,
                         ButtonModel addJar,
                         ButtonModel addLibrary, 
                         ButtonModel addAntArtifact,
                         ButtonModel remove, 
                         ButtonModel moveUp,
                         ButtonModel moveDown, 
                         ButtonModel edit,
                         boolean allowRemoveClassPath,
                         Document libPath,
                         ClassPathUiSupport.Callback callback,
                         String[] antArtifactTypes,
                         FileFilter filter,
                         int fileSelectionMode) {

        // Remember all buttons

        this.list = list;
        this.listModel = list.getModel();
        this.selectionModel = list.getSelectionModel();
        this.addJar = addJar;
        this.addLibrary = addLibrary;
        this.addAntArtifact = addAntArtifact;
        this.remove = remove;
        this.moveUp = moveUp;
        this.moveDown = moveDown;
        this.edit = edit;
        this.libraryPath = libPath;
        this.callback = callback;
        this.helper = helper;
        this.refHelper = refHelper;
        this.project = project;
        this.filter = filter;
        this.fileSelectionMode = fileSelectionMode;
        this.antArtifactTypes = antArtifactTypes;
        this.allowRemoveClassPath = allowRemoveClassPath;
    }

    public static void register(Project project,
                                AntProjectHelper helper,
                                ReferenceHelper refHelper,
                                ListComponent list,
                                ButtonModel addJar,
                                ButtonModel addLibrary, 
                                ButtonModel addAntArtifact,
                                ButtonModel remove, 
                                ButtonModel moveUp,
                                ButtonModel moveDown, 
                                ButtonModel edit,
                                Document libPath,
                                ClassPathUiSupport.Callback callback) {    
        register(project, helper, refHelper, list, addJar, addLibrary, 
                addAntArtifact, remove, moveUp, moveDown, edit, false, libPath,
                callback);
    }
    
    public static void register(Project project,
                                AntProjectHelper helper,
                                ReferenceHelper refHelper,
                                ListComponent list,
                                ButtonModel addJar,
                                ButtonModel addLibrary,
                                ButtonModel addAntArtifact,
                                ButtonModel remove,
                                ButtonModel moveUp,
                                ButtonModel moveDown,
                                ButtonModel edit,
                                boolean allowRemoveClassPath,
                                Document libPath,
                                ClassPathUiSupport.Callback callback) {
        register(project, helper, refHelper, list, addJar, addLibrary,
                addAntArtifact, remove, moveUp, moveDown, edit, allowRemoveClassPath, libPath,
                callback, DEFAULT_ANT_ARTIFACT_TYPES, JAR_ZIP_FILTER,
                JFileChooser.FILES_AND_DIRECTORIES);
    }

    public static void register(Project project,
                                AntProjectHelper helper,
                                ReferenceHelper refHelper,
                                ListComponent list,
                                ButtonModel addJar,
                                ButtonModel addLibrary,
                                ButtonModel addAntArtifact,
                                ButtonModel remove,
                                ButtonModel moveUp,
                                ButtonModel moveDown,
                                ButtonModel edit,
                                Document libPath,
                                ClassPathUiSupport.Callback callback,
                                String[] antArtifactTypes,
                                FileFilter filter,
                                int fileSelectionMode) {
        register(project, helper, refHelper, list, addJar, addLibrary, addAntArtifact, remove, moveUp, moveDown, edit, false, libPath, callback, antArtifactTypes, filter, fileSelectionMode);
    }

    /**Added {@code allowRemoveClassPath} option that will allow the user to delete {@code ${javac.classpath}}.
     *
     * @since org.netbeans.modules.java.api.common/0 1.14
     */
    public static void register(Project project,
                                AntProjectHelper helper,
                                ReferenceHelper refHelper,
                                ListComponent list,
                                ButtonModel addJar,
                                ButtonModel addLibrary, 
                                ButtonModel addAntArtifact,
                                ButtonModel remove, 
                                ButtonModel moveUp,
                                ButtonModel moveDown, 
                                ButtonModel edit,
                                boolean allowRemoveClassPath,
                                Document libPath,
                                ClassPathUiSupport.Callback callback,
                                String[] antArtifactTypes,
                                FileFilter filter,
                                int fileSelectionMode) {    

        EditMediator em = new EditMediator( project, 
                                            helper,
                                            refHelper,
                                            list,
                                            addJar,
                                            addLibrary, 
                                            addAntArtifact,
                                            remove,    
                                            moveUp,
                                            moveDown,
                                            edit,
                                            allowRemoveClassPath,
                                            libPath,
                                            callback,
                                            antArtifactTypes,
                                            filter,
                                            fileSelectionMode);

        // Register the listener on all buttons
        addJar.addActionListener( em ); 
        addLibrary.addActionListener( em );
        addAntArtifact.addActionListener( em );
        remove.addActionListener( em );
        moveUp.addActionListener( em );
        moveDown.addActionListener( em );
        edit.addActionListener(em);
        // On list selection
        em.selectionModel.addListSelectionListener( em );
        // Set the initial state of the buttons
        em.valueChanged( null );
    }

    // Implementation of ActionListener ------------------------------------

    /** Handles button events
     */        
    public void actionPerformed( final ActionEvent e ) {

        RP.post(new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Object source = e.getSource();

                        if ( source == addJar ) { 
                            // Let user search for the Jar file
                            FileChooser chooser;
                            if (helper.isSharableProject()) {
                                chooser = new FileChooser(helper, true);
                            } else {
                                chooser = new FileChooser(FileUtil.toFile(project.getProjectDirectory()), null);
                            }
                            chooser.enableVariableBasedSelection(true);
                            chooser.setFileHidingEnabled(false);
                            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
                            chooser.setFileSelectionMode(fileSelectionMode);
                            chooser.setMultiSelectionEnabled( true );
                            chooser.setDialogTitle( NbBundle.getMessage( EditMediator.class, "LBL_AddJar_DialogTitle" ) ); // NOI18N
                            //#61789 on old macosx (jdk 1.4.1) these two method need to be called in this order.
                            chooser.setAcceptAllFileFilterUsed( false );
                            chooser.setFileFilter(filter);
                            File curDir = getLastUsedClassPathFolder(); 
                            chooser.setCurrentDirectory (curDir);
                            chooser.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage( EditMediator.class, "LBL_AddJar_DialogTitle" ));
                            int option = chooser.showOpenDialog( SwingUtilities.getWindowAncestor( list.getComponent() ) ); // Show the chooser

                            if ( option == JFileChooser.APPROVE_OPTION ) {

                                String filePaths[];
                                try {
                                    filePaths = chooser.getSelectedPaths();
                                } catch (IOException ex) {
                                    // TODO: add localized message
                                    Exceptions.printStackTrace(ex);
                                    return;
                                }

                                // check corrupted jar/zip files
                                File base = FileUtil.toFile(helper.getProjectDirectory());
                                List<String> newPaths = new ArrayList<String> ();
                                for (String path : filePaths) {
                                    File fl = PropertyUtils.resolveFile(base, path);
                                    FileObject fo = FileUtil.toFileObject(fl);
                                    if (fo == null) {
                                        JOptionPane.showMessageDialog (
                                            SwingUtilities.getWindowAncestor (list.getComponent ()),
                                            NbBundle.getMessage (EditMediator.class, "LBL_Missing_JAR", fl),
                                                NbBundle.getMessage (EditMediator.class, "LBL_Missing_JAR_title"),
                                                JOptionPane.WARNING_MESSAGE
                                        );
                                        continue;
                                    }
                                    assert fo != null : fl;
                                    if (FileUtil.isArchiveFile (fo))
                                        try {
                                            new JarFile (fl);
                                        } catch (IOException ex) {
                                            JOptionPane.showMessageDialog (
                                                SwingUtilities.getWindowAncestor (list.getComponent ()),
                                                NbBundle.getMessage (EditMediator.class, "LBL_Corrupted_JAR", fl),
                                                    NbBundle.getMessage (EditMediator.class, "LBL_Corrupted_JAR_title"),
                                                    JOptionPane.WARNING_MESSAGE
                                            );
                                            continue;
                                        }
                                    newPaths.add (path);
                                }
                                filePaths = newPaths.toArray (new String [0]);

                                // value of PATH_IN_DEPLOYMENT depends on whether file or folder is being added.
                                // do not override value set by callback.initAdditionalProperties if includeNewFilesInDeployment
                                int[] newSelection = ClassPathUiSupport.addJarFiles( listModel, list.getSelectedIndices(), 
                                        filePaths, base,
                                        chooser.getSelectedPathVariables(), callback);
                                list.setSelectedIndices( newSelection );
                                curDir = FileUtil.normalizeFile(chooser.getCurrentDirectory());
                                setLastUsedClassPathFolder(curDir);
                            }
                        }
                        else if ( source == addLibrary ) {
                            //TODO this piece needs to go somewhere else?
                            File librariesFile = null;
                            LibraryManager manager = null;
                            boolean empty = false;
                            try {
                                String path = libraryPath.getText(0, libraryPath.getLength());
                                if (path != null && path.length() > 0) {
                                    librariesFile = FileUtil.normalizeFile(PropertyUtils.resolveFile(FileUtil.toFile(helper.getProjectDirectory()), path));
                                    final URL librariesFolder = Utilities.toURI(librariesFile).toURL();
                                    manager = LibraryManager.forLocation(librariesFolder);
                                } else {
                                    empty = true;
                                }
                            } catch (BadLocationException ex) {
                                empty = true;
                                Exceptions.printStackTrace(ex);
                            } catch (MalformedURLException ex2) {
                                Exceptions.printStackTrace(ex2);
                            }
                            if (manager == null && empty) {
                                manager = LibraryManager.getDefault();
                            }
                            if (manager == null) {
                                //TODO some error message
                                return;
                            }

                            Set<Library> added = LibraryChooser.showDialog(manager,
                                    createLibraryFilter(),
                                    empty ?
                                        CustomizerUtilities.getLibraryChooserImportHandler(refHelper) :
                                        CustomizerUtilities.getLibraryChooserImportHandler(librariesFile));
                            if (added != null) {
                                final Set<Library> includedLibraries = getIncludedLibraries(listModel);
                               int[] newSelection = ClassPathUiSupport.addLibraries(listModel, list.getSelectedIndices(), 
                                       added.toArray(new Library[0]), includedLibraries, callback);
                               list.setSelectedIndices( newSelection );
                            }
                        }
                        else if ( source == edit ) { 
                            ClassPathUiSupport.edit( listModel, list.getSelectedIndices(),  helper);
                            if (list instanceof JListListComponent) {
                                ((JListListComponent)list).list.repaint();
                                for(ListDataListener listener: ((JListListComponent)list).getModel().getListDataListeners())
                                    listener.contentsChanged(new ListDataEvent(source, ListDataEvent.CONTENTS_CHANGED, 
                                            list.getSelectedIndices().length > 0 ?list.getSelectedIndices()[0]: null, 
                                            list.getSelectedIndices().length > 0 ?list.getSelectedIndices()[list.getSelectedIndices().length-1]:null));
                            } else if (list instanceof JTableListComponent) {
                                ((JTableListComponent)list).table.repaint();
                                for(ListDataListener listener: ((JTableListComponent)list).getModel().getListDataListeners())
                                    listener.contentsChanged(new ListDataEvent(source, ListDataEvent.CONTENTS_CHANGED, 
                                            list.getSelectedIndices().length > 0 ?list.getSelectedIndices()[0]: null, 
                                            list.getSelectedIndices().length > 0 ?list.getSelectedIndices()[list.getSelectedIndices().length-1]:null));
                            } else {
                                assert false : "do not know how to handle " + list.getClass().getName();
                            }
                        }
                        else if ( source == addAntArtifact ) { 
                            AntArtifactItem artifactItems[] = AntArtifactChooser.showDialog(
                                    antArtifactTypes, project, list.getComponent().getParent());
                            if (artifactItems != null) {
                                int[] newSelection = ClassPathUiSupport.addArtifacts( listModel, list.getSelectedIndices(), artifactItems, callback);
                                list.setSelectedIndices( newSelection );
                            }
                        }
                        else if ( source == remove ) { 
                            int[] newSelection = ClassPathUiSupport.remove( listModel, list.getSelectedIndices() );
                            list.setSelectedIndices( newSelection );
                        }
                        else if ( source == moveUp ) {
                            int[] newSelection = ClassPathUiSupport.moveUp( listModel, list.getSelectedIndices() );
                            list.setSelectedIndices( newSelection );
                        }
                        else if ( source == moveDown ) {
                            int[] newSelection = ClassPathUiSupport.moveDown( listModel, list.getSelectedIndices() );
                            list.setSelectedIndices( newSelection );
                        }
                    }
                });
            }
        });
    }    


    private static final String LAST_USED_CP_FOLDER = "lastUsedClassPathFolder";    //NOI18N

    private static Preferences getPreferences() {
        return NbPreferences.forModule(EditMediator.class);
    }

    private Set<Library> getIncludedLibraries(@NonNull DefaultListModel model) {
        final Set<Library> inc = new HashSet<Library>();
        for (final Object item : model.toArray()) {
            if (item instanceof ClassPathSupport.Item) {
                final ClassPathSupport.Item cpItem = (ClassPathSupport.Item) item;
                if (cpItem.getType() == ClassPathSupport.Item.TYPE_LIBRARY && cpItem.getLibrary() != null) {
                    inc.add(cpItem.getLibrary());
                }
            }
        }
        return Collections.unmodifiableSet(inc);
    }

    public static File getLastUsedClassPathFolder () {
        return new File(getPreferences().get(LAST_USED_CP_FOLDER, System.getProperty("user.home")));
    }

    public static void setLastUsedClassPathFolder (File folder) {
        assert folder != null : "ClassPath root can not be null";
        String path = folder.getAbsolutePath();
        getPreferences().put(LAST_USED_CP_FOLDER, path);
    }

    /** Handles changes in the selection
     */        
    public void valueChanged( ListSelectionEvent e ) {

        // remove enabled only if selection is not empty
        boolean canRemove = false;
        // and when the selection does not contain unremovable item
        if ( selectionModel.getMinSelectionIndex() != -1 ) {
            canRemove = true;
            int iMin = selectionModel.getMinSelectionIndex();
            int iMax = selectionModel.getMaxSelectionIndex();
            for ( int i = iMin; i <= iMax; i++ ) {

                if ( selectionModel.isSelectedIndex( i ) ) {
                    ClassPathSupport.Item item = (ClassPathSupport.Item)listModel.get( i );
                    if ( item != null && item.getType() == ClassPathSupport.Item.TYPE_CLASSPATH ) {
                        canRemove = allowRemoveClassPath;
                        break;
                    }
                }
            }
        }

        // addJar allways enabled            
        // addLibrary allways enabled            
        // addArtifact allways enabled            
        edit.setEnabled(ClassPathUiSupport.canEdit(selectionModel, listModel));            
        remove.setEnabled( canRemove );
        moveUp.setEnabled( ClassPathUiSupport.canMoveUp( selectionModel ) );
        moveDown.setEnabled( ClassPathUiSupport.canMoveDown( selectionModel, listModel.getSize() ) );       

    }

    public static Filter createLibraryFilter() {
        return  new Filter() {
            public boolean accept(Library library) {
                if ("javascript".equals(library.getType())) { //NOI18N
                    return false;
                }
                try {
                    library.getContent("classpath"); //NOI18N
                    return true;
                } catch (IllegalArgumentException ex) {
                    return false;
                }
            }
        };
    }

    public interface ListComponent {
        public Component getComponent();
        public int[] getSelectedIndices();
        public void setSelectedIndices(int[] indices);
        public DefaultListModel getModel();
        public ListSelectionModel getSelectionModel();
    }

    private static final class JListListComponent implements ListComponent {
        private JList list;

        public JListListComponent(JList list) {
            this.list = list;
        }

        public Component getComponent() {
            return list;
        }

        public int[] getSelectedIndices() {
            return list.getSelectedIndices();
        }

        public void setSelectedIndices(int[] indices) {
            list.setSelectedIndices(indices);
        }

        public DefaultListModel getModel() {
            return (DefaultListModel)list.getModel();
        }

        public ListSelectionModel getSelectionModel() {
            return list.getSelectionModel();
        }
    }

    private static final class JTableListComponent implements ListComponent {
        private JTable table;
        private DefaultListModel model;

        public JTableListComponent(JTable table, DefaultListModel model) {
            this.table = table;
            this.model = model;
        }

        public Component getComponent() {
            return table;
        }

        public int[] getSelectedIndices() {
            return table.getSelectedRows();
        }

        public void setSelectedIndices(int[] indices) {
            table.clearSelection();
            for (int i = 0; i < indices.length; i++) {
                table.addRowSelectionInterval(indices[i], indices[i]);
            }
        }

        public DefaultListModel getModel() {
            return model;
        }

        public ListSelectionModel getSelectionModel() {
            return table.getSelectionModel();
        }
    }

    public static ListComponent createListComponent(JList list) {
        return new JListListComponent(list);
    }

    public static ListComponent createListComponent(JTable table, DefaultListModel model) {
        return new JTableListComponent(table, model);
    }
    
    private static class SimpleFileFilter extends FileFilter {

        private String description;
        private Collection extensions;


        public SimpleFileFilter (String description, String[] extensions) {
            this.description = description;
            this.extensions = Arrays.asList(extensions);
        }

        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            String name = f.getName();
            int index = name.lastIndexOf('.');   //NOI18N
            if (index <= 0 || index==name.length()-1)
                return false;
            String extension = name.substring (index+1).toUpperCase();
            return this.extensions.contains(extension);
        }

        public String getDescription() {
            return this.description;
        }
    }
    
}


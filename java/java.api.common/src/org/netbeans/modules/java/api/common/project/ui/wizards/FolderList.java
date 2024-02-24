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

package org.netbeans.modules.java.api.common.project.ui.wizards;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.api.common.project.ui.customizer.SourceRootsUi;
import org.netbeans.spi.java.project.support.JavadocAndSourceRootDetection;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 * List of source/test roots
 * @author tzezula
 */
public final class FolderList extends javax.swing.JPanel {

    public static final String PROP_FILES = "files";    //NOI18N
    public static final String PROP_LAST_USED_DIR = "lastUsedDir";  //NOI18N
    private static final Logger LOG = Logger.getLogger(FolderList.class.getName());
    private static final Pattern TESTS_RE = Pattern.compile(".*test.*",Pattern.CASE_INSENSITIVE);   //NOI18N

    private String fcMessage;
    private File projectFolder;
    private File lastUsedFolder;
    private FolderList relatedFolderList;
    private FileFilter relatedFolderFilter;

    /** Creates new form FolderList */
    public FolderList (String label, char mnemonic, String accessibleDesc, String fcMessage,
                       char addButtonMnemonic, String addButtonAccessibleDesc,
                       char removeButtonMnemonic,String removeButtonAccessibleDesc) {
        this.fcMessage = fcMessage;
        initComponents();
        this.jLabel1.setText(label);
        this.jLabel1.setDisplayedMnemonic(mnemonic);
        this.roots.getAccessibleContext().setAccessibleName(accessibleDesc);
        this.roots.setCellRenderer(new Renderer());
        this.roots.setModel (new DefaultListModel());
        this.roots.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    removeButton.setEnabled(roots.getSelectedIndices().length != 0);
                }
            }
        });
        this.roots.setDragEnabled(true);
        this.roots.setDropMode(DropMode.INSERT);
        this.roots.setTransferHandler(new DNDHandle());
        this.addButton.getAccessibleContext().setAccessibleDescription(addButtonAccessibleDesc);
        this.addButton.setMnemonic (addButtonMnemonic);        
        this.removeButton.getAccessibleContext().setAccessibleDescription(removeButtonAccessibleDesc);
        this.removeButton.setMnemonic (removeButtonMnemonic);
        this.removeButton.setEnabled(false);
    }

    public void setRelatedFolderList (FolderList relatedFolderList) {
        setRelatedFolderList(relatedFolderList, null);
    }

    /**
     * Sets the related {@link FolderList} used to verify duplicates and filter used to
     * move the recognized folders into the related {@link FolderList}
     * @param relatedFolderList the related {@link FolderList}
     * @param relatedFolderFilter filder used to reparent the recognized {@link File} or null
     */
    public void setRelatedFolderList (FolderList relatedFolderList, FileFilter relatedFolderFilter) {
        this.relatedFolderList = relatedFolderList;
        this.relatedFolderFilter = relatedFolderFilter;
    }

    public File[] getFiles () {
        Object[] files = ((DefaultListModel)this.roots.getModel()).toArray();
        File[] result = new File[files.length];
        System.arraycopy(files, 0, result, 0, files.length);
        return result;
    }

    public void setProjectFolder (File projectFolder) {
        this.projectFolder = projectFolder;
    }

    public void setFiles (File[] files) {
        DefaultListModel model = ((DefaultListModel)this.roots.getModel());
        model.clear();
        for (int i=0; i<files.length; i++) {
            model.addElement (files[i]);
        }
        if (files.length>0) {
            this.roots.setSelectedIndex(0);
        }
    }

    public void setLastUsedDir (File lastUsedDir) {
        if (this.lastUsedFolder == null ? lastUsedDir != null : !this.lastUsedFolder.equals(lastUsedDir)) {
            File oldValue = this.lastUsedFolder;
            this.lastUsedFolder = lastUsedDir;
            this.firePropertyChange(PROP_LAST_USED_DIR, oldValue, this.lastUsedFolder);
        }
    }
    
    public File getLastUsedDir () {
        return this.lastUsedFolder;
    }

    @Override
    public void setEnabled(final boolean active) {
        this.addButton.setEnabled(active);
        this.removeButton.setEnabled(active);
        this.jScrollPane1.setEnabled(active);
        this.roots.setEnabled(active);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        roots = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(roots);
        jLabel1.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FolderList.class, "ACSD_NA")); // NOI18N

        jScrollPane1.setViewportView(roots);
        roots.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FolderList.class, "ACSD_FolderList")); // NOI18N
        roots.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FolderList.class, "ACSD_NA")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jScrollPane1, gridBagConstraints);

        addButton.setText(NbBundle.getMessage(FolderList.class, "CTL_AddFolder")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(addButton, gridBagConstraints);
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FolderList.class, "ACSD_NA")); // NOI18N

        removeButton.setText(NbBundle.getMessage(FolderList.class, "CTL_RemoveFolder")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(removeButton, gridBagConstraints);
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FolderList.class, "ACSD_NA")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FolderList.class, "ACSD_NA")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FolderList.class, "ACSD_NA")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        Object[] selection = this.roots.getSelectedValues ();
        for (int i=0; i<selection.length; i++) {
            ((DefaultListModel)this.roots.getModel()).removeElement (selection[i]);
        }
        this.firePropertyChange(PROP_FILES, null, null);
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(this.fcMessage);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        if (this.lastUsedFolder != null && this.lastUsedFolder.isDirectory()) {
            chooser.setCurrentDirectory (this.lastUsedFolder);
        }
        else if (this.projectFolder != null && this.projectFolder.isDirectory()) {
            chooser.setCurrentDirectory (this.projectFolder);
        }
        if (chooser.showOpenDialog(this)== JFileChooser.APPROVE_OPTION) {
            final File[] files = normalizeFiles(chooser.getSelectedFiles());
            final AtomicReference<List<File>> toAddRef = new AtomicReference<List<File>>();
            class ScanTask implements ProgressRunnable<Void>, Cancellable {
                private final AtomicBoolean cancel = new AtomicBoolean();
                @Override
                public Void run(final ProgressHandle handle) {
                    final List<File> toAdd = new ArrayList<File>();
                    for (File file : files) {
                        if (cancel.get()) {
                            return null;
                        }
                        final FileObject fo = FileUtil.toFileObject(file);
                        if (fo != null) {
                            final Collection<? extends FileObject> detectedRoots = JavadocAndSourceRootDetection.findSourceRoots(fo,cancel);
                            if (detectedRoots.isEmpty()) {
                                toAdd.add(file);
                            } else {
                                for (FileObject detectedRoot : detectedRoots) {
                                    toAdd.add (FileUtil.toFile(detectedRoot));
                                }
                            }
                        } else {
                            if (file.exists()) {
                                toAdd.add(file);
                            } else {
                                LOG.log(
                                    Level.WARNING,
                                    "Ignoring non existent folder: {0}",    //NOI18N
                                    file);
                            }
                        }
                    }
                    toAddRef.set(toAdd);    //threading: Needs to be a tail call!
                    return null;
                }

                @Override
                public boolean cancel() {
                    cancel.set(true);
                    return true;
                }
            };
            final ScanTask task = new ScanTask();
            ProgressUtils.showProgressDialogAndRun(task, NbBundle.getMessage(FolderList.class, "TXT_SearchingSourceRoots"), false);
            final List<File> toAdd = toAddRef.get();
            final List<File> related = new LinkedList<File>();
            if (relatedFolderList != null && relatedFolderFilter != null && toAdd != null) {
                if (relatedFolderFilter instanceof ContextFileFilter) {
                    ((ContextFileFilter)relatedFolderFilter).setContext(files);
                }
                for (Iterator<File> it = toAdd.iterator(); it.hasNext(); ) {
                    File f = it.next();
                    if (relatedFolderFilter.accept(f)) {
                        it.remove();
                        related.add(f);
                    }
                }
            }
            final File[] toAddArr = toAdd == null ? files : toAdd.toArray(new File[0]);
            Set<File> invalidRoots = new HashSet<File>();
            addFiles(toAddArr, invalidRoots);
            if (!related.isEmpty()) {
                relatedFolderList.addFiles(related.toArray(new File[0]), invalidRoots);
            }
            File cd = chooser.getCurrentDirectory();
            if (cd != null) {
                this.setLastUsedDir(FileUtil.normalizeFile(cd));
            }
            if (invalidRoots.size()>0) {
                SourceRootsUi.showIllegalRootsDialog(invalidRoots);
            }
        }
    }//GEN-LAST:event_addButtonActionPerformed


    private void addFiles (final File[] toAddArr, final Set<? super File> invalidRoots) {
        final int[] indecesToSelect = new int[toAddArr.length];
        final DefaultListModel model = (DefaultListModel)this.roots.getModel();
        final File[] relatedFolders = this.relatedFolderList == null ?
            new File[0] : this.relatedFolderList.getFiles();
        for (int i=0, index=model.size(); i<toAddArr.length; i++) {
            File normalizedFile = toAddArr[i];
            if (!isValidRoot(normalizedFile, relatedFolders, this.projectFolder)) {
                invalidRoots.add (normalizedFile);
            }
            else {
                int pos = model.indexOf (normalizedFile);
                if (pos == -1) {
                    model.addElement (normalizedFile);
                    indecesToSelect[i] = index;
                }
                else {
                    indecesToSelect[i] = pos;
                }
                index++;
            }
        }
        this.roots.setSelectedIndices(indecesToSelect);
        this.firePropertyChange(PROP_FILES, null, null);
    }

    private static File[] normalizeFiles(final File... files) {
        for (int i=0; i< files.length; i++) {
            files[i] = FileUtil.normalizeFile(files[i]);
        }
        return files;
    }

    public static boolean isValidRoot (File file, File[] relatedRoots, File projectFolder) {
        Project p;
        if ((p = FileOwnerQuery.getOwner(Utilities.toURI(file)))!=null
            && !file.getAbsolutePath().startsWith(projectFolder.getAbsolutePath()+File.separatorChar)) {
            final Sources sources = p.getLookup().lookup(Sources.class);
            if (sources == null) {
                return false;
            }
            final SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            final SourceGroup[] javaGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            final SourceGroup[] groups = new SourceGroup [sourceGroups.length + javaGroups.length];
            System.arraycopy(sourceGroups,0,groups,0,sourceGroups.length);
            System.arraycopy(javaGroups,0,groups,sourceGroups.length,javaGroups.length);
            final FileObject projectDirectory = p.getProjectDirectory();
            final FileObject fileObject = FileUtil.toFileObject(file);
            if (projectDirectory == null || fileObject == null) {
                return false;
            }
            for (int i = 0; i< groups.length; i++) {
                final FileObject sgRoot = groups[i].getRootFolder();
                if (fileObject.equals(sgRoot)) {
                    return false;
                }
                if (!projectDirectory.equals(sgRoot) && FileUtil.isParentOf(sgRoot, fileObject)) {
                    return false;
                }
            }
            return true;
        }
        else if (contains (file, relatedRoots)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a {@link FileFilter} accepting all files for which any
     * of its path elements starting from selectedFolder (exclusive) to the end is maching
     * ".*test.* case insensitive regular expression
     * @return the {@link FileFilter}
     */
    public static FileFilter testRootsFilter () {
        return new ContextFileFilter() {
            private Set<File> selectedFiles;

            @Override
            public boolean accept(File pathname) {
                while(pathname != null && selectedFiles != null && !selectedFiles.contains(pathname)) {
                    String toTest = pathname.getName();
                    if (TESTS_RE.matcher(toTest).matches()) {
                        return true;
                    }
                    pathname = pathname.getParentFile();
                }
                return false;
            }

            @Override
            public void setContext(File[] selectedFiles) {
                this.selectedFiles = new HashSet<File>(Arrays.asList(selectedFiles));
            }
        };
    }

    private static boolean contains (File folder, File[] roots) {
        String path = folder.getAbsolutePath ();
        for (int i=0; i<roots.length; i++) {
            String rootPath = roots[i].getAbsolutePath();
            if (rootPath.equals (path) || path.startsWith (rootPath + File.separatorChar)) {
                return true;
            }
        }
        return false;
    }

    private static class Renderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (" ".equals(value)) { // NOI18N
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
            File f = (File) value;
            String message = f.getAbsolutePath();
            Component result = super.getListCellRendererComponent(list, message, index, isSelected, cellHasFocus);
            return result;
        }        
    }

    private static class FileListTransferable implements Transferable {

        private final List<? extends File> data;

        public FileListTransferable(final List<? extends File> data) {
            data.getClass();
            this.data = data;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.javaFileListFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.javaFileListFlavor == flavor;
        }

        @Override
        public List<? extends File> getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return data;
        }

    }

    private class DNDHandle extends TransferHandler {

        private int[] indices = new int[0];

        @Override
        public int getSourceActions(JComponent comp) {
            return MOVE;
        }

        @Override
        public Transferable createTransferable(JComponent comp) {
            final JList list = (JList)comp;
            indices = list.getSelectedIndices();
            if (indices.length == 0) {
                return null;
            }
            return new FileListTransferable(safeCopy(list.getSelectedValues(),File.class));
        }

        private <T> List<? extends T> safeCopy(Object[] data, Class<T> clazz) {
            final List<T> result = new ArrayList<T>(data.length);
            for (Object d : data) {
                result.add(clazz.cast(d));
            }
            return result;
        }

        @Override
        public void exportDone(JComponent comp, Transferable trans, int action) {
            if (action == MOVE) {
                final JList from = (JList) comp;
                final DefaultListModel model = (DefaultListModel) from.getModel();
                for (int i=indices.length-1; i>=0; i--) {
                    model.removeElementAt(indices[i]);
                }
            }
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {

            if (!support.isDrop()) {
                return false;
            }

            if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                return false;
            }


            boolean actionSupported = (MOVE & support.getSourceDropActions()) == MOVE;
            if (!actionSupported) {
                return false;
            }

            support.setDropAction(MOVE);
            return true;
        }


        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            JList.DropLocation dl = (JList.DropLocation)support.getDropLocation();
            int index = Math.max(0, dl.getIndex());
            List<? extends File> data;
            try {
                data = (List<? extends File>)support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            } catch (UnsupportedFlavorException e) {
                return false;
            } catch (java.io.IOException e) {
                return false;
            }

            final List<File> validRoots = new ArrayList<File>();
            final Set<File> invalidRoots = new HashSet<File>();
            if (relatedFolderList != null && projectFolder != null) {
                final File[] relatedFolders = support.getSourceDropActions() == MOVE ?
                        new File[0]:
                        relatedFolderList.getFiles();
                for (File file : data) {
                    if (!isValidRoot(file, relatedFolders, projectFolder)) {
                        invalidRoots.add (file);
                    } else {
                        validRoots.add(file);
                    }
                }
            } else {
                validRoots.addAll(data);
            }

            JList list = (JList)support.getComponent();
            DefaultListModel model = (DefaultListModel)list.getModel();
            int[] indices = new int[validRoots.size()];
            for (int i=0; i< validRoots.size(); i++,index++) {
                model.insertElementAt(validRoots.get(i), index);
                indices[i]=index;
                updateIndexes(index);
            }
            if (!validRoots.isEmpty()) {
                Rectangle rect = list.getCellBounds(indices[0], indices[indices.length-1]);
                list.scrollRectToVisible(rect);
                list.setSelectedIndices(indices);
                list.requestFocusInWindow();
            }
            if (!invalidRoots.isEmpty()) {
                SourceRootsUi.showIllegalRootsDialog(invalidRoots);
            }
            return true;
        }

        private void updateIndexes(int index) {
            for (int i=0; i< indices.length; i++) {
                if (index<indices[i]) {
                    indices[i]++;
                }
            }
        }

    }

    private static interface ContextFileFilter extends FileFilter {
        void setContext(final File[] selectedFiles);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeButton;
    private javax.swing.JList roots;
    // End of variables declaration//GEN-END:variables
    
}

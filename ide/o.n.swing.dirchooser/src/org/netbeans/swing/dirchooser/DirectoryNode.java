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
/*
 * Contributor(s): Soot Phengsy
 */

package org.netbeans.swing.dirchooser;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * A directory tree node.
 *
 * @author Soot Phengsy
 */
public class DirectoryNode extends DefaultMutableTreeNode {
    
    public static final int SINGLE_SELECTION = 0;
    
    public static final int DIG_IN_SELECTION = 4;

    /** case insensitive file name's comparator */
    static final FileNameComparator FILE_NAME_COMPARATOR = new FileNameComparator();
    
    private File directory;
    
    private boolean isDir;
    
    private boolean loaded;
    
    private boolean isSelected;
    private final boolean exists;
    
    public DirectoryNode(File file) {
        this(file, true, false, false, false);
    }
    
    public DirectoryNode(File file, boolean allowsChildren) {
        this(file, allowsChildren, false, false, false);
    }
    
    public DirectoryNode(File file, boolean allowsChildren, boolean isSelected,
            boolean isChecked, boolean isEditable) {
        super(file, allowsChildren);
        this.directory = file;
        isDir = file.isDirectory();
        exists = isDir || file.exists();
        this.isSelected = isSelected;
    }
    
    public boolean isLoaded() {
        return this.loaded;
    }
    
    public File getFile() {
        return this.directory;
    }
    
    public void setFile(File file) {
        setUserObject(file);
        this.directory = file;
        this.loaded = false;
    }
    
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
    
    @Override
    public boolean isLeaf() {
        return !this.isDir;
    }
    
    @Override
    public boolean getAllowsChildren() {
        return this.isDir;
    }
    
    public boolean isSelected() {
        return this.isSelected;
    }
    
    public boolean loadChildren(JFileChooser chooser, boolean descend) {
        //fixed bug #97124
        if (loaded == false) {
            
            List<File> files = getFiles(chooser);
            
            if(files.isEmpty()) {
                return false;
            }
            
            for (File child : files) {
                if(chooser.accept(child)) {
                    try {
                        DirectoryNode node = new DirectoryNode(child);
                        if (descend == false) {
                            break;
                        }
                        add(node);
                    } catch (NullPointerException t) {
                        Logger.getLogger(DirectoryNode.class.getName()).log(Level.INFO, null, t);
                    }
                }
            }
            
            if (descend == true || getChildCount() > 0) {
                loaded = true;
            }
        }
        
        return loaded;
    }

    private List<File> getFiles(JFileChooser chooser) {
        //fixed bug #97124
        List<File> list = new ArrayList<>();
        
        // Fix for IZ#116859 [60cat] Node update bug in the "open project" panel while deleting directories
        if ( directory == null || !directory.exists() ){
            return list;
        }

        File[] files = chooser.getFileSystemView().getFiles(directory, chooser.isFileHidingEnabled());
        int mode = chooser.getFileSelectionMode();
        if (mode == JFileChooser.DIRECTORIES_ONLY) {
            for(int i = 0; i < files.length; i++) {
                File child = files[i];
                if (child.isDirectory()) {
                    list.add(child);
                }
            }
            list.sort(FILE_NAME_COMPARATOR);
        } else if (mode == JFileChooser.FILES_AND_DIRECTORIES || mode == JFileChooser.FILES_ONLY) {
            List<File> dirList = new ArrayList<>();
            List<File> fileList = new ArrayList<>();
            for(int i = 0; i < files.length; i++) {
                File child = files[i];
                if (child.isDirectory()) {
                    dirList.add(child);
                } else {
                    fileList.add(child);
                }
            }
            
            dirList.sort(FILE_NAME_COMPARATOR);
            fileList.sort(FILE_NAME_COMPARATOR);
            
            list.addAll(dirList);
            list.addAll(fileList);
        }

        return list;
    }

    public boolean isNetBeansProject() {
        return isNetBeansProject(directory);
    }
    
    public static boolean isNetBeansProject (File directory) {
        boolean retVal = false;
        if (directory != null) {
            FileObject fo = convertToValidDir(directory);
            if (fo != null) {
                if (Utilities.isUnix() && fo.getParent() != null
                        && fo.getParent().getParent() == null) {
                    retVal = false; // Ignore all subfolders of / on unixes
                    // (e.g. /net, /proc)
                } else {
                    retVal = ProjectManager.getDefault().isProject(fo);
                }
            }
        }
        return retVal;
    }
    
    private static FileObject convertToValidDir(File f) {
        FileObject fo;
        File testFile = new File(f.getPath());
        if (testFile.getParent() == null) {
            // BTW this means that roots of file systems can't be project
            // directories.
            return null;
        }
        
        /**
         *
         * ATTENTION: on Windows may occure dir.isDirectory () == dir.isFile () ==
         *
         * true then its used testFile instead of dir.
         *
         */
        if (!testFile.isDirectory()) {
            return null;
        }
        
        fo = FileUtil.toFileObject(FileUtil.normalizeFile(testFile));
        if (fo.isValid() && fo.isFolder()) {
            return fo;
        } else {
            return null;
        }
    }

    Icon getIcon (JFileChooser fileChooser) {
        if (exists) {
            return fileChooser.getIcon(getFile());
        } else {
            return null;
        }
    }

    String getText (JFileChooser fileChooser) {
        if (exists) {
            return "<html>" + fileChooser.getName(getFile()) + "</html>"; //NOI18N
        } else {
            return "<html> </html>"; //NOI18N
        }
    }
    
    /** Compares files ignoring case sensitivity */
    private static class FileNameComparator implements Comparator<File> {

        @Override
        public int compare(File f1, File f2) {
            return String.CASE_INSENSITIVE_ORDER.compare(f1.getName(), f2.getName());
        }
        
    }
    
}

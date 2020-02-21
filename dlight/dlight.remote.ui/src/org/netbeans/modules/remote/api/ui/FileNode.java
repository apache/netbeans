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

package org.netbeans.modules.remote.api.ui;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A directory tree node.
 *
 */
class FileNode extends DefaultMutableTreeNode {
    
    public final static int SINGLE_SELECTION = 0;
    
    public final static int DIG_IN_SELECTION = 4;

    /** case insensitive file name's comparator */
    static final FileNameComparator FILE_NAME_COMPARATOR = new FileNameComparator();
    
    private File file;
    
    private boolean isDir;
    
    private boolean loaded;
    
    private boolean isSelected;
    private final boolean exists;
    
    public FileNode(File file) {
        this(file, true, false, false, false);
    }
    
    public FileNode(File file, boolean allowsChildren) {
        this(file, allowsChildren, false, false, false);
    }
    
    public FileNode(File file, boolean allowsChildren, boolean isSelected,
            boolean isChecked, boolean isEditable) {
        super(file, allowsChildren);
        this.file = file;
        isDir = file.isDirectory();
        exists = file.exists();
        this.isSelected = isSelected;
    }
    
    public boolean isLoaded() {
        return this.loaded;
    }
    
    public File getFile() {
        return this.file;
    }
    
    public void setFile(File file) {
        setUserObject(file);
        this.file = file;
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
            
            ArrayList files = getFiles(chooser);
            
            if(files.isEmpty()) {
                return false;
            }
            
            for(int i = 0; i < files.size(); i++) {
                File child = (File) files.get(i);
                
                if(chooser.accept(child)) {
                    try {
                        FileNode node = new FileNode(child);
                        if (descend == false) {
                            break;
                        }
                        add(node);
                    } catch (NullPointerException t) {
                        Logger.getLogger(FileNode.class.getName()).log(Level.INFO, null, t);
                    }
                }
            }
            
            if (descend == true ||  (getChildCount() > 0)) {
                loaded = true;
            }
        }
        
        return loaded;
    }

    private ArrayList getFiles(JFileChooser chooser) {
        //fixed bug #97124
        ArrayList list = new ArrayList();
        
        // Fix for IZ#116859 [60cat] Node update bug in the "open project" panel while deleting directories
        if ( file == null || !file.exists() ){
            return list;
        }

        File[] files = chooser.getFileSystemView().getFiles(file, chooser.isFileHidingEnabled());
        int mode = chooser.getFileSelectionMode();
        if(mode == JFileChooser.DIRECTORIES_ONLY) {
            for(int i = 0; i < files.length; i++) {
                File child = files[i];
                if (child.isDirectory()) {
                    list.add(child);
                }
            }
            Collections.sort(list, FILE_NAME_COMPARATOR);
        } else if(mode == JFileChooser.FILES_AND_DIRECTORIES || mode == JFileChooser.FILES_ONLY) {
            ArrayList dirList = new ArrayList();
            ArrayList fileList = new ArrayList();
            for(int i = 0; i < files.length; i++) {
                File child = files[i];
                if (child.isDirectory()) {
                    dirList.add(child);
                } else {
                    fileList.add(child);
                }
            }
            
            Collections.sort(dirList, FILE_NAME_COMPARATOR);
            Collections.sort(fileList, FILE_NAME_COMPARATOR);
            
            list.addAll(dirList);
            list.addAll(fileList);
        }

        return list;
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

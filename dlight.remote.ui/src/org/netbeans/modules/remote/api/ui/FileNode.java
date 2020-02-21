/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
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

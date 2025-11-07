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

package org.netbeans.modules.masterfs.filebasedfs.utils;

import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileSystemView;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.LockForFile;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.openide.filesystems.FileObject;


public final class FileInfo {
    private static boolean IS_WINDOWS = org.openide.util.BaseUtilities.isWindows();

    private int isFile = -1;
    private int isDirectory = -1;
    private int exists = -1;
    private int isComputeNode = -1;
    private int isUnixSpecialFile = -1;
    private int isUNC = -1;    
    private int isConvertibleToFileObject = -1;

    private Integer id = null;        
    private FileInfo root = null;    
    private final File file;
    
    private FileInfo parent = null;
    private FileNaming fileNaming = null;
    private FileObject fObject = null;

    public FileInfo(final File file, int exists) {
        assert file != null;
        this.file = file;
        this.exists = exists; 
    }

    public FileInfo(final File file) {
        assert file != null;
        this.file = file;
    }

    public FileInfo(final FileInfo parent, final File file) {
        this (file);
        this.parent = parent;
    }
    
    public boolean isFile() {
        if (isFile == -1) {
            isFile = (getFile().isFile()) ? 1 : 0;
        }
        return (isFile == 0) ? false : true;
    }

    public boolean isDirectory() {
        if (isDirectory == -1) {
            isDirectory = (getFile().isDirectory()) ? 1 : 0;
        }
        return (isDirectory == 0) ? false : true;
    }
    
    public boolean isDirectoryComputed() {
        return isDirectory != -1;
    }

    public boolean  exists() {
        if (exists == -1) {
            exists = 0;
            if (FileChangedManager.getInstance().exists(getFile())) {
                exists = 1;
            } else {
                String path = getFile().getPath();
                if (path.startsWith("\\\\") && path.indexOf('\\', 2) == -1) { // NOI18N
                    exists = 1;
                }
            }
        }
        return (exists == 0) ? false : true;
    }

    private static FileSystemView FILESYSTEMVIEW;
    private static synchronized FileSystemView fsView() {
        if (FILESYSTEMVIEW == null) {
            try {
                FILESYSTEMVIEW = FileSystemView.getFileSystemView();
            } catch (Throwable ex) {
                FILESYSTEMVIEW = new FileSystemView() {
                    @Override public File createNewFolder(File containingDir) throws IOException {
                        throw new IOException();
                    }
                    @Override public boolean isComputerNode(File dir) {
                        return false;
                    }
                };
            }
        }
        return FILESYSTEMVIEW;
    }
    private boolean isComputeNode() {
        if (isComputeNode == -1) {
            isComputeNode = fsView().isComputerNode(getFile()) ? 1 : 0;
        }

        return (isComputeNode == 1) ? true : false;
    }


    public boolean isUnixSpecialFile() {
        if (isUnixSpecialFile == -1) {
            isUnixSpecialFile = (!IS_WINDOWS && !isDirectory() && !isFile() && exists()) ? 1 : 0;
        }        
        return (isUnixSpecialFile == 1) ? true : false;
    }


    public boolean isUNCFolder() {
        if (isUNC == -1) {
            isUNC = ((isWindows() && !isFile() && !isDirectory() && !exists() && isComputeNode())) ? 1 : 0;
        }                
        return (isUNC == 1) ? true : false;
    }


    public boolean isWindows() {
        return FileInfo.IS_WINDOWS;
    }
    
    public boolean isConvertibleToFileObject() {
        if (isConvertibleToFileObject == -1) {
            isConvertibleToFileObject = (isSupportedFile() && exists()) ?  1 : 0;
        }
        
        return (isConvertibleToFileObject == 1) ? true : false;
    }

    public boolean isSupportedFile() {
        return (!getFile().getName().equals(".nbattrs") &&
                !LockForFile.hasActiveLockFileSigns(getFile().getName()));
    }
    
    public FileInfo getRoot() {
        if (root == null) {
            File tmp = getFile();
            File retVal = tmp;
            while (tmp != null) {
                retVal = tmp;
                tmp = tmp.getParentFile();
            }
            if ("\\\\".equals(retVal.getPath())) {  // NOI18N
                // UNC paths => return \\computerName\sharedFolder (or \\ if path is only \\ or \\computerName)
                String filename = getFile().getAbsolutePath();
                int firstSlash = filename.indexOf("\\", 2);  //NOI18N
                if(firstSlash != -1) {
                    int secondSlash = filename.indexOf("\\", firstSlash+1);  //NOI18N
                    if(secondSlash != -1) {
                        filename = filename.substring(0, secondSlash);
                    }
                    retVal = new File(filename);
                } else {
                    retVal = getFile();
                }
            }
            
            root = new FileInfo (retVal);
        }
        
        return root;
    }


    public File getFile() {
        return file;
    }

    public Integer getID() {
        if (id == null) {
            id = NamingFactory.createID(getFile());
        }        
        return id;
    }

    public FileInfo getParent() {
        return parent;
    }

    public FileNaming getFileNaming() {
        return fileNaming;
    }

    public void setFileNaming(FileNaming fileNaming) {
        this.fileNaming = fileNaming;
    }

    public FileObject getFObject() {
        return fObject;
    }

    public void setFObject(FileObject fObject) {
        this.fObject = fObject;
    }

    @Override
    public String toString() {
        return getFile().toString();
    }

    public static final String composeName(String name, String ext) {
        return (ext != null && ext.length() > 0) ? (name + "." + ext) : name;//NOI18N
    }

    public static final String getName(String name) {
        int i = name.lastIndexOf('.');
        
        /** period at first position is not considered as extension-separator */
        return (i <= 0 || i == (name.length()-1)) ? name : name.substring(0, i);
    }
    
    public static final String getExt(String name) {
        int i = name.lastIndexOf('.') + 1;
        
        /** period at first position is not considered as extension-separator */
        return ((i <= 1) || (i == name.length())) ? "" : name.substring(i); // NOI18N
    }
}

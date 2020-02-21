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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileObject;

public final class FolderEntry implements SourceFolderInfo {

    private final FileObject fileObject;
    private final FSPath fsPath;
    private final String folderName;
    private final boolean addSubfolders;
    //private final FileFilter fileFilter;

    public FolderEntry(FileObject fileObject, String folderName) {
        this.fileObject = fileObject;
        this.fsPath = null;
        this.folderName = folderName;
        addSubfolders = true;
        //fileFilter = null;
    }

    public FolderEntry(FSPath fsPath, String folderName) {
        this.fileObject = null;
        this.fsPath = fsPath;
        this.folderName = folderName;
        addSubfolders = true;
        //fileFilter = null;
    }

    @Override
    public String getFolderName() {
        return folderName;
    }

//    public void setFolderName(String file) {
//        this.folderName = file;
//    }

    @Override
    public boolean isAddSubfoldersSelected() {
        return addSubfolders;
    }

//    public void setAddSubfoldersSelected(boolean selected) {
//        this.addSubfolders = selected;
//    }

    @Override
    public FileObject getFileObject() {
        if (fileObject != null) {
            return fileObject;
        } else {
            return fsPath.getFileObject();
        }
    }

//    public void setFile(File file) {
//        this.file = file;
//    }

//    @Override
//    public FileFilter getFileFilter() {
//        return fileFilter;
//    }

//    public void setFileFilter(FileFilter ff) {
//        fileFilter = ff;
//    }

    @Override
    public String toString() {
        return folderName;
    }
}

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

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;
import org.openide.filesystems.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.openide.util.Enumerations;

public final class RootObjWindows extends FileObject {
    public RootObjWindows() {
    }

    @Override
    public final String getName() {
        return "";//NOI18N
    }

    @Override
    public final String getExt() {
        return "";//NOI18N
    }

    @Override
    public final FileSystem getFileSystem() throws FileStateInvalidException {
        return FileBasedFileSystem.getInstance();
    }

    @Override
    public FileObject getFileObject(String relativePath) {
        return super.getFileObject(relativePath);
    }

    
    @Override
    public final FileObject getParent() {
        return null;
    }

    @Override
    public final boolean isFolder() {
        return true;
    }

    @Override
    public final boolean isData() {
        return !isFolder();
    }

    @Override
    public final Date lastModified() {
        return new Date(0);
    }

    @Override
    public final boolean isRoot() {
        return true;
    }


    /* Test whether the file is valid. The file can be invalid if it has been deserialized
    * and the file no longer exists on disk; or if the file has been deleted.
    *
    * @return true if the file object is valid
    */
    @Override
    public final boolean isValid() {
        return true;
    }

    @Override
    public final void rename(final FileLock lock, final String name, final String ext) throws IOException {
        //throw new IOException(getPath());
        FSException.io("EXC_CannotRenameRoot", getFileSystem().getDisplayName()); // NOI18N        
    }

    @Override
    public final void delete(final FileLock lock) throws IOException {
        //throw new IOException(getPath());
        FSException.io("EXC_CannotDeleteRoot", getFileSystem().getDisplayName()); // NOI18N        
    }

    @Override
    public final Object getAttribute(final String attrName) {        
        return null;
    }

    @Override
    public final void setAttribute(final String attrName, final Object value) throws IOException {  
        throw new FileStateInvalidException(); // NOI18N        
    }

    @Override
    public final Enumeration<String> getAttributes() {
        return Enumerations.empty();
    }

    @Override
    public final void addFileChangeListener(final FileChangeListener fcl) {
        //TODO: adding new FileBasedFS into allInstances should lead to firing event
    }

    @Override
    public final void removeFileChangeListener(final FileChangeListener fcl) {
        //TODO: adding new FileBasedFS into allInstances should lead to firing event
    }

    @Override
    public final long getSize() {
        return 0;
    }

    @Override
    public final InputStream getInputStream() throws FileNotFoundException {
        throw new FileNotFoundException(); // NOI18N        
    }

    @Override
    public final OutputStream getOutputStream(final FileLock lock) throws IOException {
        throw new FileNotFoundException(); // NOI18N 
    }

    @Override
    public final FileLock lock() throws IOException {
        throw new FileStateInvalidException(); // NOI18N        
    }

    @Override
    public final void setImportant(final boolean b) {
    }

    @Override
    public final FileObject[] getChildren() {
        Collection<? extends FileObjectFactory> all = FileBasedFileSystem.factories().values();
        ArrayList<FileObject> rootChildren = new ArrayList<>();         
        for (FileObjectFactory fs : all) {
            BaseFileObj root = fs.getRoot();
            if (root != null) { // #252580
                rootChildren.add(root);
            }
        }
        return rootChildren.toArray(FileObject[]::new);
    }

    @Override
    public final FileObject getFileObject(String name, final String ext) {
        FileObject first = getFileObjectImpl(name, ext);
        if (first != null) {
            return first;
        }
        if (name.length() >= 2 && name.charAt(1) == ':') {
            final File root = new File(name.charAt(0) + ":\\"); // NOI18N
            FileObjectFactory.getInstance(root, true);
        }
        return getFileObjectImpl(name, ext);
    }
    
    private FileObject getFileObjectImpl(String name, String ext) {
        FileObject[] rootChildren =  getChildren();
        for (int i = 0; i < rootChildren.length; i++) {
            FileObject fileObject = rootChildren[i];            
            // UNC absolute path
            if(name.startsWith("//")) { // NOI18N
                // replace '/' by '\'
                name = name.replace('/', '\\');  //NOI18N
            }
            String real = fileObject.getNameExt();
            if (real.endsWith("\\")) {
                real = real.substring(0, real.length() - 1);
            }
            if (FileInfo.composeName(name, ext).equals(real)) {
                return fileObject;
            }
        }
        return null;
    }

    
    @Override
    public final FileObject createFolder(final String name) throws IOException {
        throw new FileStateInvalidException(); // NOI18N        
    }

    @Override
    public final FileObject createData(final String name, final String ext) throws IOException {
        throw new FileStateInvalidException(); // NOI18N        
    }

    @Override
    public final boolean isReadOnly() {
        return true;
    }

    @Override
    public String getPath() {
        return "";
    }

    
    @Override
    public String toString() {
        return "";
    }
}

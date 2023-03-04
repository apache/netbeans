/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.xml.util;

import java.io.*;
import java.util.*;

import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;

/**
 * This file object represents an InputStream.
 *
 * @author  Petr Kuzel
 * @version untested draft
 */
public class StreamFileObject extends FileObject {

    /** Serial Version UID */
    private static final long serialVersionUID =8966806836211837503L;


    private org.openide.filesystems.FileObject[] files; //kids

    private boolean isRoot; //does it represent folder (root);
    
    private InputStream peer; //wrapped input stream
    
    private FileSystem fs;
    
    
    //my filesystem
    
    /** Creates new StreamFileObject */
    public StreamFileObject(InputStream in) {
        this(in, false);
    }
    
    public StreamFileObject(InputStream in, boolean isRoot) {
        this.isRoot = isRoot;
        peer = in;
        if (isRoot) {
            files = new org.openide.filesystems.FileObject[] {
                new StreamFileObject(in)
            };
        } else {
            files = new org.openide.filesystems.FileObject[0];
        }
        fs = new StreamFileSystem(this);
    }

    public org.openide.filesystems.FileObject[] getChildren() {
        return files;
    }
    
    public void removeFileChangeListener(org.openide.filesystems.FileChangeListener fileChangeListener) {
    }
    
    public org.openide.filesystems.FileLock lock() throws java.io.IOException {
        return FileLock.NONE;
    }
    
    public java.lang.Object getAttribute(java.lang.String str) {
        return null;
    }
    
    public java.util.Date lastModified() {
        return new Date(0L);
    }
    
    public java.lang.String getExt() {
        return "InputStream"; // NOI18N
    }
    
    @Deprecated
    public boolean isReadOnly() {
        return true;
    }
    
    public org.openide.filesystems.FileObject createData(java.lang.String str, java.lang.String str1) throws java.io.IOException {
        return null;
    }
    
    public void delete(org.openide.filesystems.FileLock fileLock) throws java.io.IOException {
    }
    
    public org.openide.filesystems.FileObject createFolder(java.lang.String str) throws java.io.IOException {
        return null;
    }
    
    public void rename(org.openide.filesystems.FileLock fileLock, java.lang.String str, java.lang.String str2) throws java.io.IOException {        
    }
    
    public boolean isData() {
        return isRoot == false; 
    }
    
    public java.io.OutputStream getOutputStream(org.openide.filesystems.FileLock fileLock) throws java.io.IOException {
        throw new IOException("r/o"); // NOI18N
    }
    
    public java.io.InputStream getInputStream() throws java.io.FileNotFoundException {
        return peer;
    }
    
    public boolean isValid() {
        return true;
    }
    
    public java.util.Enumeration<String> getAttributes() {
        return org.openide.util.Enumerations.empty();
    }
    
    public java.lang.String getName() {
        return "StreamFileObject"; // NOI18N
    }
    
    @Deprecated
    public void setImportant(boolean param) {
    }
    
    public boolean isFolder() {
        return isRoot;
    }
    
    public void setAttribute(java.lang.String str, java.lang.Object obj) throws java.io.IOException {
    }
    
    public void addFileChangeListener(org.openide.filesystems.FileChangeListener fileChangeListener) {
    }
    
    public long getSize() {
        return 766; //!!!
    }
    
    public org.openide.filesystems.FileObject getParent() {
        if (isRoot) return null;
        return fs.getRoot();
    }
    
    public boolean isRoot() {
        return isRoot;
    }
    
    public org.openide.filesystems.FileObject getFileObject(java.lang.String str, java.lang.String str1) {
        return null;
    }
    
    public org.openide.filesystems.FileSystem getFileSystem() throws org.openide.filesystems.FileStateInvalidException {
        return fs;
    }
    
}

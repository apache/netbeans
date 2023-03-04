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

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

/**
 * @author Radek Matous
 */
public class ReplaceForSerialization extends Object implements java.io.Serializable {
    /**
     * generated Serialized Version UID
     */
    static final long serialVersionUID = -7451332135435542113L;
    private final String absolutePath;

    public ReplaceForSerialization(final File file) {
        absolutePath = file.getAbsolutePath();
    }

    public final Object readResolve() {
        final File file = new File(absolutePath);
        final FileObject retVal = FileBasedFileSystem.getInstance().getFileObject(file);               
        return (retVal != null) ? retVal : new Invalid (file);
    }

    private static class Invalid extends BaseFileObj {
        protected Invalid(File file) {
            super(file);
        }

        public void delete(FileLock lock, ProvidedExtensions.IOHandler io) throws IOException {
            throw new IOException(getPath()); 
        }
        
        boolean checkLock(FileLock lock) throws IOException {
            return false;
        }

        protected void setValid(boolean valid) {}

        public boolean isFolder() {
            return false;
        }

        /* Test whether the file is valid. The file can be invalid if it has been deserialized
        * and the file no longer exists on disk; or if the file has been deleted.
        *
        * @return true if the file object is valid
        */
        public boolean isValid() {
            return false;
        }

        public InputStream getInputStream() throws FileNotFoundException {
            throw new FileNotFoundException (getPath());
        }

        public OutputStream getOutputStream(FileLock lock) throws IOException {
            throw new IOException (getPath());
        }

        public FileLock lock() throws IOException {
            throw new IOException (getPath());
        }

        public FileObject[] getChildren() {
            return new FileObject[] {};
        }

        public FileObject getFileObject(String name, String ext) {
            return null;
        }

        public FileObject createFolder(String name) throws IOException {
            throw new IOException (getPath());
        }

        public FileObject createData(String name, String ext) throws IOException {
            throw new IOException (getPath());
        }

        public void refreshImpl(final boolean expected, boolean fire) {
        }

        @Override
        protected boolean noFolderListeners() {
            return true;
        }
    }
}

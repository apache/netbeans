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
package org.openide.filesystems;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import org.openide.util.Enumerations;

final class VirtualFileObject extends AbstractFolder {
    private final boolean folder;

    public VirtualFileObject(FileSystem fileSystem, FileObject myObj, String nameExt, boolean folder) {
        super(fileSystem, myObj, nameExt);
        this.folder = folder;
        this.validFlag = false;
    }

    @Override
    void setAttribute(String attrName, Object value, boolean fire) throws IOException {
        throw new IOException();
    }

    @Override
    protected String[] list() {
        return new String[0];
    }

    @Override
    protected AbstractFolder createFile(String name) {
        return new VirtualFileObject(this.getFileSystem(), this, name, false);
    }

    @Override
    void handleDelete(FileLock lock) throws IOException {
        throw new IOException();
    }

    @Override
    public void rename(FileLock lock, String name, String ext) throws IOException {
        throw new IOException();
    }

    @Override
    public boolean isFolder() {
        return folder;
    }

    @Override
    public Date lastModified() {
        return getParent().lastModified();
    }

    @Override
    public boolean isData() {
        return !folder;
    }

    @Override
    public Object getAttribute(String attrName) {
        return null;
    }

    @Override
    public void setAttribute(String attrName, Object value) throws IOException {
        throw new IOException();
    }

    @Override
    public Enumeration<String> getAttributes() {
        return Enumerations.empty();
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        throw new FileNotFoundException();
    }

    @Override
    public OutputStream getOutputStream(FileLock lock) throws IOException {
        throw new IOException();
    }

    @Override
    public FileLock lock() throws IOException {
        throw new IOException();
    }

    @Override
    public void setImportant(boolean b) {
    }

    @Override
    public FileObject createFolder(String name) throws IOException {
        throw new IOException();
    }

    @Override
    public FileObject createData(String name, String ext) throws IOException {
        throw new IOException();
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public boolean isVirtual() {
        return true;
    }

    @Override
    URL computeURL() {
        URL url = getParent().computeURL();
        try {
            return new URL(url, getNameExt());
        } catch (MalformedURLException ex) {
            return super.computeURL();
        }
    }
}

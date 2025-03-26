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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;
import org.openide.filesystems.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;

public final class RootObj<T extends FileObject> extends FileObject {
    private T realRoot = null;

    public RootObj(final T realRoot) {
        this.realRoot = realRoot;
    }

    public final String getName() {
        return getRealRoot().getName();//NOI18N
    }

    public final String getExt() {
        return getRealRoot().getExt();//NOI18N
    }

    public final FileSystem getFileSystem() throws FileStateInvalidException {
        return getRealRoot().getFileSystem();
    }

    public final FileObject getParent() {
        return null;
    }

    public final boolean isFolder() {
        return true;
    }

    public final boolean isData() {
        return !isFolder();
    }

    public final Date lastModified() {
        return new Date(0);
    }

    public final boolean isRoot() {
        return true;
    }


    /* Test whether the file is valid. The file can be invalid if it has been deserialized
    * and the file no longer exists on disk; or if the file has been deleted.
    *
    * @return true if the file object is valid
    */
    public final boolean isValid() {
        return true;
    }

    public final void rename(final FileLock lock, final String name, final String ext) throws IOException {
        //throw new IOException(getPath());
        FSException.io("EXC_CannotRenameRoot", getFileSystem().getDisplayName()); // NOI18N        
    }

    public final void delete(final FileLock lock) throws IOException {
        //throw new IOException(getPath());
        FSException.io("EXC_CannotDeleteRoot", getFileSystem().getDisplayName()); // NOI18N        
    }

    public final Object getAttribute(final String attrName) {        
        if (attrName.equals("SupportsRefreshForNoPublicAPI")) {
            return true;
        }
        if (attrName.equals("refreshSlow")) {
            return new RefreshSlow();
        }
        return getRealRoot().getAttribute(attrName);
    }

    public final void setAttribute(final String attrName, final Object value) throws IOException {
        if ("request_for_refreshing_files_be_aware_this_is_not_public_api".equals(attrName) && (value instanceof File[])) {//NOI18N
            invokeRefreshFor(null, (File[])value);
            return;
        }        
        getRealRoot().setAttribute(attrName, value);
    }
    
    static void invokeRefreshFor(RefreshSlow slow, File[] files) {
        invokeRefreshFor(slow, files, false);
    }
    static void invokeRefreshFor(RefreshSlow slow, File[] files, boolean ignoreRecursiveListeners) {
        //first normalize
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            files[i] = FileUtil.normalizeFile(file);
        }
        Map<FileObjectFactory, List<File>> files2Factory = new HashMap<FileObjectFactory, List<File>>();
        Map<File, ? extends FileObjectFactory> roots2Factory = FileBasedFileSystem.factories();
        Arrays.sort(files);
        for (File file : files) {
            FileObjectFactory factory =  roots2Factory.get(file);
            if (factory == null) {
                // UNC - do not use getParentFile to search for root
                factory =  roots2Factory.get(new FileInfo(file).getRoot().getFile());
            }
            if (factory != null) {
                List<File> lf = files2Factory.get(factory);
                if (lf == null) {
                    lf = new ArrayList<File>();
                    files2Factory.put(factory, lf);
                } else {
                    File tmp = file;
                    while (tmp.getParentFile() != null) {
                        if (lf.contains(tmp)) {
                            tmp = null;
                            break;
                        }
                        tmp = tmp.getParentFile();
                    }                    
                    if (tmp == null) {
                        continue;
                    }
                }
                lf.add(file);
            }
        }
        if (slow != null) {
            int cnt = 0;
            for (Map.Entry<FileObjectFactory, List<File>> entry : files2Factory.entrySet()) {
                FileObjectFactory factory = entry.getKey();
                cnt += factory.getSize();
            }
            slow.estimate(cnt);
        }
        for (Map.Entry<FileObjectFactory, List<File>> entry : files2Factory.entrySet()) {
            FileObjectFactory factory = entry.getKey();
            List<File> lf = entry.getValue();
            if (lf.size() == 1) {
                for (File file : lf) {
                    if (file.getParentFile() == null) {
                        factory.refresh(slow, ignoreRecursiveListeners, true);
                    } else {
                        factory.refreshFor(slow, ignoreRecursiveListeners, file);
                    }
                }
            } else if (lf.size() > 1) {
                final File[] arr = lf.toArray(new File[0]);
                Arrays.sort(arr);
                factory.refreshFor(slow, ignoreRecursiveListeners, arr);
            }
        }
    }
    

    public final Enumeration<String> getAttributes() {
        return getRealRoot().getAttributes();
    }

    public final void addFileChangeListener(final FileChangeListener fcl) {
        getRealRoot().addFileChangeListener(fcl);
    }

    public final void removeFileChangeListener(final FileChangeListener fcl) {
        getRealRoot().removeFileChangeListener(fcl);
    }

    public final long getSize() {
        return 0;
    }

    public final InputStream getInputStream() throws FileNotFoundException {
        return getRealRoot().getInputStream();
    }

    public final OutputStream getOutputStream(final FileLock lock) throws IOException {
        return getRealRoot().getOutputStream(lock);
    }

    public final FileLock lock() throws IOException {
        return getRealRoot().lock();
    }

    @Deprecated
    public final void setImportant(final boolean b) {
        getRealRoot().setImportant(b); 
    }

    public final FileObject[] getChildren() {
        return getRealRoot().getChildren();
    }

    public final FileObject getFileObject(final String name, final String ext) {
        return getRealRoot().getFileObject(name, ext);
    }

    @Override
    public final FileObject getFileObject(String relativePath) {
        return getRealRoot().getFileObject(relativePath);
    }

    public final FileObject createFolder(final String name) throws IOException {
        return getRealRoot().createFolder(name);
    }

    public final FileObject createData(final String name, final String ext) throws IOException {
        return getRealRoot().createData(name, ext);
    }

    @Deprecated
    public final boolean isReadOnly() {
        return getRealRoot().isReadOnly();
    }

    public final T getRealRoot() {
        return realRoot;
    }

    @Override
    public String getPath() {
        return "";
    }

    
    @Override
    public String toString() {
        return getRealRoot().toString();
    }
}

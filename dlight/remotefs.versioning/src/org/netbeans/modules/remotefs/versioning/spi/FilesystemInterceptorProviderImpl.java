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
package org.netbeans.modules.remotefs.versioning.spi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.remote.impl.fileoperations.spi.AnnotationProvider;
import org.netbeans.modules.remote.impl.fileoperations.spi.FileOperationsProvider;
import org.netbeans.modules.remote.impl.fileoperations.spi.FileOperationsProvider.FileOperations;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor;
import org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor.VCSAnnotationEvent;
import org.openide.filesystems.*;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service = FilesystemInterceptorProvider.class, position = 1000)
public class FilesystemInterceptorProviderImpl extends FilesystemInterceptorProvider {
    private final Map<FileSystem, FilesystemInterceptor> map = new HashMap<>();

    @Override
    public synchronized FilesystemInterceptor getFilesystemInterceptor(FileSystem fs) {
        FilesystemInterceptor interceptor;
        synchronized (map) {
            interceptor = map.get(fs);
            if (interceptor == null) {
                interceptor = new FilesystemInterceptorImpl(fs);
                map.put(fs, interceptor);
            }
        }
        return interceptor;
    }

    private static final class FilesystemInterceptorImpl implements FilesystemInterceptor, FileChangeListener, VCSFilesystemInterceptor.VCSAnnotationListener {
        private final FileSystem fs;
        
        public FilesystemInterceptorImpl(FileSystem fs) {
            this.fs = fs;
            VCSFilesystemInterceptor.registerFileStatusListener(this);
            fs.addFileChangeListener(this);
        }
        
        @Override
        public void annotationChanged(VCSAnnotationEvent ev) {
            ((VersioningAnnotationProviderImpl)AnnotationProvider.getDefault()).deliverStatusEvent(fs, ev);
        }

        @Override
        public boolean canWriteReadonlyFile(FileProxyI file) {
            return VCSFilesystemInterceptor.canWriteReadonlyFile(toVCSFileProxy(file));
        }

        @Override
        public Object getAttribute(FileProxyI file, String attrName) {
            return VCSFilesystemInterceptor.getAttribute(toVCSFileProxy(file), attrName);
        }

        @Override
        public void beforeChange(FileProxyI file) {
            VCSFilesystemInterceptor.beforeChange(toVCSFileProxy(file));
        }

        @Override
        public void fileChanged(FileProxyI file) {
            VCSFilesystemInterceptor.fileChanged(toVCSFileProxy(file));
        }
        
        @Override
        public void fileChanged(FileEvent fe) {
            VCSFilesystemInterceptor.fileChanged(toVCSFileProxy(fe.getFile()));
       }

        @Override
        public IOHandler getDeleteHandler(FileProxyI file) {
            final VCSFilesystemInterceptor.IOHandler deleteHandler = VCSFilesystemInterceptor.getDeleteHandler(toVCSFileProxy(file));
            if (deleteHandler != null) {
                return new IOHandler() {

                    @Override
                    public void handle() throws IOException {
                        deleteHandler.handle();
                    }
                };
            }
            return null;
        }

        @Override
        public void deleteSuccess(FileProxyI file) {
            VCSFilesystemInterceptor.deleteSuccess(toVCSFileProxy(file));
        }

        @Override
        public void deletedExternally(FileProxyI file) {
            VCSFilesystemInterceptor.deletedExternally(toVCSFileProxy(file));
        }

        @Override
        public void fileDeleted(FileEvent fe) {
        }

        @Override
        public void beforeCreate(FileProxyI parent, String name, boolean isFolder) {
            VCSFilesystemInterceptor.beforeCreate(toVCSFileProxy(parent), name, isFolder);
        }

        @Override
        public void createFailure(FileProxyI parent, String name, boolean isFolder) {
            VCSFilesystemInterceptor.createFailure(toVCSFileProxy(parent), name, isFolder);
        }

        @Override
        public void createSuccess(FileProxyI fo) {
            VCSFilesystemInterceptor.createSuccess(toVCSFileProxy(fo));
        }

        @Override
        public void createdExternally(FileProxyI fo) {
            VCSFilesystemInterceptor.createdExternally(toVCSFileProxy(fo));
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
        }

        @Override
        public IOHandler getMoveHandler(FileProxyI from, FileProxyI to) {
            final VCSFilesystemInterceptor.IOHandler moveHandler = VCSFilesystemInterceptor.getMoveHandler(toVCSFileProxy(from), toVCSFileProxy(to));
            if (moveHandler != null) {
                return new IOHandler() {

                    @Override
                    public void handle() throws IOException {
                        moveHandler.handle();
                    }
                };
            }
            return null;
        }

        @Override
        public IOHandler getRenameHandler(FileProxyI from, String newName) {
            final VCSFilesystemInterceptor.IOHandler renameHandler = VCSFilesystemInterceptor.getRenameHandler(toVCSFileProxy(from), newName);
            if (renameHandler != null) {
                return new IOHandler() {

                    @Override
                    public void handle() throws IOException {
                        renameHandler.handle();
                    }
                };
            }
            return null;
        }
        
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            String name = fe.getName();
            String ext = fe.getExt();
            if(ext != null && !ext.isEmpty()) {
                name += "." + ext; // NOI18N
            }
            VCSFilesystemInterceptor.afterMove(
                VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(fe.getFile()).getParentFile(), name),
                toVCSFileProxy(fe.getFile())
            );
        }

        @Override
        public void afterMove(FileProxyI from, FileProxyI to) {
            VCSFilesystemInterceptor.afterMove(toVCSFileProxy(from), toVCSFileProxy(to));
        }

        @Override
        public FilesystemInterceptorProvider.IOHandler getCopyHandler(FileProxyI from, FileProxyI to) {
            final VCSFilesystemInterceptor.IOHandler copyHandler = VCSFilesystemInterceptor.getCopyHandler(toVCSFileProxy(from), toVCSFileProxy(to));
            if (copyHandler != null) {
                return new FilesystemInterceptorProvider.IOHandler() {

                    @Override
                    public void handle() throws IOException {
                        copyHandler.handle();
                    }
                };
            }
            return null;
        }

        @Override
        public void beforeCopy(FileProxyI from, FileProxyI to) {
            VCSFilesystemInterceptor.beforeCopy(toVCSFileProxy(from), toVCSFileProxy(to));
        }

        @Override
        public void copySuccess(FileProxyI from, FileProxyI to) {
            VCSFilesystemInterceptor.copySuccess(toVCSFileProxy(from), toVCSFileProxy(to));
        }

        @Override
        public void fileLocked(FileProxyI fo) throws IOException {
            VCSFilesystemInterceptor.fileLocked(toVCSFileProxy(fo));
        }

        @Override
        public long refreshRecursively(FileProxyI dir, long lastTimeStamp, List<? super FileProxyI> children) {
            List<VCSFileProxy> res = new ArrayList<>();
            for(Object f : children) {
                res.add(toVCSFileProxy((FileProxyI)f));
            }
            // The below looks a bit strange, but VCSFilesystemInterceptor.listFiles 
            // in fact calls refreshRecursively (and that's all it does -
            // so I'd propose to rename it as well, but it resides in versioning.core and has too many dependencies
            return VCSFilesystemInterceptor.listFiles(toVCSFileProxy(dir), lastTimeStamp, res);
        }

        @Override
        public String toString() {
            return fs.getDisplayName();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    }

    public static VCSFileProxy toVCSFileProxy(FileObject file) {
        return VCSFileProxy.createFileProxy(file);
    }

    public static VCSFileProxy toVCSFileProxy(FileProxyI proxy) {
        FileSystem fileSystem = proxy.getFileSystem();
        VCSFileProxy res;
        FileOperations fileOperations = (FileOperations) fileSystem.getRoot().getAttribute(FileOperationsProvider.ATTRIBUTE);
        if (fileOperations != null) {
            res = VCSFileProxy.createFileProxy(fileSystem.getRoot());
            String[] split = proxy.getPath().split("/"); // NOI18N
            for(int i = 0; i < split.length; i++) {
                if (split[i].isEmpty() || ".".equals(split[i])) { // NOI18N
                    continue;
                }
                res = VCSFileProxy.createFileProxy(res, split[i]);
            }
        } else {
            res = VCSFileProxy.createFileProxy(new File(proxy.getPath()));
        }
        return res;
    }
}

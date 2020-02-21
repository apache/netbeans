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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.extexecution.ProcessBuilder;
import org.netbeans.modules.remote.impl.fileoperations.spi.FileOperationsProvider;
import org.netbeans.modules.remote.impl.fileoperations.spi.RemoteVcsSupportUtil;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remotefs.versioning.api.RemoteVcsSupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.filesystems.VCSFileProxyOperations;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 */
@ServiceProviders({
    @ServiceProvider(service=FileOperationsProvider.class, position = 1000),
    @ServiceProvider(service=VCSFileProxyOperations.Provider.class, position = 1000)
})

public class FileProxyProviderImpl extends FileOperationsProvider implements VCSFileProxyOperations.Provider {
    private final Map<FileSystem, FileOperationsImpl> map = new HashMap<>();
    private static final Logger LOG = Logger.getLogger(FileProxyProviderImpl.class.getName());

    @Override
    public synchronized FileOperations getFileOperations(FileSystem fs) {
        return getFileOperationsImpl(fs);
    }

    private synchronized FileOperationsImpl getFileOperationsImpl(FileSystem fs) {
        FileOperationsImpl fileOperations;
        synchronized(map) {
            fileOperations = map.get(fs);
            if (fileOperations == null) {
                fileOperations = new FileOperationsImpl(fs);
                map.put(fs, fileOperations);
            }
        }
        return fileOperations;
    }

    @Override
    public VCSFileProxyOperations getVCSFileProxyOperations(URI uri) {
        FileSystem fs = FileSystemProvider.getFileSystem(uri);
        return getFileOperationsImpl(fs);
    }

    @Override
    public VCSFileProxyOperations getVCSFileProxyOperations(FileSystem fs) {
        return getFileOperationsImpl(fs);
    }

    
    private static final class FileOperationsImpl extends FileOperations implements VCSFileProxyOperations {
        private boolean assertIt = false;
        
        protected FileOperationsImpl(FileSystem fs) {
            super(fs);
            // comment assert because IDE team is not going to fix calling IO operations in EDT
            //assert (assertIt = true);
        }

        @Override
        public String getName(VCSFileProxy file) {
            return getName(toFileProxy(file));
        }

        @Override
        public boolean isDirectory(VCSFileProxy file) {
            softEDTAssert();
            return isDirectory(toFileProxy(file));
        }

        @Override
        public boolean isFile(VCSFileProxy file) {
            softEDTAssert();
            return isFile(toFileProxy(file));
        }

        @Override
        public boolean canWrite(VCSFileProxy file) {
            softEDTAssert();
            return canWrite(toFileProxy(file));
        }

        @Override
        public VCSFileProxy getParentFile(VCSFileProxy file) {
            softEDTAssert();
            String parent = getDir(toFileProxy(file));
            if (parent == null) {
                return null;
            }
            FileObject root = getRoot();
            VCSFileProxy res = VCSFileProxy.createFileProxy(root);
            String[] split = parent.split("/"); // NOI18N
            for (int i = 0; i < split.length; i++) {
                if (split[i].isEmpty() || ".".equals(split[i])) { // NOI18N
                    continue;
                }
                res = VCSFileProxy.createFileProxy(res, split[i]);
            }
            return res;
        }


        @Override
        public String getAbsolutePath(VCSFileProxy file) {
            return file.getPath();
        }

        @Override
        public boolean exists(VCSFileProxy file) {
            softEDTAssert();
            return exists(toFileProxy(file));
        }

        @Override
        public VCSFileProxy normalize(VCSFileProxy file) {
            softEDTAssert();
            String path = normalizeUnixPath(toFileProxy(file));
            if (file.getPath().equals(path)) {
                return file;
            }
            FileObject root = getRoot();
            VCSFileProxy res = VCSFileProxy.createFileProxy(root);
            String[] split = path.split("/"); // NOI18N
            for (int i = 0; i < split.length; i++) {
                if (split[i].isEmpty() || ".".equals(split[i])) { // NOI18N
                    continue;
                }
                res = VCSFileProxy.createFileProxy(res, split[i]);
            }
            return res;
        }

        @Override
        public FileObject toFileObject(VCSFileProxy path) {
            softEDTAssert();
            return toFileObject(toFileProxy(path));
        }

        @Override
        public URI toURI(VCSFileProxy file) throws URISyntaxException {
            Boolean isDirFast = null;
            try {
                isDirFast = RemoteVcsSupportUtil.isDirectoryFast(RemoteVcsSupport.getFileSystem(file), file.getPath());
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
            boolean isDir = (isDirFast == null) ? file.isDirectory() : isDirFast;
            return super.toURI(file.getPath(), isDir);
        }

        @Override
        public VCSFileProxy[] list(VCSFileProxy path) {
            softEDTAssert();
            String[] list = list(toFileProxy(path));
            if (list == null) {
                return null;
            }
            VCSFileProxy[] res = new VCSFileProxy[list.length];
            for(int i = 0; i < list.length; i++) {
                res[i] = VCSFileProxy.createFileProxy(path, list[i]);
            }
            return res;
        }

        @Override
        public ProcessBuilder createProcessBuilder(VCSFileProxy file) {
            softEDTAssert();
            return createProcessBuilder(toFileProxy(file));
        }

        @Override
        public void refreshFor(VCSFileProxy... files) {
            List<FileProxyO> list = new ArrayList<>();
            for(VCSFileProxy f : files) {
                list.add(toFileProxy(f));
            }
            refreshFor(list.toArray(new FileProxyO[list.size()]));
        }

        @Override
        public long lastModified(VCSFileProxy file) {
            softEDTAssert();
            return lastModified(toFileProxy(file));
        }

        @Override
        public InputStream getInputStream(VCSFileProxy file, boolean checkLock) throws FileNotFoundException {
            softEDTAssert();
            FileObject fo = toFileObject(file);
            if (fo == null) {
                if (file.exists()) {
                    VCSFileProxy parent = file.getParentFile();
                    while(parent != null) {
                        FileObject parentFO = parent.toFileObject();
                        if (parentFO != null) {
                            parentFO.refresh();
                            break;
                        }
                        parent = parent.getParentFile();
                    }
                }
                fo = toFileObject(file);
                if (fo == null) {
                    throw new FileNotFoundException("File not found: " + file.getPath()); //NOI18N
                }
            }
            return getInputStream(fo, checkLock);
        }

        private static final Set<Integer> alreadyTraced = new HashSet<>();
        private void softEDTAssert() {
            if (assertIt) {
                if (SwingUtilities.isEventDispatchThread()) {
                    final Exception exception = new Exception();
                    int hashCode = Arrays.hashCode(exception.getStackTrace());
                    if (alreadyTraced.add(hashCode)) {
                        LOG.log(Level.INFO, "Method cannot be called in EDT", exception); //NOI18N
                    }
                }
            }
        }
    }

    private static FileProxyO toFileProxy(final VCSFileProxy file) {
        return new FileProxyOImpl(file);
    }

    private static final class FileProxyOImpl implements FileProxyO {

        private final VCSFileProxy file;

        public FileProxyOImpl(VCSFileProxy file) {
            this.file = file;
        }

        @Override
        public String getPath() {
            return file.getPath();
        }

        @Override
        public String toString() {
            return file.getPath();
        }

        @Override
        public int hashCode() {
            return file.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FileProxyOImpl other = (FileProxyOImpl) obj;
            if (this.file != other.file && (this.file == null || !this.file.equals(other.file))) {
                return false;
            }
            return true;
        }

    }
}

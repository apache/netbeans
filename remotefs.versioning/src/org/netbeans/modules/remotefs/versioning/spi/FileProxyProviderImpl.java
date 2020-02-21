/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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

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

package org.netbeans.modules.git.remote;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitRepository;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.remotefs.versioning.spi.FilesystemInterceptorProviderImpl;
import org.netbeans.modules.remotefs.versioning.spi.VersioningAnnotationProviderImpl;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 */
public abstract class AbstractLocalGitTestCase extends NbTestCase {

    protected VCSFileProxy repositoryLocation;
    protected static final String NULL_OBJECT_ID = "0000000000000000000000000000000000000000";
    protected StatusRefreshLogHandler refreshHandler;

    private enum Scope{All, Successful, Failed};
    private static final Scope TESTS_SCOPE = Scope.Successful;
    private boolean skipTest = false;

    public AbstractLocalGitTestCase (String name) {
        super(name);
        String gitPath = "/usr/bin/git";
        if (!new File(gitPath).exists()) {
            skipTest = true;
        }
    }
    
    protected abstract boolean isFailed();
    protected abstract boolean isRunAll();

    @Override
    public boolean canRun() {
        if (skipTest) {
            return false;
        }
        if (!isRunAll()) {
            switch (TESTS_SCOPE) {
                case Failed:
                    return isFailed();
                case Successful:
                    return !isFailed();
            }
        }
        return super.canRun();
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("versioning.git.handleExternalEvents", "false");
        System.setProperty("org.netbeans.modules.masterfs.watcher.disable", "true");
        System.setProperty("org.netbeans.modules.git.remote.localfilesystem.enable", "true");
        VCSFileProxy workDir = VCSFileProxy.createFileProxy(getWorkDir());
        VCSFileProxy userdir = VCSFileProxy.createFileProxy(workDir.getParentFile(), "userdir");
        VCSFileProxySupport.mkdirs(userdir);
        System.setProperty("netbeans.user", userdir.getPath());
        super.setUp();
        repositoryLocation = VCSFileProxy.createFileProxy(workDir, "work");
        clearWorkDir();
        VCSFileProxySupport.mkdirs(repositoryLocation);
        getClient(repositoryLocation).init(GitUtils.NULL_PROGRESS_MONITOR);
        VCSFileProxy repositoryMetadata = VCSFileProxy.createFileProxy(repositoryLocation, ".git");
        assertTrue(repositoryMetadata.exists());
        MockServices.setServices(new Class[] {VersioningAnnotationProviderImpl.class, GitVCS.class, FilesystemInterceptorProviderImpl.class});
        Git.STATUS_LOG.setLevel(Level.ALL);
        refreshHandler = new StatusRefreshLogHandler(repositoryLocation);
        Git.STATUS_LOG.addHandler(refreshHandler);
    }

    @Override
    protected void tearDown() throws Exception {
        Git.STATUS_LOG.removeHandler(refreshHandler);
        Git.shutDown();
        super.tearDown();
    }
    
    protected VCSFileProxy getRepositoryLocation() {
        return repositoryLocation;
    }
    
    protected VCSFileProxy createFolder(String name) throws IOException {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject folder = wd.createFolder(name);
        return VCSFileProxy.createFileProxy(folder);
    }

    protected VCSFileProxy createFolder(VCSFileProxy parent, String name) throws IOException {
        FileObject parentFO = parent.toFileObject();
        FileObject folder = parentFO.createFolder(name);
        return VCSFileProxy.createFileProxy(folder);
    }

    protected VCSFileProxy createFile(VCSFileProxy parent, String name) throws IOException {
        FileObject parentFO = parent.toFileObject();
        FileObject fo = parentFO.createData(name);
        return VCSFileProxy.createFileProxy(fo);
    }

    protected VCSFileProxy createFile(String name) throws IOException {
        return createFile(VCSFileProxy.createFileProxy(getWorkDir()), name);
    }

    protected void write(VCSFileProxy file, String str) throws IOException {
        OutputStreamWriter w = null;
        try {
            w = new OutputStreamWriter(VCSFileProxySupport.getOutputStream(file));
            w.write(str);
            w.flush();
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    protected String read (VCSFileProxy file) throws IOException {
        BufferedReader r = null;
        try {
            StringBuilder sb = new StringBuilder();
            r = new BufferedReader(new InputStreamReader(file.getInputStream(false), "UTF-8"));
            for (String line = r.readLine(); line != null; line = r.readLine()) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(line);
            }
            return sb.toString();
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }

    protected FileStatusCache getCache () {
        return Git.getInstance().getFileStatusCache();
    }

    protected GitClient getClient (VCSFileProxy repositoryLocation) throws GitException {
        return GitRepository.getInstance(repositoryLocation).createClient();
    }

    protected void add (VCSFileProxy... files) throws GitException {
        getClient(repositoryLocation).add(files == null ? new VCSFileProxy[0] : files, GitUtils.NULL_PROGRESS_MONITOR);
    }

    protected void commit (VCSFileProxy... files) throws GitException {
        getClient(repositoryLocation).commit(files == null ? new VCSFileProxy[0] : files, "blablabla", null, null, GitUtils.NULL_PROGRESS_MONITOR);
    }

    protected void delete (boolean cached, VCSFileProxy... files) throws GitException {
        getClient(repositoryLocation).remove(files == null ? new VCSFileProxy[0] : files, cached, GitUtils.NULL_PROGRESS_MONITOR);
    }

    protected VCSFileProxy initSecondRepository () throws GitException {
        VCSFileProxy secondRepositoryFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), "work_2"); //NOI18N
        VCSFileProxySupport.mkdirs(secondRepositoryFolder);
        getClient(secondRepositoryFolder).init(GitUtils.NULL_PROGRESS_MONITOR);
        assertTrue(secondRepositoryFolder.isDirectory());
        return secondRepositoryFolder;
    }

    protected class StatusRefreshLogHandler extends Handler {
        private Set<VCSFileProxy> filesToRefresh;
        private boolean filesRefreshed;
        private final HashSet<VCSFileProxy> refreshedFiles = new HashSet<>();
        private final VCSFileProxy topFolder;
        private final Set<String> interestingFiles = new HashSet<>();
        private boolean active;
        private CountDownLatch latch;

        public StatusRefreshLogHandler (VCSFileProxy topFolder) {
            this.topFolder = topFolder;
        }

        @Override
        public void publish(LogRecord record) {
            if (!active) {
                return;
            }
            if (record.getMessage().contains("refreshAllRoots() roots: finished")) {
                synchronized (this) {
                    if (refreshedFiles.containsAll(filesToRefresh)) {
                        filesRefreshed = true;
                        latch.countDown();
                    }
                }
            } else if (record.getMessage().contains("refreshAllRoots() roots: ")) {
                synchronized (this) {
                    for (VCSFileProxy f : (Set<VCSFileProxy>) record.getParameters()[0]) {
                        if (f.getPath().startsWith(topFolder.getPath()))
                        refreshedFiles.add(f);
                    }
                }
            } else if (record.getMessage().equals("refreshAllRoots() file status: {0} {1}")) {
                interestingFiles.add((String) record.getParameters()[0]);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        public void setFilesToRefresh (Set<VCSFileProxy> files) {
            active = false;
            filesRefreshed = false;
            refreshedFiles.clear();
            filesToRefresh = files;
            interestingFiles.clear();
            active = true;
            latch = new CountDownLatch(1);
        }

        public boolean waitForFilesToRefresh () throws InterruptedException {
            return latch.await(5, TimeUnit.SECONDS);
        }

        public boolean getFilesRefreshed () {
            return filesRefreshed;
        }

        Set<String> getInterestingFiles () {
            return new HashSet<>(interestingFiles);
        }

    }

    protected final void runExternally (VCSFileProxy workdir, List<String> command) throws Exception {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        String[] args = command.toArray(new String[command.size()]);
        VCSFileProxySupport.mkdirs(workdir);
        ProcessUtils.ExitStatus executeInDir = ProcessUtils.executeInDir(workdir.getPath(), null, false, canceled, workdir, "git", args);
        if (!executeInDir.error.isEmpty()) {
            throw new Exception(executeInDir.error);
        }
    }
    
    protected void renameDO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(from.toFileObject());
        daoFrom.rename(to.getName());
    }

    protected void renameFO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        // ensure parent is known by filesystems
        // otherwise no event will be thrown
        FileObject parent = from.getParentFile().toFileObject();
        FileObject foFrom = from.toFileObject();
        FileLock lock = foFrom.lock();
        try {
            foFrom.rename(lock, to.getName(), null);
        } finally {
            lock.releaseLock();
        }
    }

    protected void moveDO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(from.toFileObject());
        DataObject daoTarget = DataObject.find(to.getParentFile().toFileObject());
        daoFrom.move((DataFolder) daoTarget);
    }

    protected void copyDO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(from.toFileObject());
        DataObject daoTarget = DataObject.find(to.getParentFile().toFileObject());
        daoFrom.copy((DataFolder) daoTarget);
    }

    protected void moveFO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        FileObject foFrom = from.toFileObject();
        assertNotNull(foFrom);
        FileObject foTarget = to.getParentFile().toFileObject();
        assertNotNull(foTarget);
        FileLock lock = foFrom.lock();
        try {
            foFrom.move(lock, foTarget, to.getName(), null);
        } finally {
            lock.releaseLock();
        }
    }

    protected void copyFO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        FileObject foFrom = from.toFileObject();
        assertNotNull(foFrom);
        FileObject foTarget = to.getParentFile().toFileObject();
        assertNotNull(foTarget);
        FileLock lock = foFrom.lock();
        try {
            foFrom.copy(foTarget, getName(to), getExt(to));
        } finally {
            lock.releaseLock();
        }
    }

    protected void delete(VCSFileProxy file) throws IOException {
        DataObject dao = DataObject.find(file.toFileObject());
        dao.delete();
    }
    
    protected void deleteFO (VCSFileProxy toDelete) throws DataObjectNotFoundException, IOException {
        FileObject fo = toDelete.toFileObject();
        assertNotNull(fo);
        FileLock lock = fo.lock();
        try {
            fo.delete(lock);
        } finally {
            lock.releaseLock();
        }
    }

    protected String getName(VCSFileProxy f) {
        String ret = f.getName();
        int idx = ret.lastIndexOf(".");
        return idx > -1 ? ret.substring(0, idx) : ret;
    }

    protected String getExt(VCSFileProxy f) {
        String ret = f.getName();
        int idx = ret.lastIndexOf(".");
        return idx > -1 ? ret.substring(idx) : null;
    }
    
    protected class LogHandler extends Handler {
        private VCSFileProxy fileToInitialize;
        private boolean filesInitialized;
        private final HashSet<VCSFileProxy> initializedFiles = new HashSet<>();

        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().contains("GitFolderEventsHandler.initializeFiles: finished")) {
                synchronized (this) {
                    filesInitialized = true;
                    notifyAll();
                }
            } else if (record.getMessage().contains("GitFolderEventsHandler.initializeFiles: ")) {
                if (record.getParameters()[0].equals(fileToInitialize.getPath())) {
                    synchronized (this) {
                        initializedFiles.add(fileToInitialize);
                        notifyAll();
                    }
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        protected void setFilesToInitializeRoots (VCSFileProxy file) {
            fileToInitialize = file;
            initializedFiles.clear();
            filesInitialized = false;
        }

        protected boolean waitForFilesToInitializeRoots() throws InterruptedException {
            for (int i = 0; i < 20; ++i) {
                synchronized (this) {
                    if (filesInitialized && initializedFiles.contains(fileToInitialize)) {
                        return true;
                    }
                    wait(500);
                }
            }
            return false;
        }
    }

}

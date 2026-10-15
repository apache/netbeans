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

package org.netbeans.modules.git;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.netbeans.junit.NbTestCase;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitRepository;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author ondra
 */
public abstract class AbstractGitTestCase extends NbTestCase {

    @SuppressWarnings("ProtectedField")
    protected File testBase;
    @SuppressWarnings("ProtectedField")
    protected File userdir;
    @SuppressWarnings("ProtectedField")
    protected File repositoryLocation;

    protected static final String NULL_OBJECT_ID = "0000000000000000000000000000000000000000";

    public AbstractGitTestCase (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        testBase = Files.createTempDirectory("NBGitTest").toFile();
        userdir = new File(getWorkDir().getParentFile(), "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        super.setUp();
        repositoryLocation = new File(testBase, "work");
        clearWorkDir();
        getClient(repositoryLocation).init(GitUtils.NULL_PROGRESS_MONITOR);
        File repositoryMetadata = new File(repositoryLocation, ".git");
        assertTrue(repositoryMetadata.exists());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        NbPreferences.root().flush();
        NbPreferences.root().sync();
        Files.walkFileTree(testBase.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                if (e == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    throw e;
                }
            }
        });
    }

    protected File getRepositoryLocation() {
        return repositoryLocation;
    }
    
    protected File createFolder(String name) throws IOException {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject folder = wd.createFolder(name);
        return FileUtil.toFile(folder);
    }

    protected File createFolder(File parent, String name) throws IOException {
        FileObject parentFO = FileUtil.toFileObject(parent);
        FileObject folder = parentFO.createFolder(name);
        return FileUtil.toFile(folder);
    }

    protected File createFile(File parent, String name) throws IOException {
        FileObject parentFO = FileUtil.toFileObject(parent);
        FileObject fo = parentFO.createData(name);
        return FileUtil.toFile(fo);
    }

    protected File createFile(String name) throws IOException {
        return createFile(getWorkDir(), name);
    }

    protected void write(File file, String str) throws IOException {
        try (FileWriter w = new FileWriter(file)) {
            w.write(str);
            w.flush();
        }
    }

    protected String read (File file) throws IOException {
        BufferedReader r = null;
        try {
            StringBuilder sb = new StringBuilder();
            r = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
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

    protected GitClient getClient (File repositoryLocation) throws GitException {
        return GitRepository.getInstance(repositoryLocation).createClient();
    }

    protected void add (File... files) throws GitException {
        getClient(repositoryLocation).add(files == null ? new File[0] : files, GitUtils.NULL_PROGRESS_MONITOR);
    }

    protected void commit (File... files) throws GitException {
        getClient(repositoryLocation).commit(files == null ? new File[0] : files, "blablabla", null, null, GitUtils.NULL_PROGRESS_MONITOR);
    }

    protected void delete (boolean cached, File... files) throws GitException {
        getClient(repositoryLocation).remove(files == null ? new File[0] : files, cached, GitUtils.NULL_PROGRESS_MONITOR);
    }

    protected File initSecondRepository () throws GitException {
        File secondRepositoryFolder = new File(repositoryLocation.getParentFile(), "work_2"); //NOI18N
        getClient(secondRepositoryFolder).init(GitUtils.NULL_PROGRESS_MONITOR);
        assertTrue(secondRepositoryFolder.isDirectory());
        return secondRepositoryFolder;
    }

    @SuppressWarnings("ProtectedInnerClass")
    protected class StatusRefreshLogHandler extends Handler {
        private Set<File> filesToRefresh;
        private boolean filesRefreshed;
        private final HashSet<File> refreshedFiles = new HashSet<>();
        private final File topFolder;
        private final Set<String> interestingFiles = new HashSet<>();
        private boolean active;

        public StatusRefreshLogHandler (File topFolder) {
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
                        notifyAll();
                    }
                }
            } else if (record.getMessage().contains("refreshAllRoots() roots: ")) {
                synchronized (this) {
                    for (File f : (Set<File>) record.getParameters()[0]) {
                        if (f.getAbsolutePath().startsWith(topFolder.getAbsolutePath()))
                        refreshedFiles.add(f);
                    }
                    notifyAll();
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

        @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
        public void setFilesToRefresh (Set<File> files) {
            active = false;
            filesRefreshed = false;
            refreshedFiles.clear();
            filesToRefresh = files;
            interestingFiles.clear();
            active = true;
        }

        public boolean waitForFilesToRefresh () throws InterruptedException {
            for (int i = 0; i < 50; ++i) {
                synchronized (this) {
                    if (filesRefreshed) {
                        return true;
                    }
                    wait(500);
                }
            }
            return false;
        }

        public boolean getFilesRefreshed () {
            return filesRefreshed;
        }

        Set<String> getInterestingFiles () {
            return new HashSet<>(interestingFiles);
        }

    }

    protected final void runExternally (File workdir, List<String> command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectOutput(Redirect.DISCARD);
        pb.directory(workdir);
        Process p = pb.start();
        final List<String> err;
        try (BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream(), UTF_8))) {
            err = new LinkedList<>();
            Thread t2 = new Thread(() -> {
                try {
                    for (String line = errReader.readLine(); line != null; line = errReader.readLine()) {
                        err.add(line);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
            t2.start();
            t2.join();
            p.waitFor();
        }
        if (!err.isEmpty()) {
            throw new Exception(err.toString());
        }
    }
}

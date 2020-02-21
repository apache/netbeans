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

package org.netbeans.modules.git.remote.cli.jgit;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.git.remote.cli.ApiUtils;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRepository;
import org.netbeans.modules.git.remote.cli.GitStatus;
import org.netbeans.modules.git.remote.cli.GitStatus.Status;
import org.netbeans.modules.git.remote.cli.jgit.commands.GitCommand;
import org.netbeans.modules.git.remote.cli.jgit.utils.TestUtils;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.git.remote.cli.progress.StatusListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.remotefs.versioning.api.RemoteVcsSupport;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;

/**
 *
 */
public abstract class AbstractGitTestCase extends NbTestCase {
    private final VCSFileProxy workDir;
    private final VCSFileProxy wc;
    private final VCSFileProxy repositoryLocation;
    private JGitRepository localRepository;
    protected static final ProgressMonitor NULL_PROGRESS_MONITOR = new NullProgressMonitor ();
    private boolean skipTest = false;
    private String gitPath;
    protected Version version;

    private enum Scope{All, Successful, Failed};
    private static final Scope TESTS_SCOPE = Scope.Successful;
    
    public AbstractGitTestCase (String testName) throws IOException {
        super(testName);
        System.setProperty("work.dir", getWorkDirPath());
        System.setProperty("file.encoding", "UTF-8");
        workDir = VCSFileProxy.createFileProxy(getWorkDir());
        repositoryLocation = VCSFileProxy.createFileProxy(workDir, "repo");
        wc = VCSFileProxy.createFileProxy(workDir, getName() + "_wc");
        gitPath = "/usr/bin/git";
        //gitPath = "/export/home/projects/git/git";
        FileObject git = VCSFileProxySupport.getResource(workDir, gitPath).toFileObject();
        if (git == null || !git.isValid()) {
            skipTest = true;
            return;
        }
        version = new Version(ExecutionEnvironmentFactory.getLocal(), git);
        if (version.compareTo(new Version(1,8,0)) < 0) {
            System.err.println("Usupported git version "+version);
            skipTest = true;
            return;
        } else {
            System.err.println("git version "+version);
        }
        GitCommand.setGitCommand(gitPath);
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
        super.setUp();
        clearWorkDir();
        VCSFileProxySupport.mkdirs(wc);
        initializeRepository();
    }

    @Override
    protected void tearDown() throws Exception {
        VCSFileProxySupport.delete(workDir);
    }
    
    protected VCSFileProxy getWorkingDirectory () {
        return wc;
    }

    protected JGitRepository getLocalGitRepository () {
        return localRepository;
    }

    protected JGitRepository getRemoteGitRepository () {
        return new JGitRepository(repositoryLocation);
    }

    protected void write(VCSFileProxy file, String str) throws IOException {
        OutputStreamWriter w = null;
        try {
            Charset encoding = RemoteVcsSupport.getEncoding(file);
            w = new OutputStreamWriter(VCSFileProxySupport.getOutputStream(file), encoding);
            w.write(str);
            w.flush();
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    protected String read(VCSFileProxy file) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = null;
        try {
            Charset encoding = RemoteVcsSupport.getEncoding(file);
            r = new BufferedReader(new InputStreamReader(file.getInputStream(false), encoding));
            String s = r.readLine();
            if (s != null) {
                while( true ) {
                    sb.append(s);
                    s = r.readLine();
                    if (s == null) break;
                    sb.append('\n');
                }
            }
        } finally {
            if (r != null) {
                r.close();
            }
        }
        return sb.toString();
    }

    protected static void assertStatus (Map<VCSFileProxy, GitStatus> statuses, VCSFileProxy workDir, VCSFileProxy file, boolean tracked,
            Status headVsIndex, Status indexVsWorking, Status headVsWorking, boolean conflict) {
        GitStatus status = statuses.get(file);
//if(KIT) {}
//else    {while (status == null) {
//            if (file.equals(workDir)) {
//                break;
//            }
//            file = file.getParentFile();
//            status = statuses.get(file);
//        }
//        if (headVsIndex == Status.STATUS_NORMAL && indexVsWorking == Status.STATUS_NORMAL && headVsWorking == Status.STATUS_NORMAL && status == null) {
//            // Status does not print up-to-date files
//            return;
//        }}
        assertNotNull(file.getPath() + " not in " + statuses.keySet(), status);
        assertEquals(TestUtils.getRelativePath(file, workDir), status.getRelativePath());
        assertEquals(tracked, status.isTracked());
        assertEquals(headVsIndex, status.getStatusHeadIndex());
        assertEquals(indexVsWorking, status.getStatusIndexWC());
        assertEquals(headVsWorking, status.getStatusHeadWC());
        assertEquals(conflict, status.isConflict());
    }

//    protected GitRepository cloneRemoteRepository (File target) throws GitException {
//        return GitRepository.cloneRepository(target, repositoryLocation.getAbsolutePath(), null);
//    }

    private void initializeRepository() throws Exception {
        runExternally(repositoryLocation, Arrays.asList("init", "--bare"));
        runExternally(wc.getParentFile(), Arrays.asList("clone", repositoryLocation.getPath(), wc.getName()));

        List<String> res = runExternally(wc.getParentFile(), Arrays.asList("config", "--global", "--get", "user.name"));
        if (res.size() == 0 || res.get(0).isEmpty()) {
            runExternally(wc.getParentFile(), Arrays.asList("config", "--global", "user.name", "Your Name"));
        }
        res = runExternally(wc.getParentFile(), Arrays.asList("config", "--global", "--get", "user.email"));
        if (res.size() == 0 || res.get(0).isEmpty()) {
            runExternally(wc.getParentFile(), Arrays.asList("config", "--global", "user.email", "you@example.com"));
        }
        
        localRepository = new JGitRepository(wc);
    }

    protected GitClient getClient (VCSFileProxy repository) throws GitException {
        return GitRepository.getInstance(repository).createClient();
    }

    protected void add (VCSFileProxy... files) throws GitException {
        getClient(wc).add(files, NULL_PROGRESS_MONITOR);
    }

    protected void commit (VCSFileProxy... files) throws GitException {
        getClient(wc).commit(files, "commit", null, null, NULL_PROGRESS_MONITOR);
    }

    protected void remove (boolean cached, VCSFileProxy... files) throws GitException {
        getClient(wc).remove(files, cached, NULL_PROGRESS_MONITOR);
    }

    protected void copyFile(VCSFileProxy source, VCSFileProxy target) throws IOException {
        VCSFileProxySupport.mkdirs(target.getParentFile());
        if (source.isDirectory()) {
            VCSFileProxy[] children = source.listFiles();
            if (children != null) {
                for (VCSFileProxy child : children) {
                    copyFile(child, VCSFileProxy.createFileProxy(target, child.getName()));
                }
            }
        } else if (source.isFile()) {
            VCSFileProxySupport.createNew(target);
            String s = read(source);
            if (s != null) {
                write(target, s);
            }
        }
    }

    protected void assertEqualsID(String golden, String fact) {
        assertNotNull(golden);
        assertNotNull(fact);
        assertTrue(golden.length() >= 7);
        assertTrue(fact.length() >= 7);
        if (golden.length() > fact.length()) {
            if (!golden.startsWith(fact)) {
                assertEquals(golden, fact);
            }
        } else if (golden.length() < fact.length()) {
            if (!fact.startsWith(golden)) {
                assertEquals(golden, fact);
            }
        } else {
            assertEquals(golden, fact);
        }
    }

    protected void clearRepositoryPool() throws NoSuchFieldException, IllegalArgumentException, IllegalArgumentException, IllegalAccessException {
        ApiUtils.clearRepositoryPool();
    }

    protected boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("windows");
    }

    protected static void assertDirCacheEntry (JGitRepository repository, VCSFileProxy workDir, Collection<VCSFileProxy> files) throws IOException {
//        DirCache cache = repository.lockDirCache();
//        for (VCSFileProxy f : files) {
//            String relativePath = Utils.getRelativePath(workDir, f);
//            DirCacheEntry e = cache.getEntry(relativePath);
//            assertNotNull(e);
//            assertEquals(relativePath, e.getPathString());
//            if (f.lastModified() != e.getLastModified()) {
//                assertEquals((f.lastModified() / 1000) * 1000, (e.getLastModified() / 1000) * 1000);
//            }
//            try (InputStream in = f.getInputStream(false)) {
//                assertEquals(e.getObjectId(), repository.newObjectInserter().idFor(Constants.OBJ_BLOB, VCSFileProxySupport.length(f), in));
//            }
//            if (e.getLength() == 0 && VCSFileProxySupport.length(f) != 0) {
//                assertTrue(e.isSmudged());
//            } else {
//                assertEquals(VCSFileProxySupport.length(f), e.getLength());
//            }
//        }
//        cache.unlock();
    }
    
    protected long getLinkLastModified(VCSFileProxy link) throws IOException {
        File javaFile = link.toFile();
        return Files.readAttributes(Paths.get(javaFile.getAbsolutePath()), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).lastModifiedTime().toMillis();
    }
    
    protected void assertFile(VCSFileProxy test, String pass) throws IOException {
        String message = "Difference between " + test + " and golden string";
        assertFile(message, test, pass);
    }

    private void assertFile(String message, VCSFileProxy test, String pass) throws IOException {
        String testString = read(test);
        assertTrue(message, testString.equals(pass));
    }

    protected static class Monitor extends ProgressMonitor.DefaultProgressMonitor implements FileListener {
        public final HashSet<VCSFileProxy> notifiedFiles = new HashSet<VCSFileProxy>();
        public final List<String> notifiedWarnings = new LinkedList<String>();
        private boolean barrierAccessed;
        public int count;
        public volatile boolean cont;

        public Monitor () {
            cont = true;
        }

        @Override
        public void notifyFile (VCSFileProxy file, String relativePathToRoot) {
            notifiedFiles.add(file);
            barrierReached();
        }

        @Override
        public void notifyError(String message) {
            fail(message);
        }

        @Override
        public void notifyWarning (String message) {
            notifiedWarnings.add(message);
        }

        private void barrierReached() {
            barrierAccessed = true;
            ++count;
            while (!cont) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
        }

        public void waitAtBarrier() throws InterruptedException {
            for (int i = 0; i < 100; ++i) {
                if (barrierAccessed) {
                    break;
                }
                Thread.sleep(100);
            }
            assertTrue(barrierAccessed);
        }
    }
    
    private static class NullProgressMonitor extends ProgressMonitor {

        @Override
        public boolean isCanceled () {
            return false;
        }

        @Override
        public void setCancelDelegate(Cancellable c) {
        }

        @Override
        public boolean cancel() {
            return false;
        }

        @Override
        public void started (String command) {
        }

        @Override
        public void finished () {
        }

        @Override
        public void preparationsFailed (String message) {
        }

        @Override
        public void notifyError (String message) {
        }

        @Override
        public void notifyWarning (String message) {
        }
    }
    
    protected final List<String> runExternally (VCSFileProxy workdir, List<String> command) throws Exception {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        String[] args = command.toArray(new String[command.size()]);
        VCSFileProxySupport.mkdirs(workdir);
        ProcessUtils.ExitStatus executeInDir = ProcessUtils.executeInDir(workdir.getPath(), null, false, canceled, workdir, gitPath, args);
        return Arrays.asList(executeInDir.output.split("\n"));
    }
    
    protected void assertStatus(Map<VCSFileProxy, GitStatus> statuses, VCSFileProxy repository, VCSFileProxy file, boolean tracked,
            Status headVsIndex, Status indexVsWorking, Status headVsWorking, boolean conflict, TestStatusListener monitor) {
        assertStatus(statuses, repository, file, tracked, headVsIndex, indexVsWorking, headVsWorking, conflict);
        assertStatus(monitor.notifiedStatuses, repository, file, tracked, headVsIndex, indexVsWorking, headVsWorking, conflict);
    }

    protected static class TestStatusListener implements StatusListener {
        private final Map<VCSFileProxy, GitStatus> notifiedStatuses;

        public TestStatusListener() {
            notifiedStatuses = new HashMap<VCSFileProxy, GitStatus>();
        }

        @Override
        public void notifyStatus(GitStatus status) {
            notifiedStatuses.put(status.getFile(), status);
        }
    }
    
    protected static final class Version implements Comparable<Version> {

        public final int major;
        public final int minor;
        public final int last;
        public final String version;
        
        public Version(int major, int minor, int last) {
            this.major = major;
            this.minor = minor;
            this.last = last;
            this.version = ""+major+"."+minor+"."+last;
        }

        public Version(ExecutionEnvironment execEnv, FileObject binary) {
            version = getVersion(execEnv, binary);
            String[] split = version.split("\\.");
            if (split.length >= 1) {
                major = Integer.parseInt(split[0]);
                if (split.length >= 2) {
                    minor = Integer.parseInt(split[1]);
                    if (split.length >= 3) {
                        last = Integer.parseInt(split[2]);
                    } else {
                        last = 0;
                    }
                } else {
                    minor = 0;
                    last = 0;
                }
            } else {
                major = 0;
                minor = 0;
                last = 0;
            }
        }

        private String getVersion(ExecutionEnvironment execEnv, FileObject binary) {
            ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
            String[] args = new String[]{"--version"};
            ProcessUtils.ExitStatus executeInDir = ProcessUtils.executeInDir(binary.getParent().getPath(), null, false, canceled, VCSFileProxy.createFileProxy(binary.getParent()), binary.getPath(), args);
            List<String> outputLines = Arrays.asList(executeInDir.output.split("\n"));
            String aVersion = "0.0.0";
            if (outputLines.size() > 0) {
                String v = outputLines.get(0);
                int i = v.indexOf("version");
                if (i >= 0) {
                    //OEL
                    // svn, version 1.6.11 (r934486)
                    // git version 1.7.1
                    // Mercurial Distributed SCM (version 1.4)
                    //SunOS 11.3
                    // svn, version 1.7.20 (r1667490)
                    // Mercurial Distributed SCM (version 3.7.3)
                    // git version 2.7.4
                    int last = i + 8;
                    for (int k = i + 8; k < v.length(); k++) {
                        char c = v.charAt(k);
                        if (c >= '0' && c <= '9' || c == '.') {
                        } else {
                            break;
                        }
                        last++;
                    }
                    aVersion = v.substring(i + 8, last);
                }
            }
            return aVersion;
        }

        @Override
        public int compareTo(Version o) {
            int res = this.major - o.major;
            if (res == 0) {
                res = this.minor - o.minor;
                if (res == 0) {
                    res = this.last - o.last;
                }
            }
            return res;
        }
        
        @Override
        public String toString() {
            return version;
        }
    }
}

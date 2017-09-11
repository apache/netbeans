/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
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

/**
 *
 * @author ondra
 */
public abstract class AbstractGitTestCase extends NbTestCase {

    protected File repositoryLocation;
    
    protected static final String NULL_OBJECT_ID = "0000000000000000000000000000000000000000";

    public AbstractGitTestCase (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        File userdir = new File(getWorkDir().getParentFile(), "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        super.setUp();
        repositoryLocation = new File(getWorkDir(), "work");
        clearWorkDir();
        getClient(repositoryLocation).init(GitUtils.NULL_PROGRESS_MONITOR);
        File repositoryMetadata = new File(repositoryLocation, ".git");
        assertTrue(repositoryMetadata.exists());
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
        FileWriter w = null;
        try {
            w = new FileWriter(file);
            w.write(str);
            w.flush();
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    protected String read (File file) throws IOException {
        BufferedReader r = null;
        try {
            StringBuilder sb = new StringBuilder();
            r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
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

    protected class StatusRefreshLogHandler extends Handler {
        private Set<File> filesToRefresh;
        private boolean filesRefreshed;
        private final HashSet<File> refreshedFiles = new HashSet<File>();
        private final File topFolder;
        private final Set<String> interestingFiles = new HashSet<String>();
        boolean active;

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
            return new HashSet<String>(interestingFiles);
        }

    }

    protected final void runExternally (File workdir, List<String> command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workdir);
        Process p = pb.start();
        final BufferedReader outReader = new BufferedReader(Channels.newReader(Channels.newChannel(p.getInputStream()), "UTF-8"));
        final BufferedReader errReader = new BufferedReader(Channels.newReader(Channels.newChannel(p.getErrorStream()), "UTF-8"));
        final List<String> output = new LinkedList<String>();
        final List<String> err = new LinkedList<String>();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run () {
                try {
                    for (String line = outReader.readLine(); line != null; line = outReader.readLine()) {
                        output.add(line);
                    }
                } catch (IOException ex) {
                    
                }
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run () {
                try {
                    for (String line = errReader.readLine(); line != null; line = errReader.readLine()) {
                        err.add(line);
                    }
                } catch (IOException ex) {
                    
                }
            }
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        p.waitFor();
        outReader.close();
        errReader.close();
        if (!err.isEmpty()) {
            throw new Exception(err.toString());
        }
    }
}

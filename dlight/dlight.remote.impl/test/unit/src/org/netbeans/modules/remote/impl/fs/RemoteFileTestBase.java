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

package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fs.server.FSSTransport;
import org.netbeans.modules.remote.test.RemoteTestSuiteBase;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class RemoteFileTestBase extends NativeExecutionBaseTestCase {

    static {
        System.setProperty("jsch.connection.timeout", "30000");
        System.setProperty("socket.connection.timeout", "30000");
        System.setProperty("remote.throw.assertions", "true");
        System.setProperty("remote.user.password.keep_in_memory", "true");        
        System.setProperty("remote.fs_server.verbose", "0");
        System.setProperty("remote.fs_server.log", "true");        
        System.setProperty("remote.fs_server.suppress.stderr", "false");
        TestLogHandler.attach(RemoteLogger.getInstance());
    }
    private static HashMap<ExecutionEnvironment, Exception> deadHosts = new HashMap<ExecutionEnvironment, Exception>();
    protected RemoteFileSystem fs;
    protected RemoteFileObject rootFO;
    protected final ExecutionEnvironment execEnv;

    protected String sharedLibExt;

    private Level oldLevel = null;
    
    public RemoteFileTestBase(String testName) {
        super(testName);
        fs = null;
        rootFO = null;
        execEnv = null;        
    }

    public RemoteFileTestBase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
        this.execEnv = execEnv;
    }
    
    protected void reconnect(int timeout, boolean resetFileSystem) throws Exception {
        char[] paswd = PasswordManager.getInstance().getPassword(execEnv);
        ConnectionManager.getInstance().disconnect(execEnv);
        sleep(timeout);
        assertFalse("Failure disconnecting from " + execEnv, ConnectionManager.getInstance().isConnectedTo(execEnv));
        sleep(timeout);
        RemoteFileSystemManager.getInstance().resetFileSystem(execEnv, false);
        PasswordManager.getInstance().storePassword(execEnv, paswd, true);
        ConnectionManager.getInstance().connectTo(execEnv);
        assertTrue("Failure reconnecting to " + execEnv, ConnectionManager.getInstance().isConnectedTo(execEnv));
    }

    @Override
    protected int timeOut() {
        return super.timeOut();
    }    

    @Override
    protected void setUp() throws Exception {
        if (execEnv != null) {
            HangupEnvList.clearHung(execEnv);
        }
        RemoteTestSuiteBase.registerTestSetup(this);
        if (FSSTransport.getInstance(execEnv) != null) {
            FSSTransport.getInstance(execEnv).testSetCleanupUponStart(true);
        }
        super.setUp();
        setLoggers(true);
        if (execEnv == null) {
            return;
        }
        RemoteFileSystemManager.getInstance().resetFileSystem(execEnv, true);
        fs = RemoteFileSystemManager.getInstance().getFileSystem(execEnv);
        assertNotNull("Null remote file system", fs);
        File cache = fs.getCache();
        removeDirectoryContent(cache);
        rootFO = fs.getRoot();
        assertNotNull("Null root file object", rootFO);
        assertTrue("Can not create directory " + cache.getAbsolutePath(), cache.exists() || cache.mkdirs());
        ExecutionEnvironment env = getTestExecutionEnvironment();
        if (deadHosts.containsKey(env)) {
            throw deadHosts.get(env);
        }
        try {
            ConnectionManager.getInstance().connectTo(env);
        } catch (Exception ex) {
            Exception wrapper = new IOException("Skip rest tests for dead host "+env.getDisplayName(), ex);
            deadHosts.put(env, wrapper);
            throw ex;
        }
        if (HostInfoUtils.getHostInfo(execEnv).getOSFamily() == OSFamily.MACOSX) {
            sharedLibExt = ".dylib";
        } else {
            sharedLibExt = ".so";
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        setLoggers(false);
        RemoteTestSuiteBase.registerTestTearDown(this);
        if (execEnv != null) {
            HangupEnvList.clearHung(execEnv);
        }
    }
   
    @org.netbeans.api.annotations.common.SuppressWarnings("LG")
    private void setLoggers(boolean setup) {
        final Logger logger = Logger.getLogger("remote.support.logger");
        if (setup) {
            TestLogHandler.attach(logger);
            if (NativeExecutionTestSupport.getBoolean("remote", "logging.finest")
                 || NativeExecutionTestSupport.getBoolean("remote", getClass().getName() + ".logging.finest")) {
                oldLevel = logger.getLevel();
                logger.setLevel(Level.FINEST);
            }
        } else {
            if (oldLevel != null) {
                logger.setLevel(oldLevel);
            }
        }
    }

    protected String mkTempAndRefreshParent() throws Exception {
        return mkTempAndRefreshParent(false);
    }
    
    protected void removeRemoteDirIfNotNull(String path) throws Exception {
        if (path != null) {
            ProcessUtils.execute(execEnv, "chmod", "-R", "u+w", path);
            CommonTasksSupport.rmDir(execEnv, path, true, new OutputStreamWriter(System.err)).get();
        }
    }

    protected String execute(String command, String... args) {
        return execute(execEnv, command, args);
    }

    protected static String execute(ExecutionEnvironment env, String command, String... args) {
        ProcessUtils.ExitStatus res = ProcessUtils.execute(env, command, args);
        assertEquals(command + ' ' + args + " at " + env.getDisplayName() + " failed: " + res.getErrorString(), 0, res.exitCode);
        return res.getOutputString();
    }

    protected static String executeInDir(String dir, ExecutionEnvironment env, String command, String... args) {
        ProcessUtils.ExitStatus res = ProcessUtils.executeInDir(dir, env, command, args);
        assertEquals(command + ' ' + args + " at " + env.getDisplayName() + " failed: " + res.getErrorString(), 0, res.exitCode);
        return res.getOutputString();
    }
    
    protected String executeInDir(String dir, String command, String... args) {
        return executeInDir(dir, execEnv, command, args);
    }

    protected RemoteFileObject getFileObject(String path) throws Exception {
        RemoteFileObject fo = rootFO.getFileObject(path);
        assertNotNullFileObject(fo, null, path);
        return fo;
    }
    
    private void assertNotNullFileObject(FileObject fo, FileObject parent, String relOrAbsPath) throws Exception    {
        if (fo == null) {
            String absPath;
            StringBuilder message = new StringBuilder();
            message.append("Null file object for ").append(relOrAbsPath);
            if (parent == null) {
                absPath = relOrAbsPath;
                message.append(" in ").append(execEnv);
            } else {
                absPath = parent.getPath() + '/' + relOrAbsPath;
                message.append(" in ").append(parent);
            }
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "ls", "-ld", absPath);
            System.err.printf("Null file object for %s:%s\n", execEnv, absPath);
            System.err.printf("ls -ld %s\nrc=%d\n%s\n%s", absPath, res.exitCode, res.getOutputString(), res.getErrorString());
            String dirName = PathUtilities.getDirName(absPath);
            String baseName = PathUtilities.getBaseName(absPath);
            RemoteFileObject parentFO = rootFO.getFileObject(dirName);
            System.err.printf("parentFO=%s\n", parentFO);
            if (parentFO != null) {                
                File cache = parentFO.getImplementor().getCache();
                if(cache == null) {
                    System.err.printf("Cache file is null\n");
                } else {
                    File storageFile = new File(cache, RemoteFileSystem.CACHE_FILE_NAME);
                    if (storageFile.exists()) {
                        System.err.printf("Parent directory cache (%s) content:\n", storageFile.getAbsolutePath());
                        printFile(storageFile, null, System.err);
                    } else {
                        System.err.printf("Parent directory cache (%s) does not exist\n", storageFile.getAbsolutePath());
                    }
                }
                fo = parentFO.getFileObject(baseName);
                System.err.printf("2-nd attempt %s: %s\n", (fo == null ? "failed" : "succeeded"), fo);
                if (fo == null) {
                    parentFO.refresh();
                    fo = parentFO.getFileObject(baseName);
                    System.err.printf("3-rd attempt %s: %s\n", (fo == null ? "failed" : "succeeded"), fo);
                }
            }
            if (res.isOK()) {
                message.append("; ls reports that file exists:\n").append(res.getOutputString());
            } else {
                message.append("; ls reports that file does NOT exist:\n").append(res.getErrorString());
            }
            assertTrue(message.toString(), false);
        }
    }

    protected FileObject getFileObject(FileObject base, String path) throws Exception {
        FileObject fo = base.getFileObject(path);
        assertNotNullFileObject(fo, base, path);
        return fo;
    }

    protected void upload(File file, String remotePath) throws Exception {
        Future<UploadStatus> task = CommonTasksSupport.uploadFile(file, execEnv, remotePath, -1);
        UploadStatus res = task.get();
        assertEquals("Failed uploading " + file.getAbsolutePath() + " to " + execEnv + ":" + remotePath
                + ": " + res.getError(), 0, res.getExitCode());
    }

    protected void mkDir(String dir) throws InterruptedException, ExecutionException, TimeoutException {
        Future<Integer> mkDirTask = CommonTasksSupport.mkDir(execEnv, dir, new PrintWriter(System.err));
        //System.out.printf("Mkdir %s\n", dir);
        int rc = mkDirTask.get(30, TimeUnit.SECONDS);
        //System.out.printf("mkdir %s done, rc=%d\n", dir, rc);
        assertEquals(0, rc);
    }

    protected String mkTempAndRefreshParent(boolean directory) throws Exception {
        String path = mkTemp(execEnv, directory);
        refreshParent(path);
        return path;
    }

    protected void refreshParent(String path) throws Exception {
        String parent = PathUtilities.getDirName(path);
        getFileObject(parent).refresh();
    }
    
    protected String readRemoteFile(String absPath) throws Exception {
        FileObject fo = getFileObject(absPath);
        assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo.isValid());
        return readFile(fo);
    }

    protected String getFileName(ExecutionEnvironment execEnv, String absPath) {
        return execEnv.toString() + ':' + absPath;
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
            ProcessUtils.ExitStatus status = ProcessUtils.execute(execEnv, binary.getPath(), "--version");
            List<String> outputLines = status.getOutputLines();
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

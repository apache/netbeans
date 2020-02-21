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

package org.netbeans.modules.cnd.remote.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.modules.cnd.makeproject.MakeActionProviderImpl;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.cnd.test.CndTestIOProvider;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.remote.sync.FtpSyncFactory;
import org.netbeans.modules.cnd.remote.sync.RemoteSyncTestSupport;
import org.netbeans.modules.cnd.remote.sync.RfsSyncFactory;
import org.netbeans.modules.cnd.remote.sync.SharedSyncFactory;
import org.netbeans.modules.cnd.spi.remote.setup.HostValidator;
import org.netbeans.modules.cnd.spi.remote.setup.HostValidatorFactory;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.IOProvider;

/**
 * A common base class for remote "unit" tests
 */
public abstract class RemoteTestBase extends CndBaseTestCase {

    protected static final Logger log = RemoteUtil.LOGGER;

    public static enum Sync {
        FTP(FtpSyncFactory.ID),
        RFS(RfsSyncFactory.ID),
        SHARED(SharedSyncFactory.ID);
        public final String ID;
        Sync(String id) {
            this.ID = id;
        }
    }

    public static enum Toolchain {
        GNU("GNU"),
        SUN("OracleDeveloperStudio");
        public final String ID;
        Toolchain(String id) {
            this.ID = id;
        }
    }

    protected static class ProcessReader implements Runnable {

        private final BufferedReader errorReader;
        private final PrintWriter errorWriter;

        public ProcessReader(InputStream errorStream, PrintWriter errorWriter) {
            this.errorReader = new BufferedReader(new InputStreamReader(errorStream));
            this.errorWriter = errorWriter;
        }
        @Override
        public void run() {
            try {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    if (errorWriter != null) {
                         errorWriter.println(line);
                    }
                    RemoteUtil.LOGGER.fine(line);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static final List<String> SUCCESS_PREFIXES = Collections.unmodifiableList(Arrays.asList(
        "BUILD SUCCESSFUL",
        "CLEAN SUCCESSFUL"
    ));

    private static final List<String> ERROR_PREFIXES = Collections.unmodifiableList(Arrays.asList(
        "Error copying project files",
        "CLEAN FAILED",
        "BUILD FAILED"
    ));

    private static final Pattern EXIT_VALUE_PATTERN = Pattern.compile("\\(exit value (\\d+),");

    static {
        System.setProperty("jsch.connection.timeout", "30000");
        System.setProperty("socket.connection.timeout", "30000");
        System.setProperty("sftp.put.retries", "5");
        System.setProperty("rfs.instable.sleep", "100");
        System.setProperty("remote.throw.assertions", "true");
        System.setProperty("remote.user.password.keep_in_memory", "true");
    }

    static {
        TestLogHandler.attach(log);
    }

    private Level oldRemoteLevel = null;
    private Level oldExecutionLevel = null;
    
    // we need this for tests which should run NOT for all environments
    public RemoteTestBase(String testName) {
        super(testName);
        cleanUserDir();        
    }

    protected RemoteTestBase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
        cleanUserDir();
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("LG")
    private void setLoggers(boolean setup) {
        final Logger remoteLogger = Logger.getLogger("cnd.remote.logger");
        final Logger executionLogger = Logger.getLogger("nativeexecution.support.logger.level");
        if (setup) {
            if (NativeExecutionTestSupport.getBoolean(RemoteDevelopmentTest.DEFAULT_SECTION, "logging.finest")
                 || NativeExecutionTestSupport.getBoolean(RemoteDevelopmentTest.DEFAULT_SECTION, getClass().getName() + ".logging.finest")) {
                oldRemoteLevel = remoteLogger.getLevel();
                remoteLogger.setLevel(Level.ALL);
                oldExecutionLevel = executionLogger.getLevel();
                executionLogger.setLevel(Level.FINEST);
            }
        } else {
            if (oldRemoteLevel != null) {
                remoteLogger.setLevel(oldRemoteLevel);
            }
            if (oldExecutionLevel != null) {
                executionLogger.setLevel(oldExecutionLevel);
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        setLoggers(true);
        System.err.printf("\n###> setUp    %s\n", getClass().getName() + '.' + getName());
        super.setUp();
        connectRemoteHost();
    }

    protected void connectRemoteHost() throws Exception {
        final ExecutionEnvironment env = getTestExecutionEnvironment();
        if (env != null) {
            // the password should be stored in the initialization phase
            ConnectionManager.getInstance().connectTo(env);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        //ConnectionManager.getInstance().disconnect(getTestExecutionEnvironment());
        System.err.printf("\n###< tearDown %s\n", getClass().getName() + '.' + getName());
        setLoggers(false);
    }

    private static Set<ExecutionEnvironment> hosts = new HashSet<>();

    protected static synchronized void setupHost(ExecutionEnvironment execEnv) {
        if (! hosts.contains(execEnv)) {
            ToolsCacheManager tcm = ToolsCacheManager.createInstance(true);
            HostValidator validator = HostValidatorFactory.create(tcm);
            boolean ok = validator.validate(execEnv, true, new PrintWriter(System.out));
            if (ok) {
                hosts.add(execEnv);
            }
            assertTrue("Error setting up host " + execEnv, ok);
            tcm.applyChanges();
        }
    }

//    protected void rebuildProject(MakeProject makeProject, long timeout, TimeUnit unit)
//            throws InterruptedException, IllegalArgumentException, TimeoutException {
//        buildProject(makeProject, ActionProvider.COMMAND_REBUILD, timeout, unit);
//    }

//    protected void buildProject(MakeProject makeProject, long timeout, TimeUnit unit)
//            throws InterruptedException, IllegalArgumentException, TimeoutException {
//        buildProject(makeProject, ActionProvider.COMMAND_BUILD, timeout, unit);
//    }

    protected void buildProject(MakeProject makeProject, String command, long timeout, TimeUnit unit) throws Exception {

        assertNotSame("buildProject should never be called with \"rebuild\" command.", ActionProvider.COMMAND_REBUILD, command);

        final CountDownLatch done = new CountDownLatch(1);
        final AtomicInteger build_rc = new AtomicInteger(-1);
        IOProvider iop = IOProvider.getDefault();
        assert iop instanceof CndTestIOProvider;
        final List<String> lines = Collections.synchronizedList(new ArrayList<String>());
        CndTestIOProvider.Listener listener = new CndTestIOProvider.Listener() {

            @Override
            public void linePrinted(String line) {
                lines.add(line);
                if (line != null) {
                    if (isSuccessLine(line)) {
                        build_rc.set(0);
                        done.countDown();
                    } else if (isFailureLine(line)) {
                        int rc = -1;
                        Matcher m = EXIT_VALUE_PATTERN.matcher(line);
                        if (m.find()) {
                            try {
                                rc = Integer.parseInt(m.group(1));
                            } catch (NumberFormatException nfe) {
                                nfe.printStackTrace(System.err);
                            }
                        }
                        build_rc.set(rc);
                        done.countDown();
                    }
                }
            }

            private boolean isSuccessLine(String line) {
                for (String successLine : SUCCESS_PREFIXES) {
                    if (line.trim().startsWith(successLine)) {
                        return true;
                    }
                }
                return false;
            }

            private boolean isFailureLine(String line) {
                for (String errorLine : ERROR_PREFIXES) {
                    if (line.trim().startsWith(errorLine)) {
                        return true;
                    }
                }
                return false;
            }
        };
        try {
            ((CndTestIOProvider) iop).addListener(listener);
            MakeActionProviderImpl makeActionProvider = new MakeActionProviderImpl(makeProject);
            makeActionProvider.invokeAction(command, Lookup.EMPTY);
            if (timeout <= 0) {
                done.await();
            } else {
                    if (!done.await(timeout, unit)) {
                        assertTrue("Timeout: could not build within " + timeout + " " + unit.toString().toLowerCase(), false);
                    }
                }
            //Thread.sleep(3000); // give building thread time to finish and to kill rfs_controller
            RemoteSyncTestSupport.waitWorkerFinished(isDebugged() ? 1800 : 30);
            if (build_rc.get() != 0) {
                StringBuilder sb = new StringBuilder("-------- Console output of failed test ").append(getName()).append(" --------\n\n");
                for (String l : lines) {
                    sb.append(l).append('\n');
                }
                sb.append("----------------\n");
                System.err.println(sb);
                assertTrue("Build failed: on " + makeProject.getDevelopmentHost()  + ": RC=" + build_rc.get(), build_rc.get() == 0);
            }
        } finally {
            ((CndTestIOProvider) iop).removeListener(listener);
        }
    }

    protected void clearRemoteSyncRoot() {
        String dirToRemove = RemotePathMap.getRemoteSyncRoot(getTestExecutionEnvironment());
        boolean isOk = ProcessUtils.execute(getTestExecutionEnvironment(), "sh", "-c", "rm -rf " + dirToRemove + "/*").isOK();
        assertTrue("Failed to remove " + dirToRemove, isOk);
    }

    protected String mkTempAndRefreshParent(boolean directory) throws Exception {
        String path = mkTemp(getTestExecutionEnvironment(), directory);
        String parent = PathUtilities.getDirName(path);
        getFileObject(parent).refresh();
        return path;
    }

    protected FileObject getFileObject(String path) throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        FileObject fo = FileSystemProvider.getFileObject(env, path);
        assertNotNull("Null file object for " + env + ":" + path, fo);
        return fo;
    }

    protected static void addPropertyFromRcFile(String section, String varName) throws IOException, FormatException {
        addPropertyFromRcFile(section, varName, null);
    }

    protected static void addPropertyFromRcFile(String section, String varName, String defaultValue) throws IOException, FormatException {
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        String value = rcFile.get(section, varName, defaultValue);
        if (value != null && value.length() > 0) {
            System.setProperty(varName, value);
        }
    }

    protected static FileSystem getLocalFileSystem() {
        return FileSystemProvider.getFileSystem(ExecutionEnvironmentFactory.getLocal());
    }
}

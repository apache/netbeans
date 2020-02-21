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

package org.netbeans.modules.remote.impl.fs.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.ProcessStatusEx;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fs.RefreshManager;
import org.netbeans.modules.remote.impl.fs.RemoteExceptions;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystem;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemManager;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemUtils;
import org.netbeans.modules.remote.impl.fs.ui.RemoteNotifier;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 */
/*package*/ final class FSSDispatcher implements Disposer<FSSResponse> {

    private static final boolean SUPPRESS_STDERR = Boolean.parseBoolean(System.getProperty("remote.fs_server.suppress.stderr", "true"));
    private static final Map<ExecutionEnvironment, FSSDispatcher> instances = 
            new HashMap<>();
    private static final Object instanceLock = new Object();
    
    private final ExecutionEnvironment env;
    private final Map<Integer, FSSResponse> responses = new LinkedHashMap<>();
    private final Object responseLock = new Object();

    private static final String USER_DEFINED_REMOTE_SERVER_PATH = System.getProperty("remote.fs_server.path");
    private static final String USER_DEFINED_LOCAL_SERVER_PATH = System.getProperty("local.fs_server.path");
    private static final int REFRESH_INTERVAL = Integer.getInteger("remote.fs_server.refresh", 0); // NOI18N
    private static final String SERVER_CACHE = System.getProperty("remote.fs_server.remote.cache");

    // Actually this RP should have only 2 tasks: one reads error, another stdout;
    // but in the case of, say, connection failure and reconnect, old task can still be alive,
    // while we need to post new one.
    private final RequestProcessor RP = new RequestProcessor(getClass().getSimpleName(), 20);
    
    private FsServer server;
    private final Object serverLock = new Object();
    
    private final String traceName;
    
    private volatile boolean valid = true;
    private final AtomicInteger attempts = new AtomicInteger();
    private static final int MAX_ATTEMPTS = Integer.getInteger("remote.fs_server.attempts", 3); // NOI18N
    
    private final AtomicReference<String> lastErrorMessage = new AtomicReference<>();
    
    private volatile boolean cleanupUponStart = false;
    
    private volatile FileSystemProvider.AccessCheckType accessCheckType;
    
    private String getMinServerVersion() {
        return "1.12.0"; // NOI18N
    }
    
    private FSSDispatcher(ExecutionEnvironment env) {
        this.env = env;
        traceName = "fs_server[" + env + ']'; // NOI18N
        accessCheckType = restoreAccessCheckType();
    }
    
    public void setCleanupUponStart(boolean cleanup) {
        cleanupUponStart = cleanup;
    }
    
    private void addToRefresh(String path) {
        RefreshManager refreshManager = getFileSystem().getRefreshManager();
        refreshManager.scheduleRefreshExistent(Arrays.asList(path), false);
    }

    private RemoteFileSystem getFileSystem() {
        return RemoteFileSystemManager.getInstance().getFileSystem(env);
    }

    public static FSSDispatcher getInstance(ExecutionEnvironment env) {
        synchronized (instanceLock) {
            FSSDispatcher instance = instances.get(env);
            if (instance == null) {
                instance = new FSSDispatcher(env);
                instances.put(env, instance);
            }
            return instance;
        }
    }

    public boolean isRefreshing() {
        return REFRESH_INTERVAL > 0;
    }

    public void connected() {
        boolean wasValid  = valid;
        valid = true;
        if (wasValid && getFileSystem().getRoot().getImplementor().hasCache()) {
            RP.post(new ConnectTask());
        }
    }

    /*package*/ void requestRefreshCycle(String path) {
        RP.post(new RefreshTask(path));
    }

    private void sendRefreshRequest(String path) {
        FSSRequest req = new FSSRequest(FSSRequestKind.FS_REQ_REFRESH, path, true);
        try {
            dispatch(req);
        } catch (ConnectException ex) {
            // nothing to report: no connection => no refresh
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } catch (InterruptedException ex) {
            // don't report InterruptedException
        } catch (ExecutionException ex) {
            ex.printStackTrace(System.err);
        }
    }

    private void sendAccessTypeChangeRequest(FileSystemProvider.AccessCheckType accessCheckType) {
        String option;
        switch (accessCheckType) {
            case FAST:
                option = "access=fast"; //NOI18N
                break;
            case FULL:
                option = "access=full"; //NOI18N
                break;
            default:
                throw new IllegalArgumentException("Unexpected access check type" + accessCheckType.name()); //NOI18N
        }
        
        FSSRequest req = new FSSRequest(FSSRequestKind.FS_REQ_OPTION, option, true);
        FsServer server = getServer();
        if (server != null) {
            sendRequest(server.getWriter(), req);
        }
    }

    private static FileSystemProvider.AccessCheckType restoreAccessCheckType() {
        FileSystemProvider.AccessCheckType result = FileSystemProvider.AccessCheckType.FAST;
        String name = NbPreferences.forModule(FSSDispatcher.class).get("accessCheckType", result.name()); // NOI18N
        if (name != null) {
            try {
                result = FileSystemProvider.AccessCheckType.valueOf(name);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return result;
    }

    private static void storeAccessCheckType(FileSystemProvider.AccessCheckType accessCheckType) {
        NbPreferences.forModule(FSSDispatcher.class).put("accessCheckType", accessCheckType.name()); // NOI18N
    }

    public void setAccessCheckType(FileSystemProvider.AccessCheckType accessCheckType) {
        storeAccessCheckType(accessCheckType);
        this.accessCheckType = accessCheckType;
        if (getServer() != null) {
            sendAccessTypeChangeRequest(accessCheckType);
        }
    }

    FileSystemProvider.AccessCheckType getAccessCheckType() {
        return accessCheckType;
    }

    private class RefreshTask implements Runnable {
        
        private final String path;

        public RefreshTask(String path) {
            this.path = path;
        }
        
        @Override
        public void run() {
            sendRefreshRequest(path);
        }        
    }

    private class ConnectTask implements Runnable {
        @Override
        public void run() {
            String oldThreadName = Thread.currentThread().getName();
            Thread.currentThread().setName("fs_server on-connect initialization for " + env); // NOI18N
            try {                
                sendRefreshRequest("/"); // in turn calls getOrCreateServer() //NOI18N
            } finally {
                Thread.currentThread().setName(oldThreadName);
            }
        }
    }
    
    private class MainLoop implements Runnable {
        @Override
        public void run() {
            int exceptionsCount = 0;
            String oldThreadName = Thread.currentThread().getName();
            try {
                Thread.currentThread().setName("fs_server dispatcher for " + env); // NOI18N
                FsServer server = getServer();
                if (server == null) {
                    RemoteLogger.warning("Can not launch file system server on {0}", env);
                    return;
                }
                BufferedReader reader = server.getReader();
                String prevLine = null;
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) {
                        RemoteLogger.fine("error: empty line for {0} prev.line was {1}" ,traceName, prevLine);
                        continue;
                    }
                    prevLine = line;
                    Buffer buf = new Buffer(line);
                    char respKind = buf.getChar();
                    int respId = buf.getInt();
                    if (respId == 0) {
                        RemoteLogger.finest("got fs_server notification: {0}", line);
                        if (respKind == FSSResponseKind.FS_RSP_CHANGE.getChar()) {                            
                            // example: "c 0 8 /tmp/tmp"
                            String path = buf.getString();
                            addToRefresh(path);
                        } else {
                            RemoteLogger.info("wrong response #0: {1}", line);
                        }
                    } else {
                        synchronized (responseLock) {
                            FSSResponse response = responses.get(respId);
                            if (response == null) {
                                RemoteLogger.fine("skipping {0} response #{1}: {2}",
                                        traceName, respId, line);
                            } else {
                                response.addPackage(FSSResponseKind.fromChar(respKind), line);
                            }
                        }
                    }
                }
                Collection<FSSResponse> respCopy;
                synchronized (responseLock) {
                    respCopy = new ArrayList<>(responses.values());
                    responses.clear();
                }                
                ExecutionException ex = new ExecutionException(
                        RemoteExceptions.createConnectException(RemoteFileSystemUtils.getConnectExceptionMessage(env)));

                for (FSSResponse resp : respCopy) {
                    resp.failed(ex);
                }
                NativeProcess process = server.getProcess();
                if (!ProcessUtils.isAlive(process)) {
                    int rc = process.waitFor();                    
                    RemoteLogger.finest("fs_server (pid {0} at {1}) exited with rc = {2}", process.getPID(), env, rc);//NOI18N
                    
                }
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
            } catch (Throwable thr) { // too wide, but we need to guarantee dispatcher is alive
                thr.printStackTrace(System.err);
                if (exceptionsCount++ > 1000) {
                    setInvalid(true);
                }
            } finally {
                try {
                    checkValid();
                } catch (ExecutionException ex) {
                    ex.printStackTrace(System.err);
                } catch (InterruptedException ex) {
                    // none
                }
                Thread.currentThread().setName(oldThreadName);
            }
        }
    }

    public boolean isValidFast() {
        return valid;
    }

    private void setInvalid(boolean force) {
        if (force) {
            valid = false;
        } else {
            if (attempts.incrementAndGet() > MAX_ATTEMPTS) {                
                valid = false;
            }
        }
        RemoteLogger.log(Level.WARNING, "fs_server at {0} failed: {1} ", env, lastErrorMessage.get());
    }

    /**
     * Slow validity check - includes launching (if needed) of remote tools, etc.
     * It can be slow on first call within a session or after reconncet.
     * It should be fast in other cases.
     *
     * It should throw one of declared exceptions
     * if the check is not possible (for example, the connection to host is lost).
     * Such exception will be rethrown to client.
     * Otherwise it should just return true or false.
     */
    public boolean isValidSlow()
            throws ConnectException, InterruptedException {
        FsServer srv = null;
        try {
            srv = getOrCreateServer();
        } catch (ConnectException ex) {
            throw ex;
        } catch (InitializationException | IOException | ExecutionException ex) {
            ex.printStackTrace(System.err);
        }
        if (srv == null) {
            setInvalid(true);
            return false;
        } else {
            return true;
        }
    }

    private void checkValid() throws ExecutionException, InterruptedException {
        if (ConnectionManager.getInstance().isConnectedTo(env)) {
            FsServer srv = getServer();
            if (srv != null) {
                NativeProcess process = srv.getProcess();
                if (!ProcessUtils.isAlive(process)) {
                    try {
                        int rc = process.waitFor();
                        if (rc != 0 && rc != -1) { // -1 most likely means just disconnect
                            setInvalid(false);
                        }
                        ExecutionException exception = new ExecutionException(lastErrorMessage.get(), null);
                        synchronized (responseLock) {
                            for (FSSResponse rsp : responses.values()) {                                
                                rsp.failed(exception);
                            }
                        }
                        throw exception;
                    } catch (IllegalThreadStateException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            }
        }
    }

    @Override
    public void dispose(FSSResponse response) {
        synchronized (responseLock) {
            responses.remove(response.getId());
        }
    }

    public FSSResponse dispatch(FSSRequest request) throws IOException, ConnectException, 
            InterruptedException, ExecutionException {
        return dispatch(request, null);
    }
    
    public FSSResponse dispatch(FSSRequest request, FSSResponse.Listener listener) throws IOException, ConnectException, 
            InterruptedException, ExecutionException {
        FSSResponse response = null;
        if (request.needsResponse()) {
            response = new FSSResponse(request, this, listener, env);
            synchronized (responseLock) {
                RemoteLogger.assertNull(responses.get(request.getId()),
                        "response should be null for id {0}", request.getId()); // NOI18N
                responses.put(request.getId(), response);
            }
        }
        FsServer srv;
        try {
            srv = getOrCreateServer();
        } catch (ConnectException ex) {
            throw ex; // that's normal, transport still valid, just disconnect occurred
        } catch (IOException ex) {
            setInvalid(false);
            throw ex;
        } catch (ExecutionException ex) {
            setInvalid(false);
            throw ex;
        } catch (InitializationException ex) {
            throw new ExecutionException(ex);
        }
        PrintWriter writer = srv.getWriter();
        sendRequest(writer, request);
        if(writer.checkError()) { // should we use just input stream instead of writer?
            checkValid();
        }     
        return response;
    }
    
    /*package*/ static void sendRequest(PrintWriter writer, FSSRequest request) {
        String escapedPath = FSSUtil.escape(request.getPath());
        String path2 = request.getPath2();
        String buffer;
        if (path2 == null) {
            buffer = String.format("%c %d %d %s\n", request.getKind().getChar(), // NOI18N
                request.getId(), escapedPath.length(), escapedPath);
        } else {
            String escapedPath2 = FSSUtil.escape(path2);
            buffer = String.format("%c %d %d %s %d %s\n", request.getKind().getChar(), // NOI18N
                    request.getId(), escapedPath.length(), escapedPath, escapedPath2.length(), escapedPath2);
        }
        RemoteLogger.finest("### sending request {0}", buffer);
        writer.print(buffer);
        writer.flush();   
    }

    void shutdown() {
        FsServer server = getServer();
        if (server != null) {            
            NativeProcess process = server.getProcess();
            if (process.isAlive()) {
                FSSRequest req = new FSSRequest(FSSRequestKind.FS_REQ_QUIT, "", true); //NOI18N
                try {
                    dispatch(req);
                } catch (IOException | InterruptedException | ExecutionException ex) {
                    RemoteLogger.fine(ex);
                }
            }
        }
    }

    private FsServer getServer() {        
        synchronized (serverLock) {
            return server;
        }
    }
    
    /*package*/ static File testGetOriginalFSServerFile(ExecutionEnvironment execEnv) 
            throws IOException, ConnectionManager.CancellationException {
        String path = getOriginalFSServerPath(execEnv);
        return InstalledFileLocator.getDefault().locate(
                path, "org.netbeans.modules.remote.impl", false); // NOI18N
   }
    
    void testDump(PrintStream ps) {
        ps.printf("Dumping %s [%s]\n", this.traceName, this.valid ? "valid" : "invalid"); //NOI18N
        ps.printf("\tlastErrorMessage=%s \n", this.lastErrorMessage); // NOI18N
        FsServer srv;
        synchronized (serverLock) {
            srv = this.server; 
        }
        if (srv != null) {
            srv.testDump(ps);
        }
    }

    private static String getOriginalFSServerPath(ExecutionEnvironment execEnv) 
            throws IOException, ConnectionManager.CancellationException {

        String toolPath = "";
        MacroExpanderFactory.MacroExpander macroExpander = MacroExpanderFactory.getExpander(execEnv);
        try {
            String platformPath;
            HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
            HostInfo.OSFamily osFamily = hostInfo.getOSFamily();
            if (osFamily == HostInfo.OSFamily.UNKNOWN) {
                throw new IOException("Unsupported platform on " + execEnv.getDisplayName()); //NOI18N
            } else {
                String toExpand = "$osname-$platform" + // NOI18N
                        ((osFamily == HostInfo.OSFamily.LINUX && 
                        hostInfo.getCpuFamily() != HostInfo.CpuFamily.SPARC) ? "${_isa}" : ""); // NOI18N
                platformPath = macroExpander.expandPredefinedMacros(toExpand);
            }
            toolPath += "bin/" + platformPath + "/fs_server"; //NOI18N
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return toolPath;
    }
    
    private String checkServerSetup() throws ConnectException, IOException, 
            InterruptedException, ExecutionException {

        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            throw RemoteExceptions.createConnectException(RemoteFileSystemUtils.getConnectExceptionMessage(env));
        }

        String toolPath;
        try {
            toolPath = getOriginalFSServerPath(env);
        } catch (IOException ioe) {
            setInvalid(true);
            throw ioe;
        } catch (ConnectionManager.CancellationException ex) {
            // we can never get here since we already checked isConnectedTo above
            Exceptions.printStackTrace(ex);
            throw RemoteExceptions.createConnectException(RemoteFileSystemUtils.getConnectExceptionMessage(env));
        }
        HostInfo hostInfo;
        try {
            hostInfo = HostInfoUtils.getHostInfo(env);
        } catch (ConnectionManager.CancellationException ex) {
            Exceptions.printStackTrace(ex); // can never happen as we checked isConnected() above
            throw RemoteExceptions.createConnectException(RemoteFileSystemUtils.getConnectExceptionMessage(env));
        }

        String remotePath = USER_DEFINED_REMOTE_SERVER_PATH;
        if (remotePath == null) {
            remotePath = hostInfo.getTempDir() + "/" + toolPath; // NOI18N
        }
        String remoteBase = PathUtilities.getDirName(remotePath);
        
        File localFile;
        if (USER_DEFINED_LOCAL_SERVER_PATH != null) {
            localFile = new File(USER_DEFINED_LOCAL_SERVER_PATH);
        } else {
            localFile = InstalledFileLocator.getDefault().locate(
                    toolPath, "org.netbeans.modules.remote.impl", false); // NOI18N
        }
        if (localFile != null && localFile.exists()) {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
            npb.setExecutable("/bin/mkdir").setArguments("-p", remoteBase); // NOI18N
            ProcessUtils.execute(npb);
            Future<CommonTasksSupport.UploadStatus> copyTask;
            copyTask = CommonTasksSupport.uploadFile(localFile, env, remotePath, 0755, true); // NOI18N
            CommonTasksSupport.UploadStatus uploadStatus = copyTask.get();
            if (!uploadStatus.isOK()) {
                setInvalid(true);
                throw new IOException(uploadStatus.getError());
            }
        } else {
            if (!HostInfoUtils.fileExists(env, remotePath)) {
                setInvalid(true);
                throw new FileNotFoundException(env.getDisplayName() + ':' + remotePath); //NOI18N
            }
        }
        return remotePath;
    }
    
    private FsServer getOrCreateServer() throws IOException, ConnectException, 
            InterruptedException, ExecutionException, InitializationException {
        synchronized (serverLock) {
            if (server != null) {
                if (!ProcessUtils.isAlive(server.getProcess())) {
                    server = null;
                }
            }
            if (server == null) {
                if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                    throw RemoteExceptions.createConnectException(RemoteFileSystemUtils.getConnectExceptionMessage(env));
                }
                String path = checkServerSetup();
                try {
                    server = new FsServer(path);
                } catch (ConnectionManager.CancellationException ex) {
                    // we can never get here since we checked isConnectedTo above
                    Exceptions.printStackTrace(ex);
                    throw RemoteExceptions.createConnectException(RemoteFileSystemUtils.getConnectExceptionMessage(env));
                }
                RP.post(new ErrorReader(server.getProcess().getErrorStream()));
                try {
                    handShake();
                    setupProhibitedToLstat();
                } catch (InitializationException ex) {
                    server = null;
                    setInvalid(true);
                    throw ex;
                }
                RP.post(new MainLoop());
            }
            return server;
        }
    }
    
    private void setupProhibitedToLstat() {
        List<String> forbiddenPaths = getFileSystem().getDirsProhibitedToStat(traceName);
        if(forbiddenPaths == null || forbiddenPaths.isEmpty()) {
            return;
        }
        FsServer srv = server;
        if (srv == null) {
            return; // this should not happen, but server is not final => need to check
        }
        StringBuilder sb = new StringBuilder("dirs-forbidden-to-stat="); // NOI18N
        boolean first = true;
        for (String path : forbiddenPaths) {
            sb.append(path).append(first ? "" : ":"); // NOI18N
            first = false;
        }
        FSSRequest req = new FSSRequest(FSSRequestKind.FS_REQ_OPTION, sb.toString());
        sendRequest(server.getWriter(), req);
    }

    private void handShake() throws IOException, InitializationException, InterruptedException {
        FSSRequest infoReq = new FSSRequest(FSSRequestKind.FS_REQ_SERVER_INFO, "");
        sendRequest(server.getWriter(), infoReq);
        String line = server.getReader().readLine();
        if (line == null) {
            NativeProcess.State state = server.getProcess().getState();
            if (state == NativeProcess.State.FINISHED) {
                //int rc = server.getProcess().waitFor();
                //if (rc == FSSExitCodes.FAILURE_LOCKING_LOCK_FILE) {
                throw new InitializationException(createInitializerExceptionMessage());
                //}
            }
        } else {
            RemoteLogger.fine("Reading handshake response from {0}:{1} -> {2}", env, server.path, line);
            Buffer buf = new Buffer(line);
            char respKind = buf.getChar();
            RemoteLogger.assertTrue(respKind == FSSResponseKind.FS_RSP_SERVER_INFO.getChar());
            int respId = buf.getInt();
            RemoteLogger.assertTrue(respId == infoReq.getId());
            String version = buf.getRest().trim();
            checkVersions(getMinServerVersion(), version);
            if (accessCheckType != FileSystemProvider.AccessCheckType.FULL) {
                sendAccessTypeChangeRequest(accessCheckType);
            }
            RemoteLogger.info("Remote agent version " + version + " [" + env + "] "); //NOI18N
        }
    }

    private String createInitializerExceptionMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("fs_server failed at ").append(env.toString()).append(' '); //NOI18N
        if (HostInfoUtils.isHostInfoAvailable(env)) {
            try {
                HostInfo hi = HostInfoUtils.getHostInfo(env);
                sb.append('(').append(hi.getOS().getName()).append(' ').append(hi.getCpuFamily()).append(' ');
                sb.append(" - ").append(hi.getOS().getVersion()).append(')'); // NOI18N
            } catch (ConnectionManager.CancellationException | IOException ex) {
                Exceptions.printStackTrace(ex); // never occurs
            }
        }
        NativeProcess process = server.getProcess();
        if (process != null) {
            sb.append(" rc=").append(process.exitValue()).append(' '); // NOI18N
        }
        sb.append(' ').append(lastErrorMessage.get());
        return sb.toString();
    }

    /** 
     * Checks versions in format N.N.N where N a number that has 1 or more digits 
     */
    private void checkVersions(String ref, String fact) throws InitializationException {
        String[] refArr = ref.split("\\."); //NOI18N
        String[] factArr = fact.split("\\."); //NOI18N
        if (refArr.length != 3) {
            Exceptions.printStackTrace(new IllegalArgumentException("wrong version format: " + ref)); //NOI18N
        }
        if (factArr.length != 3) {
            throw new InitializationException("Wrong version format: " + fact); // NOI18N
        }
        for (int i = 0; i < 3; i++) {
            int refValue;
            int factValue;
            try {
                refValue = Integer.parseInt(refArr[i]);
            } catch (NumberFormatException nfe) {
                throw new InitializationException("Wrong version format: " + ref); // NOI18N
            }
            try {
                factValue = Integer.parseInt(factArr[i]);
            } catch (NumberFormatException nfe) {
                throw new InitializationException("Wrong version format: " + fact); // NOI18N
            }
            if (factValue < refValue) {
                throw new InitializationException("Wrong server version: " + fact + " should be more or equal to " + ref); // NOI18N
            } else if (factValue > refValue) {
                break; // minor version does not matter
            }
        }        
    }

    private class ErrorReader implements Runnable {
        
        private final InputStream inputStream;

        public ErrorReader(InputStream inputStream) {
            this.inputStream = inputStream;
        }
        
        @Override
        public void run() {
            String oldThreadName = Thread.currentThread().getName();
            try {
                Thread.currentThread().setName("fs_server error reader for " + env); // NOI18N
                BufferedReader reader = ProcessUtils.getReader(inputStream, true);
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!SUPPRESS_STDERR) {
                            System.err.printf("%s %s\n", env, line); //NOI18N
                        }
                        lastErrorMessage.set(line);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                } finally {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                    }
                }
            } finally {
                Thread.currentThread().setName(oldThreadName);
            }
        }
        
    }

    private String lastPIDKey() {
        return "last_fs_server_pid:" + ExecutionEnvironmentFactory.toUniqueID(env); //NOI18N
    }

    private void setLastPID(int pid) {
        NbPreferences.forModule(FSSDispatcher.class).putInt(lastPIDKey(), pid);
    }

    private int getLastPID() {
        return NbPreferences.forModule(FSSDispatcher.class).getInt(lastPIDKey(), 0);
    }

    private class FsServer {

        private final PrintWriter writer;
        private final BufferedReader reader;
        private final NativeProcess process;
        private final String path;
        private final String[] args;

        public FsServer(String path) throws IOException, ConnectionManager.CancellationException {
            this.path = path;
            NativeProcessBuilder processBuilder = NativeProcessBuilder.newProcessBuilder(env);
            processBuilder.setExecutable(this.path);            
            List<String> argsList = new ArrayList<>();
            argsList.add("-t"); // NOI18N
            argsList.add("4"); // NOI18N
            argsList.add("-p"); // NOI18N
            argsList.add("-d"); // NOI18N
            argsList.add(getCacheDirectory());
            if (REFRESH_INTERVAL > 0) {
                argsList.add("-r"); // NOI18N
                argsList.add("" + REFRESH_INTERVAL);
            }
            final int VERBOSE = Integer.getInteger("remote.fs_server.verbose", 0); // NOI18N
            if (VERBOSE > 0) {
                argsList.add("-v"); // NOI18N
                argsList.add("" + VERBOSE); // NOI18N
            }
            if (Boolean.getBoolean("remote.fs_server.log")) {
                argsList.add("-l"); // NOI18N
            }
            // the "remote.fs_server.redirect.err" property can contain either "true" or "false" or file name
            String redirectErr = System.getProperty("remote.fs_server.redirect.err");
            if (redirectErr != null && !redirectErr.equalsIgnoreCase("false")) { //NOI18N
                argsList.add("-e"); // NOI18N
                if (!redirectErr.equalsIgnoreCase("true")) { // NOI18N
                    argsList.add(redirectErr);
                }
            }
            if (cleanupUponStart) {
                argsList.add("-c"); // NOI18N
            } else {
                if (!getFileSystem().getRoot().getImplementor().hasCache()) {
                    // there is no cache locally => clean remote cache as well
                    argsList.add("-c"); // NOI18N
                }
            }
            final int SERVER_THREADS = Integer.getInteger("remote.fs_server.threads", 0); // NOI18N
            if (SERVER_THREADS > 0 ) {
                argsList.add("-t"); // NOI18N
                argsList.add(Integer.toString(SERVER_THREADS));
            }
            boolean killAllLockers = Boolean.getBoolean("remote.fs_server.kill.all"); // NOI18N
            int killTimeout = Integer.getInteger("remote.fs_server.kill.timeout", 3000); // NOI18N
            if (killAllLockers) {
                argsList.add("-K"); // NOI18N
                argsList.add(Integer.toString(killTimeout));
            } else {
                int lastPID = getLastPID();
                if (lastPID != 0) {
                    argsList.add("-K"); // NOI18N
                    argsList.add(Integer.toString(lastPID) + ':' + Integer.toString(killTimeout)); // NOI18N
                }
            }
            this.args = argsList.toArray(new String[argsList.size()]);
            processBuilder.setArguments(this.args);
            process = processBuilder.call();
            setLastPID(process.getPID());
            Charset charset = Charset.isSupported("UTF-8") // NOI18N
                    ? Charset.forName("UTF-8") // NOI18N
                    : Charset.defaultCharset();
            writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), charset));

            InputStream inputStream = process.getInputStream();
            //Thread.sleep(100);
            int available = inputStream.available();
            if (available > 0) {
                StringBuilder sb = new StringBuilder();
                while ((available = inputStream.available()) > 0) {
                    byte[] buffer = new byte[available];
                    int cnt = inputStream.read(buffer);
                    String line = new String(buffer, charset);
                    sb.append(line).append('\n');
                }
                RemoteLogger.finest("Warning: got the following without any request on {0} startup: \n{1}\n", env, sb); //NOI18N
                RemoteNotifier.notifyError(
                        NbBundle.getMessage(FSSDispatcher.class, "EchoInProfile_Title", env.getDisplayName()),
                        NbBundle.getMessage(FSSDispatcher.class, "EchoInProfile_Details", env.getDisplayName()));
            }


            reader = new BufferedReader(new InputStreamReader(inputStream, charset));
            StringBuilder sb = new StringBuilder("[").append(env.getDisplayName()).append("] "). // NOI18N
                    append("Started remote agent ").append(path).append(' '); // NOI18N
            for (String p : args) {
                sb.append(p).append(' ');
            }
            try {
                int pid = process.getPID();
                sb.append(" [pid=").append(pid).append(" at ").append(env).append("] "); // NOI18N
            } catch (IllegalStateException ex) {
                sb.append(" [no pid] "); // NOI18N
            }
            RemoteLogger.info(sb.toString());                
        }
        
        private String getCacheDirectory() throws IOException, ConnectionManager.CancellationException {
            final String SERVER_CACHE = System.getProperty("remote.fs_server.remote.cache");
            if (SERVER_CACHE != null) {
                return SERVER_CACHE;
            } else {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                String path = hostInfo.getTempDir().trim();
                if (!path.endsWith("/")) { //NOI18N
                    path += "/"; //NOI18N
                }
                return path + "fs_server_cache"; //NOI18N
            }
        }

        public PrintWriter getWriter() {
            return writer;
        }

        public BufferedReader getReader() {
            return reader;
        }

        public NativeProcess getProcess() {
            return process;
        }

        private void testDump(PrintStream ps) {
            int pid = -2;
            try {
                pid = process.getPID();
            } catch (IOException ex) {
            }            
            ps.printf("\t[pid=%d] %s", pid, path); //NOI18N
            for (String p : args) {
                ps.print(p);
                ps.print(' '); //NOI18N
            }
            ps.print('\n');
            ps.printf("\t[pid=%d] state=%s ", pid,  process.getState()); //NOI18N
            try {
                ProcessStatusEx exitStatusEx = process.getExitStatusEx();
                ps.printf("\trc=%d signalled=%b termSignal=%s ", //NOI18N
                        exitStatusEx.getExitCode(),
                        exitStatusEx.ifSignalled(),
                        exitStatusEx.termSignal());
            } catch (Throwable thr) {
            }
            ps.print('\n');
            if (ProcessUtils.isAlive(process)) {
                HostInfo hostInfo = null;
                try {
                    hostInfo = HostInfoUtils.getHostInfo(env);
                } catch (IOException ex) {
                } catch (ConnectionManager.CancellationException ex) {
                }
                if (hostInfo != null && hostInfo.getOSFamily() == HostInfo.OSFamily.SUNOS) {
                    ProcessUtils.ExitStatus res = ProcessUtils.execute(env, "pstack", "" + pid); // NOI18N
                    if (res.isOK()) {
                        System.err.println(res.getOutputString());
                    }
                }
            }
        }
    }
    
    public static class InitializationException extends Exception {
        public InitializationException(String message) {
            super(message);
        }
    }    
}

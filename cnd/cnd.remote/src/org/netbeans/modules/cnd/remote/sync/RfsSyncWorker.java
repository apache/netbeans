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

package org.netbeans.modules.cnd.remote.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteException;
import org.netbeans.modules.cnd.remote.support.RemoteLogger;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.spi.remote.setup.support.RemoteSyncNotifier;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
/*package*/ final class RfsSyncWorker extends BaseSyncWorker implements RemoteSyncWorker {

    private static final RequestProcessor RP = new RequestProcessor("RFS Sync Worker", 20); // NOI18N
    //private NativeProcess remoteControllerProcess;
    private RfsLocalController localController;
    private RemoteProcessController remoteController;
    private String remoteDir;
    private ErrorReader errorReader;
    private static final String exitFlagFile = System.getProperty("cnd.rfs.controller.exit.flag.file");

    public RfsSyncWorker(ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err,
            FileObject privProjectStorageDir, List<FSPath> paths, List<FSPath> buildResults) {
        super(executionEnvironment, out, err, privProjectStorageDir, paths, buildResults);
    }

    @Override
    public boolean startup(Map<String, String> env2add) {

        if (SyncUtils.isDoubleRemote(executionEnvironment, fileSystem)) {
            RemoteSyncNotifier.getInstance().warnDoubleRemote(executionEnvironment, fileSystem);
            return false;
        }

        RemotePathMap mapper = RemotePathMap.getPathMap(executionEnvironment);
        remoteDir = mapper.getRemotePath("/", false); // NOI18N
        if (remoteDir == null) {
            if (err != null) {
                err.printf("%s%n", NbBundle.getMessage(getClass(), "MSG_Cant_find_sync_root", ServerList.get(executionEnvironment).toString()));
            }
            return false; // TODO: error processing
        }

        boolean success = false;
        try {
            if (out != null) {
                out.printf("%s%n", NbBundle.getMessage(getClass(), "MSG_Copying",
                        remoteDir, ServerList.get(executionEnvironment).toString()));
            }
            Future<Integer> mkDir = CommonTasksSupport.mkDir(executionEnvironment, remoteDir, err);
            if (mkDir.get() != 0) {
                throw new IOException("Can not create directory " + remoteDir); //NOI18N
            }
            success = startupImpl(env2add);
        } catch (RemoteException ex) {
            printErr(ex);
        } catch (CancellationException ex) {
            // reporting does not make sense, just return false
            RemoteUtil.LOGGER.finest(ex.getMessage());
        } catch (InterruptedException ex) {
            // reporting does not make sense, just return false
            RemoteUtil.LOGGER.finest(ex.getMessage());
        } catch (InterruptedIOException ex) {
            // reporting does not make sense, just return false
            RemoteUtil.LOGGER.finest(ex.getMessage());
        } catch (ExecutionException ex) {
            RemoteUtil.LOGGER.log(Level.FINE, null, ex);
            if (err != null) {
                err.printf("%s%n", NbBundle.getMessage(getClass(), "MSG_Error_Copying",
                        remoteDir, ServerList.get(executionEnvironment).toString(), ex.getLocalizedMessage()));
            }
        } catch (IOException ex) {
            RemoteUtil.LOGGER.log(Level.FINE, null, ex);
            if (err != null) {
                err.printf("%s%n", NbBundle.getMessage(getClass(), "MSG_Error_Copying",
                        remoteDir, ServerList.get(executionEnvironment).toString(), ex.getLocalizedMessage()));
            }
        }
        return success;
    }

    private void printErr(Exception ex) throws MissingResourceException {
        RemoteUtil.LOGGER.finest(ex.getMessage());
        if (err != null) {
            String message = NbBundle.getMessage(getClass(), "MSG_Error_Copying", remoteDir, ServerList.get(executionEnvironment).toString(), ex.getLocalizedMessage());
            err.printf("%s%n", message); // NOI18N
            err.printf("%s%n", message); // NOI18N
        }
    }

    private boolean startupImpl(Map<String, String> env2add) throws IOException, InterruptedException, ExecutionException, RemoteException, CancellationException {
        String remoteControllerPath;
        String ldLibraryPath;
        try {
            remoteControllerPath = RfsSetupProvider.getControllerPath(executionEnvironment);
            CndUtils.assertTrue(remoteControllerPath != null);
            ldLibraryPath = RfsSetupProvider.getLdLibraryPath(executionEnvironment);
            CndUtils.assertTrue(ldLibraryPath != null);
        } catch (ParseException ex) {
            throw new ExecutionException(ex);
        }

        NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(executionEnvironment);
        // nobody calls this concurrently => no synchronization
        remoteControllerCleanup(); // just in case
        pb.setExecutable(remoteControllerPath); //I18N
        pb.setWorkingDirectory(remoteDir);
        String rfsTrace = System.getProperty("cnd.rfs.controller.trace");
        if (rfsTrace != null) {
            pb.getEnvironment().put("RFS_CONTROLLER_TRACE", rfsTrace); // NOI18N
        }        
        // For the purpose of dynamic testing with discover;
        // Usually we stop controller via sending SIGTERM, but discover doesn't flush its output in this case
        if (exitFlagFile != null) {
            pb.getEnvironment().put("RFS_CONTROLLER_EXIT_FLAG_FILE", exitFlagFile); // NOI18N
        }
        NativeProcess remoteControllerProcess = pb.call(); // ProcessUtils.execute doesn't work here
        remoteController = new RemoteProcessController(remoteControllerProcess);

        errorReader = new ErrorReader(remoteControllerProcess.getErrorStream(), err);
        RP.post(errorReader);

        final InputStream rcInputStream = remoteControllerProcess.getInputStream();
        final OutputStream rcOutputStream = remoteControllerProcess.getOutputStream();
        final BufferedReader rcInputStreamReader = ProcessUtils.getReader(rcInputStream, executionEnvironment.isRemote());
        final PrintWriter rcOutputStreamWriter = ProcessUtils.getWriter(rcOutputStream, executionEnvironment.isRemote());
        localController = new RfsLocalController(
                executionEnvironment, files, buildResults, remoteController, rcInputStreamReader,
                rcOutputStreamWriter, err, privProjectStorageDir);

        if (!localController.init()) {
            remoteControllerProcess.destroy();
            return false;
        }

        // read port
        String line = rcInputStreamReader.readLine();
        String port;
        if (line != null && line.startsWith("PORT ")) { // NOI18N
            port = line.substring(5);
        } else if (line == null) {
            int rc = remoteControllerProcess.waitFor();
            throw new ExecutionException(String.format("Remote controller failed; rc=%d%n", rc), null); // NOI18N
        } else {
            String message = String.format("Protocol error: read \"%s\" expected \"%s\"%n", line,  "PORT <port-number>"); //NOI18N
            System.err.printf(message); // NOI18N
            remoteControllerProcess.destroy();
            throw new ExecutionException(message, null); //NOI18N
        }
        RemoteUtil.LOGGER.log(Level.FINE, "Remote Controller listens port {0}", port); // NOI18N
        RP.post(localController);

        String preload = RfsSetupProvider.getPreloadName(executionEnvironment);
        CndUtils.assertTrue(preload != null);        
        if (Boolean.getBoolean("cnd.rfs.discover")) {
            preload = "libdiscover.so:" + preload; // NOI18N
            String studioPath = System.getProperty("cnd.rfs.discover.studio.path", "/opt/solarisstudio12.5"); //NOI18N
            ldLibraryPath = studioPath + "/lib/compilers:" + studioPath + "/lib/compilers/amd64:" + ldLibraryPath; // NOI18N
            String discoverFile = System.getProperty("cnd.rfs.discover.file", "/tmp/rfs_preload.%p.log"); //NOI18N
            env2add.put("SUNW_DISCOVER_OPTIONS", "-w " + discoverFile); // NOI18N
        }
        env2add.put("LD_PRELOAD", preload); // NOI18N
        String ldLibPathVar = "LD_LIBRARY_PATH"; // NOI18N
        String oldLdLibPath = MacroMap.forExecEnv(executionEnvironment).get(ldLibPathVar);
        if (oldLdLibPath != null) {
            ldLibraryPath += ":" + oldLdLibPath; // NOI18N
        }
        env2add.put(ldLibPathVar, ldLibraryPath); // NOI18N
        env2add.put("RFS_CONTROLLER_DIR", remoteDir); // NOI18N
        env2add.put("RFS_CONTROLLER_PORT", port); // NOI18N

        addRemoteEnv(env2add, "cnd.rfs.preload.sleep", "RFS_PRELOAD_SLEEP"); // NOI18N
        addRemoteEnv(env2add, "cnd.rfs.preload.log", "RFS_PRELOAD_LOG"); // NOI18N
        addRemoteEnv(env2add, "cnd.rfs.controller.log", "RFS_CONTROLLER_LOG"); // NOI18N
        addRemoteEnv(env2add, "cnd.rfs.controller.port", "RFS_CONTROLLER_PORT"); // NOI18N
        addRemoteEnv(env2add, "cnd.rfs.controller.host", "RFS_CONTROLLER_HOST"); // NOI18N
        addRemoteEnv(env2add, "cnd.rfs.preload.trace", "RFS_PRELOAD_TRACE"); // NOI18N

        RemoteUtil.LOGGER.fine("Setting environment:");

        return true;
    }

    private void addRemoteEnv(Map<String, String> env2add, String localJavaPropertyName, String remoteEnvVarName) {
        String value = System.getProperty(localJavaPropertyName, null);
        if (value != null) {
            env2add.put(remoteEnvVarName, value);
        }
    }

    @Override
    public void shutdown() {
        RfsLocalController lc;
        synchronized (this) {
            lc = localController;
        }
        remoteControllerCleanup();
        localControllerCleanup();
        refreshRemoteFs();
        lc.waitShutDownFinished();
    }

    @Override
    public boolean cancel() {
        return false;
    }

    private void refreshRemoteFs() {
        RemotePathMap mapper = RemotePathMap.getPathMap(executionEnvironment);
        Collection<String> remoteDirs = new ArrayList<>(files.length);
        for (File file : files) {
            if (!file.isDirectory()) {
                file = file.getParentFile();
            }
            if (file != null) {
                String path = mapper.getRemotePath(file.getAbsolutePath());
                if (path != null) {
                    remoteDirs.add(path);
                }
            }
        }
        FileSystemProvider.scheduleRefresh(executionEnvironment, remoteDirs);
    }

    private void localControllerCleanup() {
        synchronized (this) {
            localController = null;
        }
    }

    private void remoteControllerCleanup() {
        ErrorReader r = errorReader;
        if (r != null) {
            r.stop();
        }
        RemoteProcessController rc;
        synchronized (this) {
            rc = remoteController;
            remoteController = null;
        }
        if (rc != null) {
            rc.stop();
        }
    }


    private static class ErrorReader implements Runnable {

        //private final BufferedReader errorReader;
        private final InputStream errorStream;
        private final PrintWriter errorWriter;
        private final AtomicBoolean stopped;

        public ErrorReader(InputStream errorStream, PrintWriter errorWriter) {
            this.errorStream = errorStream;
            this.errorWriter = errorWriter;
            this.stopped = new AtomicBoolean(false);
        }
        @Override
        public void run() {
            BufferedReader errorReader = null;
            try {
                errorReader = new BufferedReader(new InputStreamReader(errorStream, "UTF-8")); //NOI18N
                String line;
                while ((line = errorReader.readLine()) != null) {
                    if (stopped.get()) {
                        break;
                    }
                    if (errorWriter != null) {
                         errorWriter.println(line);
                    }
                    RemoteUtil.LOGGER.fine(line);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (errorReader != null) {
                    try {
                        errorReader.close();
                    } catch (IOException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            }
        }

        private void stop() {
            stopped.set(true);
        }
    }

    static class RemoteProcessController {
        private final NativeProcess remoteControllerProcess;
        private final AtomicBoolean stopped = new AtomicBoolean(false);

        RemoteProcessController (NativeProcess remoteControllerProcess){
            this.remoteControllerProcess = remoteControllerProcess;
        }

        boolean isAlive(){
            return ProcessUtils.isAlive(remoteControllerProcess);
        }

        boolean isStopped(){
            return stopped.get();
        }

        private void stopViaFlag() {
            try {
                ExecutionEnvironment env = this.remoteControllerProcess.getExecutionEnvironment();
                RemoteLogger.info("Stopping remote controller via flag: {0}", exitFlagFile);
                CommonTasksSupport.mkDir(env, exitFlagFile, null).get();
                RemoteLogger.info("Waiting for remote controller to finish... ");
                remoteControllerProcess.waitFor();
                RemoteLogger.info("Remote controller has finished");
                RemoteLogger.info("Remvoing flag file: {0}", exitFlagFile);
                CommonTasksSupport.rmDir(env, exitFlagFile, true, null);
                RemoteLogger.info("Stopping remote controller via flag: {0}", exitFlagFile);
            } catch (InterruptedException ex) {
                ex.printStackTrace(System.err);
            } catch (ExecutionException ex) {
                ex.printStackTrace(System.err);
            }
        }

        void stop() {
            stopped.set(true);
            if (exitFlagFile != null) {
                stopViaFlag();
                return;
            }
            remoteControllerProcess.destroy();
        }
    }
}

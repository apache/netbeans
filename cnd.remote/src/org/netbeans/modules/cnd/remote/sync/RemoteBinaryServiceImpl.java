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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.remote.api.RemoteBinaryService;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * RemoteBinaryService implementation
 */
@ServiceProvider(service = RemoteBinaryService.class)
public class RemoteBinaryServiceImpl extends RemoteBinaryService {
    
    private static final RequestProcessor RP = new RequestProcessor("RemoteBinaryServiceImpl", 1); // NOI18N

    private final Map<ExecutionEnvironment, Delegate> impls = new HashMap<>();
    private static int downloadCount = 0;

    @Override
    protected RemoteBinaryResult getRemoteBinaryImpl(ExecutionEnvironment execEnv, String remotePath) {
        CndUtils.assertNonUiThread();
        Delegate delegate;
        synchronized (this) {
            delegate = impls.get(execEnv);
            if (delegate == null) {
                delegate = new Delegate(execEnv);
                impls.put(execEnv, delegate);
            }
        }
        try {
            return delegate.getRemoteBinaryImpl(remotePath);
        } catch (InterruptedException ex) {
            // don't log InterruptedException
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (ExecutionException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /*package-local for test purposes*/ static int getDownloadCount() {
        return downloadCount;
    }

    /*package-local for test purposes*/ static void resetDownloadCount() {
        downloadCount = 0;
    }

    /**
     * Corresoinds to the particular execution environment.
     * An instance of impl is created for each execution environment
     */
    private static class Delegate {

        private final ExecutionEnvironment execEnv;
        private final Map<String, Entry> cache = new HashMap<>();

        public Delegate(ExecutionEnvironment execEnv) {
            this.execEnv = execEnv;
        }

        public RemoteBinaryResult getRemoteBinaryImpl(String remotePath) throws InterruptedException, IOException, ExecutionException {
            Entry entry;
            synchronized (this) {
                entry = cache.get(remotePath);
                if (entry == null) {
                    entry = new Entry(execEnv, remotePath);
                    cache.put(remotePath, entry);
                }
            }
            return entry.ensureSync();
        }
    }

    /**
     * Corresponds to a particular file
     */
    private static class Entry {

        private final String remotePath;
        private final ExecutionEnvironment execEnv;
        private RemoteBinaryResult lastResult;
        private String timeStamp;

        public Entry(ExecutionEnvironment execEnv, String remotePath) {
            this.remotePath = remotePath;
            this.execEnv = execEnv;
        }

        public RemoteBinaryResult ensureSync() throws InterruptedException, IOException, ExecutionException {
            String localPath = RemotePathMap.getPathMap(execEnv).getLocalPath(remotePath);

            RemoteBinaryResult result = null;

            if (localPath != null &&
                    RemotePathMap.isTheSame(execEnv,
                    //new File(remotePath).getParentFile().getAbsolutePath(),
                    CndPathUtilities.getDirName(remotePath),
                    CndFileUtils.createLocalFile(localPath).getParentFile())) {
                if (lastResult == null) {
                    lastResult = new RemoteBinaryResult(localPath, new FutureTask<>(new Callable<Boolean>() {

                        @Override
                        public Boolean call() throws Exception {
                            return true;
                        }
                    }));
                    File file = new File(localPath);
                    lastResult.setTimeStamp(""+file.lastModified()); // NOI18N
                }
            } else {
                result = syncImpl();
                if (result != null) {
                    lastResult = result;
                }
            }

            return result == null ? lastResult : result;
        }

        private Pair<String, String[]> getFullTimeLsCommand(String path) throws IOException, CancellationException {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
            switch (hostInfo.getOSFamily()) {
                case LINUX:
                    return Pair.of("/bin/ls", new String[] { "--full-time", path}); // NOI18N
                case MACOSX:
                case FREEBSD:
                    return Pair.of("/bin/ls", new String[] { "-lT", path}); // NOI18N
                case SUNOS:
                    return Pair.of("/bin/ls", new String[] { "-lE", path}); // NOI18N
                case WINDOWS:
                    throw new IllegalStateException("Windows in unsupported"); //NOI18N
                case UNKNOWN:
                default:
                    return Pair.of("/bin/ls", new String[] { "-l", path}); // NOI18N
            }
        }

        /**
         *
         * @return
         * @throws IOException
         * @throws InterruptedException
         * @throws ExecutionException
         */
        private synchronized RemoteBinaryResult syncImpl() throws IOException, InterruptedException, ExecutionException {
            final String newTimestamp = getTimestamp();

            if (timeStamp != null && timeStamp.equals(newTimestamp)) {
                if (CndFileUtils.isValidLocalFile(lastResult.localFName)) {
                    return lastResult;
                }
            }

            final File localFile = File.createTempFile("cnd-remote-binary-", ".bin"); // NOI18N
            localFile.deleteOnExit();

            FutureTask<Boolean> task = new FutureTask<>(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {

                    String remoteCopyPath = null;

                    try {
                        HostInfo hinfo = HostInfoUtils.getHostInfo(execEnv);
                        String tmpDir = hinfo.getTempDir();
                        // TODO: add utility method to do mktemp ...
                        remoteCopyPath = tmpDir + "/binary." + newTimestamp.hashCode(); // NOI18N

                        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
                        npb.setExecutable("cp").setArguments(remotePath, remoteCopyPath); // NOI18N
                        ProcessUtils.ExitStatus res = ProcessUtils.execute(npb);
                        if (!res.isOK()) {
                            return false;
                        }

                        Future<Integer> task = CommonTasksSupport.downloadFile(remoteCopyPath, execEnv, localFile.getAbsolutePath(), null);

                        if (task.get() != 0) {
                            return false;
                        }

                        timeStamp = newTimestamp;
                        downloadCount++;
                    } finally {
                        if (remoteCopyPath != null) {
                            CommonTasksSupport.rmFile(execEnv, remoteCopyPath, null);
                        }
                    }

                    return true;
                }
            });

            RP.post(task);
            RemoteBinaryResult remoteBinaryResult = new RemoteBinaryResult(localFile.getAbsolutePath(), task);
            remoteBinaryResult.setTimeStamp(newTimestamp);

            return remoteBinaryResult;
        }

        private String getTimestamp() {
            try {
                Pair<String, String[]> cmdAndArgs = getFullTimeLsCommand(remotePath); // NOI18N
                ProcessUtils.ExitStatus rc = ProcessUtils.execute(execEnv, cmdAndArgs.first(), cmdAndArgs.second());
                if (rc.isOK()) {
                    return rc.getOutputString();
                } else {
                    StringBuilder sb = new StringBuilder(cmdAndArgs.first());
                    for (String arg : cmdAndArgs.second()) {
                        if (sb.length() > 0) {
                            sb.append(' '); //NOI18N
                        }
                        sb.append(arg);
                    }
                    throw new IOException("Cannot run #" + sb + ": " + rc.getErrorString()); // NOI18N
                }
            } catch (CancellationException ex) {
                // TODO:CancellationException error processing
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            return null;
        }
    }
}

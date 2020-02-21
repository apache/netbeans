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
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteLogger;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil.PrefixedLogger;
import org.netbeans.modules.cnd.spi.remote.setup.support.RemoteSyncNotifier;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/*package*/ class RfsLocalController extends NamedRunnable {

    public static final int SKEW_THRESHOLD = Integer.getInteger("cnd.remote.skew.threshold", 1); // NOI18N

    private final RfsSyncWorker.RemoteProcessController remoteController;
    private final BufferedReader requestReader;
    private final PrintWriter responseStream;
    private final ExecutionEnvironment execEnv;
    private final PrintWriter err;
    private final FileData fileData;
    private final RemotePathMap mapper;
    private final PrefixedLogger logger;
    private final SharabilityFilter filter;
    private final FileCollector fileCollector;

    private static final boolean USE_TIMESTAMPS = DebugUtils.getBoolean("cnd.rfs.timestamps", true);
    private static  final char VERSION = USE_TIMESTAMPS ? '5' : '3';

    private static final boolean CHECK_ALIVE = DebugUtils.getBoolean("cnd.rfs.check.alive", true);

    private static final RequestProcessor RP = new RequestProcessor("RfsLocalController", 1); // NOI18N

    private static enum RequestKind {
        REQUEST,
        WRITTEN,
        PING,
        UNKNOWN,
        KILLED
    }

    public RfsLocalController(ExecutionEnvironment executionEnvironment, File[] files, List<File> buildResults,
            RfsSyncWorker.RemoteProcessController remoteController, BufferedReader requestStreamReader, PrintWriter responseStreamWriter, PrintWriter err,
            FileObject privProjectStorageDir) throws IOException {
        super("RFS local controller thread " + executionEnvironment); //NOI18N
        this.execEnv = executionEnvironment;
        this.remoteController = remoteController;
        this.requestReader = requestStreamReader;
        this.responseStream = responseStreamWriter;
        this.err = err;
        this.mapper = RemotePathMap.getPathMap(execEnv);
        this.fileData = FileData.get(privProjectStorageDir, executionEnvironment);
        this.logger = new RemoteUtil.PrefixedLogger("LC[" + executionEnvironment + "]"); //NOI18N
        this.filter = new SharabilityFilter();
        this.fileCollector = new FileCollector(files, buildResults, logger, mapper, filter, fileData, execEnv, err, false);
    }

    private void respond_ok() {
        responseStream.printf("1\n"); // NOI18N
        responseStream.flush();
    }

    private void respond_err(String tail) {
        responseStream.printf("0 %s\n", tail); // NOI18N
        responseStream.flush();
    }

//    private String toRemoteFilePathName(String localAbsFilePath) {
//        String out = localAbsFilePath;
//        if (Utilities.isWindows()) {
//            out = WindowsSupport.getInstance().convertToMSysPath(localAbsFilePath);
//        }
//        if (out.charAt(0) == '/') {
//            out = out.substring(1);
//        } else {
//            RemoteUtil.LOGGER.warning("Path must start with /: " + out + "\n");
//        }
//        return out;
//    }

    private RequestKind getRequestKind(String request) {
        switch (request.charAt(0)) {
            case 'r':   return RequestKind.REQUEST;
            case 'w':   return RequestKind.WRITTEN;
            case 'p':   return RequestKind.PING;
            default:
                if ("Killed".equals(request)){//NOI18N
                    //BZ #193114 - IllegalArgumentException: Protocol error: Killed
                    //let's check the process state
                    if (remoteController.isStopped()){
                        return RequestKind.KILLED;
                    }
                }
                return RequestKind.UNKNOWN;
        }
    }

    private final AtomicReference<CountDownLatch> shutDownLatch = new AtomicReference<>();

    /*package*/ void waitShutDownFinished() {
        CountDownLatch latch = shutDownLatch.get();
        if (latch != null) {
            try {
                latch.await();
            } catch (InterruptedException ex) {
                RemoteLogger.getInstance().log(Level.FINE, "That's just FYI: interrupted", ex);
            }
        }
    }

    @Override
    protected void runImpl() {
        try {
            shutDownLatch.set(new CountDownLatch(1));
            work();
        } finally {
            shutDownLatch.get().countDown();
        }
    }

    private void work() {
        long totalCopyingTime = 0;
        while (true) {
            try {
                String request = requestReader.readLine();
                logger.log(Level.FINEST, "REQ %s", request);
                if (request == null) {
                    break;
                }
                RequestKind kind = getRequestKind(request);
                if (kind == RequestKind.KILLED){
                    //there is something wrong with the process
                    //print to error that remote process is killed
                    if (err != null) {
                        err.append("\nRemote process is killed");//NOI18N
                    }
                    break;
                }else if (kind == RequestKind.UNKNOWN){
                    if (err != null) {
                        err.append("\nProtocol error: " + request);//NOI18N
                    }
                }else   if (kind == RequestKind.PING) {
                    logger.log(Level.FINEST, "PING from remote controller");
                    // no response needed
                    // respond_ok();
                } else {
                    if (request.charAt(1) != ' ') {
                        throw new IllegalArgumentException("Protocol error: " + request); // NOI18N
                    }
                    String remoteFile = request.substring(2);
                    String realPath = fileCollector.getCanonicalToAbsolute(remoteFile);
                    if (realPath != null) {
                        remoteFile = realPath;
                    }
                    String localFilePath = mapper.getLocalPath(remoteFile);
                    if (localFilePath != null) {
                        File localFile = CndFileUtils.createLocalFile(localFilePath);
                        if (kind == RequestKind.WRITTEN) {
                            fileData.setState(localFile, FileState.UNCONTROLLED);
                            fileCollector.addUpdate(localFile);
                            RfsListenerSupportImpl.getInstanmce(execEnv).fireFileChanged(localFile, remoteFile);
                            logger.log(Level.FINEST, "uncontrolled %s", localFile);
                        } else {
                            CndUtils.assertTrue(kind == RequestKind.REQUEST, "kind should be RequestKind.REQUEST, but is ", kind);
                            if (localFile.exists() && !localFile.isDirectory()) {
                                //FileState state = fileData.getState(localFile);
                                logger.log(Level.FINEST, "uploading %s to %s started", localFile, remoteFile);
                                long fileTime = System.currentTimeMillis();
                                Future<UploadStatus> task = CommonTasksSupport.uploadFile(localFile.getAbsolutePath(), execEnv, remoteFile, 0777);
                                try {
                                    UploadStatus uploadStatus = task.get();
                                    fileTime = System.currentTimeMillis() - fileTime;
                                    totalCopyingTime += fileTime;
                                    logger.log(Level.FINEST, "uploading %s to %s finished; rc=%d time = %d total time = %d ms",
                                            localFile, remoteFile, uploadStatus.getExitCode(), fileTime, totalCopyingTime);
                                    if (uploadStatus.isOK()) {
                                        fileData.setState(localFile, FileState.COPIED);
                                        respond_ok();
                                    } else {
                                        if (err != null) {
                                            err.println(uploadStatus.getError());
                                        }
                                        respond_err("1"); // NOI18N
                                    }
                                } catch (InterruptedException ex) {
                                    Exceptions.printStackTrace(ex);
                                    break;
                                } catch (ExecutionException ex) {
                                    Exceptions.printStackTrace(ex);
                                    respond_err("2 execution exception\n"); // NOI18N
                                } finally {
                                    responseStream.flush();
                                }
                            } else {
                                respond_ok();
                            }
                        }
                    } else {
                        respond_ok();
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        //fileData.store();
        shutdown();
    }

    private void shutdown() {
        logger.log(Level.FINEST, "shutdown");
        try {
            fileCollector.runNewFilesDiscovery();
            fileCollector.shutDownNewFilesDiscovery();
            fileData.store();
        } catch (Throwable thr) {
            thr.printStackTrace();
        }
    }

    private boolean checkVersion() throws IOException {
        String versionsString = requestReader.readLine();
        // this is made optional in order not to break compatibility
        final String controllerVersionPattern = "CONTROLLER VERSION "; // NOI18N
        if (versionsString != null && versionsString.startsWith(controllerVersionPattern)) { //NOI18N
            // for now we don't check controller versions, only protocol versions are checked
            RemoteLogger.fine("rfs_controller at {0} has version {1}", execEnv, versionsString.substring(controllerVersionPattern.length()));
            versionsString = requestReader.readLine();
        }
        if (versionsString == null) {
            return false;
        }
        String versionsPattern = "VERSIONS "; // NOI18N
        if (!versionsString.startsWith(versionsPattern)) {
            if (err != null) {
                err.printf("Protocol error, expected %s, got %s%n", versionsPattern, versionsString); //NOI18N
            }
            return false;
        }
        String[] versionsArray = versionsString.substring(versionsPattern.length()).split(" "); // NOI18N
        for (String v : versionsArray) {
            if (v.length() != 1) {
                if (err != null) {
                    err.printf("Protocol error: incorrect version format: %s%n", versionsString); //NOI18N
                }
                return false;
            }
            if (v.charAt(0) == VERSION) {
                return true;
            }
        }
        return true;
    }

    /*package*/ static char testGetVersion() {
        return VERSION;
    }

    private static class FormatException extends Exception {
        public FormatException(String message) {
            super(message);
        }
        public FormatException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private long getTimeSkew() throws IOException, CancellationException, FormatException {
        final int cnt = 10;
        responseStream.printf("SKEW_COUNT=%d\n", cnt); //NOI18N
        responseStream.flush();
        long[] deltas = new long[cnt];
        for (int i = 0; i < cnt; i++) {
            long localTime1 = System.currentTimeMillis();
            responseStream.printf("SKEW %d\n", i); //NOI18N
            responseStream.flush();
            String line = requestReader.readLine();
            long localTime2 = System.currentTimeMillis();
            try {
                long remoteTime = Long.parseLong(line);
                deltas[i] = remoteTime - (localTime1 + localTime2)/ 2;
            } catch (NumberFormatException nfe) {
                throw new FormatException("Wrong skew format: " + line, nfe); //NOI18N
            }
        }
        responseStream.printf("SKEW_END\n"); //NOI18N
        responseStream.flush();

        long skew = 0;
        for (int i = 0; i < cnt; i++) {
            skew += deltas[i];
        }
        skew /= cnt;

        String line = requestReader.readLine();
        if (!line.startsWith("FS_SKEW ")) { //NOI18N
            throw new FormatException("Wrong file system skew response: " + line); //NOI18N
        }
        try {
            long fsSkew = Long.parseLong(line.substring(8));
            fsSkew /=  1000;
            if (Math.abs(fsSkew) > SKEW_THRESHOLD) {
                RemoteSyncNotifier.getInstance().notify(execEnv, fsSkew);
            }
            return skew + fsSkew;
        } catch (NumberFormatException nfe) {
            throw new FormatException("Wrong file system skew format: " + line, nfe); //NOI18N
        }
    }

    /**
     * Feeds remote controller with the list of files and their lengths
     * @return true in the case of success, otherwise false
     * NB: in the case false is returned, no shutdown will be called
     */
    boolean init() throws IOException, CancellationException {
        if (!checkVersion()) {
            return false;
        }
        logger.log(Level.FINE, "Initialization. Version=%c", VERSION);
        if (CHECK_ALIVE && !remoteController.isAlive()) { // fixup for remote tests unstable failure (caused by jsch issue)
            if (err != null) {
                err.printf("Process exited unexpectedly when initializing%n"); //NOI18N
            }
            return false;
        }
        responseStream.printf("VERSION=%c\n", VERSION); //NOI18N
        responseStream.flush();

        long clockSkew;
        try {
            clockSkew = getTimeSkew();
            if (logger .isLoggable(Level.FINE)) {
                logger .log(Level.FINE, "HostInfo skew=%d calculated skew=%d", //NOI18N
                        new Object[]{HostInfoUtils.getHostInfo(execEnv).getClockSkew(), clockSkew});
            }
        } catch (FormatException ex) {
            if (err != null) {
                err.printf("protocol error: %s%n", ex.getMessage()); // NOI18N
            }
            return false;
        }

        long timeTotal = System.currentTimeMillis();
        fileCollector.gatherFiles();
        List<FileCollector.FileCollectorInfo> filesToFeed = fileCollector.getFiles();

        long time = System.currentTimeMillis();
        for (FileCollector.FileCollectorInfo info : filesToFeed) {
            try {
                sendFileInitRequest(info, clockSkew);
            } catch (IOException ex) {
                if (err != null) {
                    err.printf("Process exited unexpectedly while file info was being sent%n"); //NOI18N
                }
                return false;
            }
        }
        if (CHECK_ALIVE && !remoteController.isAlive()) { // fixup for remote tests unstable failure (caused by jsch issue)
            if (err != null) {
                err.printf("Process exited unexpectedly%n"); //NOI18N
            }
            return false;
        }
        responseStream.printf("\n"); // NOI18N
        responseStream.flush();
        logger.log(Level.FINE, "sending file list took %d ms", System.currentTimeMillis() - time);

        try {
            time = System.currentTimeMillis();
            readFileInitResponse();
            logger.log(Level.FINE, "reading initial response took %d ms", System.currentTimeMillis() - time);
        } catch (IOException ex) {
            if (err != null) {
                err.printf("%s%n", ex.getMessage());
                return false;
            }
        }
        fileData.store();
        if (!fileCollector.initNewFilesDiscovery()) {
            return false;
        }
        logger.log(Level.FINE, "the entire initialization took %d ms", System.currentTimeMillis() - timeTotal);
        return true;
    }

    private void readFileInitResponse() throws IOException {
        String request;
        while ((request = requestReader.readLine()) != null) {
            if (request.length() == 0) {
                break;
            }
            if (request.length() < 3) {
                throw new IllegalArgumentException("Protocol error: " + request); // NOI18N
            }
            // temporaraily we support both old and new protocols here
            if (request.startsWith("*")) { // "*" denotes new protocol
                char charState = request.charAt(1);
                FileState state = FileState.fromId(charState);
                if (state == null) {
                    throw new IllegalArgumentException("Protocol error: unexpected state: '" + charState + "'"); // NOI18N
                }
                String remotePath = request.substring(2);
                String remoteCanonicalPath = requestReader.readLine();
                if (remoteCanonicalPath == null) {
                    throw new IllegalArgumentException("Protocol error: no canoical path for " + remotePath); //NOI18N
                }
                String localFilePath = mapper.getLocalPath(remotePath);
                if (localFilePath != null) {
                    //RemoteUtil.LOGGER.log(Level.FINEST, "canonicalToAbsolute: {0} -> {0}", new Object[] {remoteCanonicalPath, remotePath});
                    fileCollector.putCanonicalToAbsolute(remoteCanonicalPath, remotePath);
                    File localFile = CndFileUtils.createLocalFile(localFilePath);
                    fileData.setState(localFile, state);
                } else {
                    logger.log(Level.FINEST, "ERROR no local file for %s", remotePath);
                }
            } else {
                // OLD protocol (temporarily)
                //update info about file where we thought file is copied, but it doesn't
                // exist remotely (i.e. project directory was removed)
                if (request.length() < 3 || !request.startsWith("t ")) { // NOI18N
                    throw new IllegalArgumentException("Protocol error: " + request); // NOI18N
                }
                String remoteFile = request.substring(2);
                String localFilePath = mapper.getLocalPath(remoteFile);
                if (localFilePath != null) {
                    File localFile = CndFileUtils.createLocalFile(localFilePath);
                    fileData.setState(localFile, FileState.TOUCHED);
                } else {
                    logger.log(Level.FINEST, "ERROR no local file for %s", remoteFile);
                }
            }
        }
    }

    private void sendFileInitRequest(FileCollector.FileCollectorInfo fgi, long timeSkew) throws IOException {
        if (CHECK_ALIVE && !remoteController.isAlive()) { // fixup for remote tests unstable failure (caused by jsch issue)
            throw new IOException("process already exited"); //NOI18N
        }
        if(fgi.isLink()) {
            responseStream.printf("L %s\n%s\n", fgi.remotePath, fgi.getLinkTarget()); //NOI18N
        } else if (fgi.file.isDirectory()) {
            responseStream.printf("D %s\n", fgi.remotePath); //NOI18N
            responseStream.flush(); //TODO: remove?
        } else {
            File file = fgi.file;
            String remotePath = fgi.remotePath;
            FileData.FileStateInfo info = fileData.getFileInfo(file);
            FileState newState;
            if (file.exists()) {
                switch(info  == null ? FileState.INITIAL : info.state) {
                    case COPIED:
                    case TOUCHED:
                        if (info.timestamp == file.lastModified()) {
                            newState = info.state;
                        } else {
                            newState = FileState.INITIAL;
                        }
                        break;
                    case ERROR: // fall through
                    case INITIAL:
                        newState = FileState.INITIAL;
                        break;
                    case UNCONTROLLED:
                    case INEXISTENT:
                        newState = info.state;
                        break;
                    default:
                        CndUtils.assertTrue(false, "Unexpected state: " + info.state); //NOI18N
                        return;
                }
            } else {
                if (info != null && info.state == FileState.UNCONTROLLED) {
                    newState = FileState.UNCONTROLLED;
                } else {
                    newState = FileState.INEXISTENT;
                }
            }
            CndUtils.assertTrue(newState == FileState.INITIAL || newState == FileState.COPIED
                    || newState == FileState.TOUCHED || newState == FileState.UNCONTROLLED
                    || newState == FileState.INEXISTENT,
                    "State shouldn't be ", newState); //NOI18N
            if (USE_TIMESTAMPS) {
                long fileTime = file.exists() ? Math.max(0, file.lastModified() + timeSkew) : 0;
                long seconds = fileTime / 1000;
                long microseconds = (fileTime % 1000) * 1000;
                responseStream.printf("%c %d %d %d %s\n", newState.id, file.length(), seconds, microseconds, remotePath); // NOI18N
            } else {
                responseStream.printf("%c %d %s\n", newState.id, file.length(), remotePath); // NOI18N
            }
            responseStream.flush(); //TODO: remove?
            if (newState == FileState.INITIAL ) {
                newState = FileState.TOUCHED;
            }
            fileData.setState(file, newState);
        }
    }


}

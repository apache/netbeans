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

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import org.netbeans.api.progress.ProgressHandle;;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteLogger;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import static org.netbeans.modules.cnd.remote.sync.FileState.COPIED;
import static org.netbeans.modules.cnd.remote.sync.FileState.ERROR;
import static org.netbeans.modules.cnd.remote.sync.FileState.INITIAL;
import static org.netbeans.modules.cnd.remote.sync.FileState.TOUCHED;
import static org.netbeans.modules.cnd.remote.sync.FileState.UNCONTROLLED;
import org.netbeans.modules.cnd.spi.remote.setup.support.RemoteSyncNotifier;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
/*package-local*/ final class FtpSyncWorker extends BaseSyncWorker implements RemoteSyncWorker, Cancellable {

    private FileData fileData;
    private FileCollector fileCollector;
    private final RemoteUtil.PrefixedLogger logger;
    private final RemotePathMap mapper;
    private final SharabilityFilter filter;

    private int uploadCount;
    private long uploadSize;
    private volatile Thread thread;
    private volatile boolean cancelled;
    private ProgressHandle progressHandle;

    private final RequestProcessor RP = new RequestProcessor("FtpSyncWorker", 3); // NOI18N

    private static final boolean HARD_CODED_FILTER = Boolean.valueOf(System.getProperty("cnd.remote.hardcoded.filter", "true")); //NOI18N

    public FtpSyncWorker(ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err,
            FileObject privProjectStorageDir, List<FSPath> paths, List<FSPath> buildResults) {
        super(executionEnvironment, out, err, privProjectStorageDir, paths, buildResults);
        this.mapper = RemotePathMap.getPathMap(executionEnvironment);
        this.logger = new RemoteUtil.PrefixedLogger("FtpSyncWorker[" + executionEnvironment + "]"); //NOI18N
        this.filter = new SharabilityFilter();
    }

    /** for trace/debug purposes */
    private StringBuilder getLocalFilesString() {
        StringBuilder sb = new StringBuilder();
        for (File f : files) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(f.getAbsolutePath());
        }
        return sb;
    }

    private boolean isIgnored(FileCollector.FileCollectorInfo collectorInfo) {
        final File file = collectorInfo.file;
        if (HARD_CODED_FILTER) {
            // Filter out configurations.xml, timestamps, etc
            // Auto-copy would never request these; but FTP will copy, unless filtered out
            File parent = file.getParentFile();
            if (parent != null) {
                if (parent.getName().equals("nbproject")) { // NOI18N
                    // we never need configuratins.xml for build purposes; however it might be quite large
                    if (file.getName().equals("configurations.xml")) { // NOI18N
                        return true;
                    }
                } else if (parent.getName().equals("private")) { // NOI18N
                    File granpa = parent.getParentFile();
                    if (granpa.getName().equals("nbproject")) { // NOI18N
                        if (!file.getName().endsWith(".mk") && !file.getName().endsWith(".sh") && !file.getName().endsWith(".bash")) { // NOI18N
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean needsCopying(FileCollector.FileCollectorInfo collectorInfo) {
        final File file = collectorInfo.file;
        if (!file.exists()) {
            return false;
        }
        if (isIgnored(collectorInfo)) {
            return false;
        }
        FileData.FileStateInfo stateInfo = fileData.getFileInfo(file);
        FileState state = (stateInfo == null) ? FileState.INITIAL : stateInfo.state;
        switch (state) {
            case INITIAL:       return true;
            case TOUCHED:       return true;
            case COPIED:        return stateInfo.timestamp != file.lastModified();
            case ERROR:         return true;
            case INEXISTENT:    return false;
            case UNCONTROLLED:  return false;
            default:
                CndUtils.assertTrue(false, "Unexpected state: " + state); //NOI18N
                return false;
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void synchronizeImpl(String remoteRoot) throws InterruptedException, ExecutionException, IOException, ConnectionManager.CancellationException {

        fileData = FileData.get(privProjectStorageDir, executionEnvironment);
        fileCollector = new FileCollector(files, buildResults, logger, mapper, filter, fileData, executionEnvironment, err, true);

        uploadCount = 0;
        uploadSize = 0;

        RemoteLogger.fine("Uploading {0} to {1} ...\n", getLocalFilesString(), executionEnvironment); // NOI18N
        long time = System.currentTimeMillis();

        if (out != null) {
            out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_GatherFiles"));
        }
        fileCollector.gatherFiles();
        if (cancelled) {
            return;
        }

        progressHandle.switchToDeterminate(fileCollector.getFiles().size());

        long time2;

        time2 = System.currentTimeMillis();
        if (out != null) {
            out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_CheckDirs"));
        }
        progressHandle.progress(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Progress_CheckDirs"));
        createDirs();
        RemoteLogger.fine("Creating directories at {0} took {1} ms", executionEnvironment, (System.currentTimeMillis()-time2));
        if (cancelled) {
            return;
        }

        if (CndUtils.getBoolean("cnd.remote.sftp.check.existence", true)) {
            time2 = System.currentTimeMillis();
            if (out != null) {
                out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_CheckExistence"));
            }
            progressHandle.progress(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Progress_CheckExistence"));
            checkExistence();
            RemoteLogger.fine("Checking file existence at {0} took {1} ms", executionEnvironment, (System.currentTimeMillis()-time2));
            if (cancelled) {
                return;
            }
        }

        time2 = System.currentTimeMillis();
        if (out != null) {
            out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_CheckLinks"));
        }
        progressHandle.progress(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Progress_CheckLinks"));
        createLinks();
        RemoteLogger.fine("Creating links at {0} took {1} ms", executionEnvironment, (System.currentTimeMillis()-time2));
        if (cancelled) {
            return;
        }

        if (!fileCollector.initNewFilesDiscovery()) {
            throw new IOException(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Msg_Err_NewFilesDiscovery"));
        }
        time2 = System.currentTimeMillis();

        if (CndUtils.getBoolean("cnd.remote.zip", true)) {
            try {
                uploadPlainFilesInZip(remoteRoot);
                RemoteLogger.fine("Uploading and extracting zip to {0} took {1} ms", executionEnvironment, (System.currentTimeMillis()-time2));
            } catch (ZipIOException ex) {
                time2 = System.currentTimeMillis();
                err.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Msg_TryingToRecoverViaPlainFiles"));
                uploadPlainFiles();
                RemoteLogger.fine("Uploading plain files to {0} took {1} ms", executionEnvironment, (System.currentTimeMillis()-time2));
            }
        } else {
            uploadPlainFiles();
        }

        time2 = System.currentTimeMillis();
        if (out != null) {
            out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_CheckExecPerm"));
        }
        progressHandle.progress(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Progress_CheckExecPerm"));
        addExecPermissions();
        RemoteLogger.fine("Checkinrg exec permissions at {0} took {1} ms", executionEnvironment, (System.currentTimeMillis()-time2));
        if (cancelled) {
            return;
        }
        if (out != null) {
            out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_Done"));
            out.println();
        }

        if (RemoteLogger.getInstance().isLoggable(Level.FINE)) {
            time = System.currentTimeMillis() - time;
            long bps = uploadSize * 1000L / time;
            String speed = (bps < 1024*8) ? (bps + " b/s") : ((bps/1024) + " Kb/s"); // NOI18N

            String strUploadSize = (uploadSize < 1024 ? (uploadSize + " bytes") : ((uploadSize/1024) + " K")); // NOI18N
            RemoteLogger.fine("\nCopied to {0}:{1}: {2} in {3} files. Time: {4} ms. Avg. speed: {5}\n", // NOI18N
                    executionEnvironment, remoteRoot,
                    strUploadSize, uploadCount, time, speed); // NOI18N
        }
    }

    private interface Feeder {
        public void feed(BufferedWriter requestWriter) throws IOException;
    }

    private interface LineProcessor {
        void processLine(String line);
    }

    private void launchAndFeed(final Feeder feeder, final LineProcessor outProcessor, final LineProcessor errProcessor, boolean throwUponFailure, String command, String... args) throws IOException {

        if (cancelled) {
            return;
        }
        NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(executionEnvironment);
        pb.setExecutable(command);
        pb.setArguments(args);
        final NativeProcess process;
        process = pb.call();

        final AtomicReference<IOException> problem = new AtomicReference<>();

        RP.post(new Runnable() {
            @Override
            public void run() {
                BufferedWriter requestWriter = null;
                try {
                    requestWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8")); //NOI18N
                    feeder.feed(requestWriter);
                } catch (IOException ex) {
                    problem.set(ex);
                } finally {
                    if (requestWriter != null) {
                        try {
                            requestWriter.close();
                        } catch (IOException ex) {
                            problem.set(ex);
                        }
                    }
                }
            }
        });

        RP.post(new Runnable() {
            @Override
            public void run() {
                BufferedReader errorReader = null;
                try {
                    errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8")); //NOI18N
                    for (String errLine = errorReader.readLine(); errLine != null; errLine = errorReader.readLine()) {
                        if (errProcessor != null) {
                            errProcessor.processLine(errLine);
                        } else {
                            err.println(errLine); // local println is OK
                        }
                    }
                } catch (IOException ex) {
                    problem.set(ex);
                } finally {
                    if (errorReader != null) {
                        try {
                            errorReader.close();
                        } catch (IOException ex) {
                            problem.set(ex);
                        }
                    }
                }
            }
        });

        RP.post(new Runnable() {
            @Override
            public void run() {
                BufferedReader outputReader = null;
                try {
                    outputReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8")); //NOI18N
                    for (String errLine = outputReader.readLine(); errLine != null; errLine = outputReader.readLine()) {
                        if (outProcessor != null) {
                            outProcessor.processLine(errLine);
                        } else {
                            if (out != null) {
                                out.println(errLine); // local println is OK
                            }
                        }
                    }
                } catch (IOException ex) {
                    problem.set(ex);
                } finally {
                    if (outputReader != null) {
                        try {
                            outputReader.close();
                        } catch (IOException ex) {
                            problem.set(ex);
                        }
                    }
                }
            }
        });

        if (problem.get() != null) {
            throw problem.get();
        }
        try {
            int rc = process.waitFor();
            if (rc != 0 && throwUponFailure) {
                throw new IOException(NbBundle.getMessage(FtpSyncWorker.class, "FTP_NonzeroRC", command, rc));
            }
        } catch (InterruptedException ex) {
            throw new InterruptedIOException();
        }
    }

    private void createDirs() throws IOException {
        final List<String> dirsToCreate = new LinkedList<>();
        for (FileCollector.FileCollectorInfo fileInfo : fileCollector.getFiles()) {
            if (fileInfo.file.isDirectory() && ! fileInfo.isLink()) {
                String remoteDir = mapper.getRemotePath(fileInfo.file.getAbsolutePath(), true);
                CndUtils.assertNotNull(remoteDir, "null remote file for " + fileInfo.file.getAbsolutePath()); //NOI18N
                if (remoteDir != null) {
                    dirsToCreate.add(remoteDir);
                }
            }
        }
        if (cancelled) {
            return;
        }
        if (!dirsToCreate.isEmpty()) {
            Feeder feeder = new Feeder() {
                @Override
                public void feed(BufferedWriter requestWriter) throws IOException {
                    for (String dir : dirsToCreate) {
                        if (cancelled) {
                            throw new InterruptedIOException();
                        }
                        requestWriter.append('"').append(dir).append('"').append(' '); // NOI18N
                    }
                }
            };
            try {
                launchAndFeed(feeder, null, null, true, "xargs", "mkdir", "-p"); // NOI18N
            } catch (InterruptedIOException ex) {
                throw new IOException(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Msg_Err_Canceled"));
            } catch (IOException ex) {
                throw new IOException(NbBundle.getMessage(FtpSyncWorker.class,
                        "FTP_Msg_Err_CheckDirs", ex.getMessage() == null ? "" : ex.getMessage()), ex);
            }
            uploadCount += dirsToCreate.size();
            progressHandle.progress(uploadCount);
        }
    }

    private void addExecPermissions() throws IOException {
        final List<String> filesToAdd = new LinkedList<>();
        for (FileCollector.FileCollectorInfo fileInfo : fileCollector.getFiles()) {
            if (!fileInfo.file.isDirectory() && fileInfo.file.canExecute() && !isIgnored(fileInfo)) {
                String remotePath = mapper.getRemotePath(fileInfo.file.getAbsolutePath(), true);
                CndUtils.assertNotNull(remotePath, "null remote file for " + fileInfo.file.getAbsolutePath()); //NOI18N
                if (remotePath != null) {
                    filesToAdd.add(remotePath);
                }
            }
        }
        if (cancelled) {
            return;
        }
        if (!filesToAdd.isEmpty()) {
            Feeder feeder = new Feeder() {
                @Override
                public void feed(BufferedWriter requestWriter) throws IOException {
                    for (String dir : filesToAdd) {
                        if (cancelled) {
                            throw new InterruptedIOException();
                        }
                        requestWriter.append('"').append(dir).append('"').append(' '); // NOI18N
                    }
                }
            };
            try {
                // chomod +x could fail in some situations, which does not necessarily mean that we can't build
                launchAndFeed(feeder, null, null, false, "xargs", "chmod", "+x"); // NOI18N
            } catch (InterruptedIOException ex) {
                throw new IOException(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Msg_Err_Canceled"));
            } catch (IOException ex) {
                throw new IOException(NbBundle.getMessage(FtpSyncWorker.class,
                        "FTP_Msg_Err_CheckDirs", ex.getMessage() == null ? "" : ex.getMessage()), ex);
            }
        }
    }

    private void checkExistence() throws IOException {
        if (cancelled) {
            return;
        }

        boolean needToCheck = false;
        // fast check
        for (FileCollector.FileCollectorInfo collectorInfo : fileCollector.getFiles()) {
            if (!collectorInfo.isLink() && collectorInfo.file.isFile() && collectorInfo.file.exists()) {
                FileData.FileStateInfo stateInfo = fileData.getFileInfo(collectorInfo.file);
                if (stateInfo != null && stateInfo.state == FileState.COPIED) {
                    needToCheck = true;
                    break;
                }
            }
        }
        if (!needToCheck) {
            return;
        }
        Feeder feeder = new Feeder() {
            private boolean mapErrorReported = false;
            @Override
            public void feed(BufferedWriter requestWriter) throws IOException {
                for (FileCollector.FileCollectorInfo collectorInfo : fileCollector.getFiles()) {
                    if (!collectorInfo.isLink() && collectorInfo.file.isFile() && collectorInfo.file.exists()) {
                        FileData.FileStateInfo stateInfo = fileData.getFileInfo(collectorInfo.file);
                        if (stateInfo != null && stateInfo.state == FileState.COPIED) {
                            String localPath = collectorInfo.file.getAbsolutePath();
                            String remotePath = mapper.getRemotePath(localPath, false);
                            if (remotePath != null) {
                                String line = "if [ ! -f \"" + remotePath + "\" ]; then echo \"" + remotePath + "\"; fi\n"; // NOI18N
                                requestWriter.append(line);
                                //NB: no flush! flush here
                            } else {
                                if (remotePath != null) {

                                } else { // this never happens since mapper is fixed, but however:
                                    if (!mapErrorReported && CndUtils.isDebugMode()) {
                                        mapErrorReported = true;
                                        CndUtils.assertUnconditional("null remote file for " + collectorInfo.file.getAbsolutePath()); //NOI18N
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };

        LineProcessor outProcessor = new LineProcessor() {
            private boolean mapErrorReported = false;
            @Override
            public void processLine(String line) {
                String localPath = mapper.getLocalPath(line);
                if (localPath != null) {
                    fileData.setState(new File(localPath), FileState.INITIAL);
                } else {
                    if (!mapErrorReported && CndUtils.isDebugMode()) { // should never be the case
                        mapErrorReported = true;
                        CndUtils.assertUnconditional("null local file for " + line); //NOI18N
                    }
                }
                //fileData.setState(new File, ERROR);
            }
        };

        try {
            launchAndFeed(feeder, outProcessor, null, true, "sh", "-s"); // NOI18N
        } catch (InterruptedIOException ex) {
            throw new IOException(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Msg_Err_Canceled"));
        } catch (IOException ex) {
            // no enough reasons to stop the build
            err.println(NbBundle.getMessage(FtpSyncWorker.class,"FTP_Msg_Err_CheckExistence", ex.getLocalizedMessage()));
        }
    }

    private void createLinks() throws IOException {
        if (cancelled) {
            return;
        }
        Feeder feeder = new Feeder() {
            @Override
            public void feed(BufferedWriter requestWriter) throws IOException {
                for (FileCollector.FileCollectorInfo fileInfo : fileCollector.getFiles()) {
                    if (cancelled) {
                        throw new InterruptedIOException();
                    }
                    if (fileInfo.isLink()) {
                        progressHandle.progress(fileInfo.file.getAbsolutePath());
                        String localBaseDir = fileInfo.file.getParentFile().getAbsolutePath();
                        String remoteBaseDir = mapper.getRemotePath(localBaseDir, true);
                        CndUtils.assertNotNull(remoteBaseDir, "null remote dir for " + localBaseDir); //NOI18N
                        if (remoteBaseDir != null) {
                            requestWriter.append("cd ").append(remoteBaseDir).append('\n'); // NOI18N
                            requestWriter.append("rm -rf ").append(fileInfo.file.getName()).append('\n'); // NOI18N
                            requestWriter.append("ln -s ") // NOI18N
                                    .append(fileInfo.getLinkTarget()).append(' ')
                                    .append(fileInfo.file.getName()).append('\n');
                        }
                        progressHandle.progress(fileInfo.file.getName(), uploadCount++);
                    }
                }
            }
        };
        try {
            launchAndFeed(feeder, null, null, true, "sh", "-s"); // NOI18N
        } catch (InterruptedIOException ex) {
            throw new IOException(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Msg_Err_Canceled"));
        } catch (IOException ex) {
            throw new IOException(NbBundle.getMessage(FtpSyncWorker.class,
                    "FTP_Msg_Err_CheckLinks", ex.getMessage() == null ? "" : ex.getMessage()), ex);
        }
    }

    private void uploadPlainFiles() throws InterruptedException, ExecutionException, IOException {

        List<FileCollector.FileCollectorInfo> toCopy = new ArrayList<>();

        for (FileCollector.FileCollectorInfo fileInfo : fileCollector.getFiles()) {
            if (cancelled) {
                throw new InterruptedException();
            }
            if (!fileInfo.isLink() && !fileInfo.file.isDirectory()) {
                if (needsCopying(fileInfo)) {
                    toCopy.add(fileInfo);
                }
            }
        }

        if (toCopy.isEmpty()) {
            if (out != null) {
                out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_NoFilesToUpload"));
            }
            return;
        }
        if (out != null) {
            out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_UploadFilesPlain", toCopy.size()));
        }
        for (FileCollector.FileCollectorInfo fileInfo : toCopy) {
            if (cancelled) {
                return;
            }
            if (!fileInfo.isLink() && !fileInfo.file.isDirectory()) {
                if (needsCopying(fileInfo)) {
                    File srcFile = fileInfo.file;
                    progressHandle.progress(srcFile.getAbsolutePath());
                    String remotePath = mapper.getRemotePath(srcFile.getAbsolutePath(), false);
                    Future<UploadStatus> fileTask = CommonTasksSupport.uploadFile(srcFile.getAbsolutePath(),
                            executionEnvironment, remotePath, srcFile.canExecute() ? 0700 : 0600);
                    UploadStatus uploadStatus = fileTask.get();
                    if (uploadStatus.isOK()) {
                        fileData.setState(srcFile, FileState.COPIED);
                        uploadSize += srcFile.length();
                    } else {
                        if (err != null) {
                            err.println(uploadStatus.getError());
                        }
                        throw new IOException(
                                NbBundle.getMessage(FtpSyncWorker.class, "FTP_Msg_Err_UploadFile",
                                        srcFile, executionEnvironment, remotePath,
                                        uploadStatus.getExitCode()));
                    }
                }
                progressHandle.progress(uploadCount++);
            }
        }
    }

    private static final class ZipIOException extends IOException {
        private ZipIOException(String message) {
            super(message);
        }
    }

    private void uploadPlainFilesInZip(String remoteRoot) throws InterruptedException, ExecutionException, IOException {
        if (out != null) {
            out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_UploadFilesInZip"));
        }

        List<FileCollector.FileCollectorInfo> toCopy = new ArrayList<>();

        for (FileCollector.FileCollectorInfo fileInfo : fileCollector.getFiles()) {
            if (cancelled) {
                throw new InterruptedException();
            }
            if (!fileInfo.isLink() && !fileInfo.file.isDirectory()) {
                if (needsCopying(fileInfo)) {
                    toCopy.add(fileInfo);
                }
            }
        }

        if (toCopy.isEmpty()) {
            return;
        }

        if (out != null) {
            out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_Zipping", toCopy.size()));
        }
        progressHandle.progress(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Progress_Zipping"));
        File zipFile = null;
        String remoteFile = null;
        try  {
            String localFileName = files[0].getName();
            if (localFileName.length() < 3) {
                localFileName = localFileName + ((localFileName.length() == 1) ? "__" : "_"); //NOI18N
            }
            zipFile = File.createTempFile(localFileName, ".zip", getTemp()); // NOI18N
            Zipper zipper = new Zipper(zipFile);
            {
                RemoteLogger.fine("SFTP/ZIP: Zipping {0} to {1}...", getLocalFilesString(), zipFile);
                long zipTime = System.currentTimeMillis();
                int progress = 0;
                for (FileCollector.FileCollectorInfo fileInfo : toCopy) {
                    if (cancelled) {
                        throw new InterruptedException();
                    }
                    File srcFile = fileInfo.file;
                    String remoteDir = mapper.getRemotePath(srcFile.getParent(), false);
                    if (remoteDir == null) { // this never happens since mapper is fixed
                        throw new IOException(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Err_CantMap", srcFile.getAbsolutePath()));
                    }
                    String base;
                    if (remoteDir.startsWith(remoteRoot)) {
                        base = remoteDir.substring(remoteRoot.length() + 1);
                    } else {
                        // this is never the case! - but...
                        throw new IOException(remoteDir + " should start with " + remoteRoot); //NOI18N
                    }
                    zipper.add(srcFile, filter, base); // TODO: do we need filter? isn't it already filtered?
                    if (progress++ % 3 == 0) {
                        progressHandle.progress(srcFile.getName(), uploadCount++);
                    }
                }
                zipper.close();
                RemoteLogger.fine("SFTP/ZIP: Zipping {0} files to {1} took {2} ms\n", //NOI18N
                        toCopy.size(), zipFile, System.currentTimeMillis()-zipTime); //NOI18N
            }

            if (cancelled) {
                throw new InterruptedException();
            }

            if (out != null) {
                out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_UploadingZip", executionEnvironment));
            }
            progressHandle.progress(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Progress_UploadingZip"));
            remoteFile = remoteRoot + '/' + zipFile.getName(); //NOI18N
            {
                long uploadStart = System.currentTimeMillis();
                Future<UploadStatus> upload = CommonTasksSupport.uploadFile(zipFile.getAbsolutePath(), executionEnvironment, remoteFile, 0600);
                UploadStatus uploadStatus = upload.get();
                RemoteLogger.fine("SFTP/ZIP:  uploading {0}to {1}:{2} finished in {3} ms with rc={4}", //NOI18N
                        zipFile, executionEnvironment, remoteFile,
                        System.currentTimeMillis()-uploadStart, uploadStatus.getExitCode());
                if (!uploadStatus.isOK()) {
                    throw new IOException(uploadStatus.getError());
                }
            }

            if (cancelled) {
                throw new InterruptedException();
            }

            if (out != null) {
                out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_Unzipping", executionEnvironment));
            }
            progressHandle.progress(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Progress_Unzipping"),
                    (uploadCount += (toCopy.size()/3)));
            {
                long unzipTime = System.currentTimeMillis();
                NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(executionEnvironment);
                pb.getEnvironment().put("TZ", TimeZone.getDefault().getID()); //NOI18N
                pb.setExecutable("unzip"); // NOI18N
                pb.setArguments("-oqq", remoteFile); // NOI18N
                pb.setWorkingDirectory(remoteRoot);
                ProcessUtils.ExitStatus status = ProcessUtils.execute(pb);
                if (RemoteUtil.LOGGER.isLoggable(Level.FINEST) && !status.getOutputString().isEmpty()) {
                    for(String s : status.getOutputString().split("\n")) { // NOI18N
                        System.out.printf("\tunzip: %s%n", s); // NOI18N
                    }
                }
                if (!status.getErrorLines().isEmpty()) {
                    for(String s : status.getErrorLines()) { // NOI18N
                        err.printf("unzip: %s%n", s); //NOI18N
                    }
                }
                RemoteLogger.fine("SFTP/ZIP: Unzipping {0}:{1} finished in {2} ms; rc={3}", // NOI18N
                        executionEnvironment , remoteFile, System.currentTimeMillis()-unzipTime, status.exitCode);

                if (status.exitCode != 0) {
                    throw new ZipIOException(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Err_Unzip",
                            remoteFile, executionEnvironment, status.exitCode)); // NOI18N
                }
                for (FileCollector.FileCollectorInfo fileInfo : toCopy) {
                    fileData.setState(fileInfo.file, FileState.COPIED);
                }
            }
        } finally {
            if (zipFile != null && zipFile.exists()) {
                if (!zipFile.delete()) {
                    RemoteUtil.LOGGER.log(Level.INFO, "Can not delete temporary file {0}", zipFile.getAbsolutePath()); //NOI18N
                }
            }
            if (remoteFile != null) {
                CommonTasksSupport.rmFile(executionEnvironment, remoteFile, null);
            }
        }
        progressHandle.progress(uploadCount += (toCopy.size()/3));
    }

    @Override
    public boolean startup(Map<String, String> env2add) {

        if (SyncUtils.isDoubleRemote(executionEnvironment, fileSystem)) {
            RemoteSyncNotifier.getInstance().warnDoubleRemote(executionEnvironment, fileSystem);
            return false;
        }

        // Later we'll allow user to specify where to copy project files to
        String remoteRoot = RemotePathMap.getRemoteSyncRoot(executionEnvironment);
        if (remoteRoot == null) {
            if (err != null) {
                err.printf("%s%n", NbBundle.getMessage(getClass(), "MSG_Cant_find_sync_root", ServerList.get(executionEnvironment).toString()));
            }
            return false; // TODO: error processing
        }

        boolean success = false;
        thread = Thread.currentThread();
        cancelled = false;
        //String title = NbBundle.getMessage(getClass(), "PROGRESS_UPLOADING", ServerList.get(executionEnvironment).getDisplayName());
        String title = "Uploading to " + ServerList.get(executionEnvironment).getDisplayName(); //NOI18N FIXUP
        progressHandle = ProgressHandle.createHandle(title, this);
        progressHandle.start();
        try {
            if (out != null) {
                out.printf("%s%n", NbBundle.getMessage(getClass(), "MSG_Copying",
                        remoteRoot, ServerList.get(executionEnvironment).toString()));
            }
            synchronizeImpl(remoteRoot);
            success = ! cancelled;
            if (success) {
                fileData.store();
            }
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
                        remoteRoot, ServerList.get(executionEnvironment).toString(), ex.getLocalizedMessage()));
            }
        } catch (IOException ex) {
            RemoteUtil.LOGGER.log(Level.FINE, null, ex);
            if (err != null) {
                err.printf("%s%n", NbBundle.getMessage(getClass(), "MSG_Error_Copying",
                        remoteRoot, ServerList.get(executionEnvironment).toString(), ex.getLocalizedMessage()));
            }
        } catch (ConnectionManager.CancellationException ex) {
            cancelled = true;
        } finally {
            cancelled = false;
            thread = null;
            progressHandle.finish();
        }
        return success;
    }

    @Override
    public void shutdown() {
        try {
            fileCollector.runNewFilesDiscovery();
            fileCollector.shutDownNewFilesDiscovery();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } catch (InterruptedException | ConnectionManager.CancellationException ex) {
            // don't report InterruptedException or CancellationException
        }
    }

    @Override
    public boolean cancel() {
        cancelled = true;
        Thread t = thread;
        if (t != null) {
            t.interrupt();
        }
        return true;
    }

    private static File getTemp() {
        String tmpPath = System.getProperty("java.io.tmpdir");
        File tmpFile = CndFileUtils.createLocalFile(tmpPath);
        return tmpFile.exists() ? tmpFile : null;
    }
}

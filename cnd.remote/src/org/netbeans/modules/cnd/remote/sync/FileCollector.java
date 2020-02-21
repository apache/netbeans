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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.spi.remote.setup.support.HostUpdatesRegistry;
//import org.netbeans.modules.cnd.remote.sync.download.HostUpdates;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ShellScriptRunner;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 */
/*package*/ class FileCollector {

    private final List<File> files;
    private final List<File> buildResults;
    private final RemoteUtil.PrefixedLogger logger;
    private final RemotePathMap mapper;
    private final SharabilityFilter filter;

    private final FileData fileData;
    private final ExecutionEnvironment execEnv;
    private final PrintWriter err;

    /**
     * Collector's behaviour differs for Rfs (auto copy) and SFTP.
     * In the case of Rfs (auto copy), rfs machinery deals with files that are in "controlled files list",
     * so collector is needed only for files that are not there.
     * In the case of SFTP, it should deal with all files.
     */
    private final boolean allFiles;

    private final Set<File> remoteUpdates = new HashSet<>();

    /**
     * Maps remote canonical remote path remote controller operates with
     * to the absolute remote path local controller uses
     */
    private final Map<String, String> canonicalToAbsolute = new HashMap<>();
    private final List<FileCollectorInfo> filesToFeed = new ArrayList<>(512);
    private String timeStampFile;

    private static final RequestProcessor RP = new RequestProcessor("FileCollector", 1); // NOI18N

    public FileCollector(File[] files, List<File> buildResults, RemoteUtil.PrefixedLogger logger, RemotePathMap mapper, SharabilityFilter filter,
            FileData fileData, ExecutionEnvironment execEnv, PrintWriter err, boolean allFiles) {
        this.files = new ArrayList<>(files.length);
        this.files.addAll(Arrays.asList(files));
        this.buildResults = new ArrayList<>(buildResults);
        this.logger = logger;
        this.mapper = mapper;
        this.filter = filter;
        this.fileData = fileData;
        this.execEnv = execEnv;
        this.err = err;
        this.allFiles = allFiles;
    }

    public List<FileCollectorInfo> getFiles() {
        return filesToFeed;
    }

    private static class DupsPreventer<T> {

        Set<T> set = new HashSet<>();

        public boolean check(T t) {
            if (set.contains(t)) {
                return false;
            }
            set.add(t);
            return true;
        }
    }

    public void gatherFiles() {
        long time = System.currentTimeMillis();

        // the set of top-level dirs
        Set<File> topDirs = new HashSet<>();
        DupsPreventer<File> dupsPreventer = new DupsPreventer<>();

        for (File file : files) {
            file = CndFileUtils.normalizeFile(file);
            if (file.isDirectory()) {
                String toRemoteFilePathName = mapper.getRemotePath(file.getAbsolutePath());
                addFileGatheringInfo(filesToFeed, file, toRemoteFilePathName);
                File[] children = file.listFiles(filter);
                if (children != null) {
                    for (File child : children) {
                        gatherFiles(child, toRemoteFilePathName, filter, filesToFeed, dupsPreventer);
                    }
                }
                topDirs.add(file);
            } else {
                final File parentFile = file.getAbsoluteFile().getParentFile();
                String toRemoteFilePathName = mapper.getRemotePath(parentFile.getAbsolutePath());
                if (!topDirs.contains(parentFile)) {
                    // add parent folder for external file
                    topDirs.add(parentFile);
                    if (dupsPreventer.check(parentFile)) {
                        addFileGatheringInfo(filesToFeed, parentFile, toRemoteFilePathName);
                    }
                }
                gatherFiles(file, toRemoteFilePathName, filter, filesToFeed, dupsPreventer);
            }
        }

        Collection<File> parents = gatherParents(topDirs);
        for (File file : parents) {
            file = CndFileUtils.normalizeFile(file);
            if (dupsPreventer.check(file)) {
                String toRemoteFilePathName = mapper.getRemotePath(file.getAbsolutePath());
                addFileGatheringInfo(filesToFeed, file, toRemoteFilePathName);
            }
        }
        logger.log(Level.FINE, "gathered %d files in %d ms", filesToFeed.size(), System.currentTimeMillis() - time);
        time = System.currentTimeMillis();
        checkLinks();
        logger.log(Level.FINE, "checking links took %d ms", System.currentTimeMillis() - time);

        time = System.currentTimeMillis();
        Collections.sort(filesToFeed, new Comparator<FileCollectorInfo>() {
            @Override
            public int compare(FileCollectorInfo f1, FileCollectorInfo f2) {
                if (f1.file.isDirectory() || f2.file.isDirectory()) {
                    if (f1.file.isDirectory() && f2.file.isDirectory()) {
                        return f1.remotePath.compareTo(f2.remotePath);
                    } else {
                        return f1.file.isDirectory() ? -1 : +1;
                    }
                } else {
                    long delta = f1.file.lastModified() - f2.file.lastModified();
                    return (delta == 0) ? 0 : ((delta < 0) ? -1 : +1); // signum(delta)
                }
            }
        });
        logger.log(Level.FINE, "sorting file list took %d ms", System.currentTimeMillis() - time);
    }

    private static void gatherFiles(File file, String base, FileFilter filter, List<FileCollectorInfo> files, DupsPreventer<File> dupsPreventer) {
        if (dupsPreventer.check(file)) {
            // it is assumed that the file itself was already filtered
            String remotePath = isEmpty(base) ? file.getName() : base + '/' + file.getName();
            files.add(new FileCollectorInfo(file, remotePath));
            if (file.isDirectory()) {
                File[] children = file.listFiles(filter);
                if (children != null) {
                    for (File child : children) {
                        String newBase = isEmpty(base) ? file.getName() : (base + "/" + file.getName()); // NOI18N
                        gatherFiles(child, newBase, filter, files, dupsPreventer);
                    }
                }
            }
        }
    }

    private static FileCollectorInfo addFileGatheringInfo(List<FileCollectorInfo> filesToFeed, final File file, String remoteFilePathName) {
        FileCollectorInfo info = new FileCollectorInfo(file, remoteFilePathName);
        filesToFeed.add(info);
        return info;
    }


    private Collection<File> gatherParents(Collection<File> files) {
        Set<File> parents = new HashSet<>();
        for (File file : files) {
            gatherParents(file, parents);
        }
        return parents;
    }

    private void gatherParents(File file, Set<File> parents) {
        //file = CndFileUtils.normalizeFile(file);
        File parent = file.getAbsoluteFile().getParentFile();
        if (parent != null && parent.getParentFile() != null) { // don't add top-level parents
            parents.add(parent);
            gatherParents(parent, parents);
        }
    }

    private void checkLinks() {
        if (Utilities.isWindows()) {
            return; // this is for Unixes only
        }
        // the counter is just in case here;
        // the real cycling check is inside checkLinks(List,List) logic
        int cnt = 0;
        final int max = 16;
        Collection<FileCollectorInfo> filesToCheck = new ArrayList<>(filesToFeed);
        do {
            filesToCheck = checkLinks(filesToCheck, filesToFeed);
        } while (!filesToCheck.isEmpty() && cnt++ < max);
        logger.log(Level.FINE, "checkLinks done in %d passes", cnt);
        if (!filesToCheck.isEmpty()) {
            logger.log(Level.INFO, "checkLinks exited by count. Cyclic symlinks?");
        }
    }

    private Collection<FileCollectorInfo> checkLinks(final Collection<FileCollectorInfo> filesToCheck, final List<FileCollectorInfo> filesToAdd) {
        Set<FileCollectorInfo> addedInfos = new HashSet<>();
        NativeProcessBuilder pb = NativeProcessBuilder.newLocalProcessBuilder();
        pb.setExecutable("sh"); //NOI18N
        pb.setArguments("-c", "xargs ls -ld | grep '^l'"); //NOI18N
        final NativeProcess process;
        try {
            process = pb.call();
        } catch (IOException ex) {
            logger.log(Level.INFO, "Error when checking links: %s", ex.getMessage());
            return addedInfos;
        }

        RP.post(new Runnable() {
            @Override
            public void run() {
                BufferedWriter requestWriter = null;
                try {
                    requestWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8")); //NOI18N
                    for (FileCollectorInfo info : filesToCheck) {
                        String path = "\"" + info.file.getAbsolutePath() + "\""; // NOI18N
                        requestWriter.append(path);
                        requestWriter.newLine();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                } finally {
                    if (requestWriter != null) {
                        try {
                            requestWriter.close();
                        } catch (IOException ex) {
                            ex.printStackTrace(System.err);
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
                        logger.log(Level.INFO, errLine);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
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
        });

        Map<String, FileCollectorInfo> map = new HashMap<>(filesToCheck.size());
        for (FileCollectorInfo info : filesToCheck) {
            map.put(info.file.getAbsolutePath(), info);
        }

        BufferedReader outputReader = null;
        try {
            outputReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8")); //NOI18N
            boolean errorReported = false;
            for (String line = outputReader.readLine(); line != null; line = outputReader.readLine()) {
                // line format is:
                // lrwxrwxrwx   1 root     root           5 Mar 24 13:33 /export/link-home -> home/
                String[] parts = line.split(" +"); // NOI18N
                if (parts.length <= 4) {
                    if (!errorReported) {
                        errorReported = true;
                        logger.log(Level.WARNING, "Unexpected ls output: %s", line);
                    }
                }
                String localLinkTarget = parts[parts.length - 1];
                if (localLinkTarget.endsWith("/")) { // NOI18N
                    localLinkTarget = localLinkTarget.substring(0, localLinkTarget.length() - 1);
                }
                String linkPath = parts[parts.length - 3];
                FileCollectorInfo info = map.get(linkPath);
                CndUtils.assertNotNull(info, "Null FileGatheringInfo for " + linkPath); //NOI18N
                if (info != null) {
                    logger.log(Level.FINEST, "\tcheckLinks: %s -> %s", linkPath, localLinkTarget);
                    //info.setLinkTarget(localLinkTarget);
                    File linkParentFile = CndFileUtils.createLocalFile(linkPath).getParentFile();
                    //File localLinkTargetFile = CndFileUtils.createLocalFile(linkParentFile, localLinkTarget);
                    File localLinkTargetFile;
                    if (CndPathUtilities.isPathAbsolute(localLinkTarget)) {
                        String remoteLinkTarget = mapper.getRemotePath(localLinkTarget, false);
                        info.setLinkTarget(remoteLinkTarget);
                        localLinkTargetFile = CndFileUtils.createLocalFile(localLinkTarget);
                    } else {
                        info.setLinkTarget(localLinkTarget); // it's relative, so it's the same for remote
                        localLinkTargetFile = CndFileUtils.createLocalFile(linkParentFile, localLinkTarget);
                    }
                    localLinkTargetFile = CndFileUtils.normalizeFile(localLinkTargetFile);
                    FileCollectorInfo targetInfo;
                    targetInfo = map.get(localLinkTargetFile.getAbsolutePath());
                    // TODO: try finding in newly added infos. Probably replace List to Map in filesToAdd
                    if (targetInfo == null) {
                        String remotePath = mapper.getRemotePath(localLinkTargetFile.getAbsolutePath(), false);
                        targetInfo = addFileGatheringInfo(filesToAdd, localLinkTargetFile, remotePath);
                        addedInfos.add(targetInfo);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }

        try {
            process.waitFor();
        } catch (InterruptedException ex) {
            // don't report InterruptedException
        }
        if (outputReader != null) {
            try {
                outputReader.close();
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
        return addedInfos;
    }

    private boolean isBsdBased() {
        HostInfo.OSFamily os;
        try {
            os = HostInfoUtils.getHostInfo(execEnv).getOSFamily();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            return false;
        } catch (ConnectionManager.CancellationException ex) {
            return false;
        }
        switch (os) {
            case MACOSX:
            case FREEBSD:
                return true;
            case SUNOS:
            case LINUX:
            case WINDOWS:
            case UNKNOWN:
                return false;
            default:
                throw new AssertionError(os.name());
        }
    }

    public boolean initNewFilesDiscovery() {
        String remoteSyncRoot = RemotePathMap.getRemoteSyncRoot(execEnv);
        ProcessUtils.ExitStatus res;
        if (isBsdBased()) {
            res = ProcessUtils.execute(execEnv, "mktemp", remoteSyncRoot + "/XXXXXXXX"); // NOI18N
        } else {
            res = ProcessUtils.execute(execEnv, "mktemp", "-p", remoteSyncRoot); // NOI18N
        }
        if (res.isOK()) {
            timeStampFile = res.getOutputString().trim();
            // On Linux, file precision is 1 second :(
            // Solaris is more precise, but, "find -newer" does not print files if time difference is less than one second!
            // So we have to sacrifice one second, otherwise new file discovery results are unstable.
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
           return true;
        } else {
            timeStampFile = null;
            String errMsg = NbBundle.getMessage(getClass(), "MSG_Error_Running_Command", "mktemp -p " + remoteSyncRoot, execEnv, res.getErrorString(), res.exitCode);
            logger.log(Level.INFO, errMsg);
            if (err != null) {
                err.printf("%s%n", errMsg); // NOI18N
            }
            return false;
        }
    }

    public String getCanonicalToAbsolute(String remoteFile) {
        return canonicalToAbsolute.get(remoteFile);
    }

    public void putCanonicalToAbsolute(String remoteCanonicalPath, String remotePath) {
        canonicalToAbsolute.put(remoteCanonicalPath, remotePath);
    }

    void addUpdate(File localFile) {
        remoteUpdates.add(localFile);
    }

    public void shutDownNewFilesDiscovery() {
        try {
            if (!remoteUpdates.isEmpty()) {
                HostUpdatesRegistry.register(remoteUpdates, execEnv, fileData.getDataFile().getParent());
                logger.log(Level.FINE, "registered  %d updated files", remoteUpdates.size());
            }
            if (timeStampFile != null && !Boolean.getBoolean("cnd.remote.keep.time.stamp")) { //NOI18N
                CommonTasksSupport.rmFile(execEnv, timeStampFile, err).get();
            }
        } catch (InterruptedIOException | InterruptedException ex) {
            // nothing
        } catch (IOException ex) {
            logger.log(Level.INFO, ex, "Error discovering newer files at remote host"); //NOI18N
        } catch (ExecutionException ex) {
            logger.log(Level.INFO, ex, "Error discovering newer files at remote host"); //NOI18N
        } catch (Throwable thr) {
            thr.printStackTrace(System.err); // this try-catch is only for instable test failures investigation
        }
        logger.log(Level.FINE, "registering %d updated files", remoteUpdates.size());
    }

    @SuppressWarnings("deprecation")
    public void runNewFilesDiscovery() throws IOException, InterruptedException, ConnectionManager.CancellationException {
        if (timeStampFile == null) {
            return;
        }
        long time = System.currentTimeMillis();
        int oldSize = remoteUpdates.size();

        StringBuilder remoteDirs = new StringBuilder();

        List<File> filesAndBuildResults = new ArrayList<>(files.size()+ buildResults.size());
        filesAndBuildResults.addAll(files);
        filesAndBuildResults.addAll(buildResults);

        for (File file : filesAndBuildResults) {
            if (file.isDirectory() || buildResults.contains(file)) {
                String rPath = mapper.getRemotePath(file.getAbsolutePath(), false);
                if (rPath == null) {
                    logger.log(Level.INFO, "Can't get remote path for %s at %s", file.getAbsolutePath(), execEnv);
                } else {
                    if (remoteDirs.length() > 0) {
                        remoteDirs.append(' ');
                    }
                    remoteDirs.append('"');
                    remoteDirs.append(rPath);
                    remoteDirs.append('"');
                }
            }
        }

        StringBuilder extOptions = new StringBuilder(" \\( "); // NOI18N
        Collection<Collection<String>> values = new ArrayList<>();
        values.add(MIMEExtensions.get(MIMENames.C_MIME_TYPE).getValues());
        values.add(MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE).getValues());
        values.add(MIMEExtensions.get(MIMENames.HEADER_MIME_TYPE).getValues());
        boolean first = true;
        for (Collection<String> v : values) {
            for (String ext : v) {
                if (first) {
                    first = false;
                } else {
                    extOptions.append(" -o "); // NOI18N
                }
                extOptions.append("-name \"*."); // NOI18N
                extOptions.append(ext);
                extOptions.append("\""); // NOI18N
            }
        }
        if (extOptions.length() > 0) {
            extOptions.append(" -o "); // NOI18N
        }
        extOptions.append(" -name Makefile"); // NOI18N
        for (File file : buildResults) {
            extOptions.append(" -o -name ").append(file.getName()); // NOI18N
        }
        extOptions.append(" \\) "); // NOI18N

        StringBuilder script = new StringBuilder("os=`uname`\n"); // NOI18N
        script.append("if [ ${os} = Darwin -o ${os} = FreeBSD ]; then\n"); // NOI18N
        script.append("    lst=`mktemp -t nblist`\n"); // NOI18N
        script.append("else\n"); // NOI18N
        script.append("    lst=`mktemp`\n"); // NOI18N
        script.append("fi\n"); // NOI18N
        script.append("find ").append(remoteDirs).append(extOptions).append(" -newer ").append(timeStampFile).append(" > ${lst}\n"); // NOI18N
        script.append("while read F; do\n"); // NOI18N
        script.append("  test -f \"$F\" &&  echo \"$F\"\n"); // NOI18N
        script.append("done < ${lst}\n"); // NOI18N
        script.append("rm ${lst}\n"); // NOI18N

        final AtomicInteger lineCnt = new AtomicInteger();

        org.netbeans.api.extexecution.input.LineProcessor lp = new org.netbeans.api.extexecution.input.LineProcessor() {
            @Override
            public void processLine(String remoteFile) {
                lineCnt.incrementAndGet();
                logger.log(Level.FINEST, "Updates check: %s", remoteFile);
                String realPath = canonicalToAbsolute.get(remoteFile);
                if (realPath != null) {
                    remoteFile = realPath;
                }
                String localPath = mapper.getLocalPath(remoteFile);
                if (localPath == null) {
                    logger.log(Level.FINE, "Can't find local path for %s", remoteFile);
                } else {
                    File localFile = CndFileUtils.createLocalFile(localPath);
                    boolean add = false;
                    if (buildResults.contains(localFile)) {
                        add = true;
                    } else if (allFiles || fileData == null || fileData.getFileInfo(localFile) == null) {
                        if (filter.accept(localFile)) {
                            add = true;
                        }
                    }
                    if (add) {
                        logger.log(Level.FINEST, "Updated %s", remoteFile);
                        remoteUpdates.add(localFile);
                        RfsListenerSupportImpl.getInstanmce(execEnv).fireFileChanged(localFile, remoteFile);
                    }
                }
            }

            @Override
            public void reset() {}

            @Override
            public void close() {}
        };

        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Started new files discovery at %s: %s", execEnv, script);
        }
        ShellScriptRunner ssr = new ShellScriptRunner(execEnv, script.toString(), lp);
        ssr.setErrorProcessor(new ShellScriptRunner.LoggerLineProcessor(getClass().getSimpleName())); //NOI18N
        int rc = ssr.execute();
        if (rc != 0 ) {
            logger.log(Level.FINE, "Error %d running script \"%s\" at %s", rc, script, execEnv);
        }
        logger.log(Level.FINE, "New files discovery at %s took %d ms; %d lines processed; %d additional new files were discovered",
                execEnv, System.currentTimeMillis() - time, lineCnt.get(), remoteUpdates.size() - oldSize);
    }

    private static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static class FileCollectorInfo {

        public final File file;
        public final String remotePath;
        private String linkTarget;
        private FileCollectorInfo linkTargetInfo;

        public FileCollectorInfo(File file, String remotePath) {
            this.file = file;
            this.remotePath = remotePath;
            CndUtils.assertTrue(remotePath.startsWith("/"), "Non-absolute remote path: ", remotePath);
            this.linkTarget = null;
        }

        @Override
        public String toString() {
            return (isLink() ? "L " : file.isDirectory() ? "D " : "F ") + file.getPath() + " -> " + remotePath; // NOI18N
        }

        public boolean isLink() {
            return linkTarget != null;
        }

        public String getLinkTarget() {
            return linkTarget;
        }

        public void setLinkTarget(String link) {
            this.linkTarget = link;
        }

        public FileCollectorInfo getLinkTargetInfo() {
            return linkTargetInfo;
        }

        public void setLinkTargetInfo(FileCollectorInfo linkTargetInfo) {
            this.linkTargetInfo = linkTargetInfo;
        }
    }

}

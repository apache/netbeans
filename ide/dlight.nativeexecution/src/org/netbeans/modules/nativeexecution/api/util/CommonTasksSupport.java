/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo;
import org.netbeans.modules.nativeexecution.signals.SignalSupport;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.openide.util.Exceptions;

/**
 * An utility class that simplifies usage of Native Execution Support Module
 * for common tasks like files copying.
 */
public final class CommonTasksSupport {

    private CommonTasksSupport() {
    }

    /** TODO: move it to some common place within nativeexecution */
    private static boolean getBoolean(String name, boolean result) {
        String text = System.getProperty(name);
        if (text != null) {
            result = Boolean.parseBoolean(text);
        }
        return result;
    }

    /**
     * Starts <tt>srcFileName</tt> file download from the host,
     * specified by the <tt>srcExecEnv</tt> saving it in the
     * <tt>dstFileName</tt> file. <p>
     * In the case of some error, message with a reason is written to the supplied
     * <tt>error</tt> (is not <tt>NULL</tt>).
     *
     * @param srcFileName full path to the source file with file name
     * @param srcExecEnv execution environment that describes the host to copy file from
     * @param dstFileName destination filename on the local host
     * @param error if not <tt>NULL</tt> and some error occurs during download,
     *        an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;Integer&gt;</tt> representing pending completion
     *         of the download task. The result of this Future is the exit
     *         code of the copying routine. 0 indicates that the file was
     *         successfully downlodaded. Result other than 0 indicates an error.
     */
    public static Future<Integer> downloadFile(
            final String srcFileName,
            final ExecutionEnvironment srcExecEnv,
            final String dstFileName,
            final Writer error) {

        return SftpSupport.getInstance(srcExecEnv).downloadFile(srcFileName, dstFileName, error);
    }

    /**
     * Starts <tt>srcFileName</tt> file download from the host,
     * specified by the <tt>srcExecEnv</tt> saving it in the
     * <tt>dstFileName</tt> file. <p>
     * In the case of some error, message with a reason is written to the supplied
     * <tt>error</tt> (is not <tt>NULL</tt>).
     *
     * @param srcFileName full path to the source file with file name
     * @param srcExecEnv execution environment that describes the host to copy file from
     * @param dstFile destination file on the local host
     * @param error if not <tt>NULL</tt> and some error occurs during download,
     *        an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;Integer&gt;</tt> representing pending completion
     *         of the download task. The result of this Future is the exit
     *         code of the copying routine. 0 indicates that the file was
     *         successfully downlodaded. Result other than 0 indicates an error.
     */
    public static Future<Integer> downloadFile(
            final String srcFileName,
            final ExecutionEnvironment srcExecEnv,
            final File dstFile,
            final Writer error) {

        return SftpSupport.getInstance(srcExecEnv).downloadFile(srcFileName, dstFile.getAbsolutePath(), error);
    }

    /**
     * Read remote file content
     *
     * @param srcFileName full path to the source file with file name
     * @param srcExecEnv execution environment that describes the host to copy file from
     * @param offset in source file
     * @param count number of reading bytes
     * @param error if not <tt>NULL</tt> and some error occurs during reading,
     *        an error message will be written to this <tt>Writer</tt>.
     * @return byte array with file content. Returns byte[0] in case error. Result can be less of count in case end of file.
     */
    public static byte[] readFile(
            final String srcFileName,
            final ExecutionEnvironment srcExecEnv,
            final long offset, final int count,
            final Writer error) {

        int bs = 512;
        long iseek = offset / bs;
        long endOffset = offset + count;
        long cnt = (endOffset / bs);
        if (endOffset%bs > 0) {
            cnt++;
        }
        cnt -= iseek;
        byte[] buffer = new byte[(int)cnt * bs];

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(srcExecEnv);
        if (iseek > 0) {
            // iseek paremeter is Solaris and Mac OS X only.
            //npb.setExecutable("/bin/dd").setArguments("if=" + srcFileName, "ibs=" + bs, "iseek=" + iseek, "count=" + cnt); // NOI18N
            npb.setExecutable("/bin/dd").setArguments("if=" + srcFileName, "ibs=" + bs, "skip=" + iseek, "count=" + cnt); // NOI18N
        } else {
            npb.setExecutable("/bin/dd").setArguments("if=" + srcFileName, "ibs=" + bs, "count=" + cnt); // NOI18N
        }

        int actual;

        try {
            NativeProcess process = npb.call();
            if (process.getState() == State.ERROR) {
                String err = ProcessUtils.readProcessErrorLine(process);
                if (iseek > 0) {
                    throw new IOException("Cannot start /bin/dd if=" + srcFileName + " ibs=" + bs + " skip=" + iseek + " count=" + cnt + ": " + err); // NOI18N
                } else {
                    throw new IOException("Cannot start /bin/dd if=" + srcFileName + " ibs=" + bs + " count=" + cnt + ": " + err); // NOI18N
                }
            }
            
            Future<List<String>> err = ProcessUtils.readProcessErrorAsync(process);

            int start = 0;
            int rest = buffer.length;
            actual = 0;
            while(true) {
                int readed = process.getInputStream().read(buffer, start, rest);
                if (readed <= 0) {
                    break;
                }
                start += readed;
                rest -= readed;
                actual += readed;
                if (rest <= 0) {
                    break;
                }
            }
            int rc = 0;

            try {
                rc = process.waitFor();
            } catch (InterruptedException ex) {
                throw new IOException("/bin/dd was interrupted"); // NOI18N
            }

            if (rc != 0) {
                StringBuilder sb = new StringBuilder("Error while reading ").append(srcFileName).append(": "); // NOI18N
                try {
                    for (String e : err.get()) {
                        sb.append(e).append(' '); //it's very unlikely that the message is multy line
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace(System.err);
                }
                throw new IOException(sb.toString());
            }
        } catch (IOException ex) {
            if (error != null) {
                try {
                    error.write(ex.getMessage());
                    error.flush();
                } catch (IOException ex1) {
                    Exceptions.printStackTrace(ex1);
                }
            }
            return new byte[0];
        }
        int extra = (int) (offset % bs);
        if (actual - extra < 0) {
            return new byte[0];
        }
        int res_size = Math.min(actual - extra, count);
        byte[] result = new byte[res_size];
        System.arraycopy(buffer, extra, result, 0, res_size);
        return result;
    }

    public static Future<UploadStatus> uploadFile(UploadParameters parameters) {
        return SftpSupport.getInstance(parameters.dstExecEnv).uploadFile(parameters);
    }

    /**
     * Starts <tt>srcFileName</tt> file upload from the localhost to the host,
     * specified by the <tt>dstExecEnv</tt> saving it in the
     * <tt>dstFileName</tt> file with the given file mode creation mask. <p>
     * In the case of some error, message with a reason is written to the supplied
     * <tt>error</tt> (is not <tt>NULL</tt>).
     *
     * @param srcFileName full path to the source file with file name
     * @param dstExecEnv execution environment that describes destination host
     * @param dstFileName destination filename on the host, specified by
     *        <tt>dstExecEnv</tt>
     * @param mask file mode creation mask (see uname(1), chmod(1)) (in octal);
     * if it is less than zero, the default mask is used (for existent files, it stays the same it was, for new files as specified by umask command))
     * @param error if not <tt>NULL</tt> and some error occurs during upload,
     *        an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;UploadStatus&gt;</tt> representing pending completion
     *         of the upload task.
     */
    public static Future<UploadStatus> uploadFile(
            final String srcFileName,
            final ExecutionEnvironment dstExecEnv,
            final String dstFileName,
            final int mask) {
        return SftpSupport.getInstance(dstExecEnv).uploadFile(new UploadParameters(
                new File(srcFileName), dstExecEnv, dstFileName, null, mask, false, null));
    }

    /**
     * Starts <tt>srcFileName</tt> file upload from the localhost to the host,
     * specified by the <tt>dstExecEnv</tt> saving it in the
     * <tt>dstFileName</tt> file with the given file mode creation mask. <p>
     * In the case of some error, message with a reason is written to the supplied
     * <tt>error</tt> (is not <tt>NULL</tt>).
     *
     * @param srcFileName full path to the source file with file name
     * @param dstExecEnv execution environment that describes destination host
     * @param dstFileName destination filename on the host, specified by
     *        <tt>dstExecEnv</tt>
     * @param mask file mode creation mask (see uname(1), chmod(1)) (in octal);
     * if it is less than zero, the default mask is used (for existent files, it stays the same it was, for new files as specified by umask command))
     * @param error if not <tt>NULL</tt> and some error occurs during upload,
     *        an error message will be written to this <tt>Writer</tt>.
     * @param checkMd5 if true, then the source file will be copied to destination only if
     *        destination does not exist or exists but its md5 sum differs from local one
     * @return a <tt>Future&lt;UploadStatus&gt;</tt> representing pending completion
     *         of the upload task. 
     */
    public static Future<UploadStatus> uploadFile(
            final String srcFileName,
            final ExecutionEnvironment dstExecEnv,
            final String dstFileName,
            final int mask, boolean checkMd5) {

        return SftpSupport.getInstance(dstExecEnv).uploadFile(new UploadParameters(
                new File(srcFileName), dstExecEnv, dstFileName, null, mask, checkMd5, null));
    }

    /**
     * Starts <tt>srcFileName</tt> file upload from the localhost to the host,
     * specified by the <tt>dstExecEnv</tt> saving it in the
     * <tt>dstFileName</tt> file with the given file mode creation mask. <p>
     * In the case of some error, message with a reason is written to the supplied
     * <tt>error</tt> (is not <tt>NULL</tt>).
     *
     * @param srcFile the source file that reside on the local host
     * @param dstExecEnv execution environment that describes destination host
     * @param dstFileName destination filename on the host, specified by
     *        <tt>dstExecEnv</tt>
     * @param mask file mode creation mask (see uname(1), chmod(1)) (in octal);
     * if it is less than zero, the default mask is used (for existent files, it stays the same it was, for new files as specified by umask command))
     * @param error if not <tt>NULL</tt> and some error occurs during upload,
     *        an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;UploadStatus&gt;</tt> representing pending completion
     *         of the upload task.
     */
    public static Future<UploadStatus> uploadFile(
            final File srcFile,
            final ExecutionEnvironment dstExecEnv,
            final String dstFileName,
            final int mask) {

        return SftpSupport.getInstance(dstExecEnv).uploadFile(new UploadParameters(srcFile, dstExecEnv, dstFileName, null, mask, false, null));
    }

    /**
     * Starts <tt>srcFileName</tt> file upload from the localhost to the host,
     * specified by the <tt>dstExecEnv</tt> saving it in the
     * <tt>dstFileName</tt> file with the given file mode creation mask. <p>
     * In the case of some error, message with a reason is written to the supplied
     * <tt>error</tt> (is not <tt>NULL</tt>).
     *
     * @param srcFile the source file that reside on the local host
     * @param dstExecEnv execution environment that describes destination host
     * @param dstFileName destination filename on the host, specified by
     *        <tt>dstExecEnv</tt>
     * @param mask file mode creation mask (see uname(1), chmod(1)) (in octal);
     * if it is less than zero, the default mask is used (for existent files, it stays the same it was, for new files as specified by umask command))
     * @param error if not <tt>NULL</tt> and some error occurs during upload,
     *        an error message will be written to this <tt>Writer</tt>.
     * @param checkMd5 if true, then the source file will be copied to destination only if
     *        destination does not exist or exists but its md5 sum differs from local one
     * @return a <tt>Future&lt;UploadStatus&gt;</tt> representing pending completion
     *         of the upload task.
     */
    public static Future<UploadStatus> uploadFile(
            final File srcFile,
            final ExecutionEnvironment dstExecEnv,
            final String dstFileName,
            final int mask, boolean checkMd5) {

        return SftpSupport.getInstance(dstExecEnv).uploadFile(new UploadParameters(
                srcFile, dstExecEnv, dstFileName, null, mask, checkMd5, null));
    }

    /**
     * Creates a task for removing a file <tt>fname</tt> from the host
     * identified by the <tt>execEnv</tt>.
     * @param execEnv execution environment to delete file from
     * @param fname the file name with the full path to it
     * @param error if not <tt>NULL</tt> and some error occurs during file
     *        removing, an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;Integer&gt;</tt> representing pending completion
     *         of the file removing task. The result of this Future indicates
     *         whether the file was removed (0) or not.
     */
    public static Future<Integer> rmFile(ExecutionEnvironment execEnv,
            String fname, final Writer error) {
        return NativeTaskExecutorService.submit(
                new CommandRunner(execEnv, error, "rm", "-f", fname), // NOI18N
                "rm -f " + fname); // NOI18N
    }

    private static class CommandRunner implements Callable<Integer> {

        private static final Logger log = org.netbeans.modules.nativeexecution.support.Logger.getInstance();
        private final ExecutionEnvironment execEnv;
        private final String cmd;
        private final String[] args;
        private final Writer error;

        public CommandRunner(ExecutionEnvironment execEnv, Writer error, String cmd, String... args) {
            this.execEnv = execEnv;
            this.cmd = cmd;
            this.args = args;
            this.error = error;
        }

        @Override
        public Integer call() throws Exception {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable(cmd).setArguments(args);
            ProcessUtils.ExitStatus res = ProcessUtils.execute(npb);
            if (res.isOK()) {
                if (error != null) {
                    for (String errLine : res.getErrorLines()) {
                        error.write(errLine);
                    }
                } else {
                    ProcessUtils.logError(Level.FINE, log, res);
                }
            }

            return res.exitCode;
        }
    }

    /**
     * Changes file permissions.
     *
     * @param execEnv  execution environment where the file is located
     * @param file  file to change permissions for
     * @param mode  new file permissions in octal form, e.g. <tt>0755</tt>
     * @param error if not <tt>null</tt> and some error occurs,
    an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;Integer&gt;</tt> representing exit code
     *         of the chmod task. <tt>0</tt> means success, any other value
     *         means failure.
     */
    public static Future<Integer> chmod(final ExecutionEnvironment execEnv,
            final String file, final int mode, final Writer error) {
        return NativeTaskExecutorService.submit(
                new CommandRunner(execEnv, error, "chmod", String.format("0%03o", mode), file), // NOI18N
                "chmod " + String.format("0%03o ", mode) + file); // NOI18N
    }

    /**
     * Creates a task for removing a directory <tt>dirname</tt> from the host
     * identified by the <tt>execEnv</tt>.
     * @param execEnv execution environment to delete the directory from
     * @param dirname the file name with the full path to it
     * @param recursively if set to <tt>true</tt> then directory is to be
     *        removed recursively.
     * @param error if not <tt>NULL</tt> and some error occurs during directory
     *        removing, an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;Integer&gt;</tt> representing pending completion
     *         of the directory removing task. The result of this Future indicates
     *         whether the directory was removed (0) or not.
     */
    public static Future<Integer> rmDir(final ExecutionEnvironment execEnv,
            String dirname, boolean recursively, final Writer error) {
        String cmd = recursively ? "rm" : "rmdir"; // NOI18N

        String[] args = recursively
                ? new String[]{"-rf", dirname} : new String[]{"-f", dirname}; // NOI18N

        return NativeTaskExecutorService.submit(
                new CommandRunner(execEnv, error, cmd, args),
                cmd + ' ' + Arrays.toString(args));
    }

    /**
     * Creates a directory (and parent directories if needed).
     *
     * @param execEnv  execution environment to create directory in
     * @param dirname  absolute path of created directory
     * @param error  if not <tt>null</tt> and some error occurs,
     *        an error message will be written to this <tt>Writer</tt>
     * @return a <tt>Future&lt;Integer&gt;</tt> representing exit code
     *         of the mkdir task. <tt>0</tt> means success, any other value
     *         means failure.
     */
    public static Future<Integer> mkDir(final ExecutionEnvironment execEnv,
            final String dirname, final Writer error) {
        return NativeTaskExecutorService.submit(
                new CommandRunner(execEnv, error, "mkdir", "-p", dirname), // NOI18N
                "mkdir -p " + dirname); // NOI18N
    }

    /**
     * Sends the signal to the process.
     *
     * @param execEnv  execution environment of the process
     * @param pid  pid of the process
     * @param signal  signal name, e.g. "SIGKILL", "SIGUSR1"
     * @param error  if not <tt>null</tt> and some error occurs,
     *        an error message will be written to this <tt>Writer</tt>
     * @return a <tt>Future&lt;Integer&gt;</tt> representing exit code
     *         of the signal task. <tt>0</tt> means success, any other value
     *         means failure.
     */
    public static Future<Integer> sendSignal(final ExecutionEnvironment execEnv, final int pid, final Signal signal, final Writer error) {
        final String descr = "Sending signal " + signal + " to the process " + pid; // NOI18N
        return NativeTaskExecutorService.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return SignalSupport.signalProcess(execEnv, pid, signal);
            }
        }, descr);
    }

    /**
     * Sends the signal to the process group.
     *
     * @param execEnv  execution environment of the process
     * @param pid  pid of the process
     * @param signal  signal name, e.g. "SIGKILL", "SIGUSR1"
     * @param error  if not <tt>null</tt> and some error occurs,
     *        an error message will be written to this <tt>Writer</tt>
     * @return a <tt>Future&lt;Integer&gt;</tt> representing exit code
     *         of the signal task. <tt>0</tt> means success, any other value
     *         means failure.
     */
    public static Future<Integer> sendSignalGrp(final ExecutionEnvironment execEnv, final int pid, final Signal signal, final Writer error) {
        final String descr = "Sending signal " + signal + " to the group " + pid; // NOI18N
        return NativeTaskExecutorService.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return SignalSupport.signalProcessGroup(execEnv, pid, signal);
            }
        }, descr);
    }

    public static Future<Integer> sendSignalSession(final ExecutionEnvironment execEnv, final int pid, final Signal signal, final Writer error) {
        final String descr = "Sending signal " + signal + " to the session " + pid; // NOI18N
        return NativeTaskExecutorService.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return SignalSupport.signalProcessGroup(execEnv, pid, signal);
            }
        }, descr);
    }

    public static class UploadStatus {
        private final int exitCode;
        private final String error;
        private final FileInfoProvider.StatInfo statInfo;

        /*packge*/ UploadStatus(int exitCode, String error, StatInfo statInfo) {
            this.exitCode = exitCode;
            this.error = error;
            this.statInfo = statInfo;
        }

        public boolean isOK() {
            return exitCode == 0;
        }
        
        public int getExitCode() {
            return exitCode;
        }
       
        public String getError() {
            return error;
        }

        public StatInfo getStatInfo() {
            return statInfo;
        }
    }
    
    /**
     * A class (an analog of C struct) that contains upload parameters.
     */
    public static final class UploadParameters  {
        
        /**
         * specifies full path to the source file on the local host
         */
        public final File srcFile;

        /** */
        public final ExecutionEnvironment dstExecEnv;

        /**
         * destination filename on the host, specified by <tt>dstExecEnv</tt>
         */
        public final String dstFileName;

        public final String dstFileToRename;

        /**
         * File mode creation mask (see uname(1), chmod(1)), in octal.
         * iIf it is less than zero (which is the default),
         * then the default mask is used (for existent files, it stays the same it was,
         * for new files as specified by umask command))
         */
        public final int mask;

        /** */
        public final ChangeListener callback;

        /**
         * Of true, then the source file will be copied to destination only if
         * destination does not exist or exists but its md5 sum differs from local one
         */
        public final boolean checkMd5;
        
        public final boolean returnStat;

        public UploadParameters(File srcFile, ExecutionEnvironment dstExecEnv, String dstFileName) {
            this(srcFile, dstExecEnv, dstFileName, null, -1, false, null);
        }

        public UploadParameters(File srcFile, ExecutionEnvironment dstExecEnv, String dstFileName, int mask) {
            this(srcFile, dstExecEnv, dstFileName, null, mask, false, null);
        }

        public UploadParameters(File srcFile, ExecutionEnvironment dstExecEnv, String dstFileName, String dstFileToRename, int mask, boolean checkMd5, ChangeListener callback) {
            this(srcFile, dstExecEnv, dstFileName, dstFileToRename, mask, checkMd5, callback, true);
        }

        public UploadParameters(File srcFile, ExecutionEnvironment dstExecEnv, String dstFileName, String dstFileToRename, int mask, boolean checkMd5, ChangeListener callback, boolean returnStat) {
            this.srcFile = srcFile;
            this.dstExecEnv = dstExecEnv;
            this.dstFileName = dstFileName;
            this.dstFileToRename = dstFileToRename;
            this.mask = mask;
            this.checkMd5 = checkMd5;
            this.callback = callback;
            this.returnStat = returnStat;
        }

        @Override
        public String toString() {
            return "UploadParameters " + "src=" + srcFile + " env=" + dstExecEnv + " dst=" + dstFileName + //NOI18N
                    "rename=" + dstFileToRename + " mask=" + mask + " md5=" + checkMd5; //NOI18N
        }
    }

    /**
     * Queue a signal to a process (sigqueue).
     *
     * Deprecated. Do not use int signo as it may denote different signals on
     * different platforms.
     *
     * @param execEnv execution environment of the process
     * @param pid pid of the process
     * @param signo signal number
     * @param value signal value
     * @param error if not <tt>null</tt> and some error occurs, an error message
     * will be written to this <tt>Writer</tt>
     * @return a <tt>Future&lt;Integer&gt;</tt> representing exit code of the
     * signal task. <tt>0</tt> means success, any other value means failure.
     */
    @Deprecated
    public static Future<Integer> sigqueue(final ExecutionEnvironment execEnv,
            final int pid, final int signo, final int value, final Writer error) {
        final String descr = "Sigqueue " + signo + " with value " + value + " to " + pid; // NOI18N
        return NativeTaskExecutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                for (Signal signal : Signal.values()) {
                    if (signal.getID() == signo) {
                        return SignalSupport.sigqueue(execEnv, pid, signal, value);
                    }
                }
                return -1;
            }
        }, descr);
    }

    public static Future<Integer> sigqueue(final ExecutionEnvironment execEnv,
            final int pid, final Signal signal, final int value, final Writer error) {
        final String descr = "Sigqueue " + signal + " with value " + value + " to " + pid; // NOI18N

        return NativeTaskExecutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return SignalSupport.sigqueue(execEnv, pid, signal, value);
            }
        }, descr);
    }
}

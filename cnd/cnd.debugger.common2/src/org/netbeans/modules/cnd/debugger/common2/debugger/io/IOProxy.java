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

package org.netbeans.modules.cnd.debugger.common2.debugger.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

/**
 * Creates two fifos for the process input and output and forward them to the specified io tab
 */
public abstract class IOProxy {
    private static final String FILENAME_PREFIX = "debuggerFifo"; // NOI18N
    private static final String FILENAME_EXTENSION = ".fifo"; // NOI18N

    private InputWriterThread irt = null;
    private final Reader ioReader;

    private OutputReaderThread ort = null;
    private final Writer ioWriter;
    
    public static IOProxy create(ExecutionEnvironment execEnv, InputOutput io) {
        IOProxy res;
        if (execEnv == null || execEnv.isLocal()) {
            res = new LocalIOProxy(io.getIn(), io.getOut());
        } else {
            res = new RemoteIOProxy(execEnv, io.getIn(), io.getOut());
        }
        res.start();
        return res;
    }

    private IOProxy(Reader ioReader, Writer ioWriter) {
        this.ioReader = ioReader;
        this.ioWriter = ioWriter;
    }

    private void start() {
        irt = new InputWriterThread();
        irt.start();
        ort = new OutputReaderThread();
        ort.start();
    }

    public void stop() {
        if (irt != null) {
            irt.cancel();
            try {
                ioReader.close();
            } catch (IOException ex) {
                // do nothing
            }
        }
        if (ort != null) {
            ort.cancel();
            try {
                ioWriter.close();
            } catch (IOException ex) {
                // do nothing
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            stop();
        } finally {
            super.finalize();
        }
    }

    abstract String[] getIOFiles();
    
    protected abstract OutputStream createInStream() throws IOException;
    protected abstract InputStream createOutStream() throws IOException;

    /** Helper class forwarding input from the io tab to the file */
    private class InputWriterThread extends Thread {
        private volatile boolean cancel = false;

        public InputWriterThread() {
            setName("TTY InputWriterThread"); // NOI18N - Note NetBeans doesn't xlate "IDE Main"
        }

        @Override
        public void run() {
            int ch;

            OutputStream pout = null;

            try {
                pout = createInStream();

                while ((ch = ioReader.read()) != -1) {
                    if (cancel) {
                        return;
                    }
                    pout.write((char) ch);
                    pout.flush();
                }
            } catch (IOException e) {
            } finally {
                // Handle EOF and other exits
                try {
                    pout.flush();
                    pout.close();
                } catch (IOException ex) {
                    //Exceptions.printStackTrace(ex);
                }
            }
        }
        
        public void cancel() {
            cancel = true;
        }
    }

    /** Helper class forwarding output from the file to the io tab */
    private class OutputReaderThread  extends Thread {
        private volatile boolean cancel = false;

        public OutputReaderThread() {
            setName("TTY OutputReaderThread"); // NOI18N - Note NetBeans doesn't xlate "IDE Main"
        }

        @Override
        public void run() {
            InputStream in = null;
            try {
                int read;
                in = createOutStream();

                while (true) {
                    read = in.read();
                    if (read == -1) {
                        if (cancel) { // 131739
                            return;
                        }
                    } else {
                        ioWriter.write((read == 10) ? '\n' : (char) read);
                    }
                    //output.flush(); // 135380
                }
            } catch (IOException e) {
            } finally {
                try {
                    ioWriter.flush();
                } catch (IOException ex) {
                    //Exceptions.printStackTrace(ex);
                }
                try {
                    in.close();
                } catch (IOException ex) {
                    //Exceptions.printStackTrace(ex);
                }
            }
        }

        public void cancel() {
            cancel = true;
        }
    }

    private static class LocalIOProxy extends IOProxy {
        private final File inFile;
        private final File outFile;

        public LocalIOProxy(Reader ioReader, Writer ioWriter) {
            super(ioReader, ioWriter);
            this.inFile = createNewFifo();
            this.inFile.deleteOnExit();
            this.outFile = createNewFifo();
            this.outFile.deleteOnExit();
        }

        private static File createNewFifo() {
            try {
                // TODO : implement more correct way of generating unique filename
                File file = File.createTempFile(FILENAME_PREFIX, FILENAME_EXTENSION); // NOI18N
                file.delete();
                String tool = "mkfifo"; // NOI18N
                if (Utilities.isWindows()) {
                    tool += ".exe"; // NOI18N
                    File toolFile = new File(CompilerSetUtils.getCygwinBase() + "/bin", tool); // NOI18N
                    if (toolFile.exists()) {
                        tool = toolFile.getAbsolutePath();
                    } else {
                        toolFile = new File(CompilerSetUtils.getCommandFolder(null), tool); // NOI18N
                        if (toolFile.exists()) {
                            tool = toolFile.getAbsolutePath();
                        }
                    }
                }
                ProcessBuilder pb = new ProcessBuilder(tool, file.getAbsolutePath()); // NOI18N
                // We need to wait for the end of this command, otherwise file may not be initialized
                ProcessUtils.execute(pb);
                return file;
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            return null;
        }

        @Override
        protected OutputStream createInStream() throws IOException {
            return new FileOutputStream(inFile);
        }

        @Override
        String[] getIOFiles() {
            return new String[] {inFile.getAbsolutePath(), outFile.getAbsolutePath()};
        }

        @Override
        protected InputStream createOutStream() throws IOException {
            return new FileInputStream(outFile);
        }

        @Override
        public void stop() {
            super.stop();
            inFile.delete();
            outFile.delete();
        }
    }

    private static class RemoteIOProxy extends IOProxy {
        private final Future<String> inFilename;
        private final Future<String> outFilename;
        private final ExecutionEnvironment execEnv;
        private final RequestProcessor RP = new RequestProcessor("Remote fifo creator", 2); //NOI18N

        public RemoteIOProxy(ExecutionEnvironment execEnv, Reader ioReader, Writer ioWriter) {
            super(ioReader, ioWriter);
            this.execEnv = execEnv;
            this.inFilename = createNewFifo();
            this.outFilename = createNewFifo();
        }
        
        @Override
        String[] getIOFiles() {
            try {
                return new String[] {inFilename.get(), outFilename.get()};
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        @Override
        protected OutputStream createInStream() throws IOException {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            try {
                npb.setCommandLine("cat > " + inFilename.get()); // NOI18N
                return ProcessUtils.ignoreProcessError(npb.call()).getOutputStream();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
        
        @Override
        protected InputStream createOutStream() throws IOException {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            try {
                npb.setCommandLine("cat " + outFilename.get()); // NOI18N
                return ProcessUtils.ignoreProcessError(npb.call()).getInputStream();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        private Future<String> createNewFifo() {
            return RP.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String tmpDir;
                    try {
                        tmpDir = HostInfoUtils.getHostInfo(execEnv).getTempDir();
                    } catch (Exception iOException) {
                        tmpDir = "/tmp"; // NOI18N
                    }
                    String name = tmpDir + '/' + FILENAME_PREFIX + "$$" + FILENAME_EXTENSION; // NOI18N
                    ExitStatus status = ProcessUtils.execute(execEnv, "sh", "-c", "mkfifo " + name + ";echo " + name); //NOI18N
                    if (status.isOK()) {
                        return status.getOutputString();
                    }
                    return null;
                }
            });
        }

        @Override
        public void stop() {
            super.stop();
            try {
                // delete files
                CommonTasksSupport.rmFile(execEnv, inFilename.get(), null);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            try {
                CommonTasksSupport.rmFile(execEnv, outFilename.get(), null);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}

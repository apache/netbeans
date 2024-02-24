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
package org.netbeans.modules.nativeexecution;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminal;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.Signal;
import org.netbeans.modules.nativeexecution.support.EnvWriter;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.PathUtils;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.support.InstalledFileLocatorProvider;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * A process to be run in *external* terminal
 * @author akrasny
 */
public final class TerminalLocalNativeProcess extends AbstractNativeProcess {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final File dorunScript;
    private ExternalTerminal terminal;
    private File resultFile;
    private final OSFamily osFamily;

    static {
        InstalledFileLocator fl = InstalledFileLocatorProvider.getDefault();
        File dorunScriptFile = fl.locate("bin/nativeexecution/dorun.sh", "org.netbeans.modules.dlight.nativeexecution", false); // NOI18N

        if (dorunScriptFile == null) {
            log.severe("Unable to locate bin/nativeexecution/dorun.sh file!"); // NOI18N
        } else if (!Utilities.isWindows()) {
            CommonTasksSupport.chmod(ExecutionEnvironmentFactory.getLocal(),
                    dorunScriptFile.getAbsolutePath(), 0755, null);
        }

        dorunScript = dorunScriptFile;
    }

    public TerminalLocalNativeProcess(
            final NativeProcessInfo info, final ExternalTerminal terminal) {
        super(info);
        this.terminal = terminal;
        setInputStream(new ByteArrayInputStream(
                (loc("TerminalLocalNativeProcess.ProcessStarted.text") + '\n').getBytes())); // NOI18N

        osFamily = hostInfo == null ? OSFamily.UNKNOWN : hostInfo.getOSFamily();
    }

    @Override
    protected void create() throws Throwable {
        File pidFileFile = null;
        File shFileFile = null;

        try {
            if (dorunScript == null) {
                throw new IOException(loc("TerminalLocalNativeProcess.dorunNotFound.text")); // NOI18N
            }

            if (osFamily == OSFamily.WINDOWS && hostInfo.getShell() == null) {
                throw new IOException(loc("NativeProcess.shellNotFound.text")); // NOI18N
            }

            final String commandLine = info.getCommandLineForShell();
            final String wDir = info.getWorkingDirectory(true);

            final File workingDirectory = (wDir == null) ? new File(".") : new File(wDir); // NOI18N

            pidFileFile = Files.createTempFile(hostInfo.getTempDirFile().toPath(), "dlight", "termexec").toFile().getAbsoluteFile(); // NOI18N
            shFileFile = new File(pidFileFile.getPath() + ".sh"); // NOI18N
            resultFile = new File(shFileFile.getPath() + ".res"); // NOI18N

            resultFile.deleteOnExit();

            String pidFile = (osFamily == OSFamily.WINDOWS) ? WindowsSupport.getInstance().convertToShellPath(pidFileFile.getPath()) : pidFileFile.getPath();
            String shFile = pidFile + ".sh"; // NOI18N

            FileOutputStream shfos = new FileOutputStream(shFileFile);

            Charset scriptCharset;

            if (info.getCharset() != null) {
                scriptCharset = info.getCharset();
            } else {
                if (osFamily == OSFamily.WINDOWS) {
                    scriptCharset = WindowsSupport.getInstance().getShellCharset();
                } else {
                    scriptCharset = Charset.defaultCharset();
                }
            }

            final ExternalTerminalAccessor terminalInfo =
                    ExternalTerminalAccessor.getDefault();

            if (terminalInfo.getTitle(terminal) == null) {
                String title = getExecutableName();

                if (title == null) {
                    title = NbBundle.getMessage(TerminalLocalNativeProcess.class, "TerminalLocalNativeProcess.terminalTitle.text"); // NOI18N
                }

                terminal = terminal.setTitle(title);
            }

            List<String> terminalArgs = new ArrayList<>();

            String shellScriptPath = dorunScript.getAbsolutePath();
            if (osFamily == OSFamily.WINDOWS) {
                shellScriptPath = WindowsSupport.getInstance().convertToShellPath(shellScriptPath);
            }
            
            terminal = terminal.setWorkdir(workingDirectory.toString());

            terminalArgs.addAll(Arrays.asList(
                    shellScriptPath,
                    "-p", terminalInfo.getPrompt(terminal), // NOI18N
                    "-x", shFile)); // NOI18N

            List<String> command = terminalInfo.wrapCommand(
                    info.getExecutionEnvironment(),
                    terminal,
                    terminalArgs);

            ProcessBuilder pb = new ProcessBuilder(command);

            if (!workingDirectory.exists()) {
                throw new FileNotFoundException(loc("NativeProcess.noSuchDirectoryError.text", workingDirectory.getAbsolutePath())); // NOI18N
            }
            
            pb.directory(workingDirectory);
            pb.redirectErrorStream(true);

            LOG.log(Level.FINEST, "External terminal command: {0}", command); // NOI18N

            final MacroMap env = info.getEnvironment().clone();

            // setup DISPLAY variable for MacOS...
            if (osFamily == OSFamily.MACOSX) {
                ProcessBuilder pb1 = new ProcessBuilder(hostInfo.getShell(), "-c", "/bin/echo $DISPLAY"); // NOI18N
                // this is an external terminal process - it's ok to use ProcessUtils.execute here
                ProcessUtils.ExitStatus res = ProcessUtils.execute(pb1);
                String display = null;

                if (res.isOK()) {
                    display = res.getOutputString();
                } 

                if (display == null || "".equals(display)) { // NOI18N
                    display = ":0.0"; // NOI18N
                }

                pb.environment().put("DISPLAY", display); // NOI18N
            }

            OutputStreamWriter shWriter = new OutputStreamWriter(shfos, scriptCharset);
            shWriter.write("echo $$ > \"" + pidFile + "\" || exit $?\n"); // NOI18N

            if (!env.isEmpty()) {
                // TODO: FIXME (?)
                // Do PATH normalization on Windows....
                // Problem here is that this is done for PATH env. variable only!

                if (osFamily == OSFamily.WINDOWS) {
                    // Make sure that path in upper case
                    // [for external terminal only]
                    String path = env.get("PATH"); // NOI18N
                    env.remove("PATH"); // NOI18N
                    env.put("PATH", WindowsSupport.getInstance().convertToAllShellPaths(path)); // NOI18N
                }

                EnvWriter ew = new EnvWriter(shWriter);
                ew.write(env);

                if (LOG.isLoggable(Level.FINEST)) {
                    env.dump(System.err);
                }
            }

            shWriter.write("exec " + commandLine + "\n"); // NOI18N
            shWriter.close();

            Process terminalProcess = ProcessUtils.ignoreProcessOutputAndError(pb.start());

            creation_ts = System.nanoTime();

            waitPID(terminalProcess, pidFileFile);

            if (isInterrupted()) {
                throw new IOException(loc("TerminalLocalNativeProcess.terminalRunCancelled.text")); // NOI18N
            }
        } catch (Throwable ex) {
            resultFile = null;
            throw ex;
        } finally {
            if (pidFileFile != null) {
                pidFileFile.delete();
            }
            if (shFileFile != null) {
                shFileFile.delete();
            }
        }
    }

    private int getPIDNoException() {
        int pid = -1;

        try {
            pid = getPID();
        } catch (Exception ex) {
        }

        return pid;
    }

    @Override
    public int waitResult() throws InterruptedException {
        int pid = getPIDNoException();

        if (pid < 0) {
            // Process was not even started
            return -1;
        }

        if (osFamily == OSFamily.LINUX || osFamily == OSFamily.SUNOS) {
            File f = new File("/proc/" + pid); // NOI18N

            while (f.exists()) {
                Thread.sleep(300);
            }
        } else {
            int rc = 0;
            while (rc == 0) {
                try {
                    rc = CommonTasksSupport.sendSignal(info.getExecutionEnvironment(), pid, Signal.NULL, null).get();
                } catch (ExecutionException ex) {
                    log.log(Level.FINEST, "", ex); // NOI18N
                    rc = -1;
                }

                Thread.sleep(300);
            }
        }

        if (resultFile == null) {
            return -1;
        }

        int exitCode = -1;

        BufferedReader statusReader = null;

        try {
            int attempts = 10;
            while (attempts-- > 0) {
                if (resultFile.exists() && resultFile.length() > 0) {
                    statusReader = new BufferedReader(new FileReader(resultFile));
                    String exitCodeString = statusReader.readLine();
                    if (exitCodeString != null) {
                        exitCode = Integer.parseInt(exitCodeString.trim());
                    }
                    break;
                }

                Thread.sleep(500);
            }
        } catch (InterruptedIOException ex) {
            throw new InterruptedException();
        } catch (IOException ex) {
        } catch (NumberFormatException ex) {
        } finally {
            if (statusReader != null) {
                try {
                    statusReader.close();
                } catch (IOException ex) {
                }
            }
        }

        return exitCode;
    }

    private void waitPID(Process termProcess, File pidFile) throws IOException {
        while (!isInterrupted()) {
            /**
             * The following sleep appears after an attempt to support konsole
             * KDE4. This was done to give some time for external process to
             * write information about process' PID to the pidfile and not to
             * get to termProcess.exitValue() too eraly...
             * Currently there are no means on KDE4 to start konsole in
             * 'not-background' mode.
             * An attempt to use --nofork fails when start konsole from jvm
             * (see http://www.nabble.com/Can%27t-use---nofork-for-KUniqueApplications-from-another-kde-process-td21047022.html)
             * So termProcess exits immediately...
             *
             * Also this sleep is justifable because this doesn't make any sense
             * to check for a pid file too often.
             *
             */
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }

            if (pidFile.exists() && pidFile.length() > 0) {
                InputStream pidIS = null;
                try {
                    pidIS = new FileInputStream(pidFile);
                    readPID(pidIS);
                } finally {
                    if (pidIS != null) {
                        pidIS.close();
                    }
                }
                break;
            }

            try {
                int result = termProcess.exitValue();

                if (result != 0) {
                    String err = ProcessUtils.readProcessErrorLine(termProcess);
                    if (!err.isEmpty()) { // it is redirected! but just in case let's read both
                        err = ProcessUtils.readProcessOutputLine(termProcess);
                    }                    
                    log.info(loc("TerminalLocalNativeProcess.terminalFailed.text")); // NOI18N
                    log.info(err);
                    throw new IOException(err);
                }

                // No exception - means process is finished..
                break;
            } catch (IllegalThreadStateException ex) {
                // expected ... means that terminal process exists
            }
        }
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(TerminalLocalNativeProcess.class, key, params);
    }

    private String getExecutableName() {
        String exec = info.getExecutable();

        if (exec != null) {
            if (new File(exec).exists()) {
                return new File(exec).getName();
            }

            if (osFamily == OSFamily.WINDOWS && new File(exec + ".exe").exists()) { // NOI18N
                return new File(exec).getName();
            }
        }

        String[] params = Utilities.parseParameters(info.getCommandLineForShell());
        if (params != null && params.length > 0) {
            exec = params[0];
            if (exec != null && new File(exec).exists()) {
                return new File(exec).getName();
            }
        }

        return null;
    }
}

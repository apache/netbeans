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

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;

import org.openide.util.Exceptions;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.openide.ErrorManager;

import org.netbeans.modules.cnd.debugger.common2.debugger.io.TermComponent;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.PathUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.api.util.Signal;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

/* package */ class ExecutorCND extends Executor {
    private NativeProcess engineProc;
    private int pid = -1;
    private final ExecutionEnvironment exEnv;
    private String startError = null;
    private final ChangeListener changeListener;

    public ExecutorCND(String name, Host host, ChangeListener changeListener) {
	super(name, host);
        exEnv = host.executionEnvironment();
        this.changeListener = changeListener;
    }

    @Override
    public ExecutionEnvironment getExecutionEnvironment() {
        return exEnv;
    }

    @Override
    public boolean isAlive() {
        try {
            engineProc.exitValue();
        } catch (IllegalThreadStateException x) {
            return true;
        }
        return false;
    }
    
    @Override
    public int getExitValue() {
        return engineProc.exitValue();
    }

    @Override
    public void terminate() throws IOException {
        engineProc.destroy();	// On unix this sends a SIGTERM
    }

    /*
     * Interrupt an arbitrary process with SIGINT
     */
    @Override
    public void interrupt(final int pid) throws IOException {
        CndUtils.assertNonUiThread();
        // use DebugBreakProcess on windows
        if (exEnv.isLocal() && Utilities.isWindows()) {
            File f = InstalledFileLocator.getDefault().locate("bin/GdbKillProc.exe", "org.netbeans.modules.cnd.debugger.common2", false); // NOI18N
            //bz#258406 - NullPointerException at org.netbeans.modules.cnd.debugger.common2.utils.ExecutorCND.interrupt
            //looks loke GdbKillProc file could not be located, will just check for null
            if (f != null && f.exists()) {
                ProcessUtils.execute(exEnv, f.getAbsolutePath(), "-s", "INT", Long.toString(pid)); //NOI18N
            }
        } else {
            try {
                CommonTasksSupport.sendSignal(exEnv, pid, Signal.SIGINT, null).get();
            } catch (InterruptedException ex) {
            } catch (ExecutionException ex) {
            }
        }
    }
    
    @Override
    public void interrupt() throws IOException {
        interrupt(pid);
    }

    @Override
    public void interruptGroup() throws IOException {
        CndUtils.assertNonUiThread();
        try {
            CommonTasksSupport.sendSignalGrp(exEnv, ExecutorCND.this.pid, Signal.SIGINT, null).get();
        } catch (InterruptedException ex) {
        } catch (ExecutionException ex) {
        }
    }

    @Override
    public void sigqueue(final int sig, final int data) throws IOException {
        CndUtils.assertNonUiThread();
        try {
            CommonTasksSupport.sigqueue(exEnv, ExecutorCND.this.pid, sig, data, null).get();
        } catch (InterruptedException ex) {
        } catch (ExecutionException ex) {
        }
    }

    @Override
    public synchronized int startShellCmd(String cmd_argv[]) {
	NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(exEnv);

        String argv[] = new String[cmd_argv.length-1];
	//argv[0] = enginePath;
	for (int cx = 1; cx < cmd_argv.length; cx++) {
	    argv[cx-1] = cmd_argv[cx];
	}

        npb.setExecutable(cmd_argv[0]);
        npb.setArguments(argv);
        npb.setUsePty(true);

        // Set env variable, otherwise xstart does not find some tools
        // OLD (see 6986489):
	// npb.getEnvironment().put("_ST_GLUE_SM_PATH", host().getRemoteStudioLocation() + "/prod/lib"); //NOI18N

        try {
            engineProc = npb.call(); // should be followed by reap, which will read out and err
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
        try {
            pid = engineProc.getPID();
            return pid;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        return 1;
        }
    }

    @Override
    public synchronized int startEngine(String enginePath,
					String engine_argv[], 
                                        Map<String, String> additionalEnv,
                                        String workDir, 
			                TermComponent console,
                                        boolean usePty,
                                        boolean disableEcho) {
        startError = null;
        
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(exEnv);
        if (changeListener != null)
            npb.addNativeProcessListener(changeListener);

	String argv[] = new String[engine_argv.length-1];
	//argv[0] = enginePath;
	for (int cx = 1; cx < engine_argv.length; cx++) {
	    argv[cx-1] = engine_argv[cx];
	}

        npb.setExecutable(enginePath);
        npb.setArguments(argv);
        npb.setWorkingDirectory(workDir);
        npb.getEnvironment().putAll(additionalEnv);
        
        if (usePty) {
            npb.setUsePty(true);
            npb.getEnvironment().put("TERM", console.getTerm().getEmulation()); // NOI18N
        }

        try {
            engineProc = npb.call(); // no ProcessUtils - we'll attach it to pty
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
            return 0;
        }

        // npb.call() may fail because of some Exception
        // in this case proc's state is ERROR and cause can be read from it's
        // error stream...
        if (engineProc.getState() == NativeProcess.State.ERROR) {
            try {
                startError = ProcessUtils.readProcessErrorLine(engineProc);
            } catch (IOException ex) {
            }

            // TODO: the only place this return value is analyzed is start2() of
            // org.netbeans.modules.cnd.debugger.gdb2.Gdb and it is compated 
            // with 0 to identify an error...
            return 0;
        }

        PtySupport.connect(console.getIO(), engineProc);

        if (disableEcho) {
            PtySupport.disableEcho(exEnv, PtySupport.getTTY(engineProc));
        }

	//startMonitor();
        
        try {
            pid = engineProc.getPID();
            return pid;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return 0;
        }
    }

    @Override
    public String getStartError() {
	return startError;
    }

    @Override
    protected int waitForEngine() throws InterruptedException {
	if (engineProc == null) {
	    return -1;
        }
	return engineProc.waitFor();
    }

    @Override
    protected void destroyEngine() {
	if (engineProc == null) {
	    return;
        }
	super.destroyEngine();
	engineProc.destroy();
    }

    @Override
    public void reap() {
	Thread reaper = new Thread() {
	    @Override
	    public void run() {
		setName("ExecutorCND Reaper"); // NOI18N
                Future<List<String>> err = null;
                Future<List<String>> out = null;
                if (Log.Executor.debug) {
                    err = ProcessUtils.readProcessErrorAsync(engineProc);
                    out = ProcessUtils.readProcessOutputAsync(engineProc);
                } else {
                    ProcessUtils.ignoreProcessOutputAndError(engineProc);
                }
		try {
		    engineProc.waitFor();
		} catch (InterruptedException ex) {
		    Exceptions.printStackTrace(ex);
		}
		int exitValue = engineProc.exitValue();
		if (Log.Executor.debug) {
		    // Normal termination is done using SIGTERM which gives us
		    // an exitValue of 1 instead of 0.
		    if (exitValue != 0 && err != null && out != null) { // err & out null checks are paranoidal
			try {
                            List<String> processError = err.get();
                            List<String> processOutput = out.get();
                            try {
                                processError = ProcessUtils.readProcessError(engineProc);
                                processOutput = ProcessUtils.readProcessOutput(engineProc);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }

                            String output = "";
                            output += String.format("Process exited with %d.\n", exitValue); // NOI18N
                            output += String.format("Output:\n"); // NOI18N
                            if (processOutput == null) {
                                output += "\t<empty>\n"; // NOI18N
                            } else {
                                for (String l : processOutput)
                                    output += l + "\n"; // NOI18N
                            }
                            output += String.format("Error:\n"); // NOI18N
                            if (processError == null) {
                                output += "\t<empty>\n"; // NOI18N
                            }  else {
                                for (String l : processError)
                                    output += l + "\n"; // NOI18N
                            }

                            final String foutput = output;
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    IpeUtils.postError(foutput);
                                }
                            });
                        } catch (InterruptedException ex) {
			} catch (ExecutionException ex) {
                        }
		    }
		}
	    }
	};
	reaper.start();
    }

    @Override
    public String readlink(long pid) {
        return PathUtils.getExePath(pid, exEnv);
    }
    
    @Override
    public String readlsof(long pid) {
        ExitStatus status = ProcessUtils.execute(exEnv, "lsof", "-p", "" + pid, "-Fn"); //NOI18N
        if (status.isOK()) {
            return status.getOutputString().split("\n")[2].substring(1); //NOI18N
        }
        return ""; //NOI18N
    }

    @Override
    public String readDirLink(long pid) {
        return PathUtils.getCwdPath(pid, exEnv);
    }

    @Override
    public boolean is_64(String filep) {
	ExitStatus status = ProcessUtils.execute(exEnv, "/usr/bin/file", filep); //NOI18N
        return status.getOutputString().contains(" 64"); //NOI18N
    }
	      
    @Override
    public InputStream getInputStream() {
	return engineProc.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() {
	return engineProc.getOutputStream();
    }
}

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

package org.netbeans.modules.python.qshell.richexecution;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A process being run connected to a Pty.
 * <p>
 * After and partial delegator to {@link java.lang.Process}.
 * <p>
 * Use {@link PtyExecutor} or subclasses thereof to create a PtyProcess.
 * <p>
 * <h3>Closing of the Pty</h3>
 * One reason we pass in a Pty is so Ptyprocess can close it when the process
 * exits. The closing of the pty really only closes the slave side file
 * descriptor and that is neccessary so that any master side reader will see
 * an EOF.
 * <br>
 * Why not close the slave side right after the process is created?
 * <br>
 * Because apparently on solaris one can manipulate terminal characteristics,
 * like size changes, only from the slave side. But the key word here is
 * apparently. SHOULD experiment further.
 */
public final class PtyProcess extends java.lang.Process {

    private final Process process;
    private final int pid;
    private final Pty pty;

    private Runnable reaper;
    private boolean reaped = false;

    PtyProcess(Process process, int pid, Pty pty) {
        this.process = process;
        this.pid = pid;
        this.pty = pty;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream() {
	if (pty == null)
	    return process.getInputStream();	// Mode.RAW
	else
	    return pty.getInputStream();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that PtyProcess always has it's error redirected via
     * {@link java.lang.ProcessBuilder#redirectErrorStream}.
     */
    @Override
    public InputStream getErrorStream() {
	if (pty == null)
	    return process.getErrorStream();	// Mode.RAW
	else
	    throw new UnsupportedOperationException("No error stream");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream getOutputStream() {
	if (pty == null)
	    return process.getOutputStream();
	else
	    return pty.getOutputStream();
    }

    /**
     * When run() it will block and wait for the started process to exit.
     * @return A Runnable which, when run(), will block and wait for the started
     * process to exit.
     */
    private Runnable getReaper() {
        if (reaper == null) {
            reaper = new Runnable() {
                @Override
                public void run() {
                    try {
                        process.waitFor();
			if (pty != null)
			    pty.close();
                        reaped = true;
                    } catch (IOException | InterruptedException ex) {
                        Logger.getLogger(PtyProcess.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
        }
        return reaper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int waitFor() {
        getReaper().run();
        return exitValue();
    }
    

    /**
     * Send a SIGHUP to the child.
     * <p>
     * On unix ...
     * <br>
     * java.lang.Process.destroy() sends a SIGTERM to the process. 
     * While that usually works for regular processes, shells tend to
     * ignore SIGTERM and instead are sensitive to SIGHUP.
     * <br>
     * Elsewhere ...
     * <br>
     * Same as {@link java.lang.Process#destroy }.
     */
    public void hangup() {
        if (reaped)
            return;
        if (pid == -1) {
            // System.out.printf("No PID -- will try terminating\n");
            process.destroy();
        } else {
            ProcessLibrary.INSTANCE.kill(pid, ProcessLibrary.SIGHUP);
        }
    }

    /**
     * Send a SIGTERM to the child.
     * <p>
     * On unix ...
     * <br>
     * Sends a SIGTERM to the process. 
     * <br>
     * Elsewhere ...
     * <br>
     * Same as {@link java.lang.Process#destroy }.
     */
    public void terminate() {
        if (reaped)
            return;
        if (pid == -1) {
            // System.out.printf("No PID -- will try terminating\n");
            process.destroy();
        } else {
            ProcessLibrary.INSTANCE.kill(pid, ProcessLibrary.SIGTERM);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        if (reaped)
            return;
        process.destroy();
    }

    public boolean isFinished() {
        return reaped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int exitValue() {
        return process.exitValue();
    }
}

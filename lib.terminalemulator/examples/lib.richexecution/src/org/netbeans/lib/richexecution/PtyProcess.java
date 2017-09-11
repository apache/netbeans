/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.richexecution;

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
 * @author ivan
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
    public InputStream getErrorStream() {
	if (pty == null)
	    return process.getErrorStream();	// Mode.RAW
	else
	    throw new UnsupportedOperationException("No error stream");
    }

    /**
     * {@inheritDoc}
     */
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
                public void run() {
                    try {
                        process.waitFor();
			if (pty != null)
			    pty.close();
                        reaped = true;
                    } catch (Exception ex) {
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
            CLibrary.INSTANCE.kill(pid, CLibrary.INSTANCE.SIGHUP());
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
            CLibrary.INSTANCE.kill(pid, CLibrary.INSTANCE.SIGTERM());
        }
    }

    /**
     * {@inheritDoc}
     */
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
    public int exitValue() {
        return process.exitValue();
    }
}

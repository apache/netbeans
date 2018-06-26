/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.modules.glassfish.tooling.server.state;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.tooling.data.GlassFishStatusCheck;
import org.netbeans.modules.glassfish.tooling.data.GlassFishStatusCheckResult;
import org.netbeans.modules.glassfish.tooling.TaskEvent;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 * Individual server status check task to verify if server administration
 * port is alive.
 * <p/>
 * @author Tomas Kraus
 */
class AdminPortTask extends AbstractTask {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(AdminPortTask.class);

    /** Log message identifier suffix when using message with timestamp. */
    private final String TM_SUFFIX = "Tm";

    /** Task name for logging purposes. */
    private final String TASK_NAME = "port-check";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Format time value in miliseconds to be printed as value in seconds
     * and miliseconds
     * <code>s.ms<code>.
     * <p/>
     * @param tm Time value in miliseconds
     * @return Time string formated as econds and miliseconds
     *         <code>s.ms<code>.
     */
    static String tm(final long tm) {
        StringBuilder sb = new StringBuilder(8);
        sb.append(Long.toString(tm / 1000));
        sb.append('.');
        sb.append(Long.toString(tm % 1000));
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Socked connecting timeout [ms]. */
    int timeout;

    /** Server administration port status check result. */
    private StatusResult result;

    /** Task start time. Used for logging purposes. Value of <code>-1</code>
      *  means that start time was not set.*/
    private long tmStart;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of administration port status check.
     * <p/>
     * @param job  Server status check job internal data.
     * @param task Individual status check task data.
     * @param timeout Socked connecting timeout.
     */
    AdminPortTask(final StatusJob job, final StatusJob.Task task,
            final int timeout) {
        super(job, task, GlassFishStatusCheck.PORT);
        this.timeout = timeout;
        this.result = null;
        this.tmStart = -1;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get last command task execution result.
     * <p/>
     * @return Last command task execution result.
     */
    StatusResult getResult() {
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build arguments for operationStateChanged method listening for state
     * changes.
     * <p/>
     * Send <code>false</code> display argument to operationStateChanged method
     * to not display GUI message about this event.
     * <p/>
     * @param exMessage Exception message.
     * @return Arguments for operationStateChanged method listening for state
     * changes.
     */
    String[] stateChangeArgs(final String exMessage) {
        return new String[] {
            job.getStatus().getServer().getHost(), TASK_NAME,
            exMessage, Boolean.toString(false)
        };
    }

    /**
     * Close socket and handle <code>IOException</code> that could
     * be thrown.
     * <p/>
     * @param socket Socket to be closed.
     */
    private void closeSocket(final Socket socket) {
        final String METHOD = "closeSocket";
        try {
            socket.close();
        } catch (IOException ioe) {
            handleIOException(ioe, job.getStatus().getServer().getHost(),
                    job.getStatus().getServer().getAdminPort(),
                    METHOD, "failed");
        }
    }

    /**
     * Handle IO Exception caught in server administration port verification
     * task.
     * <p/>
     * Set task result and call registered listeners.
     * <p/>
     * @param ioe     <code>IOException</code> caught.
     * @param host    Server administration host.
     * @param port    Server administration port.
     * @param message Message to be logged. Shall not be <code>null</code>.
     */
    private void handleIOException(final IOException ioe,
            final String host, final int port,
            final String method, final String message) {
        if (tmStart >= 0 && LOGGER.isLoggable(Level.FINEST)) {
            StringBuilder sb = new StringBuilder(
                    message.length() + TM_SUFFIX.length());
            sb.append(message).append(TM_SUFFIX);
            long tm = System.currentTimeMillis() - tmStart;
            LOGGER.log(Level.FINEST, method, sb.toString(), new Object[] {
                    tm(tm), host, Integer.toString(port), ioe.getMessage()});

        } else {
            LOGGER.log(Level.FINEST, method, message, new Object[] {
                    host, Integer.toString(port), ioe.getMessage()});
        }
        result = new StatusResult(
                GlassFishStatusCheckResult.FAILED, TaskEvent.EXCEPTION);
        handleStateChange(TaskState.FAILED, TaskEvent.EXCEPTION,
                stateChangeArgs(ioe.getLocalizedMessage()));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Runnable run() method                                                  //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Thread main method to be executed.
     * <p/>
     * Runs administration port check.
     */
    @Override
    public void run() {
        final String METHOD = "run";
        if (cancelled) {
            LOGGER.log(Level.FINER, METHOD, "cancelled");
            throw new IllegalStateException(LOGGER.excMsg(METHOD, "cancelled"));
        }
        LOGGER.log(Level.FINER, METHOD, "started", new String[] {
            job.getStatus().getServer().getName(), jobState.toString()});
        String host = job.getStatus().getServer().getHost();
        int port = job.getStatus().getServer().getAdminPort();
        if (port < 0 || host == null) {
            result = new StatusResult(GlassFishStatusCheckResult.FAILED);
            handleStateChange(TaskState.FAILED,
                    TaskEvent.CMD_FAILED, stateChangeArgs(null));
        }
        this.tmStart = System.currentTimeMillis();
        InetSocketAddress sa = new InetSocketAddress(host, port);
        Socket socket = new Socket();
        try {
            socket.connect(sa, timeout);
            socket.setSoTimeout(timeout);
        } catch (java.net.ConnectException ce) {
            handleIOException(ce, host, port, METHOD, "connect");
            return;
        } catch (java.net.SocketTimeoutException ste) {
            handleIOException(ste, host, port, METHOD, "timeout");
            return;
        } catch (IOException ioe) {
            handleIOException(ioe, host, port, METHOD, "ioException");
            return;
        } finally {
            closeSocket(socket);
        }
        if (tmStart >= 0 && LOGGER.isLoggable(Level.FINEST)) {
            long tm = System.currentTimeMillis() - tmStart;
            LOGGER.log(Level.FINEST, METHOD, "success",
                    new Object[] {tm(tm), jobState.toString(),
                        host, Integer.toString(port)});
        }
        result = new StatusResult(
                GlassFishStatusCheckResult.SUCCESS, TaskEvent.CMD_COMPLETED);
        handleStateChange(TaskState.COMPLETED, TaskEvent.CMD_COMPLETED,
                stateChangeArgs(null));
    }

}

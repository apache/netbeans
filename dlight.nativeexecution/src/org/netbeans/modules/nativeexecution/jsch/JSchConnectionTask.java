/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.jsch;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.jsch.JSchConnectionTask.Problem;
import org.netbeans.modules.nativeexecution.spi.JSchAuthenticationSelection;
import org.netbeans.modules.nativeexecution.api.util.Authentication;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
//import org.netbeans.modules.dlight.nativeexecution.ui.AuthTypeSelectorDlg;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

/**
 * Thread safe. Started only once.
 * @author ak119685
 */
public final class JSchConnectionTask implements Cancellable {

    // Connections are always established sequently in connectorThread
    private static final RequestProcessor connectorThread = new RequestProcessor("ConnectionManager queue", 1); // NOI18N
    private static final int SOCKET_CREATION_TIMEOUT = Integer.getInteger("socket.connection.timeout", 10000); // NOI18N
    private static final java.util.logging.Logger log = Logger.getInstance();
    // ------------------------------------------------------------------------
    private final JSch jsch;
    private final ExecutionEnvironment env;
    private final Object resultLock = new Object();
    private Future<JSchConnectionTask.Result> result = null;
    private volatile boolean cancelled;

    public JSchConnectionTask(final JSch jsch, final ExecutionEnvironment env) {
        this.jsch = jsch;
        this.env = env;
        cancelled = false;
    }

    public void start() {
        synchronized (resultLock) {
            if (result == null) {
                result = connectorThread.submit(new Callable<JSchConnectionTask.Result>() {

                    @Override
                    public Result call() throws Exception {
                        return connect();
                    }
                });
            }
        }
    }

    private JSchConnectionTask.Result connect() throws Exception {
        ConnectingProgressHandle.startHandle(env, this);

        try {
            try {
                env.prepareForConnection();
            } catch (Throwable th) {
                return new Result(null, new Problem(ProblemType.ENV_PREPARE_ERROR, th));
            }

            if (cancelled) {
                return new Result(null, new Problem(ProblemType.CONNECTION_CANCELLED));
            }

            if (!isReachable()) {
                if (cancelled) {
                    return new Result(null, new Problem(ProblemType.CONNECTION_CANCELLED));
                } else {
                    return new Result(null, new Problem(ProblemType.HOST_UNREACHABLE));
                }
            }

            if (cancelled) {
                return new Result(null, new Problem(ProblemType.CONNECTION_CANCELLED));
            }

            if (!initJsch(env)) {
                return new Result(null, new Problem(ProblemType.CONNECTION_CANCELLED));
            }

            // Start special shell session that will serve administrative tasks
            // like sending signals to processes...

            JSchChannelsSupport cs = new JSchChannelsSupport(jsch, env);
            try {
                cs.connect();
            } catch (InterruptedException ex) {
                cancelled = true;
            }

            if (cancelled) {
                return new Result(null, new Problem(ProblemType.CONNECTION_CANCELLED));
            }

            // OK. Connection established.

            return new Result(cs, null);
        } catch (JSchException e) {
            log.log(Level.FINE, "JSchException connecting to " + env, e); // NOI18N

            if (e.getMessage().equals("Auth cancel")) { // NOI18N
                return new Result(null, new Problem(ProblemType.CONNECTION_CANCELLED));
            } else if (e.getMessage().contains("java.net.SocketTimeoutException") // NOI18N
                    || e.getMessage().contains("timeout")) { // NOI18N
                return new Result(null, new Problem(ProblemType.CONNECTION_TIMEOUT, e));
            }
            return new Result(null, new Problem(ProblemType.CONNECTION_FAILED, e));
        } catch (java.util.concurrent.CancellationException ex) {
            log.log(Level.FINE, "CancellationException", ex); // NOI18N
            return new Result(null, new Problem(ProblemType.CONNECTION_CANCELLED));
        } catch (Throwable th) {
            return new Result(null, new Problem(ProblemType.CONNECTION_FAILED, th));
        } finally {
            ConnectingProgressHandle.stopHandle(env);
        }
    }

    private static boolean initJsch(ExecutionEnvironment env) {
        Authentication auth = Authentication.getFor(env);

        if (!auth.isDefined()) {
            return JSchAuthenticationSelection.find().initAuthentication(auth);
//            AuthTypeSelectorDlg dlg = new AuthTypeSelectorDlg();
//            if (!dlg.initAuthentication(auth)) {
//                return false;
//            }
        } else {
            auth.apply();
        }

        return true;
    }

    public Problem getProblem() throws InterruptedException, ExecutionException {
        Future<JSchConnectionTask.Result> r;
        synchronized (resultLock) {
            r = result;
        }

        if (r == null) {
            throw new IllegalStateException("Not started yet"); // NOI18N
        }

        return r.get().problem;
    }

    public JSchChannelsSupport getResult() throws InterruptedException, ExecutionException {
        Future<JSchConnectionTask.Result> r;
        synchronized (resultLock) {
            r = result;
        }

        if (r == null) {
            throw new IllegalStateException("Not started yet"); // NOI18N
        }

        return r.get().cs;
    }

    private boolean isReachable() throws IOException {
        // IZ#165591 - Trying to connect to wrong host breaks remote host setup (for other hosts)
        // To prevent this first try to just open a socket and
        // go to the jsch code in case of success only.

        // The important thing here is that we still need to be interruptable
        // In case of wrong IP address (unreachable) the SocketImpl's connect()
        // method may hang in system call for a long period of time, being
        // insensitive to interrupts.
        // So do this in a separate thread...

        Callable<Boolean> checker = new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                final Socket socket = new Socket();
                final SocketAddress addressToConnect =
                        new InetSocketAddress(env.getHostAddress(), env.getSSHPort());
                try {
                    socket.connect(addressToConnect, SOCKET_CREATION_TIMEOUT);
                } catch (Exception ioe) {
                    return false;
                } finally {
                    socket.close();
                }
                return true;
            }
        };

        final Future<Boolean> task = NativeTaskExecutorService.submit(
                checker, "Host " + env.getHost() + " availability test"); // NOI18N

        while (!cancelled && !task.isDone()) {
            try {
                task.get(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                // normally should never happen
            } catch (ExecutionException ex) {
                // normally should never happen
            } catch (TimeoutException ex) {
                // OK.. still be waiting
            }
        }

        boolean result = false;

        if (task.isDone()) {
            try {
                result = task.get();
            } catch (Exception ex) {
                // normally should never happen
            }
        }

        return result;
    }

    @Override
    public boolean cancel() {
        cancelled = true;
        return true;
    }

    public static final class Problem {

        public final ProblemType type;
        public final Throwable cause;

        public Problem(ProblemType type) {
            this(type, null);
        }

        public Problem(ProblemType type, Throwable cause) {
            this.type = type;
            this.cause = cause;
        }
    }

    public static enum ProblemType {

        ENV_PREPARE_ERROR,
        AUTH_FAIL,
        HOST_UNREACHABLE,
        CONNECTION_CANCELLED,
        CONNECTION_FAILED,
        CONNECTION_TIMEOUT,
    }

    private final static class Result {

        public final JSchChannelsSupport cs;
        public final Problem problem;

        public Result(JSchChannelsSupport cs, Problem problem) {
            this.cs = cs;
            this.problem = problem;
            if (problem != null && isUnitTestMode()) {
                new Exception("That's just a trace: connection failed: " + problem.type, problem.cause).printStackTrace(System.err); //NOI18N
            }
        }
    }
    
    // copy-paste from CndUtils
    private static boolean isUnitTestMode() {
        return Boolean.getBoolean("cnd.mode.unittest"); // NOI18N
    }
    
}

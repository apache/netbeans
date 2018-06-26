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
package org.netbeans.modules.glassfish.tooling.server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.modules.glassfish.tooling.admin.CommandFetchLogData;
import org.netbeans.modules.glassfish.tooling.admin.ResultLog;
import org.netbeans.modules.glassfish.tooling.admin.ServerAdmin;
import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.logging.Logger;
import org.netbeans.modules.glassfish.tooling.utils.OsUtils;

/**
 * Fetch GlassFish log from remote server.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class FetchLogRemote extends FetchLogPiped {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(FetchLogPiped.class);

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish remote server log fetcher.
     * <p/>
     * Super class constructor will call <code>initInputStream</code> method
     * which initializes <code>InputStream</code> as
     * <code>PipedInputStream</code> before this constructor code is being
     * executed. Here we can simply connect already initialized
     * <code>PipedInputStream</code> with newly created
     * <code>PipedInputStream</code>.
     * <p/>
     * @param server GlassFish server for fetching server log.
     * @param skip   Skip to the end of the log file.
     */
    FetchLogRemote(final GlassFishServer server, final boolean skip) {
        super(server, skip);
    }

    /**
     * Constructs an instance of GlassFish remote server log fetcher with
     * external {@link ExecutorService}.
     * <p/>
     * Super class constructor will call <code>initInputStream</code> method
     * which initializes <code>InputStream</code> as
     * <code>PipedInputStream</code> before this constructor code is being
     * executed. Here we can simply connect already initialized
     * <code>PipedInputStream</code> with newly created
     * <code>PipedInputStream</code>.
     * <p/>
     * @param executor Executor service used to start task.
     * @param server   GlassFish server for fetching server log.
     * @param skip     Skip to the end of the log file.
     */
    FetchLogRemote(final ExecutorService executor, final GlassFishServer server,
            final boolean skip) {
        super(executor, server, skip);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Runnable call() Method                                                  //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Remote server log lines reading task.
     * <p/>
     * Reads new log lines from server using GlassFish remote administration API
     * and sends them into pipe (<code>PipedInputStream</code>).
     * <p/>
     * @return <code>TaskState.COMPLETED</code> when remote administration API
     *         stopped responding or <code>TaskState.FAILED</code> when
     *         exception was caught.
     */
    @SuppressWarnings("SleepWhileInLoop")
    @Override
    public TaskState call() {
        final String METHOD = "call";
        notifyListeners(TaskState.RUNNING);
        String paramsAppendNext = null;
        Future<ResultLog> future = ServerAdmin.<ResultLog>exec(server,
                new CommandFetchLogData());
        try {
            ResultLog result = future.get();
            if (!skip && result.getState() == TaskState.COMPLETED) {
                paramsAppendNext = result.getValue().getParamsAppendNext();
                for (String line : result.getValue().getLines()) {
                    out.write(line.getBytes());
                    out.write(OsUtils.LINES_SEPARATOR.getBytes());
                }
                out.flush();
            }
            byte[] lineSeparatorOut = OsUtils.LINES_SEPARATOR.getBytes();
            while (taksExecute  && result.getState() == TaskState.COMPLETED) {
                future = ServerAdmin.<ResultLog>exec(server,
                        new CommandFetchLogData(
                        paramsAppendNext));
                result = future.get();
                if (result.getState() == TaskState.COMPLETED) {
                    paramsAppendNext = result.getValue().getParamsAppendNext();
                    for (String line : result.getValue().getLines()) {
                        byte[] lineOut = line.getBytes();
                        LOGGER.log(Level.FINEST, METHOD, "read", new Object[] {
                            new Integer(lineOut.length
                                + lineSeparatorOut.length)});
                        out.write(lineOut);
                        out.write(lineSeparatorOut);
                    }
                    out.flush();
                }
                Thread.sleep(LOG_REFRESH_DELAY);
            }
        } catch (InterruptedException ie) {
            LOGGER.log(Level.INFO, METHOD, "interrupted", ie.getMessage());
            Thread.currentThread().interrupt();
            return notifyListeners(TaskState.COMPLETED);
        } catch (ExecutionException ee) {
            LOGGER.log(Level.INFO, METHOD, "exception", ee);
            return notifyListeners(TaskState.FAILED);
        } catch (InterruptedIOException ie) {
            LOGGER.log(Level.INFO, METHOD, "interruptedIO", ie.getMessage());
            Thread.currentThread().interrupt();
            return notifyListeners(TaskState.COMPLETED);
        } catch (IOException ioe) {
            if (taksExecute) {
                LOGGER.log(Level.INFO, METHOD, "ioException", ioe);
                return notifyListeners(TaskState.FAILED);
            } else {
                LOGGER.log(Level.INFO, METHOD,
                        "ioExceptionMsg", ioe.getMessage());
                return notifyListeners(TaskState.COMPLETED);
            }
        }
        return notifyListeners(TaskState.COMPLETED);
    }

}

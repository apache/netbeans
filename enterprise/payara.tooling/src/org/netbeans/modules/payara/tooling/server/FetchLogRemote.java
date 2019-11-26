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
package org.netbeans.modules.payara.tooling.server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.admin.CommandFetchLogData;
import org.netbeans.modules.payara.tooling.admin.ResultLog;
import org.netbeans.modules.payara.tooling.admin.ServerAdmin;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.utils.OsUtils;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Fetch Payara log from remote server.
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
     * Constructs an instance of Payara remote server log fetcher.
     * <p/>
     * Super class constructor will call <code>initInputStream</code> method
     * which initializes <code>InputStream</code> as
     * <code>PipedInputStream</code> before this constructor code is being
     * executed. Here we can simply connect already initialized
     * <code>PipedInputStream</code> with newly created
     * <code>PipedInputStream</code>.
     * <p/>
     * @param server Payara server for fetching server log.
     * @param skip   Skip to the end of the log file.
     */
    FetchLogRemote(final PayaraServer server, final boolean skip) {
        super(server, skip);
    }

    /**
     * Constructs an instance of Payara remote server log fetcher with
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
     * @param server   Payara server for fetching server log.
     * @param skip     Skip to the end of the log file.
     */
    FetchLogRemote(final ExecutorService executor, final PayaraServer server,
            final boolean skip) {
        super(executor, server, skip);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Runnable call() Method                                                  //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Remote server log lines reading task.
     * <p/>
     * Reads new log lines from server using Payara remote administration API
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

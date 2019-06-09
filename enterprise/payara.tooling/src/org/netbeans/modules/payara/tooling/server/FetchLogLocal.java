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

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Fetch Payara log from local server.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class FetchLogLocal extends FetchLogPiped {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(FetchLogLocal.class);

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara local server log fetcher.
     * <p/>
     * Super class constructor will call <code>initInputStream</code> method
     * which initializes <code>InputStream</code> as
     * <code>FileInputStream</code> before this constructor code is being
     * executed.
     * <p/>
     * @param server Payara server for fetching local server log. Both
     *               <code>getDomainsFolder</code>
     *               and <code>getDomainName</code> should not return null.
     * @param skip   Skip to the end of the log file.
     */
    FetchLogLocal(final PayaraServer server, final  boolean skip) {
        super(server, skip);
    }

    /**
     * Constructs an instance of Payara local server log fetcher with
     * external {@link ExecutorService}.
     * <p/>
     * Super class constructor will call <code>initInputStream</code> method
     * which initializes <code>InputStream</code> as
     * <code>FileInputStream</code> before this constructor code is being
     * executed.
     * <p/>
     * @param executor Executor service used to start task.
     * @param server   Payara server for fetching local server log. Both
     *                 <code>getDomainsFolder</code>
     *                 and <code>getDomainName</code> should not return null.
     * @param skip     Skip to the end of the log file.
     */
    FetchLogLocal(final ExecutorService executor, final PayaraServer server,
           final boolean skip) {
        super(executor, server, skip);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Initializes active log file <code>InputStream</code>
     * as <code>FileInputStream</code> sending data from local server
     * log file.
     * <p/>
     * @return <code>FileInputStream</code> where log lines from server
     *         active log file will be available to read.
     */
    private InputStream initInputFile() {
        final String METHOD = "initInputFile";
        File logFile = ServerUtils.getServerLogFile(server);
        InputStream log;
        try {
            log = new FileInputStream(logFile);
        } catch (FileNotFoundException fnfe) {
            LOGGER.log(Level.INFO, METHOD,
                    "fileNotFound", logFile.getAbsolutePath());
            return null;
        }
        if (skip) {
            int count;
            try {
                while ((count = log.available()) > 0) {
                    log.skip(count);
                }
            } catch (IOException ioe) {
                try {
                    log.close();
                } catch (IOException ioec) {
                    LOGGER.log(Level.INFO, METHOD, "cantClose", ioec);
                }
                throw new FetchLogException(
                        LOGGER.excMsg(METHOD, "cantInit"), ioe);
            }
        }
        return log;
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
        InputStream fIn = initInputFile();
        byte[] buff = new byte[PIPE_BUFFER_SIZE];
        File logFile = ServerUtils.getServerLogFile(server);
        int inCount;
        long lastModified;
        if (fIn == null) {
            return notifyListeners(TaskState.FAILED);
        }
        while (taksExecute) {
            try {
                inCount = fIn.available();
                lastModified = logFile.lastModified();
                // Nothing to read. Check log rotation after delay.
                if (inCount <= 0) {
                    Thread.sleep(LOG_REFRESH_DELAY);
                    inCount = fIn.available();
                    if (inCount <= 0 && logFile.lastModified() > lastModified) {
                        LOGGER.log(Level.FINER, METHOD, "rotation");
                        fIn.close();
                        fIn = initInputFile();
                    }
                }
                if (inCount > 0) {
                    while (inCount > 0) {
                        int count = fIn.read(buff);
                        LOGGER.log(Level.FINEST, METHOD, "read",
                                new Object[]{new Integer(count)});
                        if (count > 0) {
                            out.write(buff, 0, count);
                        }
                        inCount -= count;
                        if (inCount <= 0) {
                            inCount = fIn.available();
                        }
                    }
                    out.flush();
                }
            } catch (InterruptedException ie) {
                LOGGER.log(Level.INFO, METHOD, "interrupted", ie.getMessage());
                Thread.currentThread().interrupt();
                return notifyListeners(TaskState.COMPLETED);
            } catch (InterruptedIOException ie) {
                LOGGER.log(Level.INFO, METHOD,
                        "interruptedIO", ie.getMessage());
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

        }
        return notifyListeners(TaskState.COMPLETED);
    }

}

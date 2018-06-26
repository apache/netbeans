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

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.logging.Logger;
import org.netbeans.modules.glassfish.tooling.utils.ServerUtils;

/**
 * Fetch GlassFish log from local server.
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
     * Constructs an instance of GlassFish local server log fetcher.
     * <p/>
     * Super class constructor will call <code>initInputStream</code> method
     * which initializes <code>InputStream</code> as
     * <code>FileInputStream</code> before this constructor code is being
     * executed.
     * <p/>
     * @param server GlassFish server for fetching local server log. Both
     *               <code>getDomainsFolder</code>
     *               and <code>getDomainName</code> should not return null.
     * @param skip   Skip to the end of the log file.
     */
    FetchLogLocal(final GlassFishServer server, final  boolean skip) {
        super(server, skip);
    }

    /**
     * Constructs an instance of GlassFish local server log fetcher with
     * external {@link ExecutorService}.
     * <p/>
     * Super class constructor will call <code>initInputStream</code> method
     * which initializes <code>InputStream</code> as
     * <code>FileInputStream</code> before this constructor code is being
     * executed.
     * <p/>
     * @param executor Executor service used to start task.
     * @param server   GlassFish server for fetching local server log. Both
     *                 <code>getDomainsFolder</code>
     *                 and <code>getDomainName</code> should not return null.
     * @param skip     Skip to the end of the log file.
     */
    FetchLogLocal(final ExecutorService executor, final GlassFishServer server,
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

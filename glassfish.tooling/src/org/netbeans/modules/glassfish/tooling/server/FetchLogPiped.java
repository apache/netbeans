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
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.*;
import java.util.logging.Level;
import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.logging.Logger;
import org.netbeans.modules.glassfish.tooling.utils.LinkedList;
import org.netbeans.modules.glassfish.tooling.utils.NetUtils;

/**
 * Fetch GlassFish log from local or remote server.
 * <p/>
 * Data are fetched in service thread and passed into
 * <code>PipedOutputStream</code>.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class FetchLogPiped
        extends FetchLog implements Callable<TaskState> {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(FetchLogPiped.class);

    /** Size of internal buffer in pipe input stream. */
    static final int PIPE_BUFFER_SIZE = 8192;

    /** Log refresh delay in miliseconds. */
    static final int LOG_REFRESH_DELAY = 1000;
    
    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server log fetcher depending
     * on server being remote or local.
     * <p/>
     * Decision if server is local or remote depends on domains folder and
     * domain name attributes stored in <code>GlassFishServer</code> object.
     * <p/>
     * @param server GlassFish server for fetching server log.
     * @param skip   Skip to the end of the log file.
     * @return Newly created <code>FetchLog</code> instance.
     */
    public static FetchLogPiped create(final GlassFishServer server,
            final boolean skip) {
        boolean isLocal = server.getDomainsFolder() != null;
        FetchLogPiped fetchLog = isLocal
                ? new FetchLogLocal(server, skip)
                : new FetchLogRemote(server, skip);
        fetchLog.start();
        return fetchLog;
    }

    /**
     * Constructs an instance of GlassFish server log fetcher depending
     * on server being remote or local.
     * <p/>
     * Decision if server is local or remote depends on domains folder and
     * domain name attributes stored in <code>GlassFishServer</code> object.
     * Log file is passed whole as is without skipping to the end.
     * <p/>
     * @param server GlassFish server for fetching server log.
     * @return Newly created <code>FetchLog</code> instance.
     */
    public static FetchLogPiped create(final GlassFishServer server) {
        return create(server, false);
    }

    /**
     * Constructs an instance of GlassFish server log fetcher depending
     * on server being remote or local  with external {@link ExecutorService}.
     * <p/>
     * Decision if server is local or remote depends on domains folder and
     * domain name attributes stored in <code>GlassFishServer</code> object.
     * <p/>
     * @param executor Executor service used to start task.
     * @param server   GlassFish server for fetching server log.
     * @param skip     Skip to the end of the log file.
     * @return Newly created <code>FetchLog</code> instance.
     */
    public static FetchLogPiped create(final ExecutorService executor,
            final GlassFishServer server, final boolean skip) {
        boolean isLocal = server.getDomainsFolder() != null;
        FetchLogPiped fetchLog = isLocal
                ? new FetchLogLocal(executor, server, skip)
                : new FetchLogRemote(executor, server, skip);
        fetchLog.start();
        return fetchLog;
    }

    /**
     * Constructs an instance of GlassFish server log fetcher depending
     * on server being remote or local with external {@link ExecutorService}.
     * <p/>
     * Decision if server is local or remote depends on domains folder and
     * domain name attributes stored in <code>GlassFishServer</code> object.
     * Log file is passed whole as is without skipping to the end.
     * <p/>
     * @param executor Executor service used to start task.
     * @param server   GlassFish server for fetching server log.
     * @return Newly created <code>FetchLog</code> instance.
     */
    public static FetchLogPiped create(final ExecutorService executor,
            final GlassFishServer server) {
        return create(executor, server, false);
    }


    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Output stream where to write retrieved remote server log. */
    final PipedOutputStream out;

    /** Running task that reads log lines from remote server. */
    Future<TaskState> task;

    /** <code>ExecutorService</code> used to run read remote server log tasks. */
    private ExecutorService executor;

    /** Internal <code>ExecutorService</code> was used. */
    private final boolean internalExecutor;

    /** Indicate whether log lines reading task should continue or exit. */
    volatile boolean taksExecute;

    /** Listeners for state change events in GlassFish log fetcher. */
    private final LinkedList<FetchLogEventListener> eventListeners;

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
    FetchLogPiped(final GlassFishServer server, boolean skip) {
        super(server, skip);
        final String METHOD = "init";
        this.eventListeners = new LinkedList();
        try {
            out = new PipedOutputStream((PipedInputStream)this.in);
        } catch (IOException ioe) {
            super.close();
            throw new FetchLogException(LOGGER.excMsg(METHOD, "cantInit"), ioe);
        }
        taksExecute = true;
        // Create internal executor to run log reader task.
        executor = new ThreadPoolExecutor(0, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, FetchLogPiped.class.getName()
                        + server.getUrl());
                t.setDaemon(true);
                return t;
            }
        });
        internalExecutor = true;
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
    @SuppressWarnings("LeakingThisInConstructor")
    FetchLogPiped(final ExecutorService executor, final GlassFishServer server,
            boolean skip) {
        super(server, skip);
        final String METHOD = "init";
        this.eventListeners = new LinkedList();
        try {
            out = new PipedOutputStream((PipedInputStream)this.in);
        } catch (IOException ioe) {
            super.close();
            throw new FetchLogException(LOGGER.excMsg(METHOD, "cantInit"), ioe);
        }
        taksExecute = true;
        // Use external executor to run log reader task.
        this.executor = executor;
        internalExecutor = false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructor callback which initializes log <code>InputStream</code>
     * as <code>PipedInputStream</code> sending data from remote server
     * log reader.
     * <p/>
     * This initialization is called form <code>FetchLog</code> super class
     * constructor. It already exists when <code>FetchLogRemote</code>
     * constructor is running so it may be used as argument for local 
     * <code>PipedOutputStream</code> initialization.
     * <p/>
     * @return <code>PipedInputStream</code> where log lines received from server
     *         will be available to read.
     */
    @Override
    InputStream initInputStream() {
        return new PipedInputStream(PIPE_BUFFER_SIZE);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Add GlassFish log fetcher state change listener at the end
     * of listeners list.
     * <p/>
     * @param listener Listener for state change events in GlassFish log fetcher
     *                 to be added. Value shall not be <code>null</code>.
     * @throws FetchLogException When <code>listener</code> parameter
     *                           is <code>null</code>.
     */
    public final void addListener(final FetchLogEventListener listener)
            throws FetchLogException {
        final String METHOD = "addListener";
        if (listener == null) {
            throw new FetchLogException(LOGGER.excMsg(METHOD, "listenerNull"));
        }
        synchronized(eventListeners) {
            eventListeners.addLast(listener);
        }
    }

    /**
     * Remove all occurrences of log fetcher state change listener
     * from listeners list.
     * <p/>
     * @param listener Listener for state change events in GlassFish log fetcher
     *                 to be removed. Value shall not be <code>null</code>.
     * @return Value of <code>true</code> when at least one listener was removed
     *         or <code>false</code> otherwise.
     * @throws FetchLogException When <code>listener</code> parameter
     *                           is <code>null</code>.
     */
    public final boolean removeListener(final FetchLogEventListener listener)
            throws FetchLogException {
        final String METHOD = "removeListener";
        if (listener == null) {
            throw new FetchLogException(LOGGER.excMsg(METHOD, "listenerNull"));
        }
        boolean removed = false;
        synchronized(eventListeners) {
            boolean isElement = !eventListeners.isEmpty();
            eventListeners.first();
            while (isElement) {
                if (listener.equals(eventListeners.getCurrent())) {
                    isElement = eventListeners.isNext();
                    eventListeners.removeAndNextOrPrevious();
                    removed = true;
                } else {
                    isElement = eventListeners.next();
                }
            }
        }
        return removed;
    }

    /**
     * Notify all GlassFish log fetcher state change listeners about state
     * change event.
     * <p/>
     * @param state Current GlassFish log fetcher state.
     * @return Current GlassFish log fetcher state.
     */
    final TaskState notifyListeners(final TaskState state) {
        if (!eventListeners.isEmpty()) {
            synchronized (eventListeners) {
                boolean isElement = !eventListeners.isEmpty();
                if (isElement) {
                    FetchLogEvent event = new FetchLogEvent(state);
                    eventListeners.first();
                    while (isElement) {
                        eventListeners.getCurrent().stateChanged(event);
                        isElement = eventListeners.next();
                    }
                }
            }
        }
        return state;
    }

    /**
     * Start task.
     */
    private void start() {
        task = executor.submit(this);
        notifyListeners(TaskState.READY);
    }

    /**
     * Stop running task if it's still running.
     * <p/>
     * @return Task execution result.
     */
    private TaskState stop() {
        final String METHOD = "stop";
        taksExecute = false;
        if (this.out != null) {
            try {
                this.out.close();
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, METHOD, "cantClose", ioe);
            }
        } else {
            LOGGER.log(Level.INFO, METHOD, "isNull");
        }
        TaskState result;        
        try {
            result = task.get();
        } catch (InterruptedException ie) {
            throw new FetchLogException(
                    LOGGER.excMsg(METHOD, "interrupted"), ie);
        } catch (ExecutionException ee) {
              throw new FetchLogException(
                      LOGGER.excMsg(METHOD, "exception"), ee);
        } catch (CancellationException ce) {
            throw new FetchLogException(
                    LOGGER.excMsg(METHOD, "cancelled"), ce);
        }
        return result;
    }

    /**
     * Stop log lines reading task and close input and output streams used
     * to access log lines received from server.
     */
    @Override
    public void close() {
        final String METHOD = "close";
        TaskState result = stop();        
        super.close();
        // Clean up internal executor.
        if (internalExecutor) {
            executor.shutdownNow();
        }
        // We may possibly change this to throw an exception when needed.
        // But streams must be cleaned up first.
        if (result != TaskState.COMPLETED) {
            LOGGER.log(Level.INFO, METHOD, "failed");
        }
    }

    /**
     * Check if log lines reading task is running.
     * <p/>
     * @return Returns <code>true</code> when task is still running
     *         or <code>false></code> otherwise.
     */
    public boolean isRunning() {
        return !task.isDone();
    }
}

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
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.*;
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.utils.LinkedList;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Fetch Payara log from local or remote server.
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
     * Constructs an instance of Payara server log fetcher depending
     * on server being remote or local.
     * <p/>
     * Decision if server is local or remote depends on domains folder and
     * domain name attributes stored in <code>PayaraServer</code> object.
     * <p/>
     * @param server Payara server for fetching server log.
     * @param skip   Skip to the end of the log file.
     * @return Newly created <code>FetchLog</code> instance.
     */
    public static FetchLogPiped create(final PayaraServer server,
            final boolean skip) {
        boolean isLocal = server.getDomainsFolder() != null;
        FetchLogPiped fetchLog = isLocal
                ? new FetchLogLocal(server, skip)
                : new FetchLogRemote(server, skip);
        fetchLog.start();
        return fetchLog;
    }

    /**
     * Constructs an instance of Payara server log fetcher depending
     * on server being remote or local.
     * <p/>
     * Decision if server is local or remote depends on domains folder and
     * domain name attributes stored in <code>PayaraServer</code> object.
     * Log file is passed whole as is without skipping to the end.
     * <p/>
     * @param server Payara server for fetching server log.
     * @return Newly created <code>FetchLog</code> instance.
     */
    public static FetchLogPiped create(final PayaraServer server) {
        return create(server, false);
    }

    /**
     * Constructs an instance of Payara server log fetcher depending
     * on server being remote or local  with external {@link ExecutorService}.
     * <p/>
     * Decision if server is local or remote depends on domains folder and
     * domain name attributes stored in <code>PayaraServer</code> object.
     * <p/>
     * @param executor Executor service used to start task.
     * @param server   Payara server for fetching server log.
     * @param skip     Skip to the end of the log file.
     * @return Newly created <code>FetchLog</code> instance.
     */
    public static FetchLogPiped create(final ExecutorService executor,
            final PayaraServer server, final boolean skip) {
        boolean isLocal = server.getDomainsFolder() != null;
        FetchLogPiped fetchLog = isLocal
                ? new FetchLogLocal(executor, server, skip)
                : new FetchLogRemote(executor, server, skip);
        fetchLog.start();
        return fetchLog;
    }

    /**
     * Constructs an instance of Payara server log fetcher depending
     * on server being remote or local with external {@link ExecutorService}.
     * <p/>
     * Decision if server is local or remote depends on domains folder and
     * domain name attributes stored in <code>PayaraServer</code> object.
     * Log file is passed whole as is without skipping to the end.
     * <p/>
     * @param executor Executor service used to start task.
     * @param server   Payara server for fetching server log.
     * @return Newly created <code>FetchLog</code> instance.
     */
    public static FetchLogPiped create(final ExecutorService executor,
            final PayaraServer server) {
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

    /** Listeners for state change events in Payara log fetcher. */
    private final LinkedList<FetchLogEventListener> eventListeners;

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
    FetchLogPiped(final PayaraServer server, boolean skip) {
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
    @SuppressWarnings("LeakingThisInConstructor")
    FetchLogPiped(final ExecutorService executor, final PayaraServer server,
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
     * Add Payara log fetcher state change listener at the end
     * of listeners list.
     * <p/>
     * @param listener Listener for state change events in Payara log fetcher
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
     * @param listener Listener for state change events in Payara log fetcher
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
     * Notify all Payara log fetcher state change listeners about state
     * change event.
     * <p/>
     * @param state Current Payara log fetcher state.
     * @return Current Payara log fetcher state.
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

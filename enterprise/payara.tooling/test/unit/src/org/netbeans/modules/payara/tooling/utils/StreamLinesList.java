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
package org.netbeans.modules.payara.tooling.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.server.FetchLog;

/**
 * Read data from stream and store them as linked list of lines.
 * <p/>
 * Allows to read Payara server log from process <code>stdout</code>
 * and/or <code>stderr</code> after server is started.
 * Log lines are stored into internal linked list and can be retrieved by test
 * to analyze them.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class StreamLinesList {

    ////////////////////////////////////////////////////////////////////////////
    // Internal classes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Internal thread used to read lines from provided stream and store them
     * into list.
     */
    private static class LinesReader implements Runnable {

        ////////////////////////////////////////////////////////////////////////
        // Class attributes                                                   //
        ////////////////////////////////////////////////////////////////////////

        /** Logger instance for this class. */
        private static final Logger LOGGER = new Logger(LinesReader.class);

        ////////////////////////////////////////////////////////////////////////
        // Instance attributes                                                //
        ////////////////////////////////////////////////////////////////////////

        /** Stream from which data should be read. */
        private final BufferedReader in;

        /** List containing lines read from stream. */
        private final LinkedList<String> lines;

        /** Store IOException if there is a problem with reading stream. */
        IOException exception;

        /** Trigger reader thread to stop execution. */
        private boolean exit;

        /** Stream was already closed notification. */
        private boolean closedStream;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Constructs an instance of stream reader class.
         * <p/>
         * @param in    Stream from which data should be read.
         * @param lines List where to store lines read from stream.
         */
        LinesReader(InputStream in, LinkedList<String> lines) {
            this.in = new BufferedReader(new InputStreamReader(in));
            this.lines = lines;
            this.exception = null;
            this.exit = false;
            this.closedStream = false;
        }

        ////////////////////////////////////////////////////////////////////////
        // Methods                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Reads lines from stream and stores them into <code>List</code>
         * provided in constructor.
         * <p/>
         * Lines are added with <code>List</code> instance lock because this
         * producer thread is different one than consumer thread.
         * Will not close stream on exit.
         */
        @Override
        public void run() {
            final String METHOD = "run";
            String line;
            try {
                while (!exit && (line = in.readLine()) != null) {
                    if (line.length() > 0) synchronized(lines) {
                        lines.addLast(line);
                    }
                }
            } catch (IOException ioe) {
                exception = ioe;
                LOGGER.log(Level.WARNING, METHOD,
                        "exception", ioe.getMessage());
                close();
            }
        }

        /**
         * Request this thread to stop execution (exit <code>run()</code>
         * method).
         */
        void stop() {
            this.exit = true;
        }

        /**
         * Clean internal structures to free memory.
         * <p/>
         * Closes internal <code>BufferedReader</code> containing stream
         * provided by constructor.
         */
        void close() {
            // Closing the stream should be atomic to avoid multiple calls.
            synchronized (in) {
                if (!closedStream) {
                    try {
                        in.close();
                        closedStream = true;
                    } catch (IOException ioe) {
                        this.exception = ioe;
                    }
                }
            }
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** List containing lines read from stream. */
    private final LinkedList<String> lines;

    /** Internal thread used to read lines from provided stream and store them
     *  into list. */
    private LinesReader linesReader;

    /** Lines reader thread holder. */
    private Thread linesReaderThread;

    /** 
    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of log lines storage.
     * <p/>
     * @param in Stream from which server log data should be read.
     */
    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public StreamLinesList(InputStream in) {
        this.lines = new LinkedList<String>();
        this.linesReader = new LinesReader(in, lines);
        this.linesReaderThread = new Thread(this.linesReader);
        this.linesReaderThread.start();
    }

    /**
     * Constructs an instance of log lines storage.
     * <p/>
     * @param in Log fetcher from which server log data should be read.
     */
    public StreamLinesList(FetchLog fetchLog) {
        this(fetchLog.getInputStream());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Check if there is next line in the incoming log lines.
     * <p/>
     * @return <code>true</code> if there is next line in the incoming log lines
     *         or <code>false</code> otherwise.
     */
    public boolean hasNext() {
        synchronized(lines) {
            return lines.isNext();
        }
    }

    /**
     * Move current log line to the next one in incoming log lines.
     * <p/>
     * @return <code>true</code> if current log line was moved to next one
     *         in incoming log lines or <code>false</code> if no next line
     *         is available.
     */
    public boolean next() {
        synchronized(lines) {
            return lines.next();
        }
    }

    /**
     * Get current log line from incoming log lines.
     * <p/>
     * @return Current log line from incoming log lines or <code>null</code>
     *         if no incoming log lines are available.
     */
    public String get() {
        synchronized(lines) {
            return lines.getCurrent();
        }
    }

    /**
     * Attempt to get next log line from incoming log lines.
     * <p/>
     * @return Next log line from incoming log lines or <code>null</code>
     *         if current log line is the last one available or there are
     *         no incoming log lines available.
     */
    public String getNext() {
        synchronized(lines) {
            if (lines.next()) {
                return lines.getCurrent();
            } else {
                return null;
            }
        }
    }

    /**
     * Check if there are at least some lines stored from log.
     * <p/>
     * @return <code>true</code> when list is empty (contains no elements)
     *         or <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return lines.isEmpty();
    }

    /**
     * Get string representation of all stored log lines.
     * </P>
     * @return <code>String</code> representation of all lines currently stored.
     */
    @Override
    public String toString() {
        return lines.toString();    }

    /**
     * Stop internal thread used to read lines from provided stream and clean
     * internal structures to help freeing memory.
     */
    public void close() {
        if (linesReader != null) {
            try {
                linesReader.stop();
                linesReader.close();
                linesReaderThread.join();
            } catch (InterruptedException ex) {
                // Thread won't be interrupted.
            }
        }
    }
}

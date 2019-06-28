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
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Fetch Payara log from server.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class FetchLog {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(FetchLog.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara server for fetching server log. */
    PayaraServer server;

    /** Input stream which will provide access to log retrieved from server. */
    final InputStream in;

    /** Request to skip to the end of log. */
    final boolean skip;

    ////////////////////////////////////////////////////////////////////////////
    // Abstract methods                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructor callback which will initialize log <code>InputStream</code>.
     * <p/>
     * @return <code>InputStream</code> where log lines received from server
     *         will be available to read.
     */
    abstract InputStream initInputStream();

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an empty instance of Payara server log fetcher using
     * provided input stream.
     * <p/>
     * <code>InputStream</code> is set using constructor argument. Child class
     * <code>initInputStream</code> method is ignored.
     * <p/>
     * @param in     Input stream used to read server log.
     * @param skip   Skip to the end of the log file.
     */
    FetchLog(InputStream in, boolean skip) {
        this.server = null;
        this.in = in;
        this.skip = skip;
    }

    /**
     * Constructs an instance of Payara server log fetcher.
     * <p/>
     * <code>InputStream</code> is set using child
     * <code>initInputStream</code> method.
     * <p/>
     * @param server Payara server for fetching server log.
     * @param skip   Skip to the end of the log file.
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    FetchLog(PayaraServer server, boolean skip) {
        this.server = server;
        this.in = initInputStream();
        this.skip = skip;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get input stream for reading lines from server log file.
     * <p/>
     * @return Input stream for reading lines from server log file.
     */
    public InputStream getInputStream() {
        return this.in;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Close input stream used to access log lines received from server.
     * <p/>
     * This should be overridden in child classes to handle all streams and
     * threads properly.
     */
    public void close() {
        final String METHOD = "close";
        if (this.in != null) {
            try {
                this.in.close();
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, METHOD, "cantClose", ioe);
            }
        } else {
            LOGGER.log(Level.INFO, METHOD, "isNull");
        }
    }

}

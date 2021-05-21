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

import java.io.InputStream;

/**
 * Fetch Payara log from provided stream.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class FetchLogSimple extends FetchLog {

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server log fetcher using provided
     * stream.
     * <p/>
     * Super class constructor will not call <code>initInputStream</code> method
     * so this method should be ignored.
     * Old log lines are never skipped so whole log is always available in
     * <code>InputStream</code>
     * <p/>
     * @param in Input stream to access server log.
     */
    public FetchLogSimple(InputStream in) {
        super(in, false);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructor callback makes no sense in this child class.
     * <p/>
     * This method throws an exception when called.
     * <p/>
     * @return <code>FileInputStream</code> where log lines received from server
     *         will be available to read.
     */
    @Override
    InputStream initInputStream() {
        throw new UnsupportedOperationException(
                "Method initInputStream should not be called in " +
                "FetchLogSimple class!");
    }
}

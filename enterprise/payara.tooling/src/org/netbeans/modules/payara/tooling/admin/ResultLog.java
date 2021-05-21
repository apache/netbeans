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
package org.netbeans.modules.payara.tooling.admin;

/**
 * Payara Administratio Command Result containing server log
 * as <code>List&ltString&gt</code> values.
 * <p/>
 * Stores administration command result values and command execution state.
 * Result value is <code>List&ltString&gt</code> with individual log lines</li>.
 * <code>String</code> with <code>X-Text-Append-Next</code> response URL
 * parameters is also stored.</li>
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ResultLog extends Result<ValueLog> {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Server log value returned by admin command execution. */
    ValueLog value;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara admin command result
     * for <code>List&ltString&gt</code> result value containing server log.
     */
    ResultLog() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get server log value returned by admin command execution.
     * <p/>
     * @return Server log value returned by admin command execution.
     */
    @Override
    public ValueLog getValue() {
        return value;
    }
    
}

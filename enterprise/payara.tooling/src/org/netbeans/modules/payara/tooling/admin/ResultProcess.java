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
 * Payara Admin Command Result containing process execution result values.
 * <p/>
 * Stores admin command result values and command execution state.
 * Result value is set of values describing process execution.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ResultProcess extends Result<ValueProcess> {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Value returned by admin command execution. */
    ValueProcess value;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara admin command result for
     * <code>ValueProcess</code> result value.
     */
    ResultProcess() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get value returned by admin command execution.
     * <p/>
     * @return Value returned by admin command execution.
     */
    @Override
    public ValueProcess getValue() {
        return value;
    }
    
}

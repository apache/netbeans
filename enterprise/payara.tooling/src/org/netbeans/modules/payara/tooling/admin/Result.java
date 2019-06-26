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

import org.netbeans.modules.payara.tooling.TaskState;

/**
 * Payara administration command result.
 * <p/>
 * Stores administration command result values and command execution state.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class Result<T> {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** State of Payara server administration command execution. */
    TaskState state;

    /** Authorization status.
     *  <p/>
     *  Value of <code>true</code> means that there was no authorization issue.
     *  Value of <code>false</code> means that authorization failed. */
    boolean auth;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara administration command result.
     */
    Result() {
        this.state = null;
        this.auth = true;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get state of Payara server administration command execution.
     * <p/>
     * @return State of Payara server administration command execution.
     */
    public TaskState getState() {
        return state;
    }

    /**
     * Get value returned by administration command execution.
     * <p/>
     * @return Value returned by administration command execution.
     */
    public abstract T getValue();


    /**
     * Get administration command execution authorization status.
     * <p/>
     * @return Value of <code>true</code> means that there was no authorization
     *         issue. Value of <code>false</code> means that authorization
     *         failed.
     */
    public boolean isAuth() {
        return auth;
    }

    /**
     * Set administration command execution authorization status.
     * <p/>
     * Use only in administration command runners to set result value.
     * <p/>
     * @param auth Authorization status: Value of <code>true</code> means that
     *             there was no authorization issue. Value of <code>false</code>
     *             means that authorization failed.
     */
    public void setAuth(final boolean auth) {
        this.auth = auth;
    }

}

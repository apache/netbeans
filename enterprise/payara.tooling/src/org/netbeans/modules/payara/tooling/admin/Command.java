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
 * Payara server administration command entity.
 * <p/>
 * Holds common data for administration command.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class Command {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Server command represented by this object. */
    protected String command;

    /** Indicate whether we shall retry command execution. */
    protected boolean retry = false;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server administration command entity
     * with specified server command.
     * <p/>
     * @param command Server command represented by this object.
     */
    protected Command(final String command) {
        this.command = command;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns server command represented by this object.  Set in constructor.
     * e.g. "deploy", "list-applications", etc.
     * <p/>
     * @return command string represented by this object.
     */
    public String getCommand() {
        return command;
    }

    // This is also kind of getter.
    /**
     * Sometimes (e.g. during startup), the server does not accept commands.  In
     * such cases, it will block for 20 seconds and then return with the message
     * "V3 cannot process this command at this time, please wait".
     * <p/>
     * In such cases, we set a flag and have the option to reissue the command.
     * <p/>
     * @return true if server responded with it's "please wait" message.
     */
    public boolean retry() {
        return retry;
    }

}

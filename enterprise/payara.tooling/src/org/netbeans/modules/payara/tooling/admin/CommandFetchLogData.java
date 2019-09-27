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
 * Payara Server View Log Command Entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerRestFetchLogData.class)
@RunnerRestClass(runner=RunnerRestFetchLogData.class)
public class CommandFetchLogData extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for view log command. */
    private static final String COMMAND = "view-log";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Query parameters to be used to read only log entries added in particular
     * interval starting from previous call that returned this value of
     * <code>paramsAppendNext</code> stored in returned <code>ValueLog</code>.
     * <p/>
     * Content of HTTP header <code>X-Text-Append-Next</code>.
     */
    final String paramsAppendNext;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server view log command entity.
     * <p/>
     * All existing log entries will be returned.
     */
    public CommandFetchLogData() {
        super(COMMAND);
        this.paramsAppendNext = null;
    }

    /**
     * Constructs an instance of Payara server view log command entity.
     * <p/>
     * Only log entries added in particular interval starting from previous
     * call that returned this value of <code>paramsAppendNext</code> will
     * be returned.
     * <p/>
     * @param paramsAppendNext Interval query parameters from Last View Log
     *        command execution.
     */
    public CommandFetchLogData(String paramsAppendNext) {
        super(COMMAND);
        this.paramsAppendNext = paramsAppendNext;
    }

}

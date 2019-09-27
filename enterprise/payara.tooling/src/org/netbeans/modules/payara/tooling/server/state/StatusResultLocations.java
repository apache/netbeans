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
package org.netbeans.modules.payara.tooling.server.state;

import org.netbeans.modules.payara.tooling.admin.ResultMap;
import org.netbeans.modules.payara.tooling.data.PayaraStatusCheckResult;
import org.netbeans.modules.payara.tooling.TaskEvent;

/**
 * Server status task execution result for <code>__locations</code> command
 * including additional information.
 * <p/>
 * This class stores task execution result only. Value <code>SUCCESS</code>
 * means that Locations command task execution finished successfully but it
 * does not mean that administration command itself returned with
 * <code>COMPLETED</code> status.
 * When <code>SUCCESS</code> status is set, stored <code>result</code> value
 * shall be examined too to see real administration command execution result.
 * <p/>
 * @author Tomas Kraus
 */
class StatusResultLocations extends StatusResult {

    ////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                //
    ////////////////////////////////////////////////////////////////////////

    /** Command <code>__locations</code> execution result. */
    final ResultMap<String, String> result;

    ////////////////////////////////////////////////////////////////////////
    // Constructors                                                       //
    ////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of individual server status result
     * for <code>__locations</code> command.
     * <p/>
     * Command <code>__locations</code> result is stored.
     * <p/>
     * @param result       Command <code>__locations</code> execution result.
     * @param status       Individual server status returned.
     * @param failureEvent Failure cause.
     */
    StatusResultLocations(final ResultMap<String, String> result,
            final PayaraStatusCheckResult status,
            final TaskEvent failureEvent) {
        super(status, failureEvent);
        this.result = result;
    }

    ////////////////////////////////////////////////////////////////////////
    // Getters                                                            //
    ////////////////////////////////////////////////////////////////////////

    /**
     * Get <code>__locations</code> command execution result.
     * <p/>
     * @return <code>__locations</code> command execution result.
     */
    public ResultMap<String, String> getStatusResult() {
        return result;
    }

}

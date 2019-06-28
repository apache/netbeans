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

import org.netbeans.modules.payara.tooling.data.PayaraStatusCheckResult;
import org.netbeans.modules.payara.tooling.TaskEvent;

/**
 * Individual server status result including additional information.
 * <p/>
 * @author tomas Kraus
 */
class StatusResult {

    ////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                //
    ////////////////////////////////////////////////////////////////////////

    /** Individual server status returned. */
    final PayaraStatusCheckResult status;

    /** Task failure event. */
    final TaskEvent event;

    ////////////////////////////////////////////////////////////////////////
    // Constructors                                                       //
    ////////////////////////////////////////////////////////////////////////
    /**
     * Creates an instance of individual server status result.
     * <p/>
     * @param status Individual server status returned.
     * @param event  Current status cause.
     */
    StatusResult(final PayaraStatusCheckResult status,
            final TaskEvent event) {
        this.status = status;
        this.event = event;
    }

    /**
     * Creates an instance of individual server status result.
     * <p/>
     * @param status Individual server status returned.
     */
    StatusResult(final PayaraStatusCheckResult status) {
        this(status, null);
    }

    ////////////////////////////////////////////////////////////////////////
    // Getters                                                            //
    ////////////////////////////////////////////////////////////////////////

    /**
     * Get individual check task status.
     * <p/>
     * @return Individual check task status.
     */
    public PayaraStatusCheckResult getStatus() {
        return status;
    }

    /**
     * Get task failure event.
     * <p/>
     * @return Task failure event.
     */
    public TaskEvent getEvent() {
        return event;
    }
    
}

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
package org.netbeans.modules.payara.tooling.data;

import org.netbeans.modules.payara.tooling.TaskEvent;

/**
 * Payara server status check task details.
 * <p/>
 * Provides access to server status check task details in status listener
 * callback methods.
 * <p/>
 * @author Tomas Kraus
 */
public interface PayaraStatusTask {

    /**
     * Get server status check type.
     * <p/>
     * @return Server status check type.
     */
    public PayaraStatusCheck getType();

    /**
     * Get last command task execution status.
     * <p/>
     * @return Last command task execution status.
     */
    public PayaraStatusCheckResult getStatus();

    /**
     * Get last command task execution status.
     * <p/>
     * @return Last command task execution status.
     */
    public TaskEvent getEvent();

}

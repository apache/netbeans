/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.nativeexecution.api.util;

import java.util.EventListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * Listens connect events
 * @author Vladimir Kvashin
 */
public interface ConnectionListener extends EventListener {
    /**
     * Is called each time connection occurs
     * @param env specifies the host that has been connected
     */
    void connected(ExecutionEnvironment env);

    /**
     * Reserved for future use.
     * Is supposed to be called each time host is disconnected
     * @param env specifies the host that is disconnected
     */
    void disconnected(ExecutionEnvironment env);
}

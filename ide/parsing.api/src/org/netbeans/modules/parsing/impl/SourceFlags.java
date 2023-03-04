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

package org.netbeans.modules.parsing.impl;

import org.netbeans.modules.parsing.api.Source;

/**
 * Represents a state of {@link Source}
 * @author Tomas Zezula
 */
public enum SourceFlags {
    /**
     * The {@link Source} is invalid
     */
    INVALID,
    
    /**
     * The {@link Source} expects change(s)
     */
    CHANGE_EXPECTED,
    
    /**
     * The {@link ParserResultTask}s on this source should be rescheduled
     */
    RESCHEDULE_FINISHED_TASKS
    
}

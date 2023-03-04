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

package org.netbeans.modules.parsing.implspi;

import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.Scheduler;

/**
 * Allows the environment implementation to reschedule tasks for a
 * source. The instance is passed to {@link SourceEnvironment#attachScheduler}.
 * @author sdedic
 * @since 9.2
 */
public interface SchedulerControl {
    /**
     * Provides access to the controlled Scheduler
     * @return the scheduler instance
     */
    public Scheduler getScheduler();
    
    /**
     * Notifies about Source change. Should be called when the scheduler
     * should reschedule source's tasks or when the Source itself changes, i.e.
     * file is moved/renamed etc.
     * 
     * @param newSource the current Source for the Scheduler
     */
    public void sourceChanged(Source newSource);
}

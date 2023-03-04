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

package org.netbeans.lib.profiler.common.event;

import org.netbeans.lib.profiler.common.Profiler;


/**
 * An event describing profiling state change.
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
public final class ProfilingStateEvent {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final int newState; // new profiling state
    private final int oldState; // old profiling state
    private Profiler source; // profiler where the profiling state change originate

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of ProfilingStateEvent */
    public ProfilingStateEvent(final int oldState, final int newState, final Profiler source) {
        this.oldState = oldState;
        this.newState = newState;
        this.source = source;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int getNewState() {
        return newState;
    }

    public int getOldState() {
        return oldState;
    }

    public Profiler getSource() {
        return source;
    }
}

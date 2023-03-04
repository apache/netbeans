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

package org.netbeans.lib.profiler.results;

import org.netbeans.lib.profiler.ProfilerClient;


/**
 *
 * @author Jaroslav Bachorik
 */
public interface ProfilingResultsProvider {
    //~ Inner Interfaces ---------------------------------------------------------------------------------------------------------

    public interface Dispatcher {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        /**
         * Called when a new pack of data is received from the server
         * @param buffer The data received. MUST be treated as IMMUTABLE
         */
        void dataFrameReceived(final byte[] buffer, final int instrumentationType);

        void pause(boolean flush);

        void reset();

        void resume();

        void shutdown();

        void startup(ProfilerClient client);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    void addDispatcher(ProfilingResultsProvider.Dispatcher dispatcher);

    void dataReady(final byte[] buffer, int instrumentationType);

    void removeDispatcher(ProfilingResultsProvider.Dispatcher dispatcher);

    void shutdown();

    void startup(ProfilerClient client);
}

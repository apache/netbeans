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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Jaroslav Bachorik
 */
public class EventBufferResultsProvider implements ProfilingResultsProvider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final Logger LOGGER = Logger.getLogger(EventBufferResultsProvider.class.getName());
    private static final EventBufferResultsProvider instance = new EventBufferResultsProvider();

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final Set listeners = Collections.synchronizedSet(new HashSet());

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of RawProfilingResultsCollector */
    private EventBufferResultsProvider() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static EventBufferResultsProvider getDefault() {
        return instance;
    }

    public void addDispatcher(ProfilingResultsProvider.Dispatcher dispatcher) {
        listeners.add(dispatcher);
    }

    public void dataReady(final byte[] buf, int instrumentationType) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Profiling data ready "+buf.length); // NOI18N
        }
        fireProcessData(buf, instrumentationType);
    }

    public void removeDispatcher(ProfilingResultsProvider.Dispatcher dispatcher) {
        listeners.remove(dispatcher);
    }

    public void shutdown() {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Shutting down profiler"); // NOI18N
        }

        fireShutdown();
    }

    public void startup(ProfilerClient client) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Starting up profiler"); // NOI18N
        }

        fireStartup(client);
    }

    private void fireProcessData(final byte[] data, final int instrumentationType) {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            ProfilingResultsProvider.Dispatcher dispatcher = (ProfilingResultsProvider.Dispatcher) iter.next();
            dispatcher.dataFrameReceived(data, instrumentationType);
        }
    }

    private void fireShutdown() {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            ProfilingResultsProvider.Dispatcher dispatcher = (ProfilingResultsProvider.Dispatcher) iter.next();
            dispatcher.shutdown();
        }
    }

    private void fireStartup(ProfilerClient client) {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            ProfilingResultsProvider.Dispatcher dispatcher = (ProfilingResultsProvider.Dispatcher) iter.next();
            dispatcher.startup(client);
        }
    }
}

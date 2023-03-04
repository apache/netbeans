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

package org.netbeans.lib.profiler.server;

import org.netbeans.lib.profiler.server.system.Timers;
import java.lang.reflect.Method;
import java.util.Arrays;


/**
 * Base class for custom profiling point server handlers. Default implementation just writes timestamped
 * event to the event buffer to be later processed by the client executor. Handlers are expected to be
 * singletons accessible via static getInstance() method.
 *
 * @author Tomas Hurka
 * @author Maros Sandor
 */
public class ProfilingPointServerHandler {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    private static ProfilingPointServerHandler[] profilingPointHandlers;
    private static int[] profilingPointIDs;
    private static ProfilingPointServerHandler instance;

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    public static synchronized ProfilingPointServerHandler getInstance(String clientInfo) {
        if (instance == null) {
            instance = new ProfilingPointServerHandler();
        }

        return instance;
    }
    
    public static synchronized void initInstances(int[] ppIDs, String[] handlerClassNames, String[] handlersInfo) {
        profilingPointIDs = ppIDs;
        profilingPointHandlers = getInstances(handlerClassNames, handlersInfo);
    }

    public static ProfilingPointServerHandler getHandler(char handlerId) {
        int idx = Arrays.binarySearch(profilingPointIDs, handlerId);
        if (idx >= 0) {
            return profilingPointHandlers[idx];
        }
        return null;
    }
    
    private static ProfilingPointServerHandler[] getInstances(String[] handlerClassNames, String[] handlersInfo) {
        ProfilingPointServerHandler[] handlers = new ProfilingPointServerHandler[handlerClassNames.length];

        for (int i = 0; i < handlerClassNames.length; i++) {
            try {
                Method method = Class.forName(handlerClassNames[i]).getMethod("getInstance", new Class[] { String.class }); //NOI18N
                handlers[i] = (ProfilingPointServerHandler) method.invoke(null, new Object[] { handlersInfo[i] });
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }

        return handlers;
    }

    /**
     * Invoked by the JFluid server when the profiling point hits.
     *
     * @param id unique ID of the profiling point
     */
    public void profilingPointHit(int id) {
        long absTimeStamp = Timers.getCurrentTimeInCounts();
        profilingPointHit(id, absTimeStamp);
    }

    /**
     * Invoked by the JFluid server when the profiling point hits.
     *
     * @param id unique ID of the profiling point
     */
    public void profilingPointHit(int id, long absTimeStamp) {
        ProfilerRuntime.writeProfilingPointHitEvent(id, absTimeStamp);
    }
}

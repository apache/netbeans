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

package org.netbeans.modules.profiler.snaptracer;

import org.netbeans.modules.profiler.snaptracer.impl.IdeSnapshot;


/**
 * ProbeStateHandler interface allows an implementing TracerProbe to be
 * notified about Tracer session state. See TracerProbe.getStateHandler().
 *
 * @author Jiri Sedlacek
 */
public interface ProbeStateHandler {

    /**
     * Invoked when the is added into the Timeline view.
     *
     * @param snapshot profiler snapshot
     */
    public void probeAdded(IdeSnapshot snapshot);

    /**
     * Invoked when the probe is removed from the Timeline view.
     *
     * @param snapshot profiler snapshot
     */
    public void probeRemoved(IdeSnapshot snapshot);


    /**
     * Invoked when setting up a new Tracer session. This method allows a
     * Probe to notify the user about initialization progress. The actual
     * initialization (and updating the TracerProgressObject) should be
     * performed in the sessionStarting() method. Useful for example for
     * messaging a delay during instrumention of classes in target application.
     *
     * @param snapshot profiler snapshot
     * @param refresh session refresh rate in miliseconds
     * @return TracerProgressObject to track initialization progress
     */
    public TracerProgressObject sessionInitializing(IdeSnapshot snapshot, int refresh);

    /**
     * Invoked when starting a new Tracer session. Any probe initialization
     * should be performed in this method. If provided by the
     * sessionInitializing method, a TracerProgressObject should be updated to
     * reflect the initialization progress. This method may throw a
     * SessionInitializationException in case of initialization failure. Any
     * packages/probes initialized so far will be correctly finished, however the
     * probe throwing the SessionInitializationException is responsible for
     * cleaning up any used resources and restoring its state without any
     * following events.
     *
     * @param snapshot profiler snapshot
     * @throws SessionInitializationException in case of initialization failure
     */
    public void sessionStarting(IdeSnapshot snapshot)
            throws SessionInitializationException;

    /**
     * Invoked when all packages/probes have been started and the Tracer session
     * is running and collecting data.
     *
     * @param snapshot profiler snapshot
     */
    public void sessionRunning(IdeSnapshot snapshot);

    /**
     * Invoked when stopping the Tracer session. Any probe cleanup should be
     * performed in this method. Any long-running cleanup code should preferably
     * be invoked in a separate worker thread to allow the Tracer session to
     * finish as fast as possible. Be sure to check/wait for the cleanup thread
     * when starting a new Tracer session in sessionStarting().
     *
     * @param snapshot profiler snapshot
     */
    public void sessionStopping(IdeSnapshot snapshot);

    /**
     * Invoked when the Tracer session has finished.
     *
     * @param snapshot profiler snapshot
     */
    public void sessionFinished(IdeSnapshot snapshot);

    /**
     * Invoked when refresh rate of the Tracer session has been changed.
     *
     * @param snapshot profiler snapshot
     * @param refresh session refresh rate in miliseconds
     */
    public void refreshRateChanged(IdeSnapshot snapshot, int refresh);


    /**
     * An abstract adapter class for receiving Tracer session state notifications.
     */
    public abstract class Adapter implements ProbeStateHandler {

        public void probeAdded(IdeSnapshot snapshot) {}

        public void probeRemoved(IdeSnapshot snapshot) {}

        /**
         * Invoked when setting up a new Tracer session. This method allows a
         * Probe to notify the user about initialization progress. The actual
         * initialization (and updating the TracerProgressObject) should be
         * performed in the sessionStarting() method. Useful for example for
         * messaging a delay during instrumention of classes in target application.
         *
         * @param snapshot profiler snapshot
         * @param refresh session refresh rate in miliseconds
         * @return TracerProgressObject null in default implementation
         */
        public TracerProgressObject sessionInitializing(IdeSnapshot snapshot, int refresh) {
            return null;
        }

        public void sessionStarting(IdeSnapshot snapshot)
                throws SessionInitializationException {}

        public void sessionRunning(IdeSnapshot snapshot) {}

        public void sessionStopping(IdeSnapshot snapshot) {}

        public void sessionFinished(IdeSnapshot snapshot) {}

        public void refreshRateChanged(IdeSnapshot snapshot, int refresh) {}

    }

}

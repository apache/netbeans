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

package org.netbeans.api.debugger;

import java.beans.PropertyChangeListener;

/**
 * This listener notifies about changes in the
 * {@link org.netbeans.api.debugger.DebuggerManager} - breakpoints, watches
 * and sessions.
 *
 * @author   Jan Jancura
 */
public interface DebuggerManagerListener extends PropertyChangeListener {

    /**
     * Called when set of breakpoints is initialized.
     *
     * @return initial set of breakpoints
     */
    public Breakpoint[] initBreakpoints ();

    /**
     * Called when some breakpoint is added.
     *
     * @param breakpoint a new breakpoint
     */
    public void breakpointAdded (Breakpoint breakpoint);

    /**
     * Called when some breakpoint is removed.
     *
     * @param breakpoint removed breakpoint
     */
    public void breakpointRemoved (Breakpoint breakpoint);

    /**
     * Called when set of watches is initialized.
     */
    public void initWatches ();

    /**
     * Called when some watch is added.
     *
     * @param watch a new watch
     */
    public void watchAdded (Watch watch);

    /**
     * Called when some watch is removed.
     *
     * @param watch removed watch
     */
    public void watchRemoved (Watch watch);

    /**
     * Called when some session is added.
     *
     * @param session a new session
     */
    public void sessionAdded (Session session);

    /**
     * Called when some session is removed.
     *
     * @param session removed session
     */
    public void sessionRemoved (Session session);

    /**
     * Called when some engine is added.
     *
     * @param engine a new engine
     */
    public void engineAdded (DebuggerEngine engine);

    /**
     * Called when some engine is removed.
     *
     * @param engine removed engine
     */
    public void engineRemoved (DebuggerEngine engine);
}

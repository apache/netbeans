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

package org.netbeans.spi.debugger;

import java.beans.PropertyChangeListener;
import org.netbeans.api.debugger.ActiveBreakpoints;

/**
 * Provider of debugger engine-related breakpoints
 * activation/deactivation, which is independent on the enabled/disabled state.
 * Register an implementation of this class for an appropriate debugger engine
 * via {@link DebuggerServiceRegistration} annotation.
 * 
 * @author Martin Entlicher
 * @since 1.51
 */
public interface BreakpointsActivationProvider {
    
    /**
     * Test if the engine's breakpoints are currently active.
     * @return <code>true</code> when breakpoints are active,
     *         <code>false</code> otherwise.
     */
    boolean areBreakpointsActive();
    
    /**
     * Activate or deactivate breakpoints handled by this debugger engine.
     * The breakpoints activation/deactivation is independent on breakpoints enabled/disabled state.
     * 
     * @param active <code>true</code> to activate breakpoints,
     *               <code>false</code> to deactivate them.
     */
    void setBreakpointsActive(boolean active);
    
    /**
     * Add a property change listener to be notified about
     * {@link ActiveBreakpoints#PROP_BREAKPOINTS_ACTIVE}
     * @param l a property change listener
     */
    void addPropertyChangeListener(PropertyChangeListener l);
    
    /**
     * Remove a property change listener.
     * @param l  a property change listener
     */
    void removePropertyChangeListener(PropertyChangeListener l);
}

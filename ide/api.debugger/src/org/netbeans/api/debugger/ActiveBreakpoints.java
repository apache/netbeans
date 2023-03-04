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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.spi.debugger.BreakpointsActivationProvider;

/**
 * This class handles an engine-related breakpoints activation/deactivation,
 * which is independent on their enabled/disabled state.
 * 
 * @author Martin Entlicher
 * @since 1.51
 */
public final class ActiveBreakpoints {

    /**
     * Property change fired when breakpoints activation/deactivation changes.
     */
    public static final String PROP_BREAKPOINTS_ACTIVE = "breakpointsActive";   // NOI18N
    /**
     * Property change fired when a set of engine-related breakpoints is changed.
     * Make public when {@link #getEngineBreakpoints()} is public and assure it's fired.
     *
    private static final String PROP_BREAKPOINTS_CHANGED = "breakpointsChanged"; // NOI18N
     */

    // These two lists are used as a trivial weak map from DebuggerEngine to ActiveBreakpoints,
    // where both keys and values are held weakly.
    private static final List<Reference<DebuggerEngine>> dEngines = new LinkedList<>();
    private static final List<Reference<ActiveBreakpoints>> aBreakpoints = new LinkedList<>();

    private static final ActiveBreakpoints UNSUPPORTED = new ActiveBreakpoints(null);

    private final BreakpointsActivationProvider bap;

    /**
     * Get an ActiveBreakpoints instance for a debugger engine.
     * @param debuggerEngine the debugger engine.
     * @return The ActiveBreakpoints instance, that delegates to an
     * {@link BreakpointsActivationProvider} provided in it's lookup.
     */
    public static ActiveBreakpoints get(DebuggerEngine debuggerEngine) {
        BreakpointsActivationProvider bap = debuggerEngine.lookupFirst(null, BreakpointsActivationProvider.class);
        if (bap == null) {
            return UNSUPPORTED;
        }
        synchronized (dEngines) {
            for (int i = 0; i < dEngines.size(); i++) {
                DebuggerEngine de = dEngines.get(i).get();
                if (de == null) {
                    dEngines.remove(i);
                    aBreakpoints.remove(i);
                    i--;
                    continue;
                }
                if (de == debuggerEngine) {
                    ActiveBreakpoints ab = aBreakpoints.get(i).get();
                    if (ab != null) {
                        return ab;
                    }
                }
            }
            // Not cached, create a new one:
            ActiveBreakpoints ab = new ActiveBreakpoints(bap);
            dEngines.add(new WeakReference<>(debuggerEngine));
            aBreakpoints.add(new WeakReference<>(ab));
            return ab;
        }
    }

    private ActiveBreakpoints(BreakpointsActivationProvider bap) {
        this.bap = bap;
    }

    /**
     * Test if breakpoint deactivation is supported.
     * When <code>false</code> is returned, {@link #setBreakpointsActive(boolean)}
     * throws UnsupportedOperationException when called with false argument.
     * @return <code>true</code> when engine-related breakpoints can be deactivated,
     *         <code>false</code> otherwise.
     */
    public boolean canDeactivateBreakpoints() {
        return bap != null;
    }
    
    /**
     * Test if the engine's breakpoints are currently active.
     * @return <code>true</code> when breakpoints are active,
     *         <code>false</code> otherwise.
     */
    public boolean areBreakpointsActive() {
        return bap == null || bap.areBreakpointsActive();
    }
    
    /**
     * Activate or deactivate breakpoints handled by this debugger engine.
     * The breakpoints activation/deactivation is independent on breakpoints enabled/disabled state.
     * 
     * @param active <code>true</code> to activate breakpoints,
     *               <code>false</code> to deactivate them.
     * @throws UnsupportedOperationException when there is an attempt to deactivate
     *         breakpoints even though {@link #canDeactivateBreakpoints()} return false
     */
    public void setBreakpointsActive(boolean active) throws UnsupportedOperationException {
        if (bap == null) {
            throw new UnsupportedOperationException("No implementation of BreakpointsActivationProvider provided by the engine.");
        }
        bap.setBreakpointsActive(active);
    }
    
    /**
     * Get the set of breakpoints that are managed by this engine.
     * These are the breakpoints that are subject of activation/deactivation
     * if it's supported.
     * @return A set of breakpoints managed by this engine.
     *
    // A possible extension
    private Set<Breakpoint> getEngineBreakpoints() {
        // Possible to iterate through breakpoints and check GroupProperties.getEngines()
        // Update the set on breakpoint add/remove
        return null;
    }
    */
    
    /**
     * Add a property change listener to be notified about properties
     * defined as PROP_* constants.
     * @param l a property change listener
     */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        if (bap != null) {
            bap.addPropertyChangeListener(l);
        }
    }
    
    /**
     * Remove a property change listener.
     * @param l  a property change listener
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        if (bap != null) {
            bap.removePropertyChangeListener(l);
        }
    }
}

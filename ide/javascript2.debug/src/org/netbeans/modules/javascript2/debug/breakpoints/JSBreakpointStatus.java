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

package org.netbeans.modules.javascript2.debug.breakpoints;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * A utility class that manages status of JavaScript breakpoints.
 * 
 * @author Martin
 */
public class JSBreakpointStatus {
    
    private static Reference<JSLineBreakpoint> activeBPRef = new WeakReference<>(null);
    
    private JSBreakpointStatus() {}
    
    public static void setValid(JSLineBreakpoint lb, String message) {
        lb.setValid(message);
    }

    public static final void setInvalid(JSLineBreakpoint lb, String message) {
        lb.setInvalid(message);
    }
    
    public static final void resetValidity(JSLineBreakpoint lb) {
        lb.resetValidity();
    }
    
    /**
     * Set the breakpoint as active (hit).
     * 
     * @param lb the active breakpoint. <code>null</code> resets to no active breakpoint.
     */
    public static void setActive(JSLineBreakpoint lb) {
        synchronized (JSBreakpointStatus.class) {
            activeBPRef = new WeakReference<>(lb);
        }
    }
    
    /**
     * @return The active breakpoint, or <code>null</code>.
     */
    public static JSLineBreakpoint getActive() {
        synchronized (JSBreakpointStatus.class) {
            return activeBPRef.get();
        }
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.dbgp.breakpoints;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.php.dbgp.DebugSession;

/**
 * @author ads
 *
 */
public abstract class AbstractBreakpoint extends Breakpoint {
    private String myId;
    private boolean isEnabled;

    protected AbstractBreakpoint() {
        isEnabled = true;
    }

    public abstract boolean isSessionRelated(DebugSession session);

    /*
     * Method is called from debugger manager listener when breakpoint is removed.
     * It is allowed to provide clear actions.
     * This method should be implemented in sublasses if needed.
     */
    public void removed() {
    }

    @Override
    public void disable() {
        if (!isEnabled) {
            return;
        }
        isEnabled = false;
        firePropertyChange(PROP_ENABLED, Boolean.TRUE, Boolean.FALSE);
    }

    @Override
    public void enable() {
        if (isEnabled) {
            return;
        }

        isEnabled = true;
        firePropertyChange(PROP_ENABLED, Boolean.FALSE, Boolean.TRUE);
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public int isTemp() {
        return 0;
    }

    public void setBreakpointId(String id) {
        myId = id;
        setValidity(VALIDITY.VALID, null);  // Has an ID - is valid
    }

    public String getBreakpointId() {
        return myId;
    }

    void setInvalid() {
        setValidity(VALIDITY.INVALID, null);
    }

    public void setInvalid(String reason) {
        setValidity(VALIDITY.INVALID, reason);
    }

    public void reset() {
        setValidity(VALIDITY.UNKNOWN, null);
        myId = null;
    }

}

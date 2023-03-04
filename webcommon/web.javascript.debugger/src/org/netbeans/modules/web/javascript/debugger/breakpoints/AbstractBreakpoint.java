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
package org.netbeans.modules.web.javascript.debugger.breakpoints;

import org.netbeans.api.debugger.Breakpoint;


public abstract class AbstractBreakpoint extends Breakpoint {
    
    private String myId;
    private boolean isEnabled;
    
    protected AbstractBreakpoint() {
        isEnabled = true;
    }
    
    /*
     * Method is called from debugger manager listener when breakpoint is removed.
     * It is allowed to provide clear actions.
     * This method should be implemented in sublasses if needed.
     */
    public void removed(){
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.Breakpoint#disable()
     */
    @Override
    public void disable() {
        if(!isEnabled) {
            return;
        }

        isEnabled = false;
        firePropertyChange(PROP_ENABLED, Boolean.TRUE, Boolean.FALSE);
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.Breakpoint#enable()
     */
    @Override
    public void enable() {
        if(isEnabled) {
            return;
        }

        isEnabled = true;
        firePropertyChange(PROP_ENABLED, Boolean.FALSE, Boolean.TRUE);
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.Breakpoint#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
    
    /* not used yet
    public void setBreakpointId( String id ) {
        myId = id ;
    }
    
    public String getBreakpointId() {
        return myId;
    }
    */
    
    public boolean isConditional() {
        return false;
    }

}

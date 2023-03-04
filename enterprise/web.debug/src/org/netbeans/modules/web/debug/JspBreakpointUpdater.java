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

package org.netbeans.modules.web.debug;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * @author Martin Grebac
 */
public class JspBreakpointUpdater extends LazyActionsManagerListener {

    private JPDADebugger debugger;

    public JspBreakpointUpdater (ContextProvider lookupProvider) {
        JPDADebugger debugger = (JPDADebugger) lookupProvider.lookupFirst (
            null, JPDADebugger.class
        );
        this.debugger = debugger;
        Context.createTimeStamp (debugger);
        JspBreakpointAnnotationListener bal = (JspBreakpointAnnotationListener) 
            DebuggerManager.getDebuggerManager ().lookupFirst 
            (null, JspBreakpointAnnotationListener.class);
        bal.updateJspLineBreakpoints ();
    }
    
    protected void destroy () {
        Context.disposeTimeStamp (debugger);
    }
    
    public String[] getProperties () {
        return new String [0];
    }
}

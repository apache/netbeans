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

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.util.Collections;
import java.util.Set;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.spi.debugger.ActionsProvider;
import org.openide.util.RequestProcessor;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
*/
@ActionsProvider.Registration(path="netbeans-JPDASession", actions="makeCalleeCurrent")
public class MakeCalleeCurrentActionProvider extends JPDADebuggerAction {

    private RequestProcessor rp;
    
    public MakeCalleeCurrentActionProvider (ContextProvider lookupProvider) {
        super (
            lookupProvider.lookupFirst(null, JPDADebugger.class)
        );
        this.rp = lookupProvider.lookupFirst(null, RequestProcessor.class);
        getDebuggerImpl ().addPropertyChangeListener 
            (JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
    }
    
    @Override
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_MAKE_CALLEE_CURRENT);
    }

    @Override
    public void doAction (Object action) {
        JPDAThread t = getDebuggerImpl ().getCurrentThread ();
        if (t == null) return;
        int i = MakeCallerCurrentActionProvider.getCurrentCallStackFrameIndex 
            (getDebuggerImpl ());
        if (i == 0) return;
        MakeCallerCurrentActionProvider.setCurrentCallStackFrameIndex 
            (getDebuggerImpl (), --i);
    }
    
    @Override
    protected void checkEnabled (int debuggerState) {
        if (debuggerState == JPDADebugger.STATE_STOPPED) {
            JPDAThread t = getDebuggerImpl ().getCurrentThread ();
            if (t != null) {
                checkEnabledLazySingleAction(debuggerState, rp);
                return;
            }
        }
        setEnabledSingleAction(false);
    }

    @Override
    protected boolean checkEnabledLazyImpl(int debuggerState) {
        int i = MakeCallerCurrentActionProvider.getCurrentCallStackFrameIndex
            (getDebuggerImpl ());
        return i > 0;
    }

}

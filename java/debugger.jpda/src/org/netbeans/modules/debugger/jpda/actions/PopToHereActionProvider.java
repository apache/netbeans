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

package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.AbsentInformationException;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.spi.debugger.ActionsProvider;


/**
* Pop to Here action implementation.
*
* @author   Jan Jancura
*/
@ActionsProvider.Registration(path="netbeans-JPDASession", actions={"popTopmostCall"})
public class PopToHereActionProvider extends JPDADebuggerActionProvider {
    
    public PopToHereActionProvider (ContextProvider lookupProvider) {
        super (
            (JPDADebuggerImpl) lookupProvider.lookupFirst 
                (null, JPDADebugger.class) 
        );
        setProviderToDisableOnLazyAction(this);
    }
    
    @Override
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_POP_TOPMOST_CALL);
    }

    @Override
    public void doAction (Object action) {
        runAction();
    }
    
    @Override
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        doLazyAction(action, new Runnable() {
            @Override
            public void run() {
                try {
                    runAction();
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
    }
    
    public void runAction() {
        try {
            JPDAThread t = getDebuggerImpl ().getCurrentThread ();
            ((JPDAThreadImpl) t).accessLock.writeLock().lock();
            try {
                CallStackFrame[] frames = t.getCallStack (0, 2);
                if (frames.length > 1) {
                    frames[0].popFrame ();
                }
            } finally {
                ((JPDAThreadImpl) t).accessLock.writeLock().unlock();
            }
        } catch (AbsentInformationException ex) {
        }
    }
    
    @Override
    protected void checkEnabled (int debuggerState) {
        if (!getDebuggerImpl().canPopFrames()) {
            setEnabled (
                ActionsManager.ACTION_POP_TOPMOST_CALL,
                false
            );
            return;
        }
        JPDAThread t;
        if (debuggerState == JPDADebugger.STATE_STOPPED) {
            t = getDebuggerImpl ().getCurrentThread ();
        } else {
            t = null;
        }
        boolean enabled;
        if (t == null) {
            enabled = false;
        } else {
            enabled = t.isSuspended();
        }
        setEnabled (
            ActionsManager.ACTION_POP_TOPMOST_CALL,
            enabled
        );
    }
}

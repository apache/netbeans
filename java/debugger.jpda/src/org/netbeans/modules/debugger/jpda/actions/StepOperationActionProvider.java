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

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.spi.debugger.ActionsProvider;


/**
 *
 * @author  Martin Entlicher
 */
@ActionsProvider.Registration(path="netbeans-JPDASession", actions={"stepOperation"})
public class StepOperationActionProvider extends JPDADebuggerActionProvider {
    
    public StepOperationActionProvider (ContextProvider lookupProvider) {
        super (
            (JPDADebuggerImpl) lookupProvider.lookupFirst
                (null, JPDADebugger.class)
        );
        setProviderToDisableOnLazyAction(this);
    }

    static ActionsManager getCurrentActionsManager () {
        return DebuggerManager.getDebuggerManager ().
            getCurrentEngine () == null ? 
            DebuggerManager.getDebuggerManager ().getActionsManager () :
            DebuggerManager.getDebuggerManager ().getCurrentEngine ().
                getActionsManager ();
    }

    @Override
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_STEP_OPERATION);
    }
    
    @Override
    public void doAction (Object action) {
        doAction(debugger, null);
    }

    @Override
    public void postAction(final Object action,
                           final Runnable actionPerformedNotifier) {
        doLazyAction(action, new Runnable() {
            @Override
            public void run() {
                try {
                    doAction(getDebuggerImpl(), null);
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
    }

    static void doAction (JPDADebugger debugger, PropertyChangeListener listener) {
        JPDAStep step = debugger.createJPDAStep(JPDAStep.STEP_OPERATION, JPDAStep.STEP_OVER);
        step.addStep(debugger.getCurrentThread());
        if (listener != null) {
            step.addPropertyChangeListener(listener);
        }
        if (debugger.getSuspend() == JPDADebugger.SUSPEND_EVENT_THREAD) {
            //debugger.getCurrentThread().resume();
            ((JPDADebuggerImpl) debugger).resumeCurrentThread();
        } else {
            ((JPDADebuggerImpl) debugger).resume();
        }
    }

    public void actionPerformed(Object action) {
        // Is never called
    }

    @Override
    protected void checkEnabled (int debuggerState) {
        Iterator i = getActions ().iterator ();
        while (i.hasNext ())
            setEnabled (
                i.next (),
                (debuggerState == JPDADebugger.STATE_STOPPED) &&
                (getDebuggerImpl ().getCurrentThread () != null)
            );
    }

}

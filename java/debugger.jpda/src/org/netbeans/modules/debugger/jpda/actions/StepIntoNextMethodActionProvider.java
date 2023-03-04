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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.spi.debugger.ActionsProvider;


/**
 * Implements non visual part of stepping through code in JPDA debugger.
 * It supports standard debugging actions StepInto, Over, Out, RunToCursor, 
 * and Go. And advanced "smart tracing" action.
 *
 * @author  Jan Jancura
 */
@ActionsProvider.Registration(path="netbeans-JPDASession", actions={"stepIntoNextMethod"})
public class StepIntoNextMethodActionProvider extends JPDADebuggerActionProvider {

    private final StepIntoNextMethod stepInto;
    
    public StepIntoNextMethodActionProvider (ContextProvider contextProvider) {
        super (
            (JPDADebuggerImpl) contextProvider.lookupFirst 
                (null, JPDADebugger.class)
        );
        stepInto = new StepIntoNextMethod(contextProvider);
        setProviderToDisableOnLazyAction(this);
    }


    // ActionProviderSupport ...................................................
    
    @Override
    public Set getActions () {
        return new HashSet<Object>(Arrays.asList (new Object[] {
            "stepIntoNextMethod", // [TODO] add constatnt
        }));
    }
    
    @Override
    public void doAction (Object action) {
        stepInto.runAction();
    }
    
    @Override
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        doLazyAction(action, new Runnable() {
            @Override
            public void run() {
                try {
                    stepInto.runAction();
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
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

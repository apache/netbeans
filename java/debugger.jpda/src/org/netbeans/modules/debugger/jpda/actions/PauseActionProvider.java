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

import java.util.Collections;
import java.util.Set;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.ThreadsCollectorImpl;
import org.netbeans.spi.debugger.ActionsProvider;


/**
* Pause action implementation.
*
* @author   Jan Jancura
* @author  Marian Petras
*/
@ActionsProvider.Registration(path="netbeans-JPDASession", actions={"pause"})
public class PauseActionProvider extends JPDADebuggerActionProvider {
    
    private final ThreadsCollectorImpl threadsCollector;

    public PauseActionProvider (ContextProvider contextProvider) {
        super (
            (JPDADebuggerImpl) contextProvider.lookupFirst 
                (null, JPDADebugger.class)
        );
        threadsCollector = (ThreadsCollectorImpl) debugger.getThreadsCollector();
        threadsCollector.addPropertyChangeListener(this);
        setProviderToDisableOnLazyAction(this);
    }
    
    @Override
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_PAUSE);
    }

    @Override
    public void doAction (Object action) {
        ((JPDADebuggerImpl) getDebuggerImpl ()).suspend ();
    }
    
    @Override
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        doLazyAction(action, new Runnable() {
            @Override
            public void run() {
                try {
                    ((JPDADebuggerImpl) getDebuggerImpl ()).suspend ();
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
    }
    
    @Override
    protected void checkEnabled (int debuggerState) {
        setEnabled (
            ActionsManager.ACTION_PAUSE,
            threadsCollector.isSomeThreadRunning()
        );
    }
    
}

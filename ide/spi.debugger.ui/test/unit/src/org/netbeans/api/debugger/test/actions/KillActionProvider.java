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

package org.netbeans.api.debugger.test.actions;

import java.util.Collections;
import java.util.Set;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.test.TestDICookie;
import org.netbeans.api.debugger.test.TestDebugger;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;


/**
* Provider for the Kill action in the test debugger.
*
* @author Maros Sandor
*/
public class KillActionProvider extends ActionsProvider {

    private ContextProvider lookupProvider;
    private TestDebugger debugger;

    public KillActionProvider (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        debugger = lookupProvider.lookupFirst(null, TestDebugger.class);
    }

    public boolean isEnabled(Object action) {
        return true;
    }

    public void addActionsProviderListener(ActionsProviderListener l) {}
    public void removeActionsProviderListener(ActionsProviderListener l) {}

    public Set getActions() {
        return Collections.singleton (ActionsManager.ACTION_KILL);
    }
        
    public void doAction (Object action) {
        debugger.finish();
        DebuggerInfo di = lookupProvider.lookupFirst(null, DebuggerInfo.class);
        TestDICookie tic = di.lookupFirst(null, TestDICookie.class);
        tic.addInfo(ActionsManager.ACTION_KILL);
    }
}

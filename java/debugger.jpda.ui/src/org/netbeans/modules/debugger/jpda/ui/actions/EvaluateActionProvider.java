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
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.CodeEvaluator;

/**
 * Invokes the expression evaluator GUI
 *
 * @author Martin Entlicher
 */
@ActionsProvider.Registration(path="netbeans-JPDASession", actions="evaluate")
public class EvaluateActionProvider extends JPDADebuggerAction {

    public EvaluateActionProvider(ContextProvider lookupProvider) {
        super (
            lookupProvider.lookupFirst(null, JPDADebugger.class)
        );
        getDebuggerImpl ().addPropertyChangeListener
            (JPDADebugger.PROP_CURRENT_THREAD, this);
    }

    @Override
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_EVALUATE);
    }

    @Override
    protected void checkEnabled (int debuggerState) {
        setEnabledSingleAction(getDebuggerImpl().getCurrentThread() != null);
    }

    @Override
    public void postAction(Object action, Runnable actionPerformedNotifier) {
        CodeEvaluator.getDefault().open();
    }

    @Override
    public void doAction(Object action) {
        // Not called since we override postAction().
        throw new UnsupportedOperationException("Not supported.");
    }

}

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
package org.netbeans.modules.cpplite.debugger;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.RequestProcessor;

/**
 * The CPP Lite Debugger's actions provider.
 */
@ActionsProvider.Registration(path="CPPLiteSession", actions={"start", "stepInto", "stepOver", "stepOut",
                                                              "pause", "continue", "kill"})
public final class CPPLiteActionsProvider extends ActionsProviderSupport {

    private static final Logger LOGGER = Logger.getLogger(CPPLiteActionsProvider.class.getName());

    private static final Set<Object> ACTIONS = new HashSet<>();
    private static final Set<Object> ACTIONS_TO_DISABLE = new HashSet<>();

    static {
        ACTIONS.add (ActionsManager.ACTION_KILL);
        ACTIONS.add (ActionsManager.ACTION_CONTINUE);
        ACTIONS.add (ActionsManager.ACTION_PAUSE);
        ACTIONS.add (ActionsManager.ACTION_START);
        ACTIONS.add (ActionsManager.ACTION_STEP_INTO);
        ACTIONS.add (ActionsManager.ACTION_STEP_OVER);
        ACTIONS.add (ActionsManager.ACTION_STEP_OUT);
        ACTIONS_TO_DISABLE.addAll(ACTIONS);
        // Ignore the KILL action
        ACTIONS_TO_DISABLE.remove(ActionsManager.ACTION_KILL);
    }

    /** The ReqeustProcessor used by action performers. */
    private static RequestProcessor     actionsRequestProcessor;
    private static RequestProcessor     killRequestProcessor;

    private final CPPLiteDebugger debugger;

    public CPPLiteActionsProvider(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, CPPLiteDebugger.class);
        // init actions
        for (Object action : ACTIONS) {
            setEnabled (action, true);
        }
    }

    @Override
    public Set getActions () {
        return ACTIONS;
    }

    @Override
    public void doAction (Object action) {
        LOGGER.log(Level.FINE, "CPPLiteDebugger.doAction({0}), is kill = {1}", new Object[]{action, action == ActionsManager.ACTION_KILL});
        if (action == ActionsManager.ACTION_KILL) {
            debugger.finish(true);
        } else
        if (action == ActionsManager.ACTION_CONTINUE) {
            debugger.resume();
        } else
        if (action == ActionsManager.ACTION_PAUSE) {
            debugger.pause();
        } else
        if (action == ActionsManager.ACTION_START) {
            return ;
        } else
        if ( action == ActionsManager.ACTION_STEP_INTO ||
             action == ActionsManager.ACTION_STEP_OUT ||
             action == ActionsManager.ACTION_STEP_OVER
        ) {
            debugger.doStep (action);
        }
    }

    @Override
    public void postAction(final Object action, final Runnable actionPerformedNotifier) {
        if (action == ActionsManager.ACTION_KILL) {
            synchronized (CPPLiteDebugger.class) {
                if (killRequestProcessor == null) {
                    killRequestProcessor = new RequestProcessor("CPPLite debugger finish RP", 1);
                }
            }
            killRequestProcessor.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        doAction(action);
                    } finally {
                        actionPerformedNotifier.run();
                    }
                }
            });
            return ;
        }
        setDebugActionsEnabled(false);
        synchronized (CPPLiteDebugger.class) {
            if (actionsRequestProcessor == null) {
                actionsRequestProcessor = new RequestProcessor("CPPLite debugger actions RP", 1);
            }
        }
        actionsRequestProcessor.post(new Runnable() {
            @Override
            public void run() {
                try {
                    doAction(action);
                } finally {
                    actionPerformedNotifier.run();
                    setDebugActionsEnabled(true);
                }
            }
        });
    }

    private void setDebugActionsEnabled(boolean enabled) {
        for (Object action : ACTIONS_TO_DISABLE) {
            setEnabled(action, enabled);
        }
    }

}

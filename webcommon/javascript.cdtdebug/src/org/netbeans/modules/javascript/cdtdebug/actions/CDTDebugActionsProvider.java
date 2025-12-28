/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.javascript.cdtdebug.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.chrome_devtools_protocol.ChromeDevToolsClient;
import org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame;

import static org.netbeans.api.debugger.ActionsManager.*;

import org.netbeans.modules.javascript.cdtdebug.CDTDebugger;
import org.netbeans.modules.javascript.cdtdebug.CDTDebuggerSessionProvider;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.RequestProcessor;

@ActionsProvider.Registration(path=CDTDebuggerSessionProvider.SESSION_NAME)
public class CDTDebugActionsProvider extends ActionsProviderSupport  implements CDTDebugger.Listener {

    private static final Logger LOG = Logger.getLogger(CDTDebugActionsProvider.class.getName());

    private static final Set<Object> ACTIONS =
            Collections.unmodifiableSet(
                new HashSet<>(
                    Arrays.asList(new Object[] {
                        ACTION_START,
                        ACTION_KILL,
                        ACTION_CONTINUE,
                        ACTION_PAUSE,
                        ACTION_STEP_INTO,
                        ACTION_STEP_OVER,
                        ACTION_STEP_OUT,
                        ACTION_FIX
                    })));

    private final CDTDebugger dbg;
    private final RequestProcessor killActionRP = new RequestProcessor(CDTDebugActionsProvider.class.getName()+".kill");
     private final PropertyChangeListener changeLiveListener = new ChangeLiveListener();

    public CDTDebugActionsProvider(ContextProvider contextProvider) {
        dbg = contextProvider.lookupFirst(null, CDTDebugger.class);
        dbg.addListener(this);
        dbg.getChangeLiveSupport().addPropertyChangeListener(changeLiveListener);
        setEnabled(ACTION_START, true);
        setEnabled(ACTION_KILL, true);
        setEnabled(ACTION_FIX, false);
        notifySuspended(false);
    }

    @Override
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        // Be able to kill the debugger at any time, not to be blocked by other actions.
        if (action == ACTION_KILL) {
            killActionRP.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        dbg.finish();
                    } finally {
                        actionPerformedNotifier.run();
                    }
                }
            });
        } else {
            super.postAction(action, actionPerformedNotifier);
        }
    }

    @Override
    public void doAction(Object action) {
        LOG.log(Level.FINE, "doAction({0})", action);
        if (action == ACTION_START) {
            dbg.start();
        } else if (action == ACTION_CONTINUE) {
            dbg.resume();
        } else if (action == ACTION_PAUSE) {
            dbg.suspend();
        } else if (action == ACTION_STEP_INTO) {
            ChromeDevToolsClient cdtc = dbg.getConnection();
            if(cdtc != null) {
                cdtc.getDebugger().stepInto(null);
            }
        } else if (action == ACTION_STEP_OVER) {
            ChromeDevToolsClient cdtc = dbg.getConnection();
            if(cdtc != null) {
                cdtc.getDebugger().stepOver(null);
            }
        } else if (action == ACTION_STEP_OUT) {
            ChromeDevToolsClient cdtc = dbg.getConnection();
            if(cdtc != null) {
                cdtc.getDebugger().stepOut(null);
            }
        }
         else if (action == ACTION_FIX) {
            dbg.getChangeLiveSupport().applyChanges();
        }
    }

    @Override
    public Set getActions() {
        return ACTIONS;
    }

    @Override
    public void notifySuspended(boolean suspended) {
        setEnabled(ACTION_CONTINUE, suspended);
        setEnabled(ACTION_PAUSE, !suspended);
        setEnabled(ACTION_STEP_INTO, suspended);
        setEnabled(ACTION_STEP_OVER, suspended);
        setEnabled(ACTION_STEP_OUT, suspended);
    }

    @Override
    public void notifyCurrentFrame(CallFrame cf) {
    }

    @Override
    public void notifyFinished() {
         dbg.getChangeLiveSupport().removePropertyChangeListener(changeLiveListener);
    }

    private class ChangeLiveListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            setEnabled(ACTION_FIX, (Boolean) evt.getNewValue());
        }

    }

}

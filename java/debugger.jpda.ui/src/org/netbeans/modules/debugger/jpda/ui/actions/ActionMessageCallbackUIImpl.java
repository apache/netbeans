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

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.DebuggerConsoleIO;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.actions.ActionErrorMessageCallback;
import org.netbeans.modules.debugger.jpda.actions.ActionMessageCallback;
import org.netbeans.modules.debugger.jpda.actions.ActionStatusDisplayCallback;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path = "netbeans-JPDASession", types={ ActionMessageCallback.class,
                                                                    ActionErrorMessageCallback.class,
                                                                    ActionStatusDisplayCallback.class })
public class ActionMessageCallbackUIImpl implements ActionMessageCallback,
                                                    ActionErrorMessageCallback,
                                                    ActionStatusDisplayCallback {
    
    private final DebuggerConsoleIO consoleIO;
    
    public ActionMessageCallbackUIImpl(ContextProvider lookupProvider) {
        JPDADebuggerImpl dbg = (JPDADebuggerImpl) lookupProvider.lookupFirst(null, JPDADebugger.class);
        this.consoleIO = dbg.getConsoleIO();
    }

    @Override
    public void messageCallback(Object action, String message) {
        if (action == ActionsManager.ACTION_FIX) {
            // Special handling of messages coming from apply code changes - print into debug output.
            consoleIO.println(message, null, false);
        } else {
            NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notify(descriptor);
        }
    }

    @Override
    public void errorMessageCallback(Object action, String message) {
        if (action == ActionsManager.ACTION_FIX) {
            // Special handling of failures of apply code changes - print into debug output.
            consoleIO.println(message, null, true);
        } else {
            NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(message, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(descriptor);
        }
    }

    @Override
    public void statusDisplayCallback(Object action, String status) {
        StatusDisplayer.getDefault().setStatusText(status);
    }
    
}

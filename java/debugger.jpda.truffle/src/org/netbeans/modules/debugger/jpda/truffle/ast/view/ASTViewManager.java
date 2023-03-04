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

package org.netbeans.modules.debugger.jpda.truffle.ast.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.options.TruffleOptions;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Manager of the ASTView.
 */
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class ASTViewManager extends DebuggerManagerAdapter {

    private volatile boolean shouldOpenView;
    private volatile boolean isAtTruffleLocation;
    private final PropertyChangeListener propListenerHolder;    // Not to have the listener collected

    public ASTViewManager() {
        shouldOpenView = TruffleOptions.isLanguageDeveloperMode();
        propListenerHolder = propEvent -> {
            boolean develMode = TruffleOptions.isLanguageDeveloperMode();
            shouldOpenView = develMode;
            if (develMode) {
                openIfCan();
            } else {
                closeView();
            }
        };
        TruffleOptions.onLanguageDeveloperModeChange(propListenerHolder);
    }

    @Override
    public void sessionAdded(Session session) {
        JPDADebugger debugger = session.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return ;
        }
        debugger.addPropertyChangeListener(JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
    }

    @Override
    public void sessionRemoved(Session session) {
        JPDADebugger debugger = session.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return ;
        }
        debugger.removePropertyChangeListener(JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME.equals(evt.getPropertyName())) {
            CallStackFrame frame = (CallStackFrame) evt.getNewValue();
            if (frame != null) {
                CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(frame.getThread());
                isAtTruffleLocation = currentPCInfo != null;
                openIfCan();
            } else {
                isAtTruffleLocation = false;
            }
        }
    }

    private void openIfCan() {
        if (shouldOpenView && isAtTruffleLocation) {
            SwingUtilities.invokeLater(() -> {
                TopComponent tc = WindowManager.getDefault().findTopComponent(ASTView.AST_VIEW_NAME);
                tc.open();
                tc.requestVisible();
            });
            shouldOpenView = false;
        }
    }

    private void closeView() {
        SwingUtilities.invokeLater(() -> {
            TopComponent tc = WindowManager.getDefault().findTopComponent(ASTView.AST_VIEW_NAME);
            tc.close();
        });
    }
}

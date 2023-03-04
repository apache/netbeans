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


package org.netbeans.modules.debugger.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.AbstractAction;


import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * Opens View TopComponent.
 *
 * @author Jan Jancura, Martin Entlicher
 */
public class ViewActions extends AbstractAction {
    
    private String viewName;

    private ViewActions (String viewName) {
        this.viewName = viewName;
    }

    public Object getValue(String key) {
        if (key == Action.NAME) {
            return NbBundle.getMessage (ViewActions.class, (String) super.getValue(key));
        }
        Object value = super.getValue(key);
        return value;
    }
    
    public void actionPerformed (ActionEvent evt) {
        openComponent (viewName, true);
    }
    
    static TopComponent openComponent (String viewName, boolean activate) {
        TopComponent view = WindowManager.getDefault().findTopComponent(viewName);
        if (view == null) {
            throw new IllegalArgumentException(viewName);
        }
        view.open();
        if (activate) {
            view.requestActive();
        }
        return view;
    }
    
    
    /**
     * Creates an action that opens Breakpoints TopComponent.
     */
    public static Action createBreakpointsViewAction () {
        ViewActions action = new ViewActions("breakpointsView");
        // When changed, update also mf-layer.xml, where are the properties duplicated because of Actions.alwaysEnabled()
        action.putValue (Action.NAME, "CTL_BreakpointsAction");
        action.putValue ("iconbase",
                "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoints.png" // NOI18N
        );
        return action;
    }

    /**
     * Creates an action that opens Call Stack TopComponent.
     */
    public static Action createCallStackViewAction () {
        ViewActions action = new ViewActions("callstackView");
        // When changed, update also mf-layer.xml, where are the properties duplicated because of Actions.alwaysEnabled()
        action.putValue (Action.NAME, "CTL_CallStackAction");
        action.putValue ("iconbase",
                "org/netbeans/modules/debugger/resources/callStackView/call_stack_16.png" // NOI18N
        );
        return action;
    }

    /**
     * Creates an action that opens Local Variables TopComponent.
     */
    public static Action createLocalsViewAction() {
        ViewActions action = new ViewActions("localsView");
        // When changed, update also mf-layer.xml, where are the properties duplicated because of Actions.alwaysEnabled()
        action.putValue (Action.NAME, "CTL_LocalVariablesAction");
        action.putValue ("iconbase",
                "org/netbeans/modules/debugger/resources/localsView/local_variable_16.png" // NOI18N
        );
        return action;
    }

    /**
     * Creates an action that opens Sessions TopComponent.
     */
    public static Action createSessionsViewAction () {
        ViewActions action = new ViewActions("sessionsView");
        // When changed, update also mf-layer.xml, where are the properties duplicated because of Actions.alwaysEnabled()
        action.putValue (Action.NAME, "CTL_SessionsAction");
        action.putValue ("iconbase",
                "org/netbeans/modules/debugger/resources/sessionsView/session_16.png" // NOI18N
        );
        return action;
    }

    /**
     * Creates an action that opens Threads TopComponent.
     */
    public static Action createThreadsViewAction () {
        ViewActions action = new ViewActions("threadsView");
        // When changed, update also mf-layer.xml, where are the properties duplicated because of Actions.alwaysEnabled()
        action.putValue (Action.NAME, "CTL_ThreadsAction");
        action.putValue ("iconbase",
                "org/netbeans/modules/debugger/resources/threadsView/ThreadGroup.gif" // NOI18N
        );
        return action;
    }
    
    
    /**
     * Creates an action that opens Watches TopComponent.
     */
    public static Action createWatchesViewAction() {
        ViewActions action = new ViewActions("watchesView");
        // When changed, update also mf-layer.xml, where are the properties duplicated because of Actions.alwaysEnabled()
        action.putValue (Action.NAME, "CTL_WatchesAction");
        action.putValue ("iconbase",
                "org/netbeans/modules/debugger/resources/watchesView/watch_16.png" // NOI18N
        );
        return action;
    }

    /**
     * Creates an action that opens Sources TopComponent.
     */
    public static Action createSourcesViewAction() {
        ViewActions action = new ViewActions("sources");
        // When changed, update also mf-layer.xml, where are the properties duplicated because of Actions.alwaysEnabled()
        action.putValue (Action.NAME, "CTL_SourcesAction");
        action.putValue ("iconbase",
                "org/netbeans/modules/debugger/resources/sourcesView/sources_16.png" // NOI18N
        );
        return action;
    }
    
    public static Action createDebuggingViewAction() {
        ViewActions action = new ViewActions("debuggingView");
        // When changed, update also mf-layer.xml, where are the properties duplicated because of Actions.alwaysEnabled()
        action.putValue (Action.NAME, "CTL_DebuggingViewAction");
        action.putValue ("iconbase",
                "org/netbeans/modules/debugger/resources/debuggingView/debugging_16.png" // NOI18N
        );
        return action;
    }

}


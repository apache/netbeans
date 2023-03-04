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

package org.netbeans.modules.debugger.ui.models;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * @author   Jan Jancura
 */
public class SessionsActionsProvider implements NodeActionsProvider {
    
    private RequestProcessor sessionActionsRP;
    
    private synchronized RequestProcessor getSessionActionsRP() {
        if (sessionActionsRP == null) {
            sessionActionsRP = new RequestProcessor("SessionsActionsProvider", 1, true);
        }
        return sessionActionsRP;
    }
    
    private final Action FINISH_ALL_ACTION = new AbstractAction 
        (NbBundle.getBundle(SessionsActionsProvider.class).getString("CTL_SessionAction_FinishAll_Label")) {
            @Override
            public boolean isEnabled() {
                return DebuggerManager.getDebuggerManager().getSessions().length > 0;
            }

            public void actionPerformed (ActionEvent e) {
                getSessionActionsRP().post(new Runnable() {
                    public void run() {
                        Session[] ss = DebuggerManager.getDebuggerManager ().
                            getSessions ();
                        int i, k = ss.length;
                        for (i = 0; i < k; i++)
                            ss [i].kill ();
                    }
                });
            }
    };
    private Action MAKE_CURRENT_ACTION = Models.createAction (
        NbBundle.getBundle(SessionsActionsProvider.class).getString("CTL_SessionAction_MakeCurrent_Label"), new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return DebuggerManager.getDebuggerManager ().
                    getCurrentSession () != node;
            }
            
            public void perform (Object[] nodes) {
                DebuggerManager.getDebuggerManager ().setCurrentSession (
                    (Session) nodes [0]
                );
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    private final Action FINISH_ACTION = Models.createAction (
        NbBundle.getBundle(SessionsActionsProvider.class).getString("CTL_SessionAction_Finish_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (final Object[] nodes) {
                getSessionActionsRP().post(new Runnable() {
                    public void run() {
                        int i, k = nodes.length;
                        for (i = 0; i < k; i++)
                            ((Session) nodes [i]).kill ();
                    }
                });
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return new Action [] {
                FINISH_ALL_ACTION
            };
        if (node instanceof Session)
            return new Action [] {
                MAKE_CURRENT_ACTION,
                FINISH_ACTION,
                null,
                FINISH_ALL_ACTION
            };
        throw new UnknownTypeException (node);
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof Session) {
            if (DebuggerManager.getDebuggerManager ().getCurrentSession () == 
                node
            ) return;
            DebuggerManager.getDebuggerManager ().setCurrentSession (
                (Session) node
            );
            return;
        }
        throw new UnknownTypeException (node);
    }

    public void addModelListener (ModelListener l) {
    }

    public void removeModelListener (ModelListener l) {
    }
}

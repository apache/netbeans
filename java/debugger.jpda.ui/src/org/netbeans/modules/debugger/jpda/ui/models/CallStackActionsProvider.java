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

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.AbsentInformationException;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;

import org.netbeans.modules.debugger.jpda.ui.SourcePath;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.util.Exceptions;


/**
 * @author   Jan Jancura
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/CallStackView", types=NodeActionsProvider.class)
public class CallStackActionsProvider implements NodeActionsProvider {
    
    private JPDADebugger    debugger;
    private ContextProvider lookupProvider;
    private final Action POP_TO_HERE_ACTION;
    private final Action MAKE_CURRENT_ACTION;
    private final Action COPY_TO_CLBD_ACTION;
    private final Action GO_TO_SOURCE_ACTION;
    private final Action ADD_BREAKPOINT_ACTION;


    public CallStackActionsProvider (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        RequestProcessor requestProcessor = lookupProvider.lookupFirst(null, RequestProcessor.class);
        POP_TO_HERE_ACTION = DebuggingActionsProvider.createPOP_TO_HERE_ACTION(requestProcessor);
        MAKE_CURRENT_ACTION = createMAKE_CURRENT_ACTION(requestProcessor);
        COPY_TO_CLBD_ACTION = createCOPY_TO_CLBD_ACTION(requestProcessor);
        GO_TO_SOURCE_ACTION = DebuggingActionsProvider.createGO_TO_SOURCE_ACTION(requestProcessor);
        ADD_BREAKPOINT_ACTION = DebuggingActionsProvider.createBREAKPOINT(requestProcessor);
    }
    

    private Action createMAKE_CURRENT_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
        NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_CallstackAction_MakeCurrent_Label"),
        new DebuggingActionsProvider.LazyActionPerformer (requestProcessor) {
            public boolean isEnabled (Object node) {
                // TODO: Check whether is not current - API change necessary
                return true;
            }
            public void run (Object[] nodes) {
                makeCurrent ((CallStackFrame) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    }
        
//    private final Action COPY_TO_CLBD_ACTION = Models.createAction (
//        NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_CallstackAction_Copy2CLBD_Label"),
//        new Models.ActionPerformer () {
//            public boolean isEnabled (Object node) {
//                return true;
//            }
//            public void perform (Object[] nodes) {
//                stackToCLBD (nodes[0]);
//            }
//        },
//        Models.MULTISELECTION_TYPE_ANY
//    );
        
    private Action createCOPY_TO_CLBD_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
        NbBundle.getBundle(CallStackActionsProvider.class).getString("CTL_CallstackAction_Copy2CLBD_Label"),
        new DebuggingActionsProvider.LazyActionPerformer (requestProcessor) {
            public boolean isEnabled (Object node) {
                JPDAThread t = debugger.getCurrentThread();
                return t != null && t.isSuspended();
            }
            public void run (Object[] nodes) {
                stackToCLBD ();
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );
    }

    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
            return new Action [] { COPY_TO_CLBD_ACTION };
        }
        
        if (!(node instanceof CallStackFrame))
            throw new UnknownTypeException (node);
        
        boolean popToHere = debugger.canPopFrames ();
        if (popToHere) {
            return new Action [] {
                MAKE_CURRENT_ACTION,
                POP_TO_HERE_ACTION,
                ADD_BREAKPOINT_ACTION,
                GO_TO_SOURCE_ACTION,
                COPY_TO_CLBD_ACTION
            };
        } else {
            return new Action [] {
                MAKE_CURRENT_ACTION,
                ADD_BREAKPOINT_ACTION,
                GO_TO_SOURCE_ACTION,
                COPY_TO_CLBD_ACTION
            };
        }
    }
    
    public void performDefaultAction (final Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof CallStackFrame) {
            lookupProvider.lookupFirst(null, RequestProcessor.class).post(new Runnable() {
                public void run() {
                    makeCurrent ((CallStackFrame) node);
                }
            });
            return;
        }
        throw new UnknownTypeException (node);
    }

    public void addModelListener (ModelListener l) {
    }

    public void removeModelListener (ModelListener l) {
    }

    private void stackToCLBD() {
        JPDAThread t = debugger.getCurrentThread();
        if (t == null) return ;
        StringBuffer frameStr = new StringBuffer(50);
        DebuggingActionsProvider.appendStackInfo(frameStr, t);
        Clipboard systemClipboard = DebuggingActionsProvider.getClipboard();
        Transferable transferableText =
                new StringSelection(frameStr.toString());
        systemClipboard.setContents(
                transferableText,
                null);
    }
    
    private void makeCurrent (final CallStackFrame frame) {
        if (debugger.getCurrentCallStackFrame () != frame) {
            frame.makeCurrent ();
        } else {
            Session session;
            try {
                session = (Session) debugger.getClass().getMethod("getSession").invoke(debugger);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                session = DebuggerManager.getDebuggerManager ().getCurrentSession ();
            }
            if (session == null) return ;
            String language = session.getCurrentLanguage ();
            SourcePath sp = session.getCurrentEngine().lookupFirst(null, SourcePath.class);
            if (sp == null) return ;
            sp.showSource (frame, language);
        }
    }
}

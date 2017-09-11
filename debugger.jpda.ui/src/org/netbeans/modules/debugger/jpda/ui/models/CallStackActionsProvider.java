/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    private Action          POP_TO_HERE_ACTION;
    private Action          MAKE_CURRENT_ACTION;
    private Action          COPY_TO_CLBD_ACTION;
    private Action          GO_TO_SOURCE_ACTION;


    public CallStackActionsProvider (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        RequestProcessor requestProcessor = lookupProvider.lookupFirst(null, RequestProcessor.class);
        POP_TO_HERE_ACTION = DebuggingActionsProvider.createPOP_TO_HERE_ACTION(requestProcessor);
        MAKE_CURRENT_ACTION = createMAKE_CURRENT_ACTION(requestProcessor);
        COPY_TO_CLBD_ACTION = createCOPY_TO_CLBD_ACTION(requestProcessor);
        GO_TO_SOURCE_ACTION = DebuggingActionsProvider.createGO_TO_SOURCE_ACTION(requestProcessor);
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
                GO_TO_SOURCE_ACTION,
                COPY_TO_CLBD_ACTION
            };
        } else {
            return new Action [] {
                MAKE_CURRENT_ACTION,
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

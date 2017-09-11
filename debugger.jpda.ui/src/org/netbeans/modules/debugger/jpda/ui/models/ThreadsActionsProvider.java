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

import java.awt.event.ActionEvent;
import java.util.Vector;
import javax.swing.Action;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.ui.SourcePath;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * @author   Jan Jancura
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/ThreadsView", types=NodeActionsProvider.class)
public class ThreadsActionsProvider implements NodeActionsProvider {

    private Action SUSPEND_ACTION;
    private Action RESUME_ACTION;
    private Action INTERRUPT_ACTION;
    private Action GO_TO_SOURCE_ACTION;

    private Action MAKE_CURRENT_ACTION = Models.createAction (
        NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_ThreadAction_MakeCurrent_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                if (node instanceof MonitorModel.ThreadWithBordel) node = ((MonitorModel.ThreadWithBordel) node).getOriginalThread();
                return debugger.getCurrentThread () != node;
            }
            
            public void perform (Object[] nodes) {
                if (nodes[0] instanceof MonitorModel.ThreadWithBordel) nodes[0] = ((MonitorModel.ThreadWithBordel) nodes[0]).getOriginalThread();
                ((JPDAThread) nodes [0]).makeCurrent ();
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );

    private static Action createGO_TO_SOURCE_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
            NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_ThreadAction_GoToSource_Label"),
            new DebuggingActionsProvider.LazyActionPerformer (requestProcessor) {
                public boolean isEnabled (Object node) {
                    if (node instanceof MonitorModel.ThreadWithBordel) node = ((MonitorModel.ThreadWithBordel) node).getOriginalThread();
                    return isGoToSourceSupported ((JPDAThread) node);
                }

                public void run (Object[] nodes) {
                    if (nodes[0] instanceof MonitorModel.ThreadWithBordel) nodes[0] = ((MonitorModel.ThreadWithBordel) nodes[0]).getOriginalThread();
                    String language = DebuggerManager.getDebuggerManager ().
                        getCurrentSession ().getCurrentLanguage ();
                    SourcePath sp = DebuggerManager.getDebuggerManager().getCurrentEngine().lookupFirst(null, SourcePath.class);
                    sp.showSource ((JPDAThread) nodes [0], language);
                }
            },
            Models.MULTISELECTION_TYPE_EXACTLY_ONE
        );
    }

    private Action createSUSPEND_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
            NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_ThreadAction_Suspend_Label"),
            new DebuggingActionsProvider.LazyActionPerformer (requestProcessor) {
                public boolean isEnabled (Object node) {
                    if (node instanceof MonitorModel.ThreadWithBordel) node = ((MonitorModel.ThreadWithBordel) node).getOriginalThread();
                    if (node instanceof JPDAThread)
                        return !((JPDAThread) node).isSuspended ();
                    else
                        return true;
                }
                public void run (Object[] nodes) {
                    int i, k = nodes.length;
                    for (i = 0; i < k; i++) {
                        Object node = (nodes[i] instanceof MonitorModel.ThreadWithBordel) ?
                                ((MonitorModel.ThreadWithBordel) nodes[i]).getOriginalThread() : nodes[i];
                        if (node instanceof JPDAThread)
                            ((JPDAThread) node).suspend ();
                        else
                            ((JPDAThreadGroup) node).suspend ();
                    }
                }
            },
            Models.MULTISELECTION_TYPE_ALL
        );
        
    }

    private Action createRESUME_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
            NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_ThreadAction_Resume_Label"),
            new DebuggingActionsProvider.LazyActionPerformer (requestProcessor) {
                public boolean isEnabled (Object node) {
                    if (node instanceof MonitorModel.ThreadWithBordel) node = ((MonitorModel.ThreadWithBordel) node).getOriginalThread();
                    if (node instanceof JPDAThread)
                        return ((JPDAThread) node).isSuspended ();
                    else
                        return true;
                }

                public void run (Object[] nodes) {
                    int i, k = nodes.length;
                    for (i = 0; i < k; i++) {
                        Object node = (nodes[i] instanceof MonitorModel.ThreadWithBordel) ?
                                ((MonitorModel.ThreadWithBordel) nodes[i]).getOriginalThread() : nodes[i];
                        if (node instanceof JPDAThread)
                            ((JPDAThread) node).resume ();
                        else
                            ((JPDAThreadGroup) node).resume ();
                    }
                }
            },
            Models.MULTISELECTION_TYPE_ALL
        );
    }
        
    private Action createINTERRUPT_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
            NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_ThreadAction_Interrupt_Label"),
            new DebuggingActionsProvider.LazyActionPerformer (requestProcessor) {
                public boolean isEnabled (Object node) {
                    if (node instanceof MonitorModel.ThreadWithBordel) node = ((MonitorModel.ThreadWithBordel) node).getOriginalThread();
                    if (node instanceof JPDAThread)
                        return !((JPDAThread) node).isSuspended ();
                    else
                        return false;
                }

                public void run (Object[] nodes) {
                    int i, k = nodes.length;
                    for (i = 0; i < k; i++) {
                        Object node = (nodes[i] instanceof MonitorModel.ThreadWithBordel) ?
                                ((MonitorModel.ThreadWithBordel) nodes[i]).getOriginalThread() : nodes[i];
                        if (node instanceof JPDAThread) {
                            ((JPDAThread) node).interrupt();
                        }
                    }
                }
            },
            Models.MULTISELECTION_TYPE_ALL
        );
    }
        
    private JPDADebugger debugger;
    
    
    public ThreadsActionsProvider (ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        RequestProcessor requestProcessor = lookupProvider.lookupFirst(null, RequestProcessor.class);
        SUSPEND_ACTION = createSUSPEND_ACTION(requestProcessor);
        RESUME_ACTION = createRESUME_ACTION(requestProcessor);
        INTERRUPT_ACTION = createINTERRUPT_ACTION(requestProcessor);
        GO_TO_SOURCE_ACTION = createGO_TO_SOURCE_ACTION(requestProcessor);
    }
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return new Action [0];
        if (node instanceof JPDAThreadGroup) {
            return new Action [] {
                RESUME_ACTION,
                SUSPEND_ACTION,
            };
        } else
        if (node instanceof JPDAThread) {
            JPDAThread t = (JPDAThread) node;
            boolean suspended = t.isSuspended ();
            Action a = null;
            if (suspended)
                a = RESUME_ACTION;
            else
                a = SUSPEND_ACTION;
            return new Action [] {
                MAKE_CURRENT_ACTION,
                a,
                INTERRUPT_ACTION,
                GO_TO_SOURCE_ACTION,
            };
        } else
        throw new UnknownTypeException (node);
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof JPDAThread)
            ((JPDAThread) node).makeCurrent ();
        else
        if (node instanceof JPDAThreadGroup) 
            return;
        else
        throw new UnknownTypeException (node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
    }

    private static boolean isGoToSourceSupported (JPDAThread t) {
        return t.isSuspended ();
    }
}

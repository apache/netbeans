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

package org.netbeans.modules.debugger.delegatingview;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;


import org.netbeans.modules.debugger.*;
import org.netbeans.modules.debugger.GUIManager.*;

import org.openide.TopManager;
import org.netbeans.modules.debugger.support.DebuggerModule;
import org.netbeans.modules.debugger.support.DelegatingView2;
import org.netbeans.modules.debugger.support.View2;
import org.netbeans.modules.debugger.support.View2Support;
import org.netbeans.modules.debugger.support.nodes.DebuggerNode;


/**
* Module installation class for HtmlModule
*
* @author Jan Jancura
*/
public class DVModule extends org.openide.modules.ModuleInstall {

    DelegatingView2 breakpointsView;
    DelegatingView2 threadsView;
    DelegatingView2 callStackView;
    DelegatingView2 sessionsView;
    DelegatingView2 watchesView; 
    DelegatingView2 classesView; 
    View2Support variablesListView;
    
    // ModuleInstall implementation ............................................
    
    /** Module installed again. */
    public void installed () {
        restored ();
    }
    
    /** Module installed again. */
    public void restored () {
        // create new "delegating" views
        breakpointsView = 
            new DelegatingView2 (new View2 [] {
                DebuggerModule.BREAKPOINTS_VIEW,//JPDA debugger
                new View2Support (              //tools debugger
                    DebuggerNode.getLocalizedString ("CTL_Breakpoints_view"), // NOI18N
                    "/org/netbeans/modules/debugger/resources/breakpoints", // NOI18N
                    "org.netbeans.modules.debugger.delegatingview.BreakpointsView", // NOI18N
                    false, // has not splitter
                    true,  // is in toolbar
                    true,  // visible
                    false  // separated
                )
            });
        threadsView = 
            new DelegatingView2 (new View2 [] {
                DebuggerModule.THREADS_VIEW,//JPDA debugger
                new View2Support (              //tools debugger
                    DebuggerNode.getLocalizedString ("CTL_Threads_view"), // NOI18N
                    "/org/netbeans/core/resources/threads", // NOI18N
                    "org.netbeans.modules.debugger.delegatingview.ThreadsView", // NOI18N
                    false, // has not splitter
                    true,  // is in toolbar
                    false,  // visible
                    false  // separated
                )
            });
        callStackView = 
            new DelegatingView2 (new View2 [] {
                DebuggerModule.CALL_STACK_VIEW,//JPDA debugger
                new View2Support (              //tools debugger
                    DebuggerNode.getLocalizedString ("CTL_Call_stack_view"), // NOI18N
                    "/org/netbeans/core/resources/callstack", // NOI18N
                    "org.netbeans.modules.debugger.delegatingview.CallStackView", // NOI18N
                    false, // has not splitter
                    true,  // is in toolbar
                    true,  // visible
                    false  // separated
                )
            });
        final DelegatingView2 sessionsView = 
            new DelegatingView2 (new View2 [] {
                DebuggerModule.SESSIONS_VIEW,//JPDA debugger
                new View2Support (              //tools debugger
                    DebuggerNode.getLocalizedString ("CTL_Sessions_view"), // NOI18N
                    "/org/netbeans/modules/debugger/multisession/resources/sessions", // NOI18N
                    "org.netbeans.modules.debugger.delegatingview.SessionsView", // NOI18N
                    false, // has not splitter
                    true,  // is in toolbar
                    false,  // visible
                    false  // separated
                )
            });
        watchesView = 
            new DelegatingView2 (new View2 [] {
                DebuggerModule.WATCHES_VIEW,//JPDA debugger
                new View2Support (              //tools debugger
                    DebuggerNode.getLocalizedString ("CTL_Watches_view"), // NOI18N
                    "/org/netbeans/core/resources/watches", // NOI18N
                    "org.netbeans.modules.debugger.delegatingview.WatchesView", // NOI18N
                    false, // has not splitter
                    true,  // is in toolbar
                    true,  // visible
                    false  // separated
                )
            });
        classesView = 
            new DelegatingView2 (new View2 [] {
                DebuggerModule.CLASSES_VIEW,  //JPDA debugger
                null                          //tools debugger
            });
        variablesListView = 
            new View2Support (              // visibility test
                DebuggerModule.VARIABLES_VIEW.getDisplayName (), // NOI18N
                DebuggerModule.VARIABLES_VIEW.getIconBase (),
                "org.netbeans.modules.debugger.support.nodes.VariablesView", // NOI18N
                DebuggerModule.VARIABLES_VIEW.hasFixedSize (), // has not splitter
                DebuggerModule.VARIABLES_VIEW.canBeHidden (),  // is in toolbar
                true,  // visible
                DebuggerModule.VARIABLES_VIEW.isSeparated ()  // separated
            );
            
        // replace original views
        View[] vs = GUIManager.getDefault ().getViews ();
        int i, k = vs.length;
        View[] nvs = new View [k];
        for (i = 0; i < k; i++)
            if (vs [i] == DebuggerModule.WATCHES_VIEW) {
                nvs [i] = watchesView;
            } else
            if (vs [i] == DebuggerModule.SESSIONS_VIEW) {
                nvs [i] = sessionsView;
            } else
            if (vs [i] == DebuggerModule.CALL_STACK_VIEW) {
                nvs [i] = callStackView;
            } else
            if (vs [i] == DebuggerModule.THREADS_VIEW) {
                nvs [i] = threadsView;
            } else
            if (vs [i] == DebuggerModule.BREAKPOINTS_VIEW) {
                nvs [i] = breakpointsView;
            } else
            if (vs [i] == DebuggerModule.CLASSES_VIEW) {
                nvs [i] = classesView;
            } else
            if (vs [i] == DebuggerModule.VARIABLES_VIEW) {
                nvs [i] = variablesListView;
            } else
                nvs [i] = vs [i];
        GUIManager.getDefault ().setViews (nvs);

        // add listener 
        try {
            final CoreDebugger cd = (CoreDebugger) TopManager.getDefault().getDebugger();
            cd.addPropertyChangeListener (new PropertyChangeListener () {
                public void propertyChange (PropertyChangeEvent e) {
                    if (e.getPropertyName () == null) return;
                    if (!e.getPropertyName ().equals (CoreDebugger.PROP_CURRENT_DEBUGGER)) return;
                    AbstractDebugger d = cd.getCurrentDebugger ();
                    if (d == null) return;
                    if (d.getClass ().getName ().indexOf ("JPDADebugger") >= 0) {
                        // switch to debugger1 views (JPDA debugger)
                        SwingUtilities.invokeLater (new Runnable () {
                            public void run () {
                                breakpointsView.setCurrentView (0);
                                classesView.setCurrentView (0);
                                sessionsView.setCurrentView (0);
                                threadsView.setCurrentView (0);
                                callStackView.setCurrentView (0);
                                watchesView.setCurrentView (0);
                                watchesView.refreshViews ();
                            }
                        });
                    } else
                    if (d.getClass ().getName ().indexOf ("ToolsDebugger") >= 0) {                        
                        // switch to debugger2 views (Tools debugger)
                        SwingUtilities.invokeLater (new Runnable () {
                            public void run () {
                                breakpointsView.setCurrentView (1);
                                classesView.setCurrentView (1);
                                sessionsView.setCurrentView (1);
                                threadsView.setCurrentView (1);
                                callStackView.setCurrentView (1);
                                watchesView.setCurrentView (1);
                                watchesView.refreshViews ();
                            }
                        });
                    }
                }
            });
        } catch (org.openide.debugger.DebuggerNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}


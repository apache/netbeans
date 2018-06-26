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

package org.netbeans.modules.web.debug.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.debug.Context;
import org.netbeans.modules.web.debug.breakpoints.JspLineBreakpoint;
import org.netbeans.modules.web.debug.util.Utils;
import org.netbeans.spi.debugger.ActionsProvider.Registration;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
*
* @author Martin Grebac, Libor Kotouc
*/
@Registration(actions={"runToCursor"}, activateForMIMETypes={"text/x-jsp"})
public class JspRunToCursorActionProvider extends ActionsProviderSupport {
    
    private static final RequestProcessor RP = new RequestProcessor(JspRunToCursorActionProvider.class);
    
    private JspLineBreakpoint breakpoint;
        
    {
        Listener listener = new Listener ();
        MainProjectManager.getDefault ().addPropertyChangeListener (listener);
        EditorContextDispatcher.getDefault().addPropertyChangeListener("text/x-jsp", listener);
        EditorContextDispatcher.getDefault().addPropertyChangeListener("text/x-tag", listener);
        DebuggerManager.getDebuggerManager ().addDebuggerListener (listener);

        setEnabledIfItShouldBe();
    }
    
    public Set getActions() {
        return Collections.singleton (ActionsManager.ACTION_RUN_TO_CURSOR);
    }
    
    public void doAction (Object action) {
        
        // 1) set breakpoint
        removeBreakpoint();
        createBreakpoint();
        // 2) start debugging of project
        final Lookup lkp = MainProjectManager.getDefault().getMainProject().getLookup();
        final ActionProvider ap = lkp.lookup(ActionProvider.class);
        try { // Do that in AWT because of issue #121374.
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    ap.invokeAction(ActionProvider.COMMAND_DEBUG, lkp);
                }
            });
        } catch (InterruptedException ex) {
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private Pair<Boolean, ? extends Callable<Boolean>> shouldBeEnabled () {

        if (!Utils.isJsp(Context.getCurrentFile())) {
            return Pair.of(false, null);
        }
        
        // check if current project supports this action
        Project p;
        if (SwingUtilities.isEventDispatchThread()) {
            final Future<Project> pl = MainProjectManager.getDefault ().getMainProjectLazy();
            if (pl.isDone()) {
                try {
                    p = pl.get();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                    return Pair.of(false, null);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                    return Pair.of(false, null);
                }
            } else {
                return Pair.of(null, new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        Project p = pl.get();
                        if (p == null) {
                            return false;
                        } else {
                            return isDebuggableProject(p);
                        }
                    }
                });
            }
        } else {
            p = MainProjectManager.getDefault ().getMainProject ();
        }
        // XXX revisit - should perhaps check selection?
        if (p == null) {
            return Pair.of(false, null);
        }
        return Pair.of(isDebuggableProject(p), null);
    }
    
    private static boolean isDebuggableProject(Project p) {
        ActionProvider actionProvider = (ActionProvider)p.getLookup ().lookup (ActionProvider.class);
        if (actionProvider == null) return false;

        String[] sa = actionProvider.getSupportedActions ();
        int i, k = sa.length;
        for (i = 0; i < k; i++) {
            if (ActionProvider.COMMAND_DEBUG.equals (sa [i])) {
                break;
            }
        }
        if (i == k) {
            return false;
        }

        // check if this action should be enabled
        return ((ActionProvider) p.getLookup ().lookup (
                ActionProvider.class
            )).isActionEnabled (
                ActionProvider.COMMAND_DEBUG, 
                p.getLookup ()
            );
    }
    
    private void setEnabledIfItShouldBe() {
        Pair<Boolean, ? extends Callable<Boolean>> shouldBeEnabled = shouldBeEnabled();
        if (shouldBeEnabled.first() != null) {
            setEnabled (
                ActionsManager.ACTION_RUN_TO_CURSOR,
                shouldBeEnabled.first()
            );
        } else {
            final Callable<Boolean> lazyEnable = shouldBeEnabled.second();
            RP.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Boolean enabled = lazyEnable.call();
                        setEnabled (
                            ActionsManager.ACTION_RUN_TO_CURSOR,
                            enabled
                        );
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
    }

    private void createBreakpoint() {
        breakpoint = JspLineBreakpoint.create (
            Context.getCurrentURL (),
            Context.getCurrentLineNumber ()
        );
        breakpoint.setHidden (true);
        DebuggerManager.getDebuggerManager ().addBreakpoint (breakpoint);
    }
    
    private void removeBreakpoint() {
        if (breakpoint != null) {
            DebuggerManager.getDebuggerManager ().removeBreakpoint (breakpoint);
            breakpoint = null;
        }
    }
    
    private class Listener implements PropertyChangeListener, DebuggerManagerListener {
        public void propertyChange (PropertyChangeEvent e) {
            if (e == null)
                return;
            if (e.getPropertyName () == JPDADebugger.PROP_STATE) {
                int state = ((Integer) e.getNewValue ()).intValue ();
                if (state == JPDADebugger.STATE_DISCONNECTED || state == JPDADebugger.STATE_STOPPED)
                    removeBreakpoint ();
                return;
            }

            setEnabledIfItShouldBe();
        }
        
        public void sessionRemoved (Session session) {
            removeBreakpoint();
        }
        
        public void breakpointAdded (Breakpoint breakpoint) {}
        public void breakpointRemoved (Breakpoint breakpoint) {}
        public Breakpoint[] initBreakpoints () {
            return new Breakpoint [0];
        }
        public void initWatches () {}
        public void sessionAdded (Session session) {}
        public void watchAdded (Watch watch) {}
        public void watchRemoved (Watch watch) {}

        public void engineAdded (DebuggerEngine engine) {
            JPDADebugger debugger = (JPDADebugger) engine.lookupFirst 
                (null, JPDADebugger.class);
            if (debugger == null) return;
            debugger.addPropertyChangeListener (
                JPDADebugger.PROP_STATE,
                this
            );
        }
        
        public void engineRemoved (DebuggerEngine engine) {
            JPDADebugger debugger = (JPDADebugger) engine.lookupFirst 
                (null, JPDADebugger.class);
            if (debugger == null) return;
            debugger.removePropertyChangeListener (
                JPDADebugger.PROP_STATE,
                this
            );
        }

    }
}

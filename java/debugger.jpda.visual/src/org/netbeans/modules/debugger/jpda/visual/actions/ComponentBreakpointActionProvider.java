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
package org.netbeans.modules.debugger.jpda.visual.actions;

import com.sun.jdi.ObjectReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.visual.JavaComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.RemoteAWTScreenshot.AWTComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.breakpoints.AWTComponentBreakpoint;
import org.netbeans.modules.debugger.jpda.visual.breakpoints.ComponentBreakpoint;
import org.netbeans.modules.debugger.jpda.visual.breakpoints.FXComponentBreakpoint;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.openide.nodes.Node;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 *
 * @author Martin Entlicher
 */
@ActionsProvider.Registration(path="netbeans-JPDASession", actions={ "toggleBreakpoint" }, activateForMIMETypes={ "text/x-debugger-visual-component" })
public class ComponentBreakpointActionProvider extends ActionsProviderSupport {
    
    public ComponentBreakpointActionProvider() {
        final Result<Node> nodeLookupResult = Utilities.actionsGlobalContext().lookupResult(Node.class);
        LookupListener ll = new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                Collection<? extends Node> nodeInstances = nodeLookupResult.allInstances();
                for (Node n : nodeInstances) {
                    JavaComponentInfo ci = n.getLookup().lookup(JavaComponentInfo.class);
                    if (ci != null) {
                        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
                        return ;
                    }
                }
                setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, false);
            }
        };
        nodeLookupResult.addLookupListener(ll);
        ll.resultChanged(null); // To initialize
    }

    @Override
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }
    
    @Override
    public void doAction(Object action) {
        Collection<? extends Node> activatedNodes = Utilities.actionsGlobalContext().lookupAll(Node.class);
        doAction(activatedNodes.toArray(new Node[]{}));
    }
    
    static void doAction(Node[] activatedNodes) {
        for (Node n : activatedNodes) {
            JavaComponentInfo ci = n.getLookup().lookup(JavaComponentInfo.class);
            if (ci != null) {
                ObjectReference component = ci.getComponent();
                ComponentBreakpoint b = findBreakpoint(component);
                if (b == null) {
                    JPDADebugger debugger = ci.getThread().getDebugger();
                    b = (ci instanceof AWTComponentInfo) ? 
                            new AWTComponentBreakpoint(
                                new ComponentBreakpoint.ComponentDescription(ci, debugger, component)
                            ) :
                            new FXComponentBreakpoint(
                                new ComponentBreakpoint.ComponentDescription(ci, debugger, component)
                            );
                    DebuggerManager.getDebuggerManager().addBreakpoint(b);
                } else {
                    DebuggerManager.getDebuggerManager().removeBreakpoint(b);
                }
            }
        }
        
    }

    public static ComponentBreakpoint findBreakpoint (ObjectReference component) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (int i = 0; i < breakpoints.length; i++) {
            if (!(breakpoints[i] instanceof ComponentBreakpoint)) {
                continue;
            }
            ComponentBreakpoint ab = (ComponentBreakpoint) breakpoints[i];
            Session currentSession = DebuggerManager.getDebuggerManager().getCurrentSession();
            if (currentSession != null) {
                JPDADebugger debugger = currentSession.lookupFirst(null, JPDADebugger.class);
                if (debugger != null) {
                    if (component.equals(ab.getComponent().getComponent(debugger))) {
                        return ab;
                    }
                }
            }
        }
        return null;
    }
    
}

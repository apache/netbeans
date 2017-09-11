/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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

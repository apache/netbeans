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

import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.visual.JavaComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.JavaComponentInfo.Stack;
import org.netbeans.modules.debugger.jpda.visual.JavaComponentInfo.Stack.Frame;
import org.netbeans.modules.debugger.jpda.visual.RemoteServices.RemoteListener;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * Go to the component source.
 * 
 * @author Martin Entlicher
 */
public class GoToAddIntoHierarchyAction extends NodeAction {
    
    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node n : activatedNodes) {
            JavaComponentInfo ci = n.getLookup().lookup(JavaComponentInfo.class);
            if (ci != null) {
                Stack stack = ci.getAddCallStack();
                String type = null;
                int line = -1;
                if (stack != null) {
                    Frame[] frames = stack.getFrames();
                    for (int i = 0; i < frames.length; i++) {
                        String className = frames[i].getClassName();
                        String methodName = frames[i].getMethodName();
                        if (!JavaComponentInfo.isCustomType(className) &&
                            (methodName.startsWith("add") || 
                             methodName.equals("createHierarchyEvents") ||
                             methodName.startsWith("onChanged") ||
                             methodName.startsWith("setParent") ||
                             methodName.startsWith("callObservers")
                            )
                        ){   // NOI18N
                            continue;
                        }
                        type = className;
                        line = frames[i].getLineNumber();
                        break;
                    }
                    if (type == null) {
                        Frame f = (frames.length > 1) ? frames[1] : frames[0];
                        type = f.getClassName();
                        line = f.getLineNumber();
                    }
                } else {
                    return;
                }
                final String showType = type;
                final int showLine = line;
                GoToSourceAction.RP.post(new Runnable() {
                    @Override
                    public void run() {
                        showSource(showType, showLine);
                    }
                });
            }
        }
    }
    
    private void showSource(String type, final int lineNumber) {
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (engine != null) {
            JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
            type = EditorContextBridge.getRelativePath (type);
            final String url = ((JPDADebuggerImpl) debugger).getEngineContext().getURL(type, true);
            //System.err.println("Going to show "+url+" for type = "+type+", line = "+lineNumber);
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    EditorContextBridge.getContext().showSource(url, lineNumber, null);
                }
            });
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        for (Node n : activatedNodes) {
            JavaComponentInfo ci = n.getLookup().lookup(JavaComponentInfo.class);
            if (ci != null) {
                Stack stack = ci.getAddCallStack();
                if (stack != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GoToAddIntoHierarchyAction.class, "CTL_GoToHierarchyAdditionSource");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(GoToAddIntoHierarchyAction.class);
    }

    
}

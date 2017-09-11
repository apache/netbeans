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

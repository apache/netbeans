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

import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.ui.SourcePath;
import org.netbeans.modules.debugger.jpda.visual.JavaComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.JavaComponentInfo.FieldInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Go to the component source.
 * 
 * @author Martin Entlicher
 */
public class GoToFieldDeclarationAction extends NodeAction {
    
    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node n : activatedNodes) {
            final JavaComponentInfo ci = n.getLookup().lookup(JavaComponentInfo.class);
            if (ci != null) {
                final FieldInfo fieldInfo = ci.getField();
                if (fieldInfo != null) {
                    GoToSourceAction.RP.post(new Runnable() {
                        @Override
                        public void run() {
                            org.netbeans.api.debugger.jpda.Field fieldVariable;
                            JPDADebuggerImpl debugger = ci.getThread().getDebugger();
                            Variable variable = debugger.getVariable(fieldInfo.getParent().getComponent());
                            fieldVariable = ((ObjectVariable) variable).getField(fieldInfo.getName());
                            SourcePath ectx = debugger.getSession().lookupFirst(null, SourcePath.class);
                            ectx.showSource (fieldVariable);
                        }
                    });
                } else {
                    NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(GoToFieldDeclarationAction.class, "MSG_NoFieldInfo"), NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(d);
                }
            }
        }
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean enabled = false;
        for (Node n : activatedNodes) {
            JavaComponentInfo ci = n.getLookup().lookup(JavaComponentInfo.class);
            if (ci != null) {
                final FieldInfo fieldInfo = ci.getField();
                if (fieldInfo != null) {
                    enabled = true;
                    break;
                }
            }
        }
        return enabled;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GoToFieldDeclarationAction.class, "CTL_GoToFieldDeclaration");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(GoToFieldDeclarationAction.class);
    }

    
}

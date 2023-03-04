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
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.ui.FixedWatchesManager;
import org.netbeans.modules.debugger.jpda.visual.JavaComponentInfo;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Add the component as a fixed watch so that it can be examined there.
 * 
 * @author Martin Entlicher
 */
public class CreateFixedWatchAction extends NodeAction {

    @Override
    public String getName() {
        return NbBundle.getMessage(GoToSourceAction.class, "CTL_CreateFixedWatch");
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean enabled = false;
        for (Node n : activatedNodes) {
            JavaComponentInfo ci = n.getLookup().lookup(JavaComponentInfo.class);
            if (ci != null) {
                enabled = true;
                break;
            }
        }
        return enabled;
        
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node n : activatedNodes) {
            JavaComponentInfo ci = n.getLookup().lookup(JavaComponentInfo.class);
            if (ci != null) {
                addFixedWatch(ci);
            }
        }
    }
    
    private void addFixedWatch(JavaComponentInfo ci) {
        JPDADebuggerImpl debugger = ci.getThread().getDebugger();
        List list = debugger.getSession().lookup("WatchesView", NodeActionsProviderFilter.class); // NOI18N
        FixedWatchesManager fwman = null;
        for (Iterator iter = list.iterator(); iter.hasNext();)  {
            Object obj = iter.next();
            if (obj instanceof FixedWatchesManager) {
                fwman = (FixedWatchesManager) obj;
                break;
            }
        }
        if (fwman != null) {
            ObjectReference component = ci.getComponent();
            fwman.addFixedWatch(ci.getDisplayName(), debugger.getVariable(component));
        }
        
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CreateFixedWatchAction.class);
    }
    
}

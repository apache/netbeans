/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.hibernate.hqleditor.ui;

import org.netbeans.modules.hibernate.hqleditor.HQLEditorController;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action which shows HQLEditor component.
 * 
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HQLEditorAction extends NodeAction {
    
    public HQLEditorAction() {
        super();
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected void performAction(final Node[] activatedNodes) {
        new HQLEditorController().init(activatedNodes);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage(HQLEditorAction.class, "CTL_HQLEditorAction");
    }
}

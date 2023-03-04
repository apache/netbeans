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

package org.netbeans.modules.form.actions;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.CodeCustomizer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

public class CustomCodeAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        RADComponent metacomp = getComponent(activatedNodes);
        if (metacomp != null)
            CodeCustomizer.show(metacomp);
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return getComponent(activatedNodes) != null;
    }

    private static RADComponent getComponent(Node[] nodes) {
        if (nodes != null && nodes.length == 1) {
            RADComponentCookie radCookie = nodes[0].getCookie(RADComponentCookie.class);
            if (radCookie != null) {
                RADComponent metacomp = radCookie.getRADComponent();
                if (metacomp != null && metacomp != metacomp.getFormModel().getTopRADComponent())
                    return metacomp;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(CustomCodeAction.class, "ACT_CustomCode"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP; // "gui.custom_code" ?
    }
    
}

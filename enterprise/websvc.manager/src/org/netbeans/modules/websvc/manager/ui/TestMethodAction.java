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

package org.netbeans.modules.websvc.manager.ui;

import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author qn145415
 */
public class TestMethodAction extends NodeAction {

    public TestMethodAction() {
        super();
    }

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            return activatedNodes[0].getLookup().lookup(WsdlSaasMethod.class) != null;
        }
        return false;
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/visualweb/saas/ui/resources/ActionIcon.gif";
    }
    
    public String getName() {
        return NbBundle.getMessage(TestMethodAction.class, "TEST_METHOD");
    }
    
    protected void performAction(org.openide.nodes.Node[] nodes) {
        if (nodes != null && nodes.length == 1) {
            WsdlSaasMethod method = nodes[0].getLookup().lookup(WsdlSaasMethod.class);
            if (method != null) {
                if (method.getSaas().getState() == Saas.State.READY) {
                    if (method.getJavaMethod() != null) {
                        TestWebServiceMethodDlg testDialog = new TestWebServiceMethodDlg(method);
                        testDialog.displayDialog();
                    } else {
                        throw new IllegalArgumentException("Could not get javaMethod for operation "+method);
                    }
                } else {
                    method.getSaas().toStateReady(false);
                }
            }
        }
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
}

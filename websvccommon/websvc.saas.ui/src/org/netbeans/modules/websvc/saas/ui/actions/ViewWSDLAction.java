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

package org.netbeans.modules.websvc.saas.ui.actions;

import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.netbeans.modules.websvc.saas.model.WsdlSaasPort;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action that displays WSDL for a selected web service.
 *
 * @author nam
 * @author Jan Stola
 */
public class ViewWSDLAction extends NodeAction {

    @Override
    protected boolean enable(Node[] nodes) {
        boolean enabled = true;
        for (Node node : nodes) {
            WsdlSaas saas = getWsdlSaas(node);
            if (saas == null || (saas.getState() != Saas.State.RETRIEVED
                    && saas.getState() != Saas.State.READY)) {
                enabled = false;
                break;
            }
        }
        return enabled;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ViewWSDLAction.class, "VIEW_WSDL"); // NOI18N
    }

    private WsdlSaas getWsdlSaas(Node node) {
        WsdlSaas saas = node.getLookup().lookup(WsdlSaas.class);
        if (saas == null) {
            WsdlSaasPort port = node.getLookup().lookup(WsdlSaasPort.class);
            if (port != null) {
                saas = port.getParentSaas();
            }
        }
        if (saas == null) {
            WsdlSaasMethod method = node.getLookup().lookup(WsdlSaasMethod.class);
            if (method != null) {
                saas = method.getSaas();
            }
        }
        return saas;
    }

    @Override
    protected void performAction(Node[] nodes) {
        for (Node node :  nodes) {
            WsdlSaas saas = getWsdlSaas(node);
            String location = saas.getWsdlData().getWsdlFile();
            FileObject wsdlFileObject = saas.getLocalWsdlFile();

            if (wsdlFileObject == null) {
                String errorMessage = NbBundle.getMessage(ViewWSDLAction.class, "WSDL_FILE_NOT_FOUND", location); // NOI18N
                NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                DialogDisplayer.getDefault().notify(d);
                continue;
            }

            //TODO: open in read-only mode
            try {
                DataObject wsdlDataObject = DataObject.find(wsdlFileObject);
                EditorCookie editorCookie = wsdlDataObject.getLookup().lookup(EditorCookie.class);
                editorCookie.open();
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    @Override
    public boolean asynchronous() {
        return true;
    }

}

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
package org.netbeans.modules.docker.ui.node;

import java.awt.Dialog;
import java.util.List;
import javax.swing.JButton;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerContainer;
import org.netbeans.modules.docker.api.DockerContainerDetail;
import org.netbeans.modules.docker.api.DockerException;
import org.netbeans.modules.docker.api.PortMapping;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * Requesting container port mappings
 * @author Danila Sergeyev
 */
public class GetPortMappingsAction extends AbstractContainerAction  {

    @NbBundle.Messages("LBL_GetPortMappingsAction=View Port Bindings")
    public GetPortMappingsAction() {
        super(Bundle.LBL_GetPortMappingsAction());
    }

    @NbBundle.Messages({
        "LBL_PortBindings=Port Bindings",
    })
    @Override
    protected void performAction(DockerContainer container) throws DockerException {
        DockerAction facade = new DockerAction(container.getInstance());
        DockerContainerDetail details = facade.getDetail(container);
        List<PortMapping> portMappings = details.portMappings();
        final ViewPortBindingsPanel panel = new ViewPortBindingsPanel(portMappings);
        
        DialogDescriptor descriptor = new DialogDescriptor(panel, Bundle.LBL_PortBindings(),
                                                           true, new Object[] {DialogDescriptor.OK_OPTION}, null,
                                                           DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setVisible(true);
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
    }

    @NbBundle.Messages({
        "# {0} - container id",
        "MSG_GettingPortMappings=Getting port bindings for container {0}"
    })
    @Override
    protected String getProgressMessage(DockerContainer container) {
        return Bundle.MSG_GettingPortMappings(container.getShortId());
    }
    
    @Override
    protected boolean isEnabled(DockerContainerDetail detail) {
        return detail.getStatus() == DockerContainer.Status.RUNNING;
    }
    
}

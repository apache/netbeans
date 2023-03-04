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

package org.netbeans.modules.websvc.wsitconf.ui.client;

import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Grebac
 */
public class ClientPanelFactory implements org.netbeans.modules.xml.multiview.ui.InnerPanelFactory {
    
    private ToolBarDesignEditor editor;
    private WSDLModel clientModel;
    private WSDLModel serviceModel;
    private Node node;
    private JaxWsModel jaxwsmodel;
    
    /**
     * Creates a new instance of ClientPanelFactory
     */
    ClientPanelFactory(ToolBarDesignEditor editor, WSDLModel model, Node node, WSDLModel serviceModel, JaxWsModel jxwsmodel) {
        this.editor=editor;
        this.clientModel = model;
        this.serviceModel = serviceModel;
        this.jaxwsmodel = jxwsmodel;
        this.node = node;        
    }
    
    public SectionInnerPanel createInnerPanel(Object key) {
        if (key instanceof String) {
            String id = (String)key;
            if (id.startsWith(ClientView.CALLBACK_NODE_ID)) {
                Binding b = PolicyModelHelper.getBinding(clientModel, id.substring(ClientView.CALLBACK_NODE_ID.length()));
                return new CallbackPanel((SectionView) editor.getContentView(), node, b, jaxwsmodel, serviceModel);
            }
            if (id.startsWith(ClientView.STS_NODE_ID)) {
                Binding b = PolicyModelHelper.getBinding(clientModel, id.substring(ClientView.STS_NODE_ID.length()));
                return new STSClientPanel((SectionView) editor.getContentView(), node, b, jaxwsmodel);
            }
            if (id.startsWith(ClientView.TRANSPORT_NODE_ID)) {
                Binding b = PolicyModelHelper.getBinding(clientModel, id.substring(ClientView.TRANSPORT_NODE_ID.length()));
                return new TransportPanelClient((SectionView) editor.getContentView(), node, b, jaxwsmodel);
            }
            if (id.startsWith(ClientView.ADVANCEDCONFIG_NODE_ID)) {
                Binding b = PolicyModelHelper.getBinding(clientModel, id.substring(ClientView.ADVANCEDCONFIG_NODE_ID.length()));
                return new AdvancedConfigPanelClient((SectionView) editor.getContentView(), node, b, serviceModel);
            }
        }
        return null;
    }

}

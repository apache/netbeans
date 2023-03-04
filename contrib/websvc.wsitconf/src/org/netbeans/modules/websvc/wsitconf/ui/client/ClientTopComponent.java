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

import java.util.Collection;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.xml.multiview.ui.InnerPanelFactory;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import java.awt.*;
import java.util.Iterator;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.xml.wsdl.model.Service;

/**
 * @author Martin Grebac
 */
public class ClientTopComponent extends TopComponent {

    static final long serialVersionUID=6021472310161712674L;
    private boolean initialized = false;
    private InnerPanelFactory panelFactory = null;

    private JaxWsModel jaxWsModel;
    private WSDLModel clientWsdlModel;
    private WSDLModel serviceModel;
    private Client client;
    private Node node;

    private JAXWSLightSupport jaxWsSupport;
    private JaxWsService jaxWsService;
    
    private Service service;

    public ClientTopComponent(Client client, JaxWsModel jaxWsModel, WSDLModel clientWsdlModel, WSDLModel serviceWsdlModel, Node node) {
        setLayout(new BorderLayout());
        this.jaxWsModel = jaxWsModel;
        this.clientWsdlModel = clientWsdlModel;
        this.serviceModel = serviceWsdlModel;
        this.initialized = false;
        this.client = client;
        this.node = node;
    }

    public ClientTopComponent(JAXWSLightSupport jaxWsSupport, JaxWsService jaxService, WSDLModel clientWsdlModel, WSDLModel serviceWsdlModel, Node node) {
        setLayout(new BorderLayout());
        this.clientWsdlModel = clientWsdlModel;
        this.serviceModel = serviceWsdlModel;
        this.initialized = false;
        this.jaxWsSupport = jaxWsSupport;
        this.jaxWsService = jaxService;
        this.client = null;
        this.jaxWsModel = null;
        this.node = node;
    }
    
    @Override
    protected String preferredID(){
        return "WSITClientTopComponent";    //NOI18N
    }

    public ClientTopComponent(Service service, WSDLModel clientWsdlModel, WSDLModel serviceWsdlModel, Node node) {
        setLayout(new BorderLayout());
        this.service = service;
        this.clientWsdlModel = clientWsdlModel;
        this.serviceModel = serviceWsdlModel;
        this.initialized = false;
        this.node = node;
    } 

    private Service getService(String name, WSDLModel m) {
        if ((name != null) && (m != null) && (m.getDefinitions()!=null)) {
            Collection services = m.getDefinitions().getServices();
            if (services != null) {
                Iterator i = services.iterator();
                Service s = null;
                while (i.hasNext()) {
                    s = (Service)i.next();
                    if ((s != null) && ((name.equals(s.getName())) || (services.size() == 1))) {
                        return s;
                    }
                }
            }
        }    
        return null;
    }
    
    /**
     * #38900 - lazy addition of GUI components
     */
    private void doInitialize() {
        initAccessibility();

        ToolBarDesignEditor tb = new ToolBarDesignEditor();
        panelFactory = new ClientPanelFactory(tb, clientWsdlModel, node, serviceModel, jaxWsModel);

        ClientView mview = null;
        if (jaxWsService == null) {
            Service s = service;
            if (client != null) {
                s = getService(client.getName(), clientWsdlModel); //TODO - the client name just won't work!!!
            }
            mview = new ClientView(panelFactory, clientWsdlModel, serviceModel, 
                    s==null ? null : s.getPorts());
        } else {
            mview = new ClientView(panelFactory, clientWsdlModel, serviceModel, null);
        }
        if (mview != null) {
            tb.setContentView(mview);
            add(tb);
        }
        setFocusable(true);
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    private void initAccessibility(){
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ClientTopComponent.class, "ACS_Tab_DESC")); // NOI18N
    }

    /**
     * #38900 - lazy addition of GUI components
     */    
    @Override
    public void addNotify() {
        if (!initialized) {
            initialized = true;
            doInitialize();
        }
        super.addNotify();
    }
    
    /**
     * Called when <code>TopComponent</code> is about to be shown.
     * Shown here means the component is selected or resides in it own cell
     * in container in its <code>Mode</code>. The container is visible and not minimized.
     * <p><em>Note:</em> component
     * is considered to be shown, even its container window
     * is overlapped by another window.</p>
     * @since 2.18
     *
     * #38900 - lazy addition of GUI components
     *
     */
    @Override
    protected void componentShowing() {
        if (!initialized) {
            initialized = true;
            doInitialize();
        }
        super.componentShowing();
    }
    
}


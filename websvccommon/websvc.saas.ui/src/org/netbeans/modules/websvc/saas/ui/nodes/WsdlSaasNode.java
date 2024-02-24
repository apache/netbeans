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

package org.netbeans.modules.websvc.saas.ui.nodes;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.Action;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.netbeans.modules.websvc.saas.model.WsdlSaasPort;
import org.netbeans.modules.websvc.saas.spi.ConsumerFlavorProvider;
import org.netbeans.modules.websvc.saas.ui.actions.ViewWSDLAction;
import org.netbeans.modules.websvc.saas.util.SaasTransferable;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.ExTransferable.Single;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author nam
 */
public class WsdlSaasNode extends SaasNode {
    private ExTransferable transferable;
    
    public WsdlSaasNode(WsdlSaas saas) {
        this(saas, new InstanceContent());
    }
    
    protected WsdlSaasNode(WsdlSaas saas, InstanceContent content) {
        super(new WsdlSaasNodeChildren(saas), new AbstractLookup(content), saas);
        content.add(saas);
        transferable = ExTransferable.create(
            new SaasTransferable<WsdlSaas>(saas, SaasTransferable.WSDL_SERVICE_FLAVORS));
    }
    
    private static WsdlSaasPort getDefaultPort(WsdlSaas saas) {
        if (saas.getPorts().isEmpty()) {
            return null;
        }
        return saas.getPorts().get(0);
    } 
    
    @Override
    public WsdlSaas getSaas() {
        return (WsdlSaas) super.getSaas();
    }
    
    private static final java.awt.Image ICON =
       ImageUtilities.loadImage( "org/netbeans/modules/websvc/saas/ui/resources/webservice.png" ); //NOI18N
    
    @Override
    public Image getGenericIcon(int type) {
        return ICON;
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>(Arrays.asList(super.getActions(context)));
        actions.add(SystemAction.get(ViewWSDLAction.class));

        return actions.toArray(new Action[0]);
    }
    
    /**
     * Create a property sheet for the individual W/S port node. The properties sheet contains the
     * the following properties:
     *  - WSDL URL
     *  - Endpoint Address
     *
     * @return property sheet for the data source nodes
     */
    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Set ss = sheet.get("data"); // NOI18N
        
        if (ss == null) {
            ss = new Set();
            ss.setName("data");  // NOI18N
            ss.setDisplayName(NbBundle.getMessage(WsdlSaasNode.class, "WS_INFO")); // NOI18N
            ss.setShortDescription(NbBundle.getMessage(WsdlSaasNode.class, "WS_INFO")); // NOI18N
            sheet.put(ss);
        }
        
        // Service name (from the wsdl)
        ss.put( new PropertySupport.ReadOnly<String>( "name", // NOI18N
                String.class,
                NbBundle.getMessage(WsdlSaasNode.class, "PORT_NAME_IN_WSDL"), // NOI18N
                NbBundle.getMessage(WsdlSaasNode.class, "PORT_NAME_IN_WSDL")) { // NOI18N
            @Override
            public String getValue() {
                return getName();
            }
        });
        
        // URL for the wsdl file (entered by the user)
        ss.put( new PropertySupport.ReadOnly<String>( "URL", // NOI18N
                String.class,
                NbBundle.getMessage(WsdlSaasNode.class, "WS_URL"), // NOI18N
                NbBundle.getMessage(WsdlSaasNode.class, "WS_URL")) { // NOI18N
            @Override
            public String getValue() {
                return saas.getUrl();
            }
        });
        
        return sheet;
    }
    
    private boolean portAdded;
    
    @Override
    public Transferable clipboardCopy() throws IOException {
        if (getSaas().getState() != Saas.State.READY) {
            getSaas().toStateReady(true);
            return super.clipboardCopy();
        }
        if (! portAdded) {
            final WsdlSaasPort port = getDefaultPort(getSaas());
            if (port != null) {
                transferable.put(new Single(ConsumerFlavorProvider.PORT_FLAVOR) {
                    @Override
                    protected Object getData() throws IOException, UnsupportedFlavorException {
                        return port;
                    }

                });
            }
            portAdded = true;
        }
        return SaasTransferable.addFlavors(transferable);
    }
    
}

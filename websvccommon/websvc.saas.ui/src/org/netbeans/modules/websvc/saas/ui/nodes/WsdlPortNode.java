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
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.WsdlSaasPort;
import org.netbeans.modules.websvc.saas.util.SaasTransferable;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author nam
 */
public class WsdlPortNode extends AbstractNode {
    private WsdlSaasPort port;
    private Transferable transferable;

    public WsdlPortNode(WsdlSaasPort port) {
        this(port, new InstanceContent());
    }
    
    private WsdlPortNode(WsdlSaasPort port, InstanceContent content) {
        super(new WsdlPortNodeChildren(port), new AbstractLookup(content));
        this.port = port;
        content.add(port);
        transferable = ExTransferable.create(
                new SaasTransferable(port,SaasTransferable.WSDL_PORT_FLAVORS));
    }    
    
    @Override
    public String getDisplayName() {
        return port.getName();
    }
    
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = SaasNode.getActions(getLookup());
        return actions.toArray(new Action[0]);
    }
    
    @Override
    public Action getPreferredAction() {
        Action[] actions = getActions(true);
        return actions.length > 0 ? actions[0] : null;
    }
    
    private static final java.awt.Image ICON =
       ImageUtilities.loadImage( "org/netbeans/modules/websvc/saas/ui/resources/wsport-closed.png" ); //NOI18N
    private static final java.awt.Image OPENED_ICON =
       ImageUtilities.loadImage( "org/netbeans/modules/websvc/saas/ui/resources/wsport-open.png" ); //NOI18N
    
    @Override
    public Image getIcon(int type) {
        return ICON;
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return OPENED_ICON;
    }

    /**
     * Create a property sheet for the individual W/S port node. The properties sheet contains the
     * the following properties:
     *  - Name of the port
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
            ss.setDisplayName(NbBundle.getMessage(WsdlPortNode.class, "WS_INFO")); // NOI18N
            ss.setShortDescription(NbBundle.getMessage(WsdlPortNode.class, "WS_INFO")); // NOI18N
            sheet.put(ss);
        }
        
        // Port name (from the wsdl)
        ss.put( new PropertySupport.ReadOnly<String>( "port", // NOI18N
                String.class,
                NbBundle.getMessage(WsdlPortNode.class, "PORT_NAME_IN_WSDL"), // NOI18N
                NbBundle.getMessage(WsdlPortNode.class, "PORT_NAME_IN_WSDL")) { // NOI18N
            @Override
            public String getValue() {
                String portName = port.getName();
                return portName;
            }
        });
        
        // URL for the wsdl file (entered by the user)
        ss.put( new PropertySupport.ReadOnly<String>( "URL", // NOI18N
                String.class,
                NbBundle.getMessage(WsdlPortNode.class, "WS_URL"), // NOI18N
                NbBundle.getMessage(WsdlPortNode.class, "WS_URL")) { // NOI18N
            @Override
            public String getValue() {
                return port.getParentSaas().getUrl();
            }
        });
        
        return sheet;
    }
    
    // Handle copying and cutting specially:    
    @Override
    public boolean canCopy() {
        return true;
    }
    @Override
    public boolean canCut() {
        return false;
    }
    
    @Override
    public Transferable clipboardCopy() throws IOException {
        if (port.getParentSaas().getState() != Saas.State.READY) {
            port.getParentSaas().toStateReady(false);
            return super.clipboardCopy();
        }
        return SaasTransferable.addFlavors(transferable);
    }
}

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
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.ui.actions.AddGroupAction;
import org.netbeans.modules.websvc.saas.ui.actions.AddServiceAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author nam
 */
@ServicesTabNodeRegistration(
    position=210,
    name="rootSaasGroup",
    displayName="org.netbeans.modules.websvc.saas.ui.nodes.Bundle#Web_Services",
    shortDescription="org.netbeans.modules.websvc.saas.ui.nodes.Bundle#Web_Services_Desc",
    iconResource="org/netbeans/modules/websvc/saas/ui/resources/webservicegroup.png"
)
public class SaasServicesRootNode extends AbstractNode {
    
    public SaasServicesRootNode() {
        this(new RootNodeChildren(SaasServicesModel.getInstance().getInitialRootGroup()), new InstanceContent());
    }

    SaasServicesRootNode(RootNodeChildren children, InstanceContent content) {
        super(children, new AbstractLookup(content));
        content.add(SaasServicesModel.getInstance().getInitialRootGroup());
    }
    
    @Override
    public String getName() {
        return "rootSaasGroup"; // NOI18N
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SaasServicesRootNode.class, "Web_Services"); // NOI18N
    }
    
    @Override
    public String getShortDescription() {
        return NbBundle.getMessage(SaasServicesRootNode.class, "Web_Services_Desc"); // NOI18N
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = SaasNode.getActions(getLookup());
        actions.add(SystemAction.get(AddServiceAction.class));
        actions.add(SystemAction.get(AddGroupAction.class));
        return actions.toArray(new Action[0]);
    }
    
    static final java.awt.Image ICON =
            ImageUtilities.loadImage( "org/netbeans/modules/websvc/saas/ui/resources/webservicegroup.png" ); //NOI18N
    
    @Override
    public Image getIcon(int type){
        return ICON;
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return ICON;
    }
    
    static class RootNodeChildren extends SaasGroupNodeChildren {

        public RootNodeChildren(SaasGroup group) {
            super(group);
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() == SaasServicesModel.getInstance().getRootGroup() &&
                SaasServicesModel.getInstance().getState() == SaasServicesModel.State.READY) {
                updateKeys();
            }
            super.propertyChange(evt);
        }
    
        @Override
        protected void updateKeys() {
            if (needsWait()) {
                setKeys(SaasNodeChildren.WAIT_HOLDER);
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        SaasServicesModel.getInstance().initRootGroup();
                    }
                });
            } else {
                super.updateKeys();
            }
        }
        
        private boolean needsWait() {
            return SaasServicesModel.getInstance().getState() != SaasServicesModel.State.READY;
        }
        
        @Override
        protected Node[] createNodes(Object key) {
            if (needsWait()) {
                return SaasNodeChildren.getWaitNode();
            }
            return super.createNodes(key);
        }
    }
}

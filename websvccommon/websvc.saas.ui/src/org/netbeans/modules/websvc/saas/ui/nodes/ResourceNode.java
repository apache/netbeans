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
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.websvc.saas.model.WadlSaasResource;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author nam
 */
public class ResourceNode extends AbstractNode {
    private final WadlSaasResource resource;
    
    public ResourceNode(WadlSaasResource resource) {
        this(resource, new InstanceContent());
    }

    protected ResourceNode(WadlSaasResource resource, InstanceContent content) {
        super(new ResourceNodeChildren(resource), new AbstractLookup(content));
        this.resource = resource;
        content.add(resource);
    }

    public Resource getResource() {
        return resource.getResource();
    }
    
    @Override
    public String getDisplayName() {
        return "[" + getResource().getPath() + "]"; // NOI18N
    }
    
    @Override
    public String getShortDescription() {
        StringBuilder sb = new StringBuilder();
        WadlSaasResource r = resource;
        while (r != null) {
            sb.insert(0, '/');
            sb.insert(0, r.getResource().getPath());
            r = r.getParent();
        }
        sb.insert(0, '/');
        sb.insert(0, resource.getSaas().getBaseURL());
        return sb.toString();
    }
    
    private static final java.awt.Image SERVICE_BADGE =
            ImageUtilities.loadImage( "org/netbeans/modules/websvc/saas/ui/resources/restservice.png" ); //NOI18N
    
    @Override
    public java.awt.Image getIcon(int type) {
        return SERVICE_BADGE;
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = SaasNode.getActions(getLookup());
        return actions.toArray(new Action[0]);
    }
}

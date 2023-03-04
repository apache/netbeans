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

package org.netbeans.modules.websvc.saas.ui.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.WadlSaasResource;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author nam
 */
public class ResourceNodeChildren extends Children.Keys<Object> {
    private final WadlSaasResource resource;
    
    public ResourceNodeChildren(WadlSaasResource resource) {
        this.resource = resource;
    }
    
    @Override
    protected void addNotify() {
        super.addNotify();
        updateKeys();
    }

    @Override
    protected void removeNotify() {
        java.util.List<String> emptyList = Collections.emptyList();
        setKeys(emptyList);
        super.removeNotify();
    }

    private void updateKeys() {
        ArrayList<Object> keys = new ArrayList<Object>();
        List<WadlSaasResource> resources = resource.getChildResources();
        Collections.sort(resources);
        keys.addAll(resources);
        
        List<WadlSaasMethod> methods = resource.getMethods();
        Collections.sort(methods);
        keys.addAll(methods);
  
        setKeys(keys);
    }
    
    @Override
    protected Node[] createNodes(Object key) {
        if (key instanceof WadlSaasResource) {
            return new Node[] { new ResourceNode(((WadlSaasResource)key)) };
        } else if (key instanceof WadlSaasMethod) {
            return new Node[] { new WadlMethodNode((WadlSaasMethod)key) };
        }
        
        return new Node[0];
    }

}

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
package org.netbeans.modules.websvc.rest.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;


public class RestServiceChildren extends Children.Keys {
    private Project project;
    private RestServicesModel model;
    private String serviceName;
  
    private static final String KEY_HTTP_METHODS = "http_methods";  //NOI18N
    private static final String KEY_SUB_RESOURCE_LOCATORS = "sub_resource_locators";        //NOI18N
    
    public RestServiceChildren(Project project, RestServicesModel model, 
            String serviceName) {
        this.project = project;
        this.model = model;
        this.serviceName = serviceName;
    }
    
    protected void addNotify() {
        super.addNotify();
 
        updateKeys();
    }
    
    protected void removeNotify() {
        super.removeNotify();
        
        setKeys(Collections.emptySet());
    }
    
    private void updateKeys() {
        final List<String> keys = new ArrayList<>();
        keys.add(KEY_HTTP_METHODS);
        keys.add(KEY_SUB_RESOURCE_LOCATORS);
        
        setKeys(keys);
    }
    
    protected Node[] createNodes(final Object key) {
        if (key.equals(KEY_HTTP_METHODS)) {
            return new Node[] { new HttpMethodsNode(project, model, serviceName) };
        } else if (key.equals(KEY_SUB_RESOURCE_LOCATORS)) {
            return new Node[] { new SubResourceLocatorsNode(project, model, serviceName) };
        }
        
        return new Node[0];
    }
}

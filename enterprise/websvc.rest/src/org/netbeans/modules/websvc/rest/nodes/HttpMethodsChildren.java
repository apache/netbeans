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
package org.netbeans.modules.websvc.rest.nodes;

import java.beans.PropertyChangeEvent;
import java.util.Comparator;
import java.util.List;

import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.model.api.HttpMethod;
import org.netbeans.modules.websvc.rest.model.api.RestMethodDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.openide.util.Exceptions;



public class HttpMethodsChildren extends ChildFactory<HttpMethodNode> implements PropertyChangeListener {
    
    public HttpMethodsChildren(Project project,RestServicesModel model, 
            String serviceName) {
        this.project = project;
        this.model = model;
        this.serviceName = serviceName;
        model.addPropertyChangeListener(this);
    }
    
    /* (non-Javadoc)
     * @see org.openide.nodes.ChildFactory#createKeys(java.util.List)
     */
    @Override
    protected boolean createKeys( final List<HttpMethodNode> keys ) {
        try {
            model.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                public Void run(RestServicesMetadata metadata) throws IOException {
                    RestServices root = metadata.getRoot();
                    RestServiceDescription desc = root.getRestServiceDescription(
                            serviceName);
                    
                    if (desc != null) {
                        for (RestMethodDescription method : desc.getMethods()) {
                            if (method instanceof HttpMethod) {
                                keys.add(new HttpMethodNode(project, desc,
                                        (HttpMethod) method));
                            }
                        }
                        keys.sort(COMPARATOR);
                    }
                    
                    return null;
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return true;
    }
    
    @Override
    protected Node createNodeForKey( HttpMethodNode node ){
        return node;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (RestServices.PROP_SERVICES.equals(evt.getPropertyName())) {
            refresh(false);
        }
    }
            
            
    
    static class HttpMethodsComparator implements Comparator<HttpMethodNode> {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare( HttpMethodNode node1, HttpMethodNode node2 ) {
            String key1 = node1.getKey();
            String key2 = node2.getKey();
            return key1.compareTo( key2 );
        }
        
    }
  
    private static final Comparator<HttpMethodNode> COMPARATOR = 
        new HttpMethodsComparator();
    
    private Project project;
    private RestServicesModel model;
    private String serviceName;
    
}

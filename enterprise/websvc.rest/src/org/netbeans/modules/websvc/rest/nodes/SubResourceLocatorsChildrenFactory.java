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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.model.api.RestMethodDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.modules.websvc.rest.model.api.SubResourceLocator;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;


/**
 * @author ads
 *
 */
class SubResourceLocatorsChildrenFactory extends ChildFactory<SubResourceLocatorNode> {
    
    SubResourceLocatorsChildrenFactory(Project project, RestServicesModel model, 
            String serviceName) {
        this.project = project;
        this.model = model;
        this.serviceName = serviceName;
    }

    @Override
    protected boolean createKeys( final List<SubResourceLocatorNode> list ) {
        try {
            model.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                @Override
                public Void run(RestServicesMetadata metadata) throws IOException {
                    RestServices root = metadata.getRoot();
                    RestServiceDescription desc = root.
                        getRestServiceDescription(serviceName);
                    
                    if (desc != null) {
                        String className = desc.getClassName();
                        List<RestMethodDescription> methods = desc.getMethods();
                        List<SubResourceLocator> locators = 
                            new ArrayList<SubResourceLocator>( methods.size());
                        for (RestMethodDescription method : methods ) {
                            if (method instanceof SubResourceLocator) {
                                locators.add((SubResourceLocator)method);
                            }
                        }
                        locators.sort(COMPARATOR);
                        for (SubResourceLocator locator : locators){
                            list.add( new SubResourceLocatorNode(project, 
                                   className , locator));
                        }
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
    protected Node createNodeForKey( SubResourceLocatorNode node ) {
        return node;
    }
    
    private static class SubResourceLocatorComparator implements 
        Comparator<SubResourceLocator> 
    {

        @Override
        public int compare( SubResourceLocator locator1, 
                SubResourceLocator locator2 ) 
        {
            String key1 = SubResourceLocatorNode.getKey(locator1);
            String key2 = SubResourceLocatorNode.getKey(locator2);
            return key1.compareTo(key2);
        }
        
    }
    
    private static final Comparator<SubResourceLocator>  COMPARATOR = 
        new SubResourceLocatorComparator();

    private Project project;
    private RestServicesModel model;
    private String serviceName;
}

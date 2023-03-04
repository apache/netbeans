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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;


/**
 * @author ads
 *
 */
class RestServiceChildFactory extends ChildFactory<RestServiceDescription> implements PropertyChangeListener {
    
    private static final Logger LOG = Logger.getLogger( 
            RestServiceChildFactory.class.getName() );

    RestServiceChildFactory(Project project, RestSupport restSupport) {
        this.project = project;
        if (restSupport != null) {
            RestServicesModel restModel = restSupport.getRestServicesModel();
            if (restModel != null) {
                restModel.addPropertyChangeListener(this);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openide.nodes.ChildFactory#createKeys(java.util.List)
     */
    @Override
    protected boolean createKeys( final List<RestServiceDescription> keys ) {
        try {
            RestServicesModel model = getModel();
            if (model != null) {
                if (Thread.interrupted()) {
                    return true;
                }
                model.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>()
                {

                    @Override
                    public Void run( RestServicesMetadata metadata )
                            throws IOException
                    {
                        RestServiceDescription[] restServiceDescription = 
                            metadata.getRoot().getRestServiceDescription();
                        Arrays.sort(restServiceDescription, COMPARATOR);
                        for (RestServiceDescription r : restServiceDescription) {
                            // ignore REST services for which we do not have sources (#216168, #229168):
                            if (r.getFile() != null) {
                                keys.add(r);
                            }
                        }
                        return null;
                    }
                });
            } else {
                LOG.log(Level.INFO, "RestServicesModel is null"); //NOI18N
                return true;
            }
        }
        catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
                
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.openide.nodes.ChildFactory#createNodeForKey(java.lang.Object)
     */
    @Override
    protected Node createNodeForKey( RestServiceDescription key ) {
        return new RestServiceNode(project, getModel(), key);
    }

    private RestServicesModel getModel() {
        RestSupport support = project.getLookup().lookup(RestSupport.class);
        if (support != null) {
            return support.getRestServicesModel();
        }
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (RestServices.PROP_SERVICES.equals(evt.getPropertyName())) {
            refresh(false);
        }
    }
    
    private static class RSDescriptionComparator implements Comparator<RestServiceDescription>{

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare( RestServiceDescription desc1, RestServiceDescription desc2 )
        {
            String key1 = RestServiceNode.getKey(desc1);
            String key2 = RestServiceNode.getKey(desc2);
            return key1.compareTo(key2);
        }
        
    }
    
    private static final RSDescriptionComparator COMPARATOR = new RSDescriptionComparator();
    private Project project;
    
}

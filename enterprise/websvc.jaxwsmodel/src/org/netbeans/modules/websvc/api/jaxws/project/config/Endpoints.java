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
/*
 * Endpoints.java
 *
 * Created on March 19, 2006, 8:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.jaxws.project.config;

import java.beans.PropertyChangeListener;
import java.io.OutputStream;
import org.netbeans.modules.schema2beans.BaseBean;
/**
 *
 * @author Roderico Cruz
 */
public class Endpoints {
     private org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoints endpoints;
    /** Creates a new instance of HandlerChains */
    public Endpoints(org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoints endpoints) {
        this.endpoints = endpoints;
    }
    
    public Endpoint[] getEndpoints() {
        org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint[] endpointArray = 
                endpoints.getEndpoint();
        Endpoint[] newEndpoints = new Endpoint[endpointArray.length];
        for (int i=0;i<endpointArray.length;i++) {
            newEndpoints[i]=new Endpoint(endpointArray[i]);
        }
        return newEndpoints;
    }
    
    public Endpoint newEndpoint() {
        org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint endpoint = 
                endpoints.newEndpoint();
        return new Endpoint(endpoint);
    }
    
    public void addEnpoint(Endpoint endpoint) {
        endpoints.addEndpoint(endpoint.getOriginal());
    }
    
    public void removeEndpoint(Endpoint endpoint) {
        endpoints.removeEndpoint(endpoint.getOriginal());
    }
    
    public Endpoint findEndpointByName(String endpointName) {
        Endpoint[]endpnts = getEndpoints();
        for (int i=0;i<endpnts.length;i++) {
            Endpoint endpoint = endpnts[i];
            if(endpointName.equals(endpoint.getEndpointName())){
                return endpoint;
            }
        }
        return null;
    }
    
    public Endpoint findEndpointByImplementation(String className) {
        Endpoint[] endpnts = getEndpoints();
        for (int i=0;i<endpnts.length;i++) {
            Endpoint endpoint = endpnts[i];
            if(className.equals(endpoint.getImplementation())) {
                return endpoint;
            }
        }
        return null;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        endpoints.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        endpoints.removePropertyChangeListener(l);
    }
    
    public void merge(Endpoints newEndpoints) {
        if (newEndpoints.endpoints!=null)
            endpoints.merge(newEndpoints.endpoints,BaseBean.MERGE_UPDATE);
    }
    
    public void write(OutputStream os) throws java.io.IOException {
        endpoints.write(os);
    }
    
}

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
/*
 * Endpoint.java
 *
 * Created on March 19, 2006, 9:04 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.jaxws.project.config;

/**
 *
 * @author rico
 */
public class Endpoint {
    org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint endpoint;
    /** Creates a new instance of Handler */
    public Endpoint(org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint endpoint) {
        this.endpoint=endpoint;
    }
    
    public org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint 
            getOriginal(){
        return endpoint;
    }
    public String getEndpointName() {
        return endpoint.getName();
    }
    
     public void setEndpointName(String name) {
        endpoint.setName(name);
    }
    public String getImplementation(){
        return endpoint.getImplementation();
    }
   
    public void setImplementation(String value) {
        endpoint.setImplementation(value);
    }
    
    public String getUrlPattern(){
        return endpoint.getUrlPattern();
    }
    
    public void setUrlPattern(String value){
        endpoint.setUrlPattern(value);
    }
}

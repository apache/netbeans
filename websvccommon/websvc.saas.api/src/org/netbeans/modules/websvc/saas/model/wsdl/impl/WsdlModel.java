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

package org.netbeans.modules.websvc.saas.model.wsdl.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSService;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Roderico Cruz
 */
public class WsdlModel {
    private WSDLModel model;

     public WsdlModel(WSDLModel model){
         this.model = model;
     }

     public List<WSService> getServices(){
         if ( model.getDefinitions() == null ){
             return Collections.emptyList();
         }
         Collection<Service> wsdlServices = model.getDefinitions().getServices();
         List<WSService> services = new ArrayList<WSService>( wsdlServices.size());
         for(Service wsdlService : wsdlServices){
             services.add(new WsdlService(wsdlService));
         }
         return services;
     }
}

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
import java.util.List;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSPort;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSService;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;

/**
 *
 * @author Roderico Cruz
 */
public class WsdlService implements WSService{
    public Service service;

    public WsdlService(Service service){
        this.service = service;
    }

    public Object getInternalJAXWSService() {
        return service;
    }

    public List<WSPort> getPorts() {
        List<WSPort> wsdlPorts = new ArrayList<WSPort>();
        Collection<Port> ports = service.getPorts();
        for(Port port : ports){
            wsdlPorts.add(new WsdlPort(port));
        }
        return wsdlPorts;
    }

    public String getName() {
        return service.getName();
    }

    public String getNamespaceURI() {
        return service.getPeer().getNamespaceURI();
    }

    public String getJavaName() {
        return service.getName(); //TODO Capitalize first char??
    }

    public WSPort getPortByName(String portName) {
        for(WSPort port : getPorts()){
            if(port.getName().equals(portName)){
                return port;
            }
        }
        return null;
    }

}

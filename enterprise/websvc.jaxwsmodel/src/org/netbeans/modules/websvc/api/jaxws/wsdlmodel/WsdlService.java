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

package org.netbeans.modules.websvc.api.jaxws.wsdlmodel;

import java.util.ArrayList;
import java.util.List;

import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Service;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSService;

/**
 *
 * @author mkuchtiak
 */
public class WsdlService implements WSService {
    
    private Service service;
    
    /** 
     * Added this default constructor to fix an issue related to the XMLEncoder.
     * See IZ 128161.
     */
    public WsdlService() {
    }
    
    /** Creates a new instance of WsdlService */
    WsdlService(Service service) {
        this.service=service;
    }
    
    public Object /*com.sun.tools.ws.processor.model.Service*/ getInternalJAXWSService() {
        return service;
    }
    
    public List<WsdlPort> getPorts() {
        List<WsdlPort> wsdlPorts = new ArrayList<>();
        if (service==null) return wsdlPorts;
        List<Port> ports = service.getPorts();
        for (Port p:ports)
            wsdlPorts.add(new WsdlPort(p));
        return wsdlPorts;
    }
    
    public String getName() {
        if (service==null) return null;
        return service.getName().getLocalPart();
    }
    
    public String getNamespaceURI() {
        return service.getName().getNamespaceURI();
    }
    
    public String getJavaName() {
        if (service==null) return null;
        return service.getJavaInterface().getName();
    }
    
    public WsdlPort getPortByName(String portName) {
        List<Port> ports = service.getPorts();
        for (Port p:ports)
            if (portName.equals(p.getName().getLocalPart())) return new WsdlPort(p);
        return null;
    }
}

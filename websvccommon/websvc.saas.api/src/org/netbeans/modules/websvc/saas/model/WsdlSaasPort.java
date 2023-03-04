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

package org.netbeans.modules.websvc.saas.model;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSPort;

/**
 *
 * @author nam
 */
public class WsdlSaasPort implements Comparable<WsdlSaasPort> {
    private WsdlSaas parentSaas;
    private WSPort port;
    private List<WsdlSaasMethod> methods;

    public WsdlSaasPort(WsdlSaas parentSaas, WSPort port) {
        this.parentSaas = parentSaas;
        this.port = port;
    }
 
    public String getName() {
        return getWsdlPort().getName();
    }
    
    public WSPort getWsdlPort() {
        return port;
    }
    
    public WsdlSaas getParentSaas() {
        return parentSaas;
    }
    
    public List<WsdlSaasMethod> getWsdlMethods() {
        if (methods == null) {
            methods = new ArrayList<WsdlSaasMethod>();
            for (WSOperation op : port.getOperations()) {
                methods.add(new WsdlSaasMethod(this, op));
            }
        }
        return methods;
    }
    
    public int compareTo(WsdlSaasPort saasPort) {
        return getWsdlPort().getName().compareTo(saasPort.getWsdlPort().getName());
    }
}

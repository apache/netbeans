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
import org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSPort;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;

/**
 *
 * @author Roderico Cruz
 */
public class WsdlPort implements WSPort{
    Port port;
    public WsdlPort(Port port){
        this.port = port;
    }
    public Object getInternalJAXWSPort() {
        return port;
    }

    public List<WSOperation> getOperations() {
        Collection<Operation> operations = port.getBinding().get().getType().get().getOperations();
        List<WSOperation> ops = new ArrayList<WSOperation>();
        for(Operation operation : operations){
            ops.add(new WsdlOperation(operation));
        }
        return ops;
    }

    public String getName() {
        return port.getName();
    }

    public String getNamespaceURI() {
        return port.getPeer().getNamespaceURI();
    }

    public String getJavaName() {
        return port.getName();  //TODO is this relevant??
    }

    public String getPortGetter() {
        return "get" + port.getName() + "Port";
    }

    public String getSOAPVersion() {
        List<SOAPBinding> soapBindings = port.getBinding().get().getExtensibilityElements(SOAPBinding.class);
        return soapBindings.get(0).getTransportURI();  //todo need to compute this
    }

    public String getStyle() {
        List<SOAPBinding> soapBindings = port.getBinding().get().getExtensibilityElements(SOAPBinding.class);
        return soapBindings.get(0).getStyle().toString();
    }

    public boolean isProvider() {
       return false;
    }

    public String getAddress() {
        List<SOAPAddress> addresses = port.getExtensibilityElements(SOAPAddress.class);
        if ( addresses.size() >0 ){
            SOAPAddress address = addresses.get(0);
            return address.getLocation();
        }
        return null;
    }

}

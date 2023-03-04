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

import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.wsdl.document.soap.SOAPStyle;

import org.netbeans.modules.websvc.jaxwsmodelapi.WSPort;

/**
 *
 * @author mkuchtiak
 */
public class WsdlPort implements WSPort {

    private Port port;
    private String soapVersion = SOAP_VERSION_11;
    
    /** Creates a new instance of WsdlPort */
    WsdlPort(Port port) {
        this.port=port;
    }
    
    public Object /*com.sun.tools.ws.processor.model.Port*/ getInternalJAXWSPort() {
        return port;
    }
    
    public List<WsdlOperation> getOperations() {
        List<WsdlOperation> wsdlOperations = new ArrayList<>();
        if (port==null) return wsdlOperations;
        List<Operation> operations = port.getOperations();
        for (Operation op:operations)
            wsdlOperations.add(new WsdlOperation(op));
        return wsdlOperations;
    }
    
    public String getName() {
        if (port==null) return null;
        return port.getName().getLocalPart();
    }
    
    public String getNamespaceURI() {
        return port.getName().getNamespaceURI();
    }
    
    public String getJavaName() {
        if (port==null) return null;
        return port.getJavaInterface().getName();
    }
    
    public String getPortGetter() {
        if (port==null) return null;
        return port.getPortGetter();
    }
    
    public String getSOAPVersion() {
        return soapVersion;
    }
    
    public void setSOAPVersion(String soapVersion) {
        this.soapVersion=soapVersion;
    }
    
    public String getStyle() {
        SOAPStyle style = port.getStyle();
        if (SOAPStyle.DOCUMENT.equals(style)) return STYLE_DOCUMENT;
        else if (SOAPStyle.RPC.equals(style)) return STYLE_RPC;
        return null;
    }
    
    public boolean isProvider(){
        return port.isProvider();
    }
    
    public String getAddress(){
        return port.getAddress();
    }
}

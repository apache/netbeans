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

package org.netbeans.modules.websvc.wsitmodelext.addressing.impl;

import org.netbeans.modules.websvc.wsitmodelext.addressing.EndpointReference;
import org.netbeans.modules.websvc.wsitmodelext.addressing.ReferenceParameters;
import org.netbeans.modules.websvc.wsitmodelext.addressing.ReferenceProperties;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;
import java.util.Collections;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Address;
import org.netbeans.modules.websvc.wsitmodelext.addressing.AddressingPortType;
import org.netbeans.modules.websvc.wsitmodelext.addressing.AddressingServiceName;

/**
 *
 * @author Martin Grebac
 */
public class EndpointReferenceImpl extends AddressingComponentImpl implements EndpointReference {
    
    /**
     * Creates a new instance of EndpointReferenceImpl
     */
    public EndpointReferenceImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public void setAddress(Address address) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Address.class, ADDRESS_PROPERTY, address, classes);
    }

    public Address getAddress() {
        return getChild(Address.class);
    }

    public void removeAddress(Address address) {
        removeChild(ADDRESS_PROPERTY, address);
    }

    public void setServiceName(AddressingServiceName serviceName) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(AddressingServiceName.class, SERVICENAME_PROPERTY, serviceName, classes);
    }

    public AddressingServiceName getServiceName() {
        return getChild(AddressingServiceName.class);
    }

    public void removeServiceName(AddressingServiceName serviceName) {
        removeChild(SERVICENAME_PROPERTY, serviceName);
    }
    
    public void setPortType(AddressingPortType addressingPortType) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(AddressingPortType.class, ADDRESSING_PORTTYPE_PROPERTY, addressingPortType, classes);
    }

    public AddressingPortType getPortType() {
        return getChild(AddressingPortType.class);
    }

    public void removePortType(AddressingPortType portType) {
        removeChild(ADDRESSING_PORTTYPE_PROPERTY, portType);
    }

    public void setReferenceProperties(ReferenceProperties referenceProperties) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(ReferenceProperties.class, REFERENCE_PROPERTIES_PROPERTY, referenceProperties, classes);
    }

    public ReferenceProperties getReferenceProperties() {
        return getChild(ReferenceProperties.class);
    }

    public void removeReferenceProperties(ReferenceProperties referenceProperties) {
        removeChild(REFERENCE_PROPERTIES_PROPERTY, referenceProperties);
    }

    public void setReferenceParameters(ReferenceParameters referenceParameters) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(ReferenceParameters.class, REFERENCE_PARAMETERS_PROPERTY, referenceParameters, classes);
    }

    public ReferenceParameters getReferenceParameters() {
        return getChild(ReferenceParameters.class);
    }

    public void removeReferenceParameters(ReferenceParameters referenceParameters) {
        removeChild(REFERENCE_PARAMETERS_PROPERTY, referenceParameters);
    }
    
}

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

package org.netbeans.modules.websvc.wsitmodelext.addressing;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;

/**
 *
 * @author Martin Grebac
 */
public interface EndpointReference extends ExtensibilityElement {
    public static final String ADDRESS_PROPERTY = "ADDRESS";            //NOI18N
    public static final String REFERENCE_PROPERTIES_PROPERTY = "REFERENCE_PROPERTIES_INTERVAL";     //NOI18N
    public static final String REFERENCE_PARAMETERS_PROPERTY = "REFERENCE_PARAMETERS";              //NOI18N
    public static final String ADDRESSING_PORTTYPE_PROPERTY = "PORTTYPE";          //NOI18N
    public static final String SERVICENAME_PROPERTY = "SERVICENAME";    //NOI18N
    
    ReferenceProperties getReferenceProperties();
    void setReferenceProperties(ReferenceProperties referenceProperties);
    void removeReferenceProperties(ReferenceProperties referenceProperties);

    ReferenceParameters getReferenceParameters();
    void setReferenceParameters(ReferenceParameters referenceParameters);
    void removeReferenceParameters(ReferenceParameters referenceParameters);

    Address getAddress();
    void setAddress(Address address);
    void removeAddress(Address address);
    
    AddressingPortType getPortType();
    void setPortType(AddressingPortType portType);
    void removePortType(AddressingPortType portType);

    AddressingServiceName getServiceName();
    void setServiceName(AddressingServiceName serviceName);
    void removeServiceName(AddressingServiceName serviceName);
}

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
public interface Addressing10EndpointReference extends ExtensibilityElement {
    public static final String ADDRESS_PROPERTY = "ADDRESS";            //NOI18N
    public static final String REFERENCE_PROPERTIES_PROPERTY = "REFERENCE_PROPERTIES_INTERVAL";     //NOI18N
    public static final String METADATA_PROPERTY = "METADATA_PARAMETERS";              //NOI18N
    
    Addressing10ReferenceProperties getReferenceProperties();
    void setReferenceProperties(Addressing10ReferenceProperties referenceProperties);
    void removeReferenceProperties(Addressing10ReferenceProperties referenceProperties);

    Addressing10Metadata getAddressing10Metadata();
    void setAddressing10Metadata(Addressing10Metadata addressingMetadata);
    void removeAddressing10Metadata(Addressing10Metadata addressingMetadata);

    Address10 getAddress();
    void setAddress(Address10 address);
    void removeAddress(Address10 address);
    
}

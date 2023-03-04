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

import org.netbeans.modules.websvc.wsitmodelext.addressing.Address10;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10Metadata;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10ReferenceProperties;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

import java.util.Collections;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10EndpointReference;

/**
 *
 * @author Martin Grebac
 */
public class Addressing10EndpointReferenceImpl extends Addressing10ComponentImpl implements Addressing10EndpointReference {
    
    /**
     * Creates a new instance of Addressing10EndpointReferenceImpl
     */
    public Addressing10EndpointReferenceImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public void setAddress(Address10 address) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Address10.class, ADDRESS_PROPERTY, address, classes);
    }

    public Address10 getAddress() {
        return getChild(Address10.class);
    }

    public void removeAddress(Address10 address) {
        removeChild(ADDRESS_PROPERTY, address);
    }

    public void setReferenceProperties(Addressing10ReferenceProperties referenceProperties) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Addressing10ReferenceProperties.class, REFERENCE_PROPERTIES_PROPERTY, referenceProperties, classes);
    }

    public Addressing10ReferenceProperties getReferenceProperties() {
        return getChild(Addressing10ReferenceProperties.class);
    }

    public void removeReferenceProperties(Addressing10ReferenceProperties referenceProperties) {
        removeChild(REFERENCE_PROPERTIES_PROPERTY, referenceProperties);
    }

    public void setAddressing10Metadata(Addressing10Metadata addressingMetadata) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Addressing10Metadata.class, METADATA_PROPERTY, addressingMetadata, classes);
    }

    public Addressing10Metadata getAddressing10Metadata() {
        return getChild(Addressing10Metadata.class);
    }

    public void removeAddressing10Metadata(Addressing10Metadata addressing10Metadata) {
        removeChild(METADATA_PROPERTY, addressing10Metadata);
    }
    
}

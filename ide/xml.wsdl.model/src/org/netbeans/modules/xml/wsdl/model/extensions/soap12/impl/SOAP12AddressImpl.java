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

package org.netbeans.modules.xml.wsdl.model.extensions.soap12.impl;

import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Address;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Component;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12QName;
import org.netbeans.modules.xml.wsdl.model.impl.WSDLAttribute;
import org.netbeans.modules.xml.xam.Component;
import org.w3c.dom.Element;


/**
 * @author Sujit Biswas
 *
 */
public class SOAP12AddressImpl extends SOAP12ComponentImpl implements SOAP12Address {
    
    /** Creates a new instance of SOAPAddressImpl */
    public SOAP12AddressImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SOAP12AddressImpl(WSDLModel model){
        this(model, createPrefixedElement(SOAP12QName.ADDRESS.getQName(), model));
    }
    
    public void accept(SOAP12Component.Visitor visitor) {
        visitor.visit(this);
    }

    public void setLocation(String locationURI) {
        setAttribute(LOCATION_PROPERTY, WSDLAttribute.LOCATION, locationURI);
    }

    public String getLocation() {
        return getAttribute(WSDLAttribute.LOCATION);
    }

    @Override
    public boolean canBeAddedTo(Component target) {
        if (target instanceof Port) {
            return true;
        }
        return false;
    }
}

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

import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Fault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12QName;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Reference;
import org.w3c.dom.Element;

/**
 * @author Sujit Biswas
 *
 */
public class SOAP12FaultImpl extends SOAP12MessageBaseImpl implements SOAP12Fault {
    
    /** Creates a new instance of SOAPFaultImpl */
    public SOAP12FaultImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SOAP12FaultImpl(WSDLModel model){
        this(model, createPrefixedElement(SOAP12QName.FAULT.getQName(), model));
    }
    
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void setName(String name) {
        setAttribute(NAME_PROPERTY, SOAP12Attribute.NAME, name);
    }

    public String getName() {
        return getAttribute(SOAP12Attribute.NAME);
    }

    public void setFault(Reference<Fault> fault) {
        Fault f = fault.get();
        setName(f == null ? null : f.getName());
    }

    public Reference<Fault> getFault() {
        String v = getName();
        return v == null ? null : new FaultReference(this, v); 
    }


    @Override
    public boolean canBeAddedTo(Component target) {
        if (target instanceof BindingFault) {
            return true;
        }
        return false;
    }
}

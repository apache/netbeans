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

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class BindingImpl extends WSDLComponentBase implements Binding {
    
    /** Creates a new instance of BindingImpl */
    public BindingImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public BindingImpl(WSDLModel model){
        this(model, createNewElement(WSDLQNames.BINDING.getQName(), model));
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public NamedComponentReference<PortType> getType() {
        return resolveGlobalReference(PortType.class, WSDLAttribute.PORT_TYPE);
    }

    public void setType(NamedComponentReference<PortType> portType) {
        setAttribute(TYPE_PROPERTY, WSDLAttribute.PORT_TYPE, portType);
    }

    public void addExtensibilityElement(ExtensibilityElement ee) {
        addAfter(EXTENSIBILITY_ELEMENT_PROPERTY, ee, TypeCollection.DOCUMENTATION.types());
    }
    
    public void removeBindingOperation(BindingOperation bindingOperation) {
        removeChild(BINDING_OPERATION_PROPERTY, bindingOperation);
    }

    public void addBindingOperation(BindingOperation bindingOperation) {
        addAfter(BINDING_OPERATION_PROPERTY, bindingOperation, TypeCollection.DOCUMENTATION_EE.types());
    }

    public Collection<BindingOperation> getBindingOperations() {
        return getChildren(BindingOperation.class);
    }
    
    public void setName(String name) {
        setAttribute(NAME_PROPERTY, WSDLAttribute.NAME, name);
    }

    public String getName() {
        return getAttribute(WSDLAttribute.NAME);
    }
}

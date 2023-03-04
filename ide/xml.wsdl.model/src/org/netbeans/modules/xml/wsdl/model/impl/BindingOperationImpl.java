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
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.Reference;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class BindingOperationImpl extends WSDLComponentBase implements BindingOperation {
    
    /** Creates a new instance of BindingOperationImpl */
    public BindingOperationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public BindingOperationImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.OPERATION.getQName(), model));
    }
    
    public void addExtensibilityElement(ExtensibilityElement ee) {
        addAfter(EXTENSIBILITY_ELEMENT_PROPERTY, ee, TypeCollection.DOCUMENTATION.types());
    }
    
    public void setBindingInput(BindingInput bindingInput) {
        setChildAfter(BindingInput.class, BINDING_INPUT_PROPERTY, bindingInput, TypeCollection.DOCUMENTATION_EE.types());
    }
    
    public BindingInput getBindingInput() {
        return getChild(BindingInput.class);
    }
    
    public void setBindingOutput(BindingOutput bindingOutput) {
        setChildAfter(BindingOutput.class, BINDING_OUTPUT_PROPERTY, bindingOutput, 
                TypeCollection.DOCUMENTATION_EXTENSIBILITY_BINDINGINPUT.types());
    }
    
    public BindingOutput getBindingOutput() {
        return getChild(BindingOutput.class);
    }
    
    public void setOperation(Reference<Operation> operationRef) {
        setName(operationRef == null ? null : operationRef.get().getName());
    }
    
    public Reference<Operation> getOperation() {
        return getName() == null ? null : new OperationReference(this, getName());
    }
    
    public void addBindingFault(BindingFault bindingFault) {
        addAfter(BINDING_FAULT_PROPERTY, bindingFault, TypeCollection.DOCUMENTATION_EXTENSIBILITY_BINDINGOUTPUT.types());
    }
    
    public void removeBindingFault(BindingFault bindingFault) {
        removeChild(BINDING_FAULT_PROPERTY, bindingFault);
    }
    
    public Collection<BindingFault> getBindingFaults() {
        return getChildren(BindingFault.class);
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
    public void setName(String name) {
        setAttribute(NAME_PROPERTY, WSDLAttribute.NAME, name);
    }

    public String getName() {
        return getAttribute(WSDLAttribute.NAME);
    }
}

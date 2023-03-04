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

import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.AbstractReference;
import org.netbeans.modules.xml.xam.Reference;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class BindingInputImpl extends NamedImpl implements BindingInput {
    
    /** Creates a new instance of BindingInputImpl */
    public BindingInputImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public BindingInputImpl(WSDLModel model){
        this(model, createNewElement(WSDLQNames.INPUT.getQName(), model));
    }

    public void accept(org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
    public void setInput(Reference<Input> input) {
        assert false: "reference to Input is read-only, use setName()";
    }

    // Note: input name is optional and only needs to be explicit when 
    // binding operation name is ambiguous.
    public Reference<Input> getInput() {
        return new InputReference(this);
    }
    
    static class InputReference extends AbstractReference<Input> implements Reference<Input> {
        public InputReference(BindingInputImpl parent){
            super(Input.class, parent, parent.getName());
        }

        public BindingInputImpl getParent() {
            return (BindingInputImpl) super.getParent();
        }
        
        public String getRefString() {
            return getParent().getName();
        }

        public Input get() {
            if (getReferenced() == null) {
                BindingOperation bo = (BindingOperation) getParent().getParent();
                if (bo != null) {
                    Operation op = bo.getOperation().get();
                    if (bo.getOperation() != null) {
                        if (op != null) {
                            setReferenced(op.getInput());
                        }
                    }
                }
            }
            return getReferenced();
        }
    }
    
}

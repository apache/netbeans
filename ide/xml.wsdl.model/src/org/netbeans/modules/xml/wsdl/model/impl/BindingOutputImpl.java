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

import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.AbstractReference;
import org.netbeans.modules.xml.xam.Reference;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class BindingOutputImpl extends NamedImpl implements BindingOutput {
    
    /** Creates a new instance of BindingOutputImpl */
    public BindingOutputImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public BindingOutputImpl(WSDLModel model){
        this(model, createNewElement(WSDLQNames.OUTPUT.getQName(), model));
    }

    public void accept(org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setOutput(Reference<Output> Output) {
        assert false: "Reference to Output is read-only, use setName() instead.";
    }

    public Reference<Output> getOutput() {
        return new OutputReference(this);
    }
    
    static class OutputReference extends AbstractReference<Output> implements Reference<Output> {
        public OutputReference(BindingOutputImpl parent){
            super(Output.class, parent, parent.getName());
        }

        public BindingOutputImpl getParent() {
            return (BindingOutputImpl) super.getParent();
        }
        
        public String getRefString() {
            return getParent().getName();
        }

        public Output get() {
            if (getReferenced() == null) {
                BindingOperation bo = (BindingOperation) getParent().getParent();
                if (bo != null) {
                    if (bo.getOperation() != null) {
                        Operation op = bo.getOperation().get();
                        if (op != null) {
                            setReferenced(op.getOutput());
                        }
                    }
                }
            }           
            return getReferenced();
        }
        
    }
}

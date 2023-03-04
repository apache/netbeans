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

import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.AbstractReference;
import org.netbeans.modules.xml.xam.Reference;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class BindingFaultImpl extends NamedImpl implements BindingFault {
    
    /** Creates a new instance of BindingFaultImpl */
    public BindingFaultImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public BindingFaultImpl(WSDLModel model){
        this(model, createNewElement(WSDLQNames.FAULT.getQName(), model));
    }

    public void accept(org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setFault(Reference<Fault> fault) {
        assert false: "Reference to Output is read-only, use setName() instead.";
    }

    public Reference<Fault> getFault() {
        return new FaultReference(this);
    }
    
    static class FaultReference extends AbstractReference<Fault> implements Reference<Fault> {
        public FaultReference(BindingFaultImpl parent){
            super(Fault.class, parent, parent.getName());
        }

        public BindingFaultImpl getParent() {
            return (BindingFaultImpl) super.getParent();
        }

        public String getRefString() {
            return getParent().getName();
        }

        public Fault get() {
            if (getReferenced() != null) return getReferenced();
            
            BindingOperation bo = (BindingOperation) getParent().getParent();
            if (bo != null) {
                if (bo.getOperation() != null) {
                    Operation op = bo.getOperation().get();
                    if (op != null) {
                        for (Fault f : op.getFaults()) {
                            if (f.getName().equals(getRefString())) {
                                setReferenced(f);
                                break;
                            }
                        }
                    }
                }
            }
            return getReferenced();
        }
        
    }
}

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

import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Fault;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.AbstractReference;
import org.netbeans.modules.xml.xam.Reference;


public class FaultReference extends AbstractReference<Fault> implements Reference<Fault> {
    
    public FaultReference(Fault referenced, AbstractDocumentComponent parent) {
        super(referenced, Fault.class, parent);
    }
    
    //used by resolve methods
    public FaultReference(AbstractDocumentComponent parent, String ref){
        super(Fault.class, parent, ref);
    }
    
    public String getRefString() {
        if (refString == null) {
            refString = getReferenced().getName();
        }
        return refString;
    }

    private SOAP12Fault getSOAPFault() {
        return (SOAP12Fault) getParent();
    }
    
    public Fault get() {
        if (getReferenced() == null) {
            if (getSOAPFault().getParent() == null || 
                getSOAPFault().getParent().getParent() == null) {
                return null;
            }
            BindingOperation bindingOp = (BindingOperation) getSOAPFault().getParent().getParent();
            Reference<Operation> ref = bindingOp.getOperation();
            Operation op = (ref == null) ? null : ref.get();
            if (op != null) {
                for (Fault f : op.getFaults()) {
                    if (refString.equals(f.getName())) {
                        setReferenced(f);
                        break;
                    }
                }
            }
        }
        return getReferenced();
    }
}

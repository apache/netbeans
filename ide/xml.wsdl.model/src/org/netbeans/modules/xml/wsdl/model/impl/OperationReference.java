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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.AbstractReference;
import org.netbeans.modules.xml.xam.Reference;

/**
 *
 * @author Nam Nguyen
 */
public class OperationReference extends AbstractReference<Operation> implements Reference<Operation> {
    
    /** Creates a new instance of OperationReference */
    public OperationReference(Operation referenced, AbstractComponent parent) {
        super(referenced, Operation.class, parent);
    }
    
    //used by resolve methods
    public OperationReference(AbstractComponent parent, String ref){
        super(Operation.class, parent, ref);
    }
    
    public String getRefString() {
        if (refString == null) {
            refString = getReferenced().getName();
        }
        return refString;
    }
    
    public Operation get() {
        if (getReferenced() != null) return getReferenced();
        
        String operationName = getRefString();
        if (operationName == null) {
            return null;
        }
        
        Binding p = (Binding) getParent().getParent();
        if (p == null || p.getType() == null) {
            return null;
        }
        
        PortType pt = p.getType().get();
        if (pt == null || pt.getOperations() == null) {
            return null;
        }
        
        Collection<Operation> operations = pt.getOperations();
        BindingOperation bindingOp = (BindingOperation) getParent();
        List<Operation> candidates = new ArrayList<Operation>();
        for (Operation op : operations) {
            if (operationName.equals(op.getName())) {
                candidates.add(op);
            }
        }
        
        // find perfect matched
        for (Operation op : candidates) {
            BindingInput bi = bindingOp.getBindingInput();
            BindingOutput bo = bindingOp.getBindingOutput();
            Input in = op.getInput();
            Output out = op.getOutput();
            if (in == null && bi == null && out != null && bo != null && out.getName().equals(bo.getName()) ||
                out == null && bo == null && in != null && bi != null && in.getName().equals(bi.getName()) ||
                in != null && bi != null && out != null && bo != null && 
                in.getName().equals(bi.getName()) && out.getName().equals(bo.getName()))
            {
                setReferenced(op);
                break;
            }
        }
        
        if (getReferenced() == null && ! candidates.isEmpty()) {
            setReferenced(candidates.get(0));
        }
        return getReferenced();
    }
}

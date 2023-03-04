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

import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Address;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Body;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Component;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Fault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Header;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12HeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Operation;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ComponentUpdater.Query;

/**
 * @author Sujit Biswas
 *
 */
public class SOAP12ComponentUpdater implements ComponentUpdater<SOAP12Component>, Query<SOAP12Component>, SOAP12Component.Visitor {
    private SOAP12Component parent;
    private Operation operation;
    private boolean canAdd;
    
    /** Creates a new instance of SOAPComponentUpdater */
    public SOAP12ComponentUpdater() {
    }
    
    public boolean canAdd(SOAP12Component target, Component child) {
        if (!(child instanceof SOAP12Component)) return false;
        update(target, (SOAP12Component) child, null);
        return canAdd;
    }

    public void update(SOAP12Component target, SOAP12Component child, Operation operation) {
        update(target, child, -1, operation);
    }

    
    public void update(SOAP12Component target, SOAP12Component child, int index, Operation operation) {
        parent = target;
        this.operation = operation;
        child.accept(this);
    }

    public void visit(SOAP12Operation child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAP12Binding child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAP12Header child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAP12Body child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAP12Fault child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAP12HeaderFault child) {
        SOAP12Header target = (SOAP12Header) parent;
        if (operation == Operation.ADD) {
            target.addSOAPHeaderFault(child);
        } else if (operation == Operation.REMOVE) {
            target.removeSOAPHeaderFault(child);
        } else if (operation == null) {
            canAdd = true;
        }
    }

    public void visit(SOAP12Address child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }
    
}

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
package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;
import org.netbeans.modules.xml.xam.ComponentUpdater.Query;

/**
 *
 * @author Nam Nguyen
 */
public class SOAPComponentUpdater implements ComponentUpdater<SOAPComponent>, Query<SOAPComponent>, SOAPComponent.Visitor {
    private SOAPComponent parent;
    private Operation operation;
    private boolean canAdd;
    
    /** Creates a new instance of SOAPComponentUpdater */
    public SOAPComponentUpdater() {
    }
    
    public boolean canAdd(SOAPComponent target, Component child) {
        if (!(child instanceof SOAPComponent)) return false;
        update(target, (SOAPComponent) child, null);
        return canAdd;
    }

    public void update(SOAPComponent target, SOAPComponent child, Operation operation) {
        update(target, child, -1, operation);
    }

    
    public void update(SOAPComponent target, SOAPComponent child, int index, Operation operation) {
        parent = target;
        this.operation = operation;
        child.accept(this);
    }

    public void visit(SOAPOperation child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAPBinding child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAPHeader child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAPBody child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAPFault child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAPHeaderFault child) {
        SOAPHeader target = (SOAPHeader) parent;
        if (operation == Operation.ADD) {
            target.addSOAPHeaderFault(child);
        } else if (operation == Operation.REMOVE) {
            target.removeSOAPHeaderFault(child);
        } else if (operation == null) {
            canAdd = true;
        }
    }

    public void visit(SOAPAddress child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }
    
}

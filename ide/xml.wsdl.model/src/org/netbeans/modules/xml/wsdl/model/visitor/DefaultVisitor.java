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

/*
 * DefaultVisitor.java
 *
 * Created on November 17, 2005, 9:59 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model.visitor;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author nn136682
 */
public class DefaultVisitor implements WSDLVisitor {
    
    /** Creates a new instance of DefaultVisitor */
    public DefaultVisitor() {
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Types types) {
        visitComponent(types);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Port port) {
        visitComponent(port);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Definitions definition) {
        visitComponent(definition);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.BindingInput bi) {
        visitComponent(bi);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.BindingOutput bo) {
        visitComponent(bo);
    }

    public void visit(OneWayOperation op) {
        visitComponent(op);
    }

    public void visit(RequestResponseOperation op) {
        visitComponent(op);
    }
    
    public void visit(NotificationOperation op) {
        visitComponent(op);
    }

    public void visit(SolicitResponseOperation op) {
        visitComponent(op);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Part part) {
        visitComponent(part);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Documentation doc) {
        visitComponent(doc);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.BindingOperation bop) {
        visitComponent(bop);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Binding binding) {
        visitComponent(binding);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Message message) {
        visitComponent(message);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Service service) {
        visitComponent(service);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.BindingFault bf) {
        visitComponent(bf);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Import importDef) {
        visitComponent(importDef);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Output out) {
        visitComponent(out);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.PortType portType) {
        visitComponent(portType);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Input in) {
        visitComponent(in);
    }
    
    public void visit(org.netbeans.modules.xml.wsdl.model.Fault fault) {
        visitComponent(fault);
    }
    
    public void visit(ExtensibilityElement ee) {
        visitComponent(ee);
    }
    
    protected void visitComponent(WSDLComponent component) {
    }
}

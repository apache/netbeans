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

package org.netbeans.modules.xml.wsdl.model.visitor;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author Nam Nguyen
 */
public class FindReferencedVisitor<T extends ReferenceableWSDLComponent> extends DefaultVisitor {
    private Class<T> type;
    private String localName;
    private T referenced = null;
    private Definitions root;
    
    /** Creates a new instance of FindReferencedVisitor */
    public FindReferencedVisitor(Definitions root) {
        this.root = root;
    }
    
    public T find(String localName, Class<T> type) {
        this.type = type;
        this.localName = localName;
        visitChildren(root);
        return referenced;
    }

    public void visit(Binding c) {
        checkReference(c, true); //extensible
    }

    public void visit(Message c) { //extensible
        checkReference(c, true);
    }

    public void visit(PortType c) { //extensible
        checkReference(c, true);
    }
    
    public void visit(RequestResponseOperation op){
        checkReference(op, true);
    }    
    
    public void visit(OneWayOperation op){
        checkReference(op, true);
    }
    
    
    public void visit(NotificationOperation op){
        checkReference(op, true);
    }
    
    public void visit(SolicitResponseOperation op){
        checkReference(op, true);
    }

    public void visit(Service s){
        checkReference(s, true);
    }
    
    public void visit(Port p){
        checkReference(p, true);
    }
    
    public void visit(Part part){
        checkReference(part, true);
    }
    
    public void visit(ExtensibilityElement c) {
        if (c instanceof ReferenceableWSDLComponent) {
            checkReference(ReferenceableWSDLComponent.class.cast(c), true);
        }
        visitChildren(c);
    }

    private void checkReference(ReferenceableWSDLComponent c, boolean checkChildren) {
         if (type.isAssignableFrom(c.getClass()) && c.getName() != null &&
                 c.getName().equals(localName)) {
             referenced = type.cast(c);
             return;
         } else if (checkChildren) {
             visitChildren(c);
         }
    }
    
    private void visitChildren(WSDLComponent c) {
        for (WSDLComponent child : c.getChildren()) {
           if (referenced != null) { // before start each visit
                return;
           }
            child.accept(this);
        }
    }
    
}

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
 * WSDLVisitor.java
 *
 * Created on November 15, 2005, 9:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model.visitor;

import org.netbeans.modules.xml.wsdl.model.*;


/**
 *
 * @author rico
 */
public interface WSDLVisitor {
    void visit(Definitions definition);
    void visit(Types types);
    void visit(Documentation doc);
    void visit(Import importDef);
    void visit(Message message);
    void visit(Part part);
    void visit(PortType portType);
    void visit(OneWayOperation op);
    void visit(RequestResponseOperation op);
    void visit(NotificationOperation op);
    void visit(SolicitResponseOperation op);
    void visit(Input in);
    void visit(Output out);
    void visit(Binding binding);
    void visit(BindingInput bi);
    void visit(BindingOutput bo);
    void visit(BindingOperation bop);
    void visit(BindingFault bf);
    void visit(Service service);
    void visit(Port port);
    void visit(Fault fault);
    void visit(ExtensibilityElement ee);
}

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

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author nn136682
 */
public class PortTypeImpl extends NamedImpl implements PortType {
    
    /** Creates a new instance of PortTypeImpl */
    public PortTypeImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    public PortTypeImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.PORTTYPE.getQName(), model));
    }

    public Collection<Operation> getOperations() {
        return getChildren(Operation.class);
    }

    public void removeOperation(Operation operation) {
        removeChild(OPERATION_PROPERTY, operation);
    }

    public void addOperation(Operation operation) {
        appendChild(OPERATION_PROPERTY, operation);
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
}

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

package org.netbeans.modules.xml.wsdl.model;

import java.util.Collection;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author rico
 * Represents a binding in the WSDL document
 */
public interface Binding extends ReferenceableWSDLComponent {
    public static final String BINDING_OPERATION_PROPERTY = "operation";
    public static final String TYPE_PROPERTY = "type";
    
    void setType(NamedComponentReference<PortType> portType);
    NamedComponentReference<PortType> getType();
    
    void addBindingOperation(BindingOperation bindingOperation);
    void removeBindingOperation(BindingOperation bindingOperation);
    Collection<BindingOperation> getBindingOperations();
}

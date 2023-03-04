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
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.Reference;

/**
 *
 * @author rico
 * Represents an operation binding in the WSDL document. This is the
 * operation that is contained in the binding element
 */
public interface BindingOperation extends Nameable<WSDLComponent>, WSDLComponent {
    public static final String BINDING_OPERATION_PROPERTY = NAME_PROPERTY;
    public static final String BINDING_INPUT_PROPERTY = "input";
    public static final String BINDING_OUTPUT_PROPERTY = "output";
    public static final String BINDING_FAULT_PROPERTY = "fault";
    
    void setBindingInput(BindingInput bindingInput);
    BindingInput getBindingInput();
    void setBindingOutput(BindingOutput bindingOutput);
    BindingOutput getBindingOutput();
    
    /**
     * Set corresponding portType operationusing the given reference.
     * @param operation reference.
     */
    void setOperation(Reference<Operation> operation);
    
    /**
     * @return reference to the corresponding operation.
     */
    Reference<Operation> getOperation();
    
    void addBindingFault(BindingFault bindingFault);
    void removeBindingFault(BindingFault bindingFault);
    Collection<BindingFault> getBindingFaults();
}

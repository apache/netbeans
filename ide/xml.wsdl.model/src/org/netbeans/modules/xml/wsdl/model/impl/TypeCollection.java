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
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Documentation;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author nn136682
 */
public enum TypeCollection {
    ALL(createAll()),
    DOCUMENTATION(createDocumentation()),
    DOCUMENTATION_EE(createDocumentationEE()),
    FOR_IMPORT(createListForImport()),
    FOR_TYPES(createListForTypes()),
    FOR_MESSAGE(createListForMessage()),
    FOR_PORTTYPE(createListForPortType()),
    FOR_BINDING(createListForBinding()),
    FOR_SERVICE(createListForService()),
    DOCUMENTATION_OUTPUT(createDocumentationOutputList()),
    DOCUMENTATION_INPUT(createDocumentationInputList()),
    DOCUMENTATION_EXTENSIBILITY_BINDINGINPUT(createListForBindingInput()),
    DOCUMENTATION_EXTENSIBILITY_BINDINGOUTPUT(createListForBindingOutput());
    
    private Collection<Class<? extends WSDLComponent>> types;
    TypeCollection(Collection<Class<? extends WSDLComponent>> types) {
        this.types = types;
    }
    public Collection<Class<? extends WSDLComponent>> types() { return types; }
    
    static Collection <Class<? extends WSDLComponent>> createAll() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(WSDLComponent.class);
        return c;
    }
    
    static Collection<Class<? extends WSDLComponent>> createDocumentation() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        return c;
    }
    
    static Collection<Class<? extends WSDLComponent>> createDocumentationEE() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(ExtensibilityElement.class);
        return c;
    }
    
    static Collection<Class<? extends WSDLComponent>> createListForImport() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        return c;
    }

    static Collection<Class<? extends WSDLComponent>> createListForTypes() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(Import.class);
        return c;
    }

    static Collection<Class<? extends WSDLComponent>> createListForMessage() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(Import.class);
        c.add(Types.class);
        return c;
    }

    static Collection<Class<? extends WSDLComponent>> createListForPortType() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(Import.class);
        c.add(Types.class);
        c.add(Message.class);
        return c;
    }

    static Collection<Class<? extends WSDLComponent>> createListForBinding() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(Import.class);
        c.add(Types.class);
        c.add(Message.class);
        c.add(PortType.class);
        return c;
    }

    static Collection<Class<? extends WSDLComponent>> createListForService() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(Import.class);
        c.add(Types.class);
        c.add(Message.class);
        c.add(PortType.class);
        c.add(Binding.class);
        return c;
    }

    static Collection<Class<? extends WSDLComponent>> createDocumentationOutputList() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(Output.class);
        return c;
    }

    static Collection<Class<? extends WSDLComponent>> createDocumentationInputList() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(Input.class);
        return c;
    }

    static Collection<Class<? extends WSDLComponent>> createListForBindingInput() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(ExtensibilityElement.class);
        c.add(BindingInput.class);
        return c;
    }
    
    static Collection<Class<? extends WSDLComponent>> createListForBindingOutput() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(ExtensibilityElement.class);
        c.add(BindingInput.class);
        c.add(BindingOutput.class);
        return c;
    }
}

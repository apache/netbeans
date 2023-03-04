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

package org.netbeans.modules.websvc.saas.model.wsdl.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter;
import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaMethod;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.ComplexTypeDefinition;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Roderico Cruz
 */
public class WsdlOperation implements WSOperation{
    private Operation operation;

    public WsdlOperation(Operation  operation){
        this.operation = operation;

    }
    public Object getInternalJAXWSOperation() {
        return operation;
    }

    public JavaMethod getJavaMethod() {
        return null;
    }

    public String getName() {
        return operation.getName();
    }

    public String getJavaName() {
        return null;
    }

    public String getReturnTypeName() {
        return Utils.getTypeName(operation.getOutput().getMessage().getQName() ); //TODO need to qualify this
    }

    private List<WSParameter> unWrapPart(Part part){
        List<WSParameter> parms = new  ArrayList<WSParameter>();
        NamedComponentReference<GlobalElement> gbr = part.getElement();
        if(gbr != null){
            GlobalElement gb = gbr.get();
            if (gb != null) {
                List<ComplexType> complexTypes = gb.getChildren(ComplexType.class);
                if(complexTypes != null && !complexTypes.isEmpty()){
                    for(ComplexType complexType : complexTypes){
                        ComplexTypeDefinition def = complexType.getDefinition();
                        if (def == null){
                            continue;
                        }
                        List<LocalElement> elements = def.getChildren(LocalElement.class);
                        for(LocalElement element : elements){
                            parms.add(new WsdlParameter(element));
                        }
                    }
                }
            }
        }
        return parms;
    }

    public List<WSParameter> getParameters() {
        List<WSParameter> parameters = new ArrayList<WSParameter>();
        Input input = operation.getInput();
        NamedComponentReference<Message> message = input.getMessage();
        Collection<Part> parts = message.get().getParts();
        for(Part part : parts){
            parameters.addAll(unWrapPart(part));
        }
        return parameters;
    }

    public Iterator<String> getExceptions() {
        List<String> exceptions = new ArrayList<String>();
        Collection<Fault> faults = operation.getFaults();
        for(Fault fault : faults){
            exceptions.add(fault.getMessage().getQName().getLocalPart());
        }
        return exceptions.iterator();
    }

    public int getOperationType() {
        return 0;
    }

    public String getOperationName() {
        return operation.getName();
    }

}

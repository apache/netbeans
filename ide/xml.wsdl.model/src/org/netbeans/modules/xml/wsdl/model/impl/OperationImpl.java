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
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 *
 * @author nn136682
 */
public abstract class OperationImpl extends NamedImpl implements Operation {
    
    /** Creates a new instance of OperationImpl */
    public OperationImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    public void setInput(Input input) {
        throw new UnsupportedOperationException(
                "This operation does not support this message exchange pattern"); //NOI18N
    }
  
    public Input getInput() {
        return null;
    }
    
    public void setOutput(Output output) {
        throw new UnsupportedOperationException(
                "This operation does not support this message exchange pattern");//NOI18N
    }

    public Output getOutput() {
        return null;
    }
    
    public Collection<Fault> getFaults() {
        return getChildren(Fault.class);
    }

    public void addFault(Fault fault) {
        appendChild(Operation.FAULT_PROPERTY, fault);
    }

    public void removeFault(Fault fault) {
        removeChild(Operation.FAULT_PROPERTY, fault);
    }

    public List<String> getParameterOrder() {
        String s = getAttribute(WSDLAttribute.PARAMETER_ORDER);
        return Util.parse(s);
    }

    public void setParameterOrder(List<String> parameterOrder) {
        setAttribute(PARAMETER_ORDER_PROPERTY, WSDLAttribute.PARAMETER_ORDER, 
                Util.toString(parameterOrder));
    }
    
    protected Object getAttributeValueOf(WSDLAttribute attr, String s) {
        if (attr == WSDLAttribute.PARAMETER_ORDER) {
            return Util.parse(s);
        } else {
            return super.getAttributeValueOf(attr, s);
        }
    }

}

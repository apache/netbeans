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
import java.util.List;
import org.netbeans.modules.xml.xam.Nameable;

/**
 *
 * @author rico
 * Represents a WSDL operation
 */
public interface Operation extends Nameable<WSDLComponent>, ReferenceableWSDLComponent, WSDLComponent {
    public static final String FAULT_PROPERTY = "fault";
    public static final String INPUT_PROPERTY = "input";
    public static final String OUTPUT_PROPERTY = "output";
    public static final String PARAMETER_ORDER_PROPERTY = "parameterOrder";
    
    Input getInput();
    void setInput(Input input);

    Output getOutput();
    void setOutput(Output output);
    
    void addFault(Fault fault);
    void removeFault(Fault fault);
    Collection<Fault> getFaults();
    
    //needed only in RPC type operations-shd we ignore this?
    void setParameterOrder(List<String> parameterOrder);
    List<String> getParameterOrder();
}

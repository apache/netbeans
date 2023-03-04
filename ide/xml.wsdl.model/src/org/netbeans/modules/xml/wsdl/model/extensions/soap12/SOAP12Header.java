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

package org.netbeans.modules.xml.wsdl.model.extensions.soap12;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;

/**
 * Represents a header element under the wsdl:input or wsdl:output element for SOAP binding
 *
 * @author Sujit Biswas
 *
 */
public interface SOAP12Header extends SOAP12HeaderBase, ExtensibilityElement.UpdaterProvider {
    public static final String HEADER_FAULT_PROPERTY = "headerFault";
    
    Collection<SOAP12HeaderFault> getSOAPHeaderFaults();
    void addSOAPHeaderFault(SOAP12HeaderFault soapHeaderFault);
    void removeSOAPHeaderFault(SOAP12HeaderFault soapHeaderFault);
}

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

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.xam.dom.ComponentFactory;

/**
 *
 * @author rico
 * Factory for providing concrete implementations of WSDLComponents
 */

public interface WSDLComponentFactory extends ComponentFactory<WSDLComponent> {
    WSDLComponent create(WSDLComponent parent, QName qName);   
    Binding createBinding();
    BindingFault createBindingFault();
    BindingInput createBindingInput();
    BindingOperation createBindingOperation();
    BindingOutput createBindingOutput();
    Documentation createDocumentation();
    Fault createFault();
    Import createImport();
    Input createInput();
    Message createMessage();
    OneWayOperation createOneWayOperation();
    SolicitResponseOperation createSolicitResponseOperation();
    RequestResponseOperation createRequestResponseOperation();
    NotificationOperation createNotificationOperation();
    Output createOutput();
    Part createPart();
    Port createPort();
    PortType createPortType();
    Service createService();
    Types createTypes();

    // SOAP
    SOAPAddress createSOAPAddress();
    SOAPBinding createSOAPBinding();
    SOAPBody createSOAPBody();
    SOAPFault createSOAPFault();
    SOAPHeader createSOAPHeader();
    SOAPHeaderFault createSOAPHeaderFault();
    SOAPOperation createSOAPOperation();
    
    // XSD
    WSDLSchema createWSDLSchema();
}

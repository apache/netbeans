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
package org.netbeans.modules.xml.wsdl.model.visitor;

import junit.framework.*;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.NamespaceLocation;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.TestCatalogModel;
import org.netbeans.modules.xml.wsdl.model.Util;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;

/**
 *
 * @author Nam Nguyen
 */
public class FindWSDLComponentTest extends TestCase {

    public FindWSDLComponentTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }
    
    public void testFindComponent() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/HelloService.wsdl");
        Definitions d = model.getDefinitions();
        RequestResponseOperation rro = (RequestResponseOperation) d.getPortTypes().iterator().next().getOperations().iterator().next();
        String xpath = "/definitions/portType/operation[1]/input";
        WSDLComponent found = (WSDLComponent) new FindWSDLComponent().findComponent(model.getRootComponent(), xpath);
        assertEquals(xpath, rro.getInput(), found);
        
        xpath = "/definitions/portType/operation[1]/output";
        found = (WSDLComponent) new FindWSDLComponent().findComponent(model.getRootComponent(), xpath);
        assertEquals(xpath, rro.getOutput(), found);
        
        Binding b = d.getBindings().iterator().next();
        SOAPBinding sb = (SOAPBinding) b.getExtensibilityElements().iterator().next();
        xpath = "/definitions/binding[@name='HelloServiceSEIBinding']/binding";
        found = (WSDLComponent) new FindWSDLComponent().findComponent(model.getRootComponent(), xpath);
        
        assertEquals("binding.soap", sb, found);
    }

    public void testFindComponentAfterWrite() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/empty.wsdl");
        model.startTransaction();
        Definitions d = model.getDefinitions();
        d.setName("HelloService");
        d.setTargetNamespace("urn:HelloService/wsdl");
        WSDLComponentFactory fact = d.getModel().getFactory();

        Message m1 = fact.createMessage();
        d.addMessage(m1);
        m1.setName("HelloServiceSEI_sayHello");
        
        model.endTransaction();
        
        String xpath = "/definitions";
        WSDLComponent found = (WSDLComponent) new FindWSDLComponent().findComponent(model.getRootComponent(), xpath);
        assertEquals(xpath, model.getRootComponent(), found);
    }

    public void testFindSchemaComponent() throws Exception {
        WSDLModel model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.TRAVEL);
        Definitions root = model.getDefinitions();
        String xpath = "/definitions/types/xs:schema/xs:element[1]";
        //   <xs:element name="itineraryFault" type="xs:string" />
        new FindWSDLComponent().findComponent(root, xpath);
    }
}

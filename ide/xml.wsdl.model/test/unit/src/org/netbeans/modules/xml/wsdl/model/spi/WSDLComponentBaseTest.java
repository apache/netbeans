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
/*
 * WSDLComponentBaseTest.java
 * JUnit based test
 *
 * Created on March 25, 2006, 5:23 AM
 */

package org.netbeans.modules.xml.wsdl.model.spi;

import java.util.Map;
import javax.xml.namespace.QName;
import junit.framework.*;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.TestCatalogModel;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.Util;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.impl.GlobalReferenceImpl;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 *
 * @author nn136682
 */
public class WSDLComponentBaseTest extends TestCase {

    public WSDLComponentBaseTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(WSDLComponentBaseTest.class);
        
        return suite;
    }

    public void testRemoveDocumentation() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/HelloService.wsdl");
        Definitions definitions = model.getDefinitions();
        Types types = definitions.getTypes();
        assertEquals("testing remove documentation", types.getDocumentation().getContentFragment());
        model.startTransaction();
        types.setDocumentation(null);
        model.endTransaction();
        assertNull(types.getDocumentation());
    }
    
    public void testGetAttributeMap() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/HelloService.wsdl");
        Definitions definitions = model.getDefinitions();
        
        Map<QName,String> map = definitions.getAttributeMap();
        assertEquals(2, map.keySet().size());
        assertEquals( "HelloService", map.get(new QName("name")));
        assertEquals("urn:HelloService/wsdl", map.get(new QName("targetNamespace")));
    }

    public void testNoNamespace() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/definitionsNoTargetN_valid.wsdl");
        Definitions definitions = model.getDefinitions();
        assertNull(definitions.getTargetNamespace());
        
        Operation op = model.findComponentByName("goodBasicWSDLOperation", Operation.class);
        assertEquals(null, ((AbstractDocumentComponent)op.getInput()).lookupNamespaceURI(""));

        assertNotNull(op.getInput().getMessage().get());
    }

    public void testAddOperationNoNamespace() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/definitionsNoTargetN_valid.wsdl");
        Definitions definitions = model.getDefinitions();
        assertNull(definitions.getTargetNamespace());
        
        OneWayOperation op = model.getFactory().createOneWayOperation();
        op.setName("noNamespaceOp");
        Input in = model.getFactory().createInput();
        Message m = model.findComponentByName("goodBasicWSDLOperationRequest", Message.class);
        in.setMessage(in.createReferenceTo(m, Message.class));
        op.setInput(in);
        
        model.startTransaction();
        PortType portType = definitions.getPortTypes().iterator().next();
        portType.addOperation(op);
        model.endTransaction();
        
        op = model.findComponentByName("noNamespaceOp", OneWayOperation.class);
        assertNotNull(op);
        assertEquals(null, ((AbstractDocumentComponent)op.getInput()).lookupNamespaceURI(""));
        ((GlobalReferenceImpl)op.getInput().getMessage()).refresh();
        assertNotNull(op.getInput().getMessage().get());
        assertEquals(m.getName(), op.getInput().getMessage().getRefString());
    }
}

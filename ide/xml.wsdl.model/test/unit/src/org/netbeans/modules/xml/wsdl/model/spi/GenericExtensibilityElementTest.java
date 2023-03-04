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

package org.netbeans.modules.xml.wsdl.model.spi;

import java.util.List;
import javax.xml.namespace.QName;
import junit.framework.*;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.TestCatalogModel;
import org.netbeans.modules.xml.wsdl.model.Util;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.visitor.FindWSDLComponent;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class GenericExtensibilityElementTest extends TestCase {
    
    public GenericExtensibilityElementTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(GenericExtensibilityElementTest.class);
        
        return suite;
    }

    public void testAnyElement() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/echo.wsdl");
        Definitions definitions = model.getDefinitions();
        String xpath = "/definitions/binding/operation[@name='operation_0']/output";
        BindingOutput output = FindWSDLComponent.findComponent(BindingOutput.class, definitions, xpath);
        assertEquals("output", output.getName());
        List<ExtensibilityElement> allEEs = output.getExtensibilityElements();
        assertEquals(1, allEEs.size());
        SOAPBody body = output.getExtensibilityElements(SOAPBody.class).get(0);
        assertEquals("someNS", body.getAnyElements().get(0).getQName().getNamespaceURI());
        
        SOAPBody innerBody = model.getFactory().createSOAPBody();
        model.startTransaction();
        body.addExtensibilityElement(innerBody);
        model.endTransaction();
        
        String localName = "element0";
        Element e0 = model.getDocument().createElementNS("someNS", localName);
        GenericExtensibilityElement gee = new GenericExtensibilityElement(model, e0);
        model.startTransaction();
        body.addAnyElement(gee, 0);
        model.endTransaction();
        assertEquals(localName, body.getAnyElements().get(0).getPeer().getLocalName());

        localName = "element2";
        Element e2 = model.getDocument().createElementNS("someNS", localName);
        GenericExtensibilityElement gee2 = new GenericExtensibilityElement(model, e2);
        model.startTransaction();
        body.addAnyElement(gee2, 2);
        model.endTransaction();
        assertEquals(localName, body.getAnyElements().get(2).getPeer().getLocalName());
        assertTrue(body.getExtensibilityElements().get(3) instanceof SOAPBody);

        model.startTransaction();
        body.removeAnyElement(gee);
        localName = "element1";
        Element e1 = model.getDocument().createElementNS("someNS", localName);
        GenericExtensibilityElement gee1 = new GenericExtensibilityElement(model, e1);
        body.addAnyElement(gee1, 1);
        model.endTransaction();
        assertEquals(localName, body.getAnyElements().get(1).getPeer().getLocalName());
        assertTrue(body.getExtensibilityElements().get(3) instanceof SOAPBody);
    }
    
    public void testAddAnyElementToEmptyWsdl() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/empty.wsdl");
        Definitions definitions = model.getDefinitions();
        
        model.startTransaction();
        QName qname = new QName("fooNS", "fooParent", "fo");
        ExtensibilityElement parentEE = new GenericExtensibilityElement(model, qname);
        definitions.addExtensibilityElement(parentEE);
        model.endTransaction();
        
        model.startTransaction();
        qname = new QName("fooNS", "foo", "fo");
        ExtensibilityElement element = new GenericExtensibilityElement(model, qname);
        String text = "asdfasdfsdf";
        element.setContentFragment(text);
        parentEE.addAnyElement(element, 0);
        model.endTransaction();

        model = Util.dumpAndReloadModel(model.getBaseDocument());
        definitions = model.getDefinitions();
        parentEE = definitions.getExtensibilityElements().get(0);
        element = parentEE.getAnyElements().get(0);
        assertEquals(text, element.getContentFragment());
    }

    public void testAddAnyElementToKnownEEwithKnownEEChildren() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/stockquote_headerFault.xml");
        String xpath = "/definitions/binding/operation/input/soap:header";
        SOAPHeader header = Util.find(SOAPHeader.class, model, xpath);
        assertEquals(2, header.getSOAPHeaderFaults().size());
        
        model.startTransaction();
        QName qname = new QName("fooNS", "foo", "fo");
        ExtensibilityElement any = new GenericExtensibilityElement(model, qname);
        header.addAnyElement(any, 1);
        header.addAnyElement((ExtensibilityElement)any.copy(header), 3);
        model.endTransaction();
        
        model = Util.dumpAndReloadModel(model.getBaseDocument());
        header = Util.find(SOAPHeader.class, model, xpath);
        any = (ExtensibilityElement)header.getChildren().get(1);
        assertEquals(qname, any.getQName());
        any = (ExtensibilityElement)header.getChildren().get(3);
        assertEquals(qname, any.getQName());
    }
}

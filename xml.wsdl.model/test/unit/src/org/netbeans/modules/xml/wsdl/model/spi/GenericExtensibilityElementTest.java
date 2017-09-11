/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

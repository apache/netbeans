/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
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

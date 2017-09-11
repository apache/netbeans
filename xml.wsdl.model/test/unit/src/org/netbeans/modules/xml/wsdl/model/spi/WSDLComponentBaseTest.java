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

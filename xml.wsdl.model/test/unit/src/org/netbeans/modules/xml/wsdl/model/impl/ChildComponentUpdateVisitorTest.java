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
 * ChildComponentUpdateVisitorTest.java
 * JUnit based test
 *
 * Created on August 23, 2006, 10:55 AM
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import junit.framework.*;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.impl.SOAPHeaderImpl;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement;

/**
 *
 * @author nn136682
 */
public class ChildComponentUpdateVisitorTest extends TestCase {
    private WSDLModel model;
    private Definitions definitions;

    public ChildComponentUpdateVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ChildComponentUpdateVisitorTest.class);
        return suite;
    }

    public void testRemoveAll_Travel() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.TRAVEL);
        definitions = model.getDefinitions();
    }

    public void testRemoveAll_Airline() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.AIRLINE);
        definitions = model.getDefinitions();
    }

    public void testRemoveAll_Hotel() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.HOTEL);
        definitions = model.getDefinitions();
    }

    public void testRemoveAll_Vehicle() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.VEHICLE);
        definitions = model.getDefinitions();
    }

    static void checkRemoveAll(WSDLComponent target) throws Exception {
        target.getModel().startTransaction();
        recursiveRemoveChildren(target);
        assertEquals("children removed", 0, target.getChildren().size());
        target.getModel().endTransaction();
    }
    
    static void recursiveRemoveChildren(WSDLComponent target) {
        WSDLModel model = target.getModel();
        ArrayList<WSDLComponent> children = new ArrayList<WSDLComponent>(target.getChildren());
        for (WSDLComponent child : children) {
            recursiveRemoveChildren(child);
        }
        if (target.getParent() != null) {
            model.removeChildComponent(target);
        }
    }

    public void testCanPasteAll_Travel() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.TRAVEL);
        recursiveCanPasteChildren(model.getDefinitions());
        recursiveCannotPasteChildren(model.getDefinitions());
    }
    
    public void testCanPasteAll_Airline() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.AIRLINE);
        recursiveCanPasteChildren(model.getDefinitions());
        recursiveCannotPasteChildren(model.getDefinitions());
    }
    
    public void testCanPasteAll_Hotel() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.HOTEL);
        recursiveCanPasteChildren(model.getDefinitions());
        recursiveCannotPasteChildren(model.getDefinitions());
    }
    
    public void testCanPasteAll_Vehicle() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.VEHICLE);
        recursiveCanPasteChildren(model.getDefinitions());
        recursiveCannotPasteChildren(model.getDefinitions());
    }
    
    public static void recursiveCanPasteChildren(WSDLComponent target) {
        WSDLModel model = target.getModel();
        ArrayList<WSDLComponent> children = new ArrayList<WSDLComponent>(target.getChildren());
        for (WSDLComponent child : children) {
            recursiveCanPasteChildren(child);
        }
        if (target.getParent() != null) {
            String msg = target.getParent().getClass().getName() + " cannot paste " + target.getClass().getName();
            assertTrue(msg, target.getParent().canPaste(target));
        }
    }

    public static void recursiveCannotPasteChildren(WSDLComponent target) {
        WSDLModel model = target.getModel();
        ArrayList<WSDLComponent> children = new ArrayList<WSDLComponent>(target.getChildren());
        for (WSDLComponent child : children) {
            recursiveCannotPasteChildren(child);
        }
        if (target.getParent() != null) {
            String msg = target.getClass().getName() + " canPaste " + target.getParent().getClass().getName();
            if (GenericExtensibilityElement.class.isAssignableFrom(target.getParent().getClass())) {
                assertTrue(msg, target.canPaste(target.getParent()));
            } else {
                assertFalse(msg, target.canPaste(target.getParent()));
            }
        }
    }

    public void testCannotAddSoapExtensibilityElement() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.VEHICLE);
        assertFalse(model.getDefinitions().canPaste(new SOAPHeaderImpl(model)));
    }
    
    public void testCannotAddFaultInOnewayOperation() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.VEHICLE);
        Iterator<PortType> iterator = model.getDefinitions().getPortTypes().iterator();
        PortType oneWayOperationPortType = iterator.next();
        Operation onewayOperation = oneWayOperationPortType.getOperations().iterator().next();
            
        assertFalse(onewayOperation.canPaste(new FaultImpl(model)));
    }
}

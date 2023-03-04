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

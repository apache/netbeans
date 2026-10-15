/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.xml.wsdl.model;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.wsdl.model.impl.WSDLModelImpl;
import org.netbeans.modules.xml.wsdl.model.visitor.FindWSDLComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author nn136682
 */
public class ImportTest extends TestCase {

    public ImportTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        //System.out.println(System.getProperty("java.class.path"));
        //Thread.dumpStack();
        //throw new IllegalStateException("Setup Exception");*/
    }

    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ImportTest.class);
        
        return suite;
    }

    public void testReadPortBinding() throws Exception {
        NamespaceLocation.AIRLINE.refreshResourceFile();
        WSDLModel travelModel = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.TRAVEL);
        Definitions root = travelModel.getDefinitions();
        String xpath = "/definitions/service[1]/port[2]";
        Port airlinePort = FindWSDLComponent.findComponent(Port.class, root, xpath);
        Binding airlineBinding = airlinePort.getBinding().get();
        assertEquals("testPortBinding", "AirlineReservationCallbackSoapBinding", airlineBinding.getName());
    }
    
    public void testWritePortBinding() throws Exception {
        WSDLModel travelModel = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.TRAVEL);
        Definitions root = travelModel.getDefinitions();
        
        travelModel.startTransaction();
        Service s = travelModel.getFactory().createService();
        root.addService(s);
        s.setName("testWritePortBinding");
        Port p = travelModel.getFactory().createPort(); s.addPort(p);
        p.setName("TestPort");
        WSDLModel vehModel = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.VEHICLE);
        Binding b = FindWSDLComponent.findComponent(Binding.class, vehModel.getDefinitions(), "/definitions/binding[1]");
        p.setBinding(p.createReferenceTo(b, Binding.class));
        travelModel.endTransaction();
        //TODO
        travelModel = Util.dumpAndReloadModel((Document)travelModel.getModelSource().getLookup().lookup(Document.class));
        root = travelModel.getDefinitions();
        String xpath = "/definitions/service[@name='testWritePortBinding']/port[@name='TestPort']";
        Port airlinePort = FindWSDLComponent.findComponent(Port.class, root, xpath);
        Binding b2 = airlinePort.getBinding().get();
        assertEquals("testPortBinding", "VehicleReservationSoapBinding", b2.getName());
    }
    
    public void testSchemaThroughWsdlImport() throws Exception {
        NamespaceLocation.PO.refreshResourceFile();
        WSDLModel model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.TESTOP);
        Definitions d = model.getDefinitions();
        List<Message> messages = new ArrayList<>(d.getMessages());
        
        Message m1 = messages.get(0);
        NamedComponentReference<GlobalType> type = m1.getParts().iterator().next().getType();
        assertEquals("PurchaseOrderType", type.get().getName());
        Message m2 = messages.get(1);
        NamedComponentReference<GlobalElement> ge = m2.getParts().iterator().next().getElement();
        assertEquals("comment", ge.get().getName());
    }
    
    public void testImportSchemaWithSameNamespace() throws Exception {
        NamespaceLocation.PO_1.refreshResourceFile();
        WSDLModel model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.TESTIMPORT);
        Definitions d = model.getDefinitions();
        List<Message> messages = new ArrayList<>(d.getMessages());
        Message m1 = messages.get(0);
        GlobalElement ge = m1.getParts().iterator().next().getElement().get();
        assertEquals("purchaseOrder", ge.getName());
    }

    public void testXsdTypeWithoutImport() throws Exception {
        WSDLModel model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.ECHO);
        Definitions d = model.getDefinitions();
        List<Message> messages = new ArrayList<>(d.getMessages());
        Message m1 = messages.get(0);
        assertEquals("message_0", m1.getName());
        Part part = m1.getParts().iterator().next();
        GlobalType gt = part.getType().get();
        assertEquals("string", gt.getName());
    }
    
//    Disabled as referenced files were partly not donated by oracle to apache
//    public void testImportSameNamespaceWsdl() throws Exception {
//        WSDLModel model = Util.loadWSDLModel("resources/GoogleFlow.wsdl");
//        Operation op = model.findComponentByName("initiate", Operation.class);
//        Message referencedMessage = op.getInput().getMessage().get();
//        assertEquals("Imported model has same namespace", model.getDefinitions().getTargetNamespace(),
//            referencedMessage.getModel().getDefinitions().getTargetNamespace());
//    }
    
//    Disabled as referenced files were partly not donated by oracle to apache
//    public void testUnqualifiedSchemaReference() throws Exception {
//        WSDLModelImpl model = (WSDLModelImpl)Util.loadWSDLModel("resources/SiebelInterface.wsdl");
//        String xpath = "/definitions/message[@name='SiebelInterfaceRequest']/part[@name='SWEExtData']";
//        Part part = Util.find(Part.class, model, xpath);
//        assertNotNull("part.element should resolve w/o prefix", part.getElement().get());
//    }

    public void testReferenceToOtherEmbededSchema() throws Exception {
        WSDLModelImpl model = (WSDLModelImpl)Util.loadWSDLModel("resources/Sumador.wsdl");
        Schema schema = new ArrayList<Schema>(model.getDefinitions().getTypes().getSchemas()).get(1);
        GlobalElement e = new ArrayList<GlobalElement>(schema.getElements()).get(1);
        assertTrue(e.getType().getRefString().equals("tns1:Operacion"));
        assertTrue(e.getType().get().getName().equals("Operacion"));
    }
    
    public void testEmbeddedSchemaWithNonDefaultXsdPrefix() throws Exception {
        WSDLModelImpl model = (WSDLModelImpl)Util.loadWSDLModel("resources/empty_non_default_xsd_prefix.wsdl");
        assertNotNull(model.getDefinitions().getTypes());
        Schema schema = model.getDefinitions().getTypes().getSchemas().iterator().next();
        GlobalSimpleType simpleType = schema.getModel().getFactory().createGlobalSimpleType();
        SimpleTypeRestriction str = schema.getModel().getFactory().createSimpleTypeRestriction();
        simpleType.setName("test");
        str.setBase(str.createReferenceTo(Util.getPrimitiveType("string"), GlobalSimpleType.class));
        simpleType.setDefinition(str);
        model.startTransaction();
        schema.addSimpleType(simpleType);
        model.endTransaction();
        assertEquals("s:string", ((AbstractDocumentComponent)str).getPeer().getAttribute("base"));
    }
    
    public void testDeleteOneOfManyEmbeddedSchemas() throws Exception {
        WSDLModelImpl model = (WSDLModelImpl)Util.loadWSDLModel("resources/newWSDL1.wsdl");
        assertNotNull(model.getDefinitions().getPeer().getAttributeNode("xmlns:xsd"));
        Schema schema = model.getDefinitions().getTypes().getSchemas().iterator().next();
        model.startTransaction();
        model.removeChildComponent(schema);
        model.endTransaction();
        assertNotNull(model.getDefinitions().getPeer().getAttributeNode("xmlns:xsd"));
    }
}

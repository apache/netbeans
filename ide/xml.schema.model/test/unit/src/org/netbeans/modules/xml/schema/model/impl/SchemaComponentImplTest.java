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
 * CommonSchemaComponentImplTest.java
 * JUnit based test
 *
 * Created on October 14, 2005, 7:19 AM
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.io.IOException;
import java.util.ArrayList;
import javax.xml.XMLConstants;
import junit.framework.*;
import java.util.Collection;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponentFactory;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.TestCatalogModel;
import org.netbeans.modules.xml.schema.model.Util;
import org.netbeans.modules.xml.schema.model.visitor.FindSchemaComponentFromDOM;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * 
 * @author Vidhya Narayanan
 * @author Nam Nguyen
 */
public class SchemaComponentImplTest extends TestCase {
    
    public SchemaComponentImplTest(String testName) {
        super(testName);
    }
    
    public static final String TEST_XSD = "resources/PurchaseOrder.xsd";
    private static final String TEST_XSD2 = "resources/KeyRef.xsd";
    
    Schema schema = null;
    SchemaModelImpl model = null;
    
    protected void setUp() throws Exception {
        SchemaModel model2 = Util.loadSchemaModel(TEST_XSD);
        schema = model2.getSchema();
    }

    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }
    
    public void testFromSameModel() throws Exception {
        SchemaModel cur_model = ((SchemaComponentImpl)schema).getModel();
        SchemaModel new_model = Util.loadSchemaModel(TEST_XSD2);
        boolean modelcheck = ((SchemaComponentImpl)schema).fromSameModel(new_model.getSchema());
        assertEquals("model is different", false, modelcheck);
        Collection<SchemaComponent> comps = schema.getChildren();
        modelcheck = ((SchemaComponentImpl)schema).fromSameModel(comps.iterator().next());
        assertEquals("model is the same", true, modelcheck);
    }

    public void testSetAnnotation() throws IOException  {
        SchemaModel model = ((SchemaComponentImpl)schema).getModel();
        Collection<SchemaComponent> comps = schema.getChildren();
        assertEquals("# children for schema", 5, comps.size());
        Annotation a = schema.getModel().getFactory().createAnnotation();
        model.startTransaction();
        ((SchemaComponentImpl)schema).setAnnotation(a);
        model.endTransaction();
        comps = schema.getChildren();
        assertEquals("# children for schema", 6, comps.size());
        java.util.Iterator i = comps.iterator();
        Annotation a1 = (Annotation)i.next();
        int index = ((java.util.List)comps).indexOf(a1);
        assertEquals("added annotation as first child", 0, index);
        assertEquals("added annotation is same as newly created", a, a1);
        
        //Check if annotation has also been added in the DOM
        //SchemaModel model = ((SchemaComponentImpl)schema).getModel();
        Document doc = ((SchemaModelImpl)model).getDocument();
        Node root = doc.getFirstChild();
        NodeList nl = root.getChildNodes();
        Node ann = nl.item(0);
        for (int j=0; j<nl.getLength(); j++) {
            if (nl.item(j) instanceof Element) {
                ann = nl.item(j);
                break;
            }
        }
        assertEquals("#1 child for schema DOM is annotation", "annotation", ann.getLocalName());
    }
    
    public void testGetAnnotations() throws IOException {
        SchemaModel model = ((SchemaComponentImpl)schema).getModel();
        Annotation a = model.getFactory().createAnnotation();
        model.startTransaction();
        ((SchemaComponentImpl)schema).setAnnotation(a);
        model.endTransaction();
        Annotation ann = schema.getAnnotation();
        assertNotNull("only one annotation should be present", ann);
    }
    
//    Disabled as referenced files were partly not donated by oracle to apache    
//    public void testSetGlobalReference() throws Exception {
//        SchemaModel mod = Util.loadSchemaModel("resources/ipo.xsd");
//        Schema schema = mod.getSchema();
//        SchemaComponentFactory fact = mod.getFactory();
//        
//        mod.startTransaction();
//        GlobalAttributeGroup gap = fact.createGlobalAttributeGroup();
//        schema.addAttributeGroup(gap);
//        gap.setName("myAttrGroup2");
//        LocalAttribute ga = fact.createLocalAttribute();
//        gap.addLocalAttribute(ga);
//        ga.setName("ga");
//        GlobalSimpleType gst = FindSchemaComponentFromDOM.find(
//                GlobalSimpleType.class, schema, "/schema/simpleType[@name='Sku']");
//        ga.setType(ga.createReferenceTo(gst, GlobalSimpleType.class));
//
//        mod.endTransaction();
//        
//        String v = ((AbstractDocumentComponent)ga).getPeer().getAttribute("type");
//        assertEquals("ref should have prefix", "ipo:Sku", v);
//        
//        mod.startTransaction();
//        /*
//        <complexType name="myCT">
//            <sequence>
//                <simpleType name="productName" type="xsd:string"/>
//            <attributeGroup ref="ipo:myAttrGroup2"/>
//        </complexType>
//         */
//        GlobalComplexType gct = fact.createGlobalComplexType();
//        schema.addComplexType(gct);
//        gct.setName("myCT");
//        Sequence seq = Util.createSequence(mod, gct);
//        LocalElement le = Util.createLocalElement(mod, seq, "productName", 0);
//        le.setType(le.createReferenceTo(Util.getPrimitiveType("string"), GlobalSimpleType.class));
//        
//        AttributeGroupReference agr = fact.createAttributeGroupReference();
//        gct.addAttributeGroupReference(agr);
//        agr.setGroup(agr.createReferenceTo(gap, GlobalAttributeGroup.class));
//
//        mod.endTransaction();
//        
//        v = ((AbstractDocumentComponent)agr).getPeer().getAttribute("ref");
//        assertEquals("ref should have prefix", "ipo:myAttrGroup2", v);
//    }
    
    public void testSetAndGetID() throws Exception {
        assertNull("id attribute is optional", schema.getId());
        schema.getModel().startTransaction();
        String v = "testSEtAndGetID";
        schema.setId(v);
        schema.getModel().endTransaction();
        assertEquals("testSetAndGetID.setID", v, schema.getId());
    }
    
    public void testCanPaste() throws Exception {
        SchemaModel mod = Util.loadSchemaModel("resources/PurchaseOrder.xsd");
        Schema schema  = mod.getSchema();
        GlobalComplexType gct = schema.getChildren(GlobalComplexType.class).get(0);
        Sequence seq = (Sequence) gct.getDefinition();
        LocalElement le = seq.getChildren(LocalElement.class).get(0);
        assertFalse(gct.canPaste(seq)); // already have sequence
        assertFalse(gct.canPaste(le));
    }

    public void testCanPasteRedefine() throws Exception {
        SchemaModel model1 = Util.loadSchemaModel("resources/PurchaseOrder_redefine.xsd");
        Schema schema  = model1.getSchema();
        Redefine redefine = schema.getRedefines().iterator().next();
        GlobalComplexType gct = redefine.getChildren(GlobalComplexType.class).get(0);
        Sequence seq = (Sequence) gct.getDefinition();
        GlobalSimpleType simple = redefine.getSimpleTypes().iterator().next();
        assertTrue(redefine.canPaste(gct));
        assertTrue(redefine.canPaste(simple));
        assertFalse(redefine.canPaste(seq));
    }

    public void testAddToSelfClosingSchema() throws Exception {
        SchemaModelImpl refmod = (SchemaModelImpl) Util.loadSchemaModel("resources/Empty_selfClosing.xsd");
        assertEquals(0, refmod.getSchema().getPeer().getChildNodes().getLength());
        
        SchemaModelImpl mod = (SchemaModelImpl) Util.loadSchemaModel("resources/Empty.xsd");
        Schema schema  = mod.getSchema();

        Util.setDocumentContentTo(mod.getBaseDocument(), "resources/Empty_selfClosing.xsd");
        mod.sync();
        
        GlobalElement ge = mod.getFactory().createGlobalElement();
        ge.setName("foo");
        mod.startTransaction();
        schema.addElement(ge);
        schema.setTargetNamespace("far");
        mod.endTransaction();
        
        //Util.dumpToFile(mod.getBaseDocument(), new File("c:/temp/testout.xml"));
        assertEquals(schema.getPeer().getChildNodes().item(1), ge.getPeer());
        assertEquals(3, schema.getPeer().getChildNodes().getLength());
    }
    
    public void testNamespaceConsolidation() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/Empty.xsd");
        SchemaModel model2 = Util.loadSchemaModel("resources/Empty_loanApp.xsd");
        
        SchemaComponentFactory factory = model.getFactory();
        GlobalElement ge = factory.createGlobalElement();
        assertNotNull(ge.getPeer().lookupPrefix(XMLConstants.W3C_XML_SCHEMA_NS_URI));
        assertEquals(XMLConstants.W3C_XML_SCHEMA_NS_URI, ge.getPeer().getNamespaceURI());
        Annotation ann = factory.createAnnotation();
        ge.setAnnotation(ann);
        assertNull(ann.getPeer().getAttributeNode(XMLConstants.XMLNS_ATTRIBUTE));
        Documentation doc = factory.createDocumentation();
        ann.addDocumentation(doc);
        assertNull(doc.getPeer().getAttributeNode(XMLConstants.XMLNS_ATTRIBUTE));
        Element copy = (Element) ge.getPeer().cloneNode(true);
        
        model.startTransaction();
        model.getSchema().addElement(ge);
        model.endTransaction();
        
        assertNull(ge.getPeer().getAttributeNode(XMLConstants.XMLNS_ATTRIBUTE));
        assertEquals("element", ((Element)ge.getPeer()).getTagName());
        
        GlobalElement geCopy = (GlobalElement) model2.getFactory().create(copy, model2.getSchema());
        model2.startTransaction();
        model2.getSchema().addElement(geCopy);
        model2.endTransaction();
        
        assertNull(ge.getPeer().getAttributeNode(XMLConstants.XMLNS_ATTRIBUTE));
        assertEquals("xs:element", ((Element)geCopy.getPeer()).getTagName());
    }
    
    public void testAddRefBeforeAddToTree() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/PurchaseOrder.xsd");
        SchemaImpl schema = (SchemaImpl) model.getSchema();
        GlobalElement ge = new ArrayList<GlobalElement>(schema.getElements()).get(1);
        SchemaComponentFactory factory = model.getFactory();
        ElementReference er = factory.createElementReference();
        assertFalse(er.isInDocumentModel());
        
        NamedComponentReference<GlobalElement> ref = er.createReferenceTo(ge, GlobalElement.class);
        String namespace = ref.getEffectiveNamespace();
        assertEquals(null, ((SchemaComponentImpl)er).lookupPrefix(namespace));
        assertEquals("po:comment", ref.getRefString());
        assertEquals(namespace, er.getPeer().getAttribute(XMLConstants.XMLNS_ATTRIBUTE+":po"));

        er.setRef(ref);
        assertEquals(namespace, er.getPeer().getAttribute(XMLConstants.XMLNS_ATTRIBUTE+":po"));
        
        model.startTransaction();
        GlobalComplexType gct = model.getSchema().getComplexTypes().iterator().next();
        Sequence seq = (Sequence) gct.getDefinition();
        seq.addContent(er, 0);
        model.endTransaction();
        
        assertNull(er.getPeer().getAttributeNode(XMLConstants.XMLNS_ATTRIBUTE+":po"));
    }
    
}

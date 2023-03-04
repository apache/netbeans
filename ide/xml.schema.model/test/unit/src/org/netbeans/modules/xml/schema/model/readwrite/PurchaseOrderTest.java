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
 * PurchaseOrderTest.java
 * JUnit based test
 *
 * Created on October 14, 2005, 6:18 AM
 */

package org.netbeans.modules.xml.schema.model.readwrite;

import java.util.Collection;
import java.util.Iterator;
import javax.swing.text.Document;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.impl.SchemaImpl;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Text;
/**
 *
 * @author nn136682
 */
public class PurchaseOrderTest extends NbTestCase {
    
    public PurchaseOrderTest(String testName) {
        super(testName);
        
    }
    
    public static final String TEST_XSD = "resources/PurchaseOrder.xsd";
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }
    
    public String getSchemaResourcePath() {
        return TEST_XSD;
    }
    
    public void testRead() throws Exception {
        SchemaModel model = Util.loadSchemaModel(getSchemaResourcePath());
        SchemaImpl si = (SchemaImpl) model.getSchema();
        si.accept(new SchemaTestVisitor());
        assertTrue(seenRef);
    }
    
    public void testWrite() throws Exception {
        SchemaModel model = Util.createEmptySchemaModel();
        SchemaImpl si = (SchemaImpl)model.getSchema();
        assertNotNull(si);
        SchemaComponentFactory factory = model.getFactory();
        
        //set attributes
        model.startTransaction();
        si.setAttributeFormDefault(Form.UNQUALIFIED);
        si.setElementFormDefault(Form.UNQUALIFIED);
        si.setTargetNamespace("http://www.example.com/PO1");
        
        
        //create and add the USAddress global complex type
        GlobalComplexType usAddressComplexType = factory.createGlobalComplexType();
        si.addComplexType(usAddressComplexType);
        assertNull("no xsd prefix", ((AbstractDocumentComponent)si).getPeer().getPrefix());
        usAddressComplexType.setName("USAddress");
        Sequence sequence = factory.createSequence();
        //create and add name element
        LocalElement el = factory.createLocalElement();
        el.setName("name");
        GlobalSimpleType stringType = Util.getPrimitiveType("string");
        NamedComponentReference<GlobalSimpleType> ref =
                factory.createGlobalReference(stringType, GlobalSimpleType.class,
                el);
        el.setType(ref);
        assertEquals("refString", "xsd:string", ref.getRefString());
        sequence.addContent(el, 0);
        
        //FIXME
        //assertNull("no xmlns:xsd", ((AbstractDocumentComponent)el).getPeer().getAttribute("xmlns:xsd"));
        
        //create and add street element
        el = factory.createLocalElement();
        el.setName("street");
        ref = factory.createGlobalReference(stringType, GlobalSimpleType.class,
                el);
        
        el.setType(ref);
        sequence.addContent(el, 1);
        usAddressComplexType.setDefinition(sequence);
        
        //create the comment global element
        GlobalElement commentGE = factory.createGlobalElement();
        si.addElement(commentGE);
        commentGE.setName("comment");
        ref = factory.createGlobalReference(stringType, GlobalSimpleType.class,
                commentGE);
        commentGE.setType(ref);
        
        //create and add the PurchaseOrderType
        GlobalComplexType poComplexType = factory.createGlobalComplexType();
        si.addComplexType(poComplexType);
        poComplexType.setName("PurchaseOrderType");
        sequence = factory.createSequence();
        el = factory.createLocalElement();
        el.setName("shipTo");
        NamedComponentReference<GlobalComplexType>ref2 =
                factory.createGlobalReference(usAddressComplexType,
                GlobalComplexType.class, el);
        
        el.setType(ref2);
        sequence.addContent(el, 0);
        el = factory.createLocalElement();
        el.setName("billTo");
        ref2 = factory.createGlobalReference(usAddressComplexType,
                GlobalComplexType.class, el);
        
        el.setType(ref);
        sequence.addContent(el, 1);
        ElementReference er = factory.createElementReference();
        NamedComponentReference<GlobalElement> refEl =
                factory.createGlobalReference(commentGE, GlobalElement.class, el);
        
        er.setRef(refEl);
        er.setMinOccurs(0);
        sequence.addContent(er, 2);
        poComplexType.setDefinition(sequence);
        
        //create purchaseOrder global element
        GlobalElement poGE = factory.createGlobalElement();
        si.addElement(poGE);
        poGE.setName("purchaseOrder");
        ref2 = factory.createGlobalReference(poComplexType,
                GlobalComplexType.class, poGE);
        
        poGE.setType(ref2);
        
        //create simple type
        GlobalSimpleType simpleType = factory.createGlobalSimpleType();
        si.addSimpleType(simpleType);
        simpleType.setName("allNNI");
        Annotation ann = factory.createAnnotation();
        simpleType.setAnnotation(ann);
        Documentation documentation = factory.createDocumentation();
        ann.addDocumentation(documentation);
        Text txt = model.getDocument().createTextNode("documentation for simple type");
        org.w3c.dom.Element e = documentation.getDocumentationElement(); 
        e.appendChild(txt);
        documentation.setDocumentationElement(e);
        Union union = factory.createUnion();
        GlobalSimpleType nonNegativeInteger = Util.getPrimitiveType("nonNegativeInteger");
        NamedComponentReference<GlobalSimpleType> nniRef =
                factory.createGlobalReference(nonNegativeInteger,
                GlobalSimpleType.class, union);
        union.addMemberType(nniRef);
        GlobalSimpleType nonPositiveInteger = Util.getPrimitiveType("nonPositiveInteger");
        NamedComponentReference<GlobalSimpleType> npiRef =
                factory.createGlobalReference(nonPositiveInteger,
                GlobalSimpleType.class, union);
        union.addMemberType(npiRef);
        simpleType.setDefinition(union);
        
        si.setVersion("1.3");
        model.endTransaction();
        //model.flush();
        //Util.dumpToTempFile(doc);
        Document d = (Document) model.getModelSource().getLookup().lookup(Document.class);
        //System.out.println(d.getText(0,d.getLength()));
        
        //verify attributes
        assertEquals("schema's attributeFormDefault", Form.UNQUALIFIED.toString(), si.getAttributeFormDefault().toString());
        assertEquals("schema's elementFormDefault", Form.UNQUALIFIED.toString(), si.getElementFormDefault().toString());
        assertEquals("schema's targetNamespace: ", "http://www.example.com/PO1", si.getTargetNamespace());
        
        //Util.dumpToFile(si.getModel().getBaseDocument(), new File(getWorkDir(), "test.xsd"));
        
        si.accept(new SchemaTestVisitor());
        assertTrue(seenRef);
        assertEquals("testWrite read again", 1, countShipToVisit);
    }
    
    private class SchemaTestVisitor extends DefaultSchemaVisitor {
        
        /** Creates a new instance of SchemaTestVisitor */
        public SchemaTestVisitor() {
        }
        
        public void visit(Schema e) {
            java.util.List<SchemaComponent> ch = e.getChildren();
            for (SchemaComponent c : ch) {
                if (c instanceof GlobalComplexType) {
                    visit((GlobalComplexType)c);
                } else if (c instanceof GlobalElement) {
                    visit((GlobalElement) c);
                } else if (c instanceof GlobalSimpleType){
                    visit((GlobalSimpleType)c);
                }
            }
        }
        
        public void visit(GlobalSimpleType e){
            Collection<SchemaComponent> ch = e.getChildren();
            for (SchemaComponent c : ch) {
                if (c instanceof Union) {
                    visit((Union)c);
                }
            }
        }
        
        public void visit(Union e){
            Collection<NamedComponentReference<GlobalSimpleType>> mts = e.getMemberTypes();
            assertEquals("Number of union member types", 2, mts.size());
            Iterator<NamedComponentReference<GlobalSimpleType>> iterator = mts.iterator();
            NamedComponentReference<GlobalSimpleType> mt = iterator.next();
            assertFalse(mt.isBroken());
            assertEquals("First union member", "nonNegativeInteger", mt.get().getName());
            mt = iterator.next();
            assertFalse(mt.isBroken());
            assertEquals("Second union member", "nonPositiveInteger", mt.get().getName());
        }
        
        public void visit(GlobalComplexType e) {
            
            Collection<SchemaComponent> ch = e.getChildren();
            for (SchemaComponent c : ch) {
                if (c instanceof Sequence) {
                    visit((Sequence)c);
                }
            }
        }
        
        public void visit(Sequence e) {
            
            Collection<SchemaComponent> ch = e.getChildren();
            for (SchemaComponent c : ch) {
                if (c instanceof LocalElement) {
                    visit((LocalElement)c);
                }
                if (c instanceof ElementReference) {
                    visit((ElementReference)c);
                }
            }
        }
    /*
      <complexType name="PurchaseOrderType">
        <sequence>
          <element name="shipTo"    type="po:USAddress"/>
          <element name="billTo"    type="po:USAddress"/>
          <element ref="po:comment" minOccurs="0"/>
      <complexType name="USAddress">
        <sequence>
          <element name="name"   type="string"/>
          <element name="street" type="string"/>
     */
        public void visit(ElementReference e) {
            if (e.getRef() != null && e.getRef().get() != null) {
                NamedComponentReference<GlobalElement> ge = e.getRef();
                assertTrue("PurchaseOrderType.ref(po:comment)", ge.getRefString().endsWith(":comment"));
                seenRef = true;
            }
        }
        
        public void visit(LocalElement e) {
            if (e.getName().equals("shipTo")) {
                NamedComponentReference<? extends GlobalType> t = e.getType();
                assertTrue("PurchaseOrderType:shipTo ref GlobalComplexType", t.get() instanceof GlobalComplexType);
                GlobalComplexType gt = (GlobalComplexType)t.get();
                assertEquals("PurchaseOrderType:shipTo complexType.name", "USAddress", gt.getName());
                countShipToVisit++;
            } else if (e.getName().equals("street")) {
                NamedComponentReference<? extends GlobalType> t = e.getType();
                GlobalSimpleType gst = (GlobalSimpleType) t.get();
                assertEquals("USAddress:street type string", "string", gst.getName());
            }
        }
        
        /*
              <element name="purchaseOrder" type="po:PurchaseOrderType"/>
              <element name="comment"       type="string"/>
         */
        public void visit(GlobalElement e) {
            
            if (e.getName().equals("purchaseOrder")) {
                assertTrue("purchaseOrder is of ComplexType", e.getType().get() instanceof GlobalComplexType);
                GlobalComplexType gct = (GlobalComplexType) e.getType().get();
                assertEquals("purchaseOrder.type", "PurchaseOrderType", gct.getName());
            } else if (e.getName().equals("comment")) {
                
                assertTrue("comment is a PrimitiveType (GlobalSimpleType)", e.getType().get() instanceof GlobalSimpleType);
            }
        }
    }
    
    boolean seenRef = false;
    int countShipToVisit = 0;

    public void testPrefixConsolidation() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
        SchemaModelImpl model = (SchemaModelImpl) Util.createEmptySchemaModel();
        Schema s = model.getSchema();
        SchemaComponentFactory factory = model.getFactory();
        
        GlobalElement ge1 = factory.createGlobalElement();
        ge1.setName("my-auto-loan-application");
        LocalComplexType lct = factory.createLocalComplexType();
        assertNotNull(lct.getPeer().getAttributeNode("xmlns:xsd"));
        ge1.setInlineType(lct);
        assertNull(lct.getPeer().getAttributeNode("xmlns:xsd"));
        ComplexContent cc = factory.createComplexContent();
        assertNotNull(cc.getPeer().getAttributeNode("xmlns:xsd"));
        lct.setDefinition(cc);
        assertNull(cc.getPeer().getAttributeNode("xmlns:xsd"));
        
        model.startTransaction();
        model.getSchema().addElement(ge1);
        model.endTransaction();
        
        assertNull(lct.getPeer().getAttributeNode("xmlns:xsd"));
        assertNull(cc.getPeer().getAttributeNode("xmlns:xsd"));
    }
}

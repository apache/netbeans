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

package org.netbeans.modules.xml.axi;

import java.lang.reflect.Field;
import java.util.Map;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.xml.axi.datatype.BooleanType;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.xam.AbstractModelFactory;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;


/**
 *
 * @author Ayub Khan
 */
public class SchemaGeneratorTest extends AbstractTestCase {
    
    public static final String TEST_XSD         = "resources/OTA_TI_simple.xsd";
    public static final String GLOBAL_ELEMENT   = "OTA_TravelItineraryRS";
    
    private Document doc = null;
    
    public static final int GE_SIZE = 4;
    
    public SchemaGeneratorTest(String testName) {
        super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    protected void tearDown() throws Exception {
        axiModel.endTransaction();
        AXIModelFactory f = AXIModelFactory.getDefault();
        Field fld = AbstractModelFactory.class.getDeclaredField("cachedModels");
        fld.setAccessible(true);
        Map cacheMap = (Map)fld.get(f);
        cacheMap.clear();
        super.tearDown();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SchemaGeneratorTest.class);
        
        return suite;
    }
    
    public void testGenerateSchema() throws Exception {
        generateSchema();
    }
    
    /**
     * Test of createElement method, of class org.netbeans.modules.xml.axi.XAMFactory.
     */
    public void generateSchema() throws Exception {
        Element element = globalElement;
        assertNotNull(element);
        SchemaModel sm = null;
        sm = getSchemaModel();
        doc = ((AbstractDocumentModel)sm).getBaseDocument();
        //DefaultSchemaGenerator schemaGenerator = new DefaultSchemaGenerator(getAXIModel());
        
        assertEquals("global elements",GE_SIZE,getSchemaModel().getSchema().getElements().size());
        //global element name change
        axiModel.startTransaction();
        for(Element e:axiModel.getRoot().getElements())
            if(e.getName().equals("CancellationStatus"))
                e.setName(e.getName()+"_");
        axiModel.endTransaction();
        assertEquals("global elements",GE_SIZE,getSchemaModel().getSchema().getElements().size());
        boolean found = false;
        for(GlobalElement ge:sm.getSchema().getElements()) {
            if(ge.getName().startsWith("CancellationStatus_")) {
                found = true;
                assertEquals("updated schemamodel", ge.getName(), "CancellationStatus_");
                assertEquals("updated schemamodel type",
                        ((LocalElement)ge.getChildren().get(0).//complexType
                        getChildren().get(0).//all
                        getChildren().get(0)).//element[date]
                        getType().getQName().getLocalPart(), "date");
            }
        }
        assertTrue("found CancellationStatus_", found);
        //System.out.println("doc: "+doc.getText(0, doc.getLength()));
        
        //check an attribute change is flushed to schema
        axiModel.startTransaction();
        for(Element e:axiModel.getRoot().getElements()) {
            if(e.getName().equals(GLOBAL_ELEMENT)) {
                for(AXIComponent e2: e.getCompositor().getChildren()) {
                    if(e2 instanceof Element) {
                        if(((Element)e2).getName().equals("Errors")) {
                            AbstractAttribute attr = ((Element)((Element)e2).getChildren().get(0).getChildren().get(0)).getAttributes().get(0);
                            assertEquals("Language", attr.getName());
                            if(attr instanceof Attribute) {
                                ((Attribute)attr).setName("XYZ");
                            }
                        }
                    }
                }
            }
        }
        axiModel.endTransaction();
        //System.out.println("doc: "+doc.getText(0, doc.getLength()));
        assertEquals("global elements",GE_SIZE,getSchemaModel().getSchema().getElements().size());
        
        found = false;
        for(GlobalElement ge:sm.getSchema().getElements()) {
            if(ge.getName().startsWith(GLOBAL_ELEMENT)) {
                LocalComplexType lct = (LocalComplexType) ge.getChildren().get(1);
                Choice choice = (Choice) lct.getChildren().get(0);
                LocalElement le = (LocalElement) choice.getChildren().get(1);
                GlobalComplexType gct = (GlobalComplexType) le.getType().get();
                le = (LocalElement) gct.getChildren().get(1).getChildren().get(0);
                gct = (GlobalComplexType) le.getType().get();
                SimpleExtension se = (SimpleExtension) gct.getChildren().get(1).getChildren().get(0);
                gct = (GlobalComplexType)se.getBase().get();
                AttributeGroupReference agr = (AttributeGroupReference)gct.getChildren().get(1).getChildren().get(0).getChildren().get(0);
                GlobalAttributeGroup gag = agr.getGroup().get();
                LocalAttribute la = (LocalAttribute)gag.getChildren().get(1);
                assertEquals("updated schemamodel", "XYZ", la.getName());
                found = true;
            }
        }
        assertTrue("Should have verified updated element", found);
        validateSchema(sm);
    }
    
    public void testGenerateSchema2() {
        assertEquals("global elements",GE_SIZE,getSchemaModel().getSchema().getElements().size());
        Element element = axiModel.getComponentFactory().createElement();
        element.setName("NewElement"+axiModel.getRoot().getElements().size());
        
        axiModel.startTransaction();
        try {
            axiModel.getRoot().addElement(element);
        } finally {
            axiModel.endTransaction();
        }
        assertEquals("global elements",GE_SIZE+1,getSchemaModel().getSchema().getElements().size());
        
//		try {
//			SchemaModel sm = getSchemaModel();
//			doc = ((AbstractDocumentModel)sm).getBaseDocument();
//			System.out.println("doc: "+doc.getText(0, doc.getLength()));
//		} catch (BadLocationException ex) {
//			ex.printStackTrace();
//		}
        
        axiModel.startTransaction();
        try {
            axiModel.getRoot().removeElement(element);
        } finally {
            axiModel.endTransaction();
        }
        assertEquals("global elements",GE_SIZE,getSchemaModel().getSchema().getElements().size());
        validateSchema(axiModel.getSchemaModel());
//		try {
//			SchemaModel sm = getSchemaModel();
//			doc = ((AbstractDocumentModel)sm).getBaseDocument();
//			System.out.println("doc: "+doc.getText(0, doc.getLength()));
//		} catch (BadLocationException ex) {
//			ex.printStackTrace();
//		}
    }
    
    
    public void testDeleteExistingGlobalElement() {
        assertEquals("global elements",GE_SIZE,getSchemaModel().getSchema().getElements().size());
        Element element = axiModel.getComponentFactory().createElement();
        element.setName("NewElement"+axiModel.getRoot().getElements().size());
        //global element name change
        axiModel.startTransaction();
        try {
            for(Element e:axiModel.getRoot().getElements())
                if(e.getName().equals("CancellationStatus2"))
                    axiModel.getRoot().removeElement(e);
        } finally {
            axiModel.endTransaction();
        }
        assertEquals("global elements",GE_SIZE-1,getSchemaModel().getSchema().getElements().size());
        validateSchema(axiModel.getSchemaModel());
//		try {
//			SchemaModel sm = getSchemaModel();
//			doc = ((AbstractDocumentModel)sm).getBaseDocument();
//			System.out.println("doc: "+doc.getText(0, doc.getLength()));
//		} catch (BadLocationException ex) {
//			ex.printStackTrace();
//		}
    }
    
    public void testDeleteExistingLocalElement() {
        assertEquals("global elements",GE_SIZE,getSchemaModel().getSchema().getElements().size());
        Element element = axiModel.getComponentFactory().createElement();
        element.setName("NewElement"+axiModel.getRoot().getElements().size());
        axiModel.startTransaction();
        for(Element e:axiModel.getRoot().getElements()) {
            if(e.getName().equals(GLOBAL_ELEMENT)) {
                Element del = null;
                for(AXIComponent e2: e.getCompositor().getChildren()) {
                    if(e2 instanceof Element) {
                        if(((Element)e2).getName().equals("Errors_")) {
                            del = (Element) e2;
                        }
                    }
                }
                if(del != null)
                    e.getCompositor().removeElement(del);
            }
        }
        axiModel.endTransaction();
        // deleted LOCAL element, no effect on global element
        assertEquals("global elements",GE_SIZE,getSchemaModel().getSchema().getElements().size());
        validateSchema(axiModel.getSchemaModel());
        
//        try {
//            SchemaModel sm = getSchemaModel();
//            doc = ((AbstractDocumentModel)sm).getBaseDocument();
//            System.out.println("doc: "+doc.getText(0, doc.getLength()));
//        } catch (BadLocationException ex) {
//            ex.printStackTrace();
//        }
    }
    
    public void testSimpleToComplexContent() {
        // 4: OTA_TravelItineraryRS, CancellationStatus, CancellationStatus2, MyAddress
        assertEquals("global elements", GE_SIZE,getSchemaModel().getSchema().getElements().size());        
        Element newElem = axiModel.getComponentFactory().createElement();
        newElem.setName("City");
        Attribute newAttr = axiModel.getComponentFactory().createAttribute();
        newAttr.setName("a1");
        
        GlobalElement ge2 = null;
        Element e2 = null;
        for(Element e:axiModel.getRoot().getElements()) {
            if(e.getName().equals("MyAddress")) {
                e2 = e;
                ge2 = (GlobalElement) e.getPeer();
            }
        }
        assertTrue("complexcontent",
                ((GlobalComplexType)ge2.getType().get()).getDefinition() instanceof SimpleContent);
        assertEquals("global Complex types",4,getSchemaModel().getSchema().getComplexTypes().size());
        
        axiModel.startTransaction();
        e2.addElement(newElem);
        e2.addAttribute(newAttr);
        axiModel.endTransaction();   
   
        assertEquals("global elements",GE_SIZE,getSchemaModel().getSchema().getElements().size());
        assertEquals("global Complex types",4,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("local element",1,e2.getChildElements().size());
        assertEquals("local attr",2,e2.getAttributes().size());
        assertNotNull("complexcontent", ge2.getType());
        org.netbeans.modules.xml.schema.model.Sequence seq = 
                (org.netbeans.modules.xml.schema.model.Sequence) 
                    ((GlobalComplexType)ge2.getType().get()).getDefinition();
        assertNotNull("sequence", seq);
        assertEquals("complexcontent attr size", 2, 
                ((GlobalComplexType)ge2.getType().get()).getLocalAttributes().size());        
        validateSchema(axiModel.getSchemaModel());
                     
        //FIXME: Exception on AXIComponent.removeChild()
//        axiModel.startTransaction();
//        e2.removeElement(newElem);
//        e2.removeAttribute(newAttr);
//        axiModel.endTransaction();     
//        
//        assertEquals("global elements",GE_SIZE-1,getSchemaModel().getSchema().getElements().size());
//        assertNotNull("complexcontent", ge2.getType());
//        newcc = (ComplexContent) ((GlobalComplexType)ge2.getType().get()).getDefinition();
//        assertNotNull("simplecontent", newcc);
//        ccr = (ComplexContentRestriction) newcc.getLocalDefinition();
//        assertNotNull("complexcontent", ccr);
//        assertNotNull("complexcontent", ccr.getBase().get() instanceof GlobalSimpleType);
//        assertEquals("complexcontent attr size", 1, ccr.getLocalAttributes().size());        
//        validateSchema(axiModel.getSchemaModel());        
    }   
    
    public void testCreateSimpleContent() {
        assertEquals("global elements",GE_SIZE,getSchemaModel().getSchema().getElements().size());        
        Element newElem = axiModel.getComponentFactory().createElement();
        newElem.setName("e1");
        
        Element newElem2 = axiModel.getComponentFactory().createElement();
        newElem2.setName("e2");
        
        axiModel.startTransaction();
        axiModel.getRoot().addElement(newElem);
        axiModel.endTransaction();
        
        GlobalElement ge2 = (GlobalElement) newElem.getPeer();
        
        axiModel.startTransaction();
        newElem.setType(new BooleanType());
        axiModel.endTransaction();
        
        assertTrue("complexcontent",ge2.getType().get() instanceof GlobalSimpleType);
        assertEquals("global elements",GE_SIZE + 1,getSchemaModel().getSchema().getElements().size());
        validateSchema(axiModel.getSchemaModel());
        
        Attribute newAttr = axiModel.getComponentFactory().createAttribute();
        newAttr.setName("a1");
        
        axiModel.startTransaction();
        newElem.addAttribute(newAttr);
        axiModel.endTransaction();
        assertNull("simplecontent", ge2.getType());
        SimpleContent newsc = (SimpleContent) ((LocalComplexType)ge2.getInlineType()).getDefinition();
        assertNotNull("simplecontent", newsc);
        SimpleExtension se = (SimpleExtension) newsc.getLocalDefinition();
        assertNotNull("simplecontent", se);
        assertNotNull("simplecontent", se.getBase().get() instanceof GlobalSimpleType);
        assertEquals("simplecontent attr size", 1, se.getLocalAttributes().size());
        
        axiModel.startTransaction();
        newElem.removeAttribute(newAttr);
        axiModel.endTransaction();
        assertNull("simplecontent", ge2.getType());
        newsc = (SimpleContent) ((LocalComplexType)ge2.getInlineType()).getDefinition();
        assertNotNull("simplecontent", newsc);
        se = (SimpleExtension) newsc.getLocalDefinition();
        assertNotNull("simplecontent", se);
        assertNotNull("simplecontent", se.getBase().get() instanceof GlobalSimpleType);
        assertEquals("simplecontent attr size", 0, se.getLocalAttributes().size());
    }    
}

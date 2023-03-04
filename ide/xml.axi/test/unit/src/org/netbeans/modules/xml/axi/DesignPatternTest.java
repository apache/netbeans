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

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.xml.axi.Compositor.CompositorType;
import org.netbeans.modules.xml.axi.datatype.Base64BinaryType;
import org.netbeans.modules.xml.axi.datatype.DateType;
import org.netbeans.modules.xml.axi.datatype.IntegerType;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;


/**
 *
 * @author Ayub Khan
 */
public class DesignPatternTest extends AbstractTestCase {
    
    public static final String LINE_RUSSIAN_DOLL_XSD   = "resources/line_RussianDoll.xsd";
    public static final String LINE_VENETIAN_BLIND_XSD = "resources/line_VenetianBlind.xsd";
    public static final String LINE_SALAMI_SLICE_XSD   = "resources/line_SalamiSlice.xsd";
    public static final String LINE_GARDEN_OF_EDEN_XSD = "resources/line_GardenOfEden.xsd";
    public static final String LOANAPP_XSD = "resources/LoanApplication.xsd";
    public static final String EMPTY_XSD = "resources/empty.xsd";
    public static final String ADDRESS3_XSD = "resources/address3.xsd";
    public static final String GLOBAL_ELEMENT   = "Line";
    
    private Document doc = null;
    
    public DesignPatternTest(String testName) {
        super(testName, LINE_RUSSIAN_DOLL_XSD, GLOBAL_ELEMENT);
    }
    
    public static Test suite() {
//        TestSuite suite = new TestSuite(DesignPatternTest.class);
        TestSuite suite = new TestSuite();
        suite.addTest(new DesignPatternTest("testLineGardenOfEden"));
        suite.addTest(new DesignPatternTest("testLineSalamiSlice"));
        suite.addTest(new DesignPatternTest("testLineVenetianBlind"));
        suite.addTest(new DesignPatternTest("testLineRussianDoll"));
        //suite.addTest(new DesignPatternTest("testLoanApp"));
        suite.addTest(new DesignPatternTest("testEmpty"));
        //suite.addTest(new DesignPatternTest("testChangeCompositor"));
        return suite;
    }
    
    public void testChangeCompositor() {
        print("testChangeCompositor");
        try {
            loadModel(ADDRESS3_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        
        ContentModel cm = findContentModel("USAddress");
        assert(cm != null);
        Compositor comp = (Compositor)cm.getChildren().get(0);
        assert(comp != null);
        assert(comp.getChildren().size() == 5);
        Element e1 = (Element)comp.getChildren().get(0);
        assert(e1.isReference());
        getAXIModel().startTransaction();
        comp.setType(CompositorType.ALL);
        getAXIModel().endTransaction();
        assert(comp != null);
        assert(comp.getChildren().size() == 5);
        e1 = (Element)comp.getChildren().get(0);
        assert(e1.isReference());
    }
    
    public void testLineRussianDoll() {
        print("testLineRussianDoll");
        try {
            loadModel(LINE_RUSSIAN_DOLL_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        axiModel.setSchemaDesignPattern(SchemaGenerator.Pattern.RUSSIAN_DOLL);
        createLine();
        
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",2,getSchemaModel().getSchema().getElements().size());
    }
    
    public void testLineVenetianBlind() {
        print("testLineVenetianBlind");
        try {
            loadModel(LINE_VENETIAN_BLIND_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",1,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        axiModel.setSchemaDesignPattern(SchemaGenerator.Pattern.VENITIAN_BLIND);
        createLine();
        
//		printDocument();
        assertEquals("global complex types",3,getSchemaModel().getSchema().getComplexTypes().size());
        //FIXME should be 1 instead of 2
//		assertEquals("global simple types",1,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",2,getSchemaModel().getSchema().getElements().size());
        
        //FIXME gives 1 instead of 2
//		assertEquals("AXI content models",2,axiModel.getRoot().getContentModels().size());
        //FIXME gives 4 instead of 2
//		assertEquals("AXI global elements",2,axiModel.getRoot().getElements().size());
    }
    
    public void testLineSalamiSlice() {
        print("testLineSalamiSlice");
        try {
            loadModel(LINE_SALAMI_SLICE_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",3,getSchemaModel().getSchema().getElements().size());
        
        axiModel.setSchemaDesignPattern(SchemaGenerator.Pattern.SALAMI_SLICE);
        createLine();
        
        //printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        //FIXME gives 8 instead of 9
//                assertEquals("global elements",9,getSchemaModel().getSchema().getElements().size());
    }
    
    public void testLineGardenOfEden() {
        print("testLineGardenOfEden");
        try {
            loadModel(LINE_GARDEN_OF_EDEN_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",2,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",3,getSchemaModel().getSchema().getElements().size());
        
        axiModel.setSchemaDesignPattern(SchemaGenerator.Pattern.GARDEN_OF_EDEN);
        createLine();
        
//		printDocument();
        assertEquals("global complex types",5,getSchemaModel().getSchema().getComplexTypes().size());
        //FIXME should be 1 instead of 2
//		assertEquals("global simple types",1,getSchemaModel().getSchema().getSimpleTypes().size());
        //FIXME gives 8 instead of 9
//		assertEquals("global elements",9,getSchemaModel().getSchema().getElements().size());
        
        //FIXME gives 2 instead of 3
//		assertEquals("AXI content models",3,axiModel.getRoot().getContentModels().size());
        //FIXME gives 4 instead of 9
//		assertEquals("AXI global elements",9,axiModel.getRoot().getElements().size());
    }
    
    private void createLine() {
        axiModel.startTransaction();
        try {
            //Add Line to root
            Element element = axiModel.getComponentFactory().createElement();
            element.setName("Line1");
            axiModel.getRoot().addElement(element);
            
            //Add Sequence to Line
            Compositor seq = axiModel.getComponentFactory().createSequence();
            element.addCompositor(seq);
            
            //Create Point
            Element point = axiModel.getComponentFactory().createElement();
            point.setName("Point1");
            
            //Add Point to Line
            seq.addElement(point);
            
            //Create and attribute x to Point
            Attribute x = axiModel.getComponentFactory().createAttribute();
            x.setName("x");
            point.addAttribute(x);
            Base64BinaryType b = new Base64BinaryType();
            b.addEnumeration("XYZ");
            x.setType(b);
            //Create and attribute y to Point
            Attribute y = axiModel.getComponentFactory().createAttribute();
            y.setName("y");
            point.addAttribute(y);
            
            //Add Sequence to Point
            Compositor seq2 = axiModel.getComponentFactory().createSequence();
            point.addCompositor(seq2);
            
            //Add Choice to Sequence
            Compositor c = axiModel.getComponentFactory().createChoice();
            seq2.addCompositor(c);
            
            //Add Info1 to Choice
            Element info1 = axiModel.getComponentFactory().createElement();
            info1.setName("Info1");
            c.addElement(info1);
            //Add Info2 to Choice
            Element info2 = axiModel.getComponentFactory().createElement();
            info2.setName("Info2");
            c.addElement(info2);
            
            //Add Info3 to Sequence
            Element ce3 = axiModel.getComponentFactory().createElement();
            ce3.setName("Info3");
            DateType d = new DateType();
            d.addEnumeration("00:00:00");
            ce3.setType(d);
            seq2.addElement(ce3);
            
            //Add Sequence to Info1
            Compositor seq11 = axiModel.getComponentFactory().createSequence();
            info1.addCompositor(seq11);
            //Add Info11 to Sequence
            Element info11 = axiModel.getComponentFactory().createElement();
            info11.setName("Info11");
            seq11.addElement(info11);
        } finally {
            axiModel.endTransaction();
        }
    }
    
    public void testLoanApp() {
        print("testLoanApp");
        try {
            loadModel(LOANAPP_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",7,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",2,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        axiModel.setSchemaDesignPattern(SchemaGenerator.Pattern.VENITIAN_BLIND);
        ModifyLoanApp();
        
//		printDocument();
        assertEquals("global complex types",9,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",6,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
    }
    
    private void ModifyLoanApp() {
        axiModel.startTransaction();
        try {
            //Add Line to root
            Element tradeIn = (Element) ((Compositor)axiModel.getRoot().
                    getElements().get(0).getCompositor()).
                    getChildren().get(6);
            Element vehicle = (Element)((Compositor)tradeIn.getCompositor()).getChildren().get(0);
            Element vehicleYear = (Element) ((Compositor)vehicle.getCompositor()).getChildren().get(0);
            
            //Create and attribute x to Point
            Attribute x = axiModel.getComponentFactory().createAttribute();
            x.setName("attr1");
            vehicleYear.addAttribute(x);
            Base64BinaryType b1 = new Base64BinaryType();
            b1.addEnumeration("XYZ");
            x.setType(b1);
            
            Attribute y = axiModel.getComponentFactory().createAttribute();
            y.setName("attr2");
            vehicleYear.addAttribute(y);
            Base64BinaryType b2 = new Base64BinaryType();
            b2.addEnumeration("ABC");
            y.setType(b2);
            
            //Add Sequence to year
            Compositor seq = axiModel.getComponentFactory().createSequence();
            vehicleYear.addCompositor(seq);
            
            //Create Gregorian
            Element gregorian = axiModel.getComponentFactory().createElement();
            gregorian.setName("Gregorian");
            
            //Add Gregorian to year
            seq.addElement(gregorian);
            
            gregorian.setAbstract(true);
            gregorian.setBlock("testBlock");
            gregorian.setDefault("testDefault");
            gregorian.setFinal("testFinal");
            gregorian.setFixed("testFixed");
            gregorian.setForm(Form.QUALIFIED);
            gregorian.setMinOccurs("3");
            gregorian.setMaxOccurs("unbounded");
            gregorian.setNillable(null);
            gregorian.setType(new IntegerType());
            
            //Create Hijri
            Element hijri = axiModel.getComponentFactory().createElement();
            hijri.setName("Hijri");
            
            //Add Hijri to year
            seq.addElement(hijri);
            
            hijri.setAbstract(true);
            hijri.setBlock("testBlock");
            hijri.setDefault("testDefault");
            hijri.setFinal("testFinal");
            hijri.setFixed("testFixed");
            hijri.setForm(Form.UNQUALIFIED);
            hijri.setMinOccurs("0");
            hijri.setMaxOccurs("1");
            hijri.setNillable(true);
            
            ContentModel ct = axiModel.getComponentFactory().createComplexType();
            ct.setName("newType");
            axiModel.getRoot().addContentModel(ct);
            Attribute a1 = axiModel.getComponentFactory().createAttribute();
            a1.setName("newAttr");
            ct.addAttribute(a1);
            hijri.setType(ct);
        } finally {
            axiModel.endTransaction();
        }
    }
    
    
    public void testEmpty() {
        print("testEmpty");
        try {
            loadModel(EMPTY_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",0,getSchemaModel().getSchema().getElements().size());
        
        axiModel.setSchemaDesignPattern(SchemaGenerator.Pattern.GARDEN_OF_EDEN);
        addGlobalElementAndChildElement();
        
//		printDocument();
        assertEquals("global complex types",3,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",4,getSchemaModel().getSchema().getElements().size());
        
        axiModel.setSchemaDesignPattern(SchemaGenerator.Pattern.VENITIAN_BLIND);
        addGlobalElementAndChildElement();
        
//		printDocument();
        assertEquals("global complex types",5,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",5,getSchemaModel().getSchema().getElements().size());
        
        axiModel.setSchemaDesignPattern(SchemaGenerator.Pattern.SALAMI_SLICE);
        addGlobalElementAndChildElement();
        
//		printDocument();
        assertEquals("global complex types",5,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",9,getSchemaModel().getSchema().getElements().size());
        
        axiModel.setSchemaDesignPattern(SchemaGenerator.Pattern.RUSSIAN_DOLL);
        addGlobalElementAndChildElement();
        
//		printDocument();
        assertEquals("global complex types",5,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",10,getSchemaModel().getSchema().getElements().size());
    }
    
    private void addGlobalElementAndChildElement() {
        axiModel.startTransaction();
        try {
            //Add purchaseOrder to root
            Element element = axiModel.getComponentFactory().createElement();
            element.setName("purchaseOrder");
            axiModel.getRoot().addElement(element);
            
            //Add Sequence to purchaseOrder
            Compositor seq = axiModel.getComponentFactory().createSequence();
            element.addCompositor(seq);
            
            //Create billTo
            Element billTo = axiModel.getComponentFactory().createElement();
            billTo.setName("billTo");
            
            //Add billTo to purchaseOrder
            seq.addElement(billTo);
            
            //Add Sequence to purchaseOrder
            Compositor seq1 = axiModel.getComponentFactory().createSequence();
            billTo.addCompositor(seq1);
            
            //Create name
            Element name = axiModel.getComponentFactory().createElement();
            name.setName("name");
            
            //Add billTo to purchaseOrder
            seq1.addElement(name);
            
            //Add Sequence to purchaseOrder
            Compositor seq2 = axiModel.getComponentFactory().createSequence();
            name.addCompositor(seq2);
            
            //Create firstName
            Element firstName = axiModel.getComponentFactory().createElement();
            firstName.setName("firstName");
            
            //Add billTo to purchaseOrder
            seq2.addElement(firstName);
        } finally {
            axiModel.endTransaction();
        }
    }
    
    private void printDocument() {
        try {
            SchemaModel sm = getSchemaModel();
            doc = ((AbstractDocumentModel)sm).getBaseDocument();
            print("doc: "+doc.getText(0, doc.getLength()));
        } catch (BadLocationException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
    }
}

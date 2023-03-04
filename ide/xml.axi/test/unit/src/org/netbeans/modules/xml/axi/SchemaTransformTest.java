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

import java.io.IOException;
import java.util.Iterator;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.xml.axi.impl.AttributeImpl;
import org.netbeans.modules.xml.axi.impl.AttributeProxy;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ComplexTypeDefinition;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;


/**
 *
 * @author Ayub Khan
 */
public class SchemaTransformTest extends AbstractTestCase {
    
    public static final String LINE_RUSSIAN_DOLL_XSD   = "resources/line_RussianDoll.xsd";
    public static final String LINE_VENETIAN_BLIND_XSD = "resources/line_VenetianBlind.xsd";
    public static final String LINE_SALAMI_SLICE_XSD   = "resources/line_SalamiSlice.xsd";
    public static final String LINE_GARDEN_OF_EDEN_XSD = "resources/line_GardenOfEden.xsd";
    public static final String LOAN_APP_XSD   = "resources/LoanApplication.xsd";
    public static final String PO_XSD   = "resources/po.xsd";
    public static final String TYPES_XSD   = "resources/types.xsd";
    public static final String RECURSION_PO_XSD = "resources/recursion_po.xsd";
    public static final String ANNOTATION_XSD = "resources/annotation.xsd";
    public static final String NEWPO_XSD   = "resources/newpo.xsd";
    public static final String NEWPO1_XSD   = "resources/newpo1.xsd";
    public static final String COURIER_XSD   = "resources/courier.xsd";
    public static final String INCLUDE_XSD   = "resources/include.xsd";
    public static final String IMPORT_XSD   = "resources/import.xsd";
    public static final String ELEMENT_REUSE_XSD   = "resources/elementreuse.xsd";
    public static final String ELEMENT_REUSE1_XSD   = "resources/elementreuse1.xsd";
    public static final String ELEMENT_REUSE2_XSD   = "resources/elementreuse2.xsd";
    public static final String TYPE_REUSE_XSD   = "resources/typereuse.xsd";
    public static final String GLOBAL_ELEMENT   = "Line";
    
    private Document doc = null;
    
    public SchemaTransformTest(String testName) {
        super(testName, LINE_RUSSIAN_DOLL_XSD, GLOBAL_ELEMENT);
    }
    
    public static Test suite() {
//        TestSuite suite = new TestSuite(DesignPatternTest.class);
        TestSuite suite = new TestSuite();
        suite.addTest(new SchemaTransformTest("testFindUsageVisitor"));
        suite.addTest(new SchemaTransformTest("testLineGardenOfEden"));
        suite.addTest(new SchemaTransformTest("testLineSalamiSlice"));
        suite.addTest(new SchemaTransformTest("testLineVenetianBlind"));
        suite.addTest(new SchemaTransformTest("testLineRussianDoll"));
//        Disabled as referenced XSD file were partly not donated by oracle to apache
//        suite.addTest(new SchemaTransformTest("testTypes"));
        //FIXME suite.addTest(new SchemaTransformTest("testLoanApp"));
        suite.addTest(new SchemaTransformTest("testAnnotation"));
//        Disabled as referenced XSD file were partly not donated by oracle to apache
//        suite.addTest(new SchemaTransformTest("testPurchaseOrder"));
	//FIXME suite.addTest(new SchemaTransformTest("testDiscardTransform"));
        suite.addTest(new SchemaTransformTest("testRenameAfterTransform"));
//        Disabled as referenced XSD file were partly not donated by oracle to apache
//        suite.addTest(new SchemaTransformTest("testNewApproach"));
        suite.addTest(new SchemaTransformTest("testMultiSchemaInclude"));
        suite.addTest(new SchemaTransformTest("testMultiSchemaImport"));
        suite.addTest(new SchemaTransformTest("testGlobalElementReuse"));
        suite.addTest(new SchemaTransformTest("testGlobalElementReuse1"));
        //FIXME suite.addTest(new SchemaTransformTest("testGlobalElementReuse2"));
        suite.addTest(new SchemaTransformTest("testContentModelReuse"));
        return suite;
    }
    
    public void testFindUsageVisitor() {
        print("testFindUsageVisitor");
        try {
            loadModel(RECURSION_PO_XSD);
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",2,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.RUSSIAN_DOLL);
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
    }
    
    public void testLineGardenOfEden() {
        print("testLineGardenOfEden");
        try {
            loadModel(LINE_RUSSIAN_DOLL_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.GARDEN_OF_EDEN);
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",3,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",3,getSchemaModel().getSchema().getElements().size());
    }
    
    public void testLineSalamiSlice() {
        print("testLineSalamiSlice");
        try {
            loadModel(LINE_GARDEN_OF_EDEN_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",2,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",3,getSchemaModel().getSchema().getElements().size());
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.SALAMI_SLICE);
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",3,getSchemaModel().getSchema().getElements().size());
    }
    
    public void testLineVenetianBlind() {
        print("testLineVenetianBlind");
        try {
            loadModel(LINE_SALAMI_SLICE_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",3,getSchemaModel().getSchema().getElements().size());
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.VENITIAN_BLIND);
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",2,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
    }
    
    public void testLineRussianDoll() {
        print("testLineRussianDoll");
        try {
            loadModel(LINE_VENETIAN_BLIND_XSD);
        } catch (Exception ex) {
//			ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",1,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.RUSSIAN_DOLL);
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
    }
    
    public void testLoanApp() {
        print("testLoanApp");
        try {
            loadModel(LOAN_APP_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",7,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",2,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        //Transform from Venetian Blind to Russian Doll
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.RUSSIAN_DOLL);
            long end = System.currentTimeMillis();
            print("Time taken to transform from VB to RD: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",0,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",1,axiModel.getRoot().getElements().size());
        
        //Transform from Russian Doll to Venetian Blind
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.VENITIAN_BLIND);
            long end = System.currentTimeMillis();
            print("Time taken to transform from RD to VB: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",17,getSchemaModel().getSchema().getComplexTypes().size());
        //FIXME gives 19 instead of 8
//		assertEquals("global simple types",8,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",17,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",1,axiModel.getRoot().getElements().size());
        
        //Transform from Venetian Blind to Salami Slice
        try {
            long start = System.currentTimeMillis();
            
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.SALAMI_SLICE);
            long end = System.currentTimeMillis();
            print("Time taken to transform from VB to SS: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",58,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",0,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",58,axiModel.getRoot().getElements().size());
        
        //Transform from Salami Slice to Venetian Blind
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.VENITIAN_BLIND);
            long end = System.currentTimeMillis();
            print("Time taken to transform from SS to VB: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        
//		printDocument();
        assertEquals("global complex types",17,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",19,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",17,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",1,axiModel.getRoot().getElements().size());
        
        //Transform from Venetian Blind to Salami Slice
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.GARDEN_OF_EDEN);
            long end = System.currentTimeMillis();
            print("Time taken to transform from VB to GE: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",18,getSchemaModel().getSchema().getComplexTypes().size());
        //FIXME should be 19 instead of 38
//		assertEquals("global simple types",19,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",58,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",18,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",58,axiModel.getRoot().getElements().size());
        
        //Transform from Salami Slice to Venetian Blind
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.VENITIAN_BLIND);
            long end = System.currentTimeMillis();
            print("Time taken to transform from GE to VB: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",17,getSchemaModel().getSchema().getComplexTypes().size());
        //FIXME should be 19 instead of 72
//		assertEquals("global simple types",19,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",17,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",1,axiModel.getRoot().getElements().size());
    }
    
    public void testPurchaseOrder() {
        print("testPurchaseOrder");
        try {
            loadModel("resources/po.xsd");
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",7,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global attribute groups",2,getSchemaModel().getSchema().getAttributeGroups().size());
        assertEquals("global groups",2,getSchemaModel().getSchema().getGroups().size());
        assertEquals("global attributes",1,getSchemaModel().getSchema().getAttributes().size());
        assertEquals("global simple types",2,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",2,getSchemaModel().getSchema().getElements().size());
        
        //Transform from Garden of Eden to Russian Doll
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.RUSSIAN_DOLL);
            long end = System.currentTimeMillis();
            print("Time taken to transform from GE to RD: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
//		printDocument();
        
        //TODO - validation fails due to ambigous schema po.xsd
//		validateSchema(axiModel.getSchemaModel());
        
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",0,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",1,axiModel.getRoot().getElements().size());
        
        
        //Transform from Russian Doll to Garden Of Eden
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.GARDEN_OF_EDEN);
            long end = System.currentTimeMillis();
            print("Time taken to transform from RD to GE: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        
//		printDocument();
        assertEquals("global complex types",7,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",2,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",18,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",7,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",18,axiModel.getRoot().getElements().size());
        
        //Transform from Venetian Blind to Salami Slice
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.SALAMI_SLICE);
            long end = System.currentTimeMillis();
            print("Time taken to transform from GE to SS: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        //TODO - validation fails due to ambigous schema po.xsd
//		validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",18,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",0,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",18,axiModel.getRoot().getElements().size());
        
        //Transform from Salami Slice to Garden of Eden
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.GARDEN_OF_EDEN);
            long end = System.currentTimeMillis();
            print("Time taken to transform from SS to GE: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        //TODO - validation fails due to ambigous schema po.xsd
//		validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",7,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",2,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",18,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",7,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",18,axiModel.getRoot().getElements().size());
        
        //Transform from Venetian Blind to Venetian Blind
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.VENITIAN_BLIND);
            long end = System.currentTimeMillis();
            print("Time taken to transform from GE to VB: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        //TODO - validation fails due to ambigous schema po.xsd
//		validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",6,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",2,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",6,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",1,axiModel.getRoot().getElements().size());
        
        //Transform from Venetian Blind to Garden of Eden
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.GARDEN_OF_EDEN);
            long end = System.currentTimeMillis();
            print("Time taken to transform from VB to GE: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        //TODO - validation fails due to ambigous schema po.xsd
//		validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",7,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",2,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",18,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",7,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",18,axiModel.getRoot().getElements().size());
    }
    
    public void testTypes() {
        print("testLineGardenOfEden");
        try {
            loadModel(TYPES_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",3,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",7,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",3,getSchemaModel().getSchema().getElements().size());
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.RUSSIAN_DOLL);
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
    
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",2,getSchemaModel().getSchema().getElements().size());
    }
    
    public void testAnnotation() {
        print("testAnnotation");
        try {
            loadModel(ANNOTATION_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",1,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.RUSSIAN_DOLL);
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
    }
    
    public void testDiscardTransform() {
        print("testDiscardTransform");
        try {
            loadModel(NEWPO1_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertPurchaseOrder();
        
        //save document contents
        Document doc = AbstractDocumentModel.class.cast(getSchemaModel()).getBaseDocument();
        String savedDoc = null;
        try {
            savedDoc = doc.getText(0, doc.getLength());
        } catch (BadLocationException ex) {
//			ex.printStackTrace();
        }
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.RUSSIAN_DOLL);
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",0,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",1,axiModel.getRoot().getElements().size());
        
        assertTrue(savedDoc != null);
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, savedDoc, null);
//				axiModel.startTransaction();
            getSchemaModel().sync();
//				axiModel.endTransaction();
//			printDocument();
            axiModel.sync();
            String newDoc = doc.getText(0, doc.getLength());
            assertEquals("newDoc is not same as saveDoc", newDoc, savedDoc);
        } catch (BadLocationException ex) {
//				ex.printStackTrace();
            fail("failed");
        } catch (IOException ex) {
//				ex.printStackTrace();
            fail("failed");
        }
//		printDocument();
//		assertPurchaseOrder();
        
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.RUSSIAN_DOLL);
        } catch (IOException ex) {
            ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",0,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",1,axiModel.getRoot().getElements().size());
        
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.RUSSIAN_DOLL);
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.GARDEN_OF_EDEN);
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",3,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",2,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",6,getSchemaModel().getSchema().getElements().size());
    }
    
    private void assertPurchaseOrder() {
        assertEquals("global complex types",3,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",1,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",2,getSchemaModel().getSchema().getElements().size());
        
        Iterator itge = getSchemaModel().getSchema().getElements().iterator();
        GlobalElement po1 = (GlobalElement)itge.next();
        if(!po1.getName().equals("purchaseOrder"))
            po1 = (GlobalElement) itge.next();
        GlobalComplexType poType = (GlobalComplexType) po1.getType().get();
        ComplexTypeDefinition ctd = poType.getDefinition();
        ComplexExtension ce = (ComplexExtension) ctd.getChildren().get(0);
        GlobalComplexType poTypeBase = ((GlobalComplexType) ce.getBase().get());
        GlobalAttributeGroup ag = poTypeBase.getAttributeGroupReferences().
                iterator().next().getGroup().get();
        
        assertEquals("po's child compositor proxy",1, ce.getChildren(Choice.class).size());
        assertEquals("po's child compositor proxy",1, poTypeBase.getChildren(Sequence.class).size());
        assertEquals("po's attribute proxy",3, ag.getChildren(LocalAttribute.class).size());
        
        assertEquals("AXI content models",5,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",2,axiModel.getRoot().getElements().size());
        
        Element po = (Element)axiModel.getRoot().getElements().get(0);
        if(!po.getName().equals("purchaseOrder"))
            po = (Element)axiModel.getRoot().getElements().get(1);
        //since CompositorProxy extends Compositor, so use Compositor
        assertEquals("po's child compositor proxy",2, po.getChildren(Compositor.class).size());
        assertEquals("po's attribute proxy",3, po.getChildren(AttributeProxy.class).size());
        assertEquals("po's attribute",0, po.getChildren(AttributeImpl.class).size());
    }
    
    public void testRenameAfterTransform() {
        print("testRenameAfterTransform");
        try {
            loadModel(COURIER_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",1,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",1,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",1,axiModel.getRoot().getElements().size());
        
        //save document contents
        Document doc = AbstractDocumentModel.class.cast(getSchemaModel()).getBaseDocument();
        String savedDoc = null;
        try {
            savedDoc = doc.getText(0, doc.getLength());
        } catch (BadLocationException ex) {
//			ex.printStackTrace();
        }
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.RUSSIAN_DOLL);
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
        
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",0,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",1,axiModel.getRoot().getElements().size());
        
        Attribute first =
                (Attribute) axiModel.getRoot().getElements().get(0).
                getChildElements().get(0).getAttributes().get(0);
        axiModel.startTransaction();
        first.setName("first2");
        axiModel.endTransaction();
        
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.VENITIAN_BLIND);
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
        
        Attribute first2 =
                (Attribute) axiModel.getRoot().getContentModels().get(0).
                getAttributes().get(0);
        axiModel.startTransaction();
        first2.setName("first3");
        axiModel.endTransaction();
        
        Attribute first3 =
                (Attribute) axiModel.getRoot().getElements().get(0).
                getChildElements().get(0).getAttributes().get(0);
        assertEquals("attr after rename", "first3", first3.getName());
        
//		printDocument();
        assertEquals("global complex types",1,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",1,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",1,axiModel.getRoot().getElements().size());
    }
    
    public void testNewApproach() {
        print("testLineGardenOfEden");
        try {
            loadModel(NEWPO_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.RUSSIAN_DOLL);
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.GARDEN_OF_EDEN);
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        validateSchema(axiModel.getSchemaModel());
    
//		printDocument();
        assertEquals("global complex types",3,getSchemaModel().getSchema().getComplexTypes().size());
//        assertEquals("global simple types",2,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",13,getSchemaModel().getSchema().getElements().size());
    }
    
    public void testMultiSchemaInclude() {
        print("testMultiSchemaInclude");
        try {
            loadModel(INCLUDE_XSD);
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
//		printDocument();
        assertEquals("global complex types",1,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",1,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",1,axiModel.getRoot().getElements().size());
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.GARDEN_OF_EDEN);
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
//		printDocument();
        assertEquals("global complex types",1,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",3,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",1,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",3,axiModel.getRoot().getElements().size());
    }
    
    public void testMultiSchemaImport() {
        print("testMultiSchemaInclude");
        try {
            loadModel(IMPORT_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
//		printDocument();
        assertEquals("global complex types",1,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",1,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",1,axiModel.getRoot().getElements().size());
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.GARDEN_OF_EDEN);
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
//		printDocument();
        assertEquals("global complex types",1,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",3,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",1,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",3,axiModel.getRoot().getElements().size());
        
        try {
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.RUSSIAN_DOLL);
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",0,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",1,axiModel.getRoot().getElements().size());
    }
    
    public void testGlobalElementReuse() {
        print("testGlobalElementReuse");
        try {
            loadModel(ELEMENT_REUSE_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        //Transform from Garden of Eden to Russian Doll
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.GARDEN_OF_EDEN);
            long end = System.currentTimeMillis();
            print("Time taken to transform from GE to RD: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
//		printDocument();
        
        assertEquals("global complex types",4,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",5,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",4,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",5,axiModel.getRoot().getElements().size());
    }
    
    public void testGlobalElementReuse1() {
        print("testGlobalElementReuse1");
        try {
            loadModel(ELEMENT_REUSE1_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        //Transform from Garden of Eden to Russian Doll
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.GARDEN_OF_EDEN);
            long end = System.currentTimeMillis();
            print("Time taken to transform from GE to RD: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
//		printDocument();
        
        assertEquals("global complex types",3,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",4,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",3,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",4,axiModel.getRoot().getElements().size());
        
        Iterator it = getSchemaModel().getSchema().getElements().iterator();
        assertEquals("global elements","newElement",((GlobalElement)it.next()).getName());
        assertEquals("global elements","newElement1",((GlobalElement)it.next()).getName());
        assertEquals("global elements","newElement2",((GlobalElement)it.next()).getName());
        assertEquals("global elements","newElement3",((GlobalElement)it.next()).getName());
        
        Iterator it2 = getSchemaModel().getSchema().getComplexTypes().iterator();
        assertEquals("global elements","newElementType",((GlobalComplexType)it2.next()).getName());
        assertEquals("global elements","newElementType1",((GlobalComplexType)it2.next()).getName());
        assertEquals("global elements","newElementType2",((GlobalComplexType)it2.next()).getName());
    }
    
    public void testGlobalElementReuse2() {
        print("testGlobalElementReuse2");
        try {
            loadModel(ELEMENT_REUSE2_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",1,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        //Transform from Garden of Eden to Russian Doll
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.GARDEN_OF_EDEN);
            long end = System.currentTimeMillis();
            print("Time taken to transform from GE to RD: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
//		printDocument();
        
        assertEquals("global complex types",2,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",3,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",2,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",3,axiModel.getRoot().getElements().size());
    }
    
    public void testContentModelReuse() {
        print("testContentModelReuse");
        try {
            loadModel(TYPE_REUSE_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        assertEquals("global complex types",1,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        
        //Transform from Garden of Eden to Russian Doll
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.GARDEN_OF_EDEN);
            long end = System.currentTimeMillis();
            print("Time taken to transform from GE to RD: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
//		printDocument();
        
        assertEquals("global complex types",2,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",5,getSchemaModel().getSchema().getElements().size());
        
        assertEquals("AXI content models",2,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",5,axiModel.getRoot().getElements().size());
        
        Iterator it = getSchemaModel().getSchema().getElements().iterator();
        assertEquals("global elements","test",((GlobalElement)it.next()).getName());
        assertEquals("global elements","street",((GlobalElement)it.next()).getName());
        assertEquals("global elements","name",((GlobalElement)it.next()).getName());
        assertEquals("global elements","city",((GlobalElement)it.next()).getName());
        assertEquals("global elements","state",((GlobalElement)it.next()).getName());
        
        Iterator it2 = getSchemaModel().getSchema().getComplexTypes().iterator();
        assertEquals("global elements","testType",((GlobalComplexType)it2.next()).getName());
        assertEquals("global elements","nameType",((GlobalComplexType)it2.next()).getName());
    }
    
    private void printDocument() {
        try {
            SchemaModel sm = getSchemaModel();
            doc = ((AbstractDocumentModel)sm).getBaseDocument();
            System.out.println("doc: "+doc.getText(0, doc.getLength()));
        } catch (BadLocationException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
    }
    
}

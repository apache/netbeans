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
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;


/**
 *
 * @author Ayub Khan
 */
public class SchemaTransformPerfTest extends AbstractTestCase {
    
//	public static final String OTA_SIMPLE_XSD   = "resources/OTA_TravelItinerary.xsd";
    public static final String OTA_SIMPLE_XSD   = "resources/OTA_TI_simple.xsd";
    public static final String GLOBAL_ELEMENT   = "Line";
    
    private Document doc = null;
    
    public SchemaTransformPerfTest(String testName) {
        super(testName, OTA_SIMPLE_XSD, GLOBAL_ELEMENT);
    }
    
    public static Test suite() {
//        TestSuite suite = new TestSuite(DesignPatternTest.class);
        TestSuite suite = new TestSuite();
//		suite.addTest(new SchemaTransformPerfTest("testTransformPerf"));
        return suite;
    }
    
    public void testTransformPerf() {
        print("testTransformPerf");
        try {
            loadModel(OTA_SIMPLE_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        
        assertEquals("global complex types",3,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global attribute groups",2,getSchemaModel().getSchema().getAttributeGroups().size());
        assertEquals("global groups",0,getSchemaModel().getSchema().getGroups().size());
        assertEquals("global attributes",0,getSchemaModel().getSchema().getAttributes().size());
        assertEquals("global simple types",3,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",3,getSchemaModel().getSchema().getElements().size());
        
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
        
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",2,getSchemaModel().getSchema().getElements().size());
        
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
        
//		printDocument();
        assertEquals("global complex types",2,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",5,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",2,getSchemaModel().getSchema().getElements().size());
        
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
        
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",9,getSchemaModel().getSchema().getElements().size());
        
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
        assertEquals("global complex types",2,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",5,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",2,getSchemaModel().getSchema().getElements().size());
        
        
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
        
//		printDocument();
        assertEquals("global complex types",2,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",5,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",9,getSchemaModel().getSchema().getElements().size());
        
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
        
//		printDocument();
        assertEquals("global complex types",2,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",5,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",2,getSchemaModel().getSchema().getElements().size());
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

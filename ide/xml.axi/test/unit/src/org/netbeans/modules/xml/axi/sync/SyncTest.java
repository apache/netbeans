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

package org.netbeans.modules.xml.axi.sync;

import javax.swing.text.BadLocationException;
import junit.framework.*;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;

        
/**
 * The unit test covers various use cases of sync on Element
 * and ElementRef.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SyncTest extends AbstractSyncTestCase {
                
    public static final String MULTIROOT_XSD  = "resources/multiRoot.xsd";
    public static final String OTA_XSD  = "resources/OTA_TravelItinerary.xsd";
    public static final String HL7_XSD  = "resources/hl7/fields.xsd";
    
    /**
     * SyncElementTest
     */
    public SyncTest(String testName) {
        super(testName, OTA_XSD, null);
    }
        
    public static Test suite() {
        TestSuite suite = new TestSuite();
//        Disabled as referenced XSD file were partly not donated by oracle to apache
//        suite.addTest(new SyncTest("testSync"));
//        suite.addTest(new SyncTest("testOTASyncPerformance"));
//        //suite.addTest(new SyncPerfTest("testHealthcareSchemaSyncPerformance"));
        return suite;
    }

    //Issue http://www.netbeans.org/issues/show_bug.cgi?id=113775
    //multiple schema root should make the schema and axi model as not well formed.
    public void testSync() throws Exception {
        AXIModel aModel = getModel(MULTIROOT_XSD);
        SchemaModel sModel = aModel.getSchemaModel();
        assert(sModel.getSchema().getChildren().size() == 0);
        assert(aModel.getRoot().getChildElements().size() == 0);
        assert(aModel.getRoot().getContentModels().size() == 0);
        javax.swing.text.Document document = ((AbstractDocumentModel)sModel).getBaseDocument();
        String newSchemaContent = "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n"+
                                    "xmlns:po=\"http://www.example.com/PO2\"\n"+
                                    "targetNamespace=\"http://www.example.com/PO2\">\n"+
                                    "<xsd:element name=\"AA\"/>\n"+
                                    "<xsd:element name=\"BB\"/>\n"+
                                    "</xsd:schema>";
        int length = document.getLength();
        document.insertString(document.getLength(), newSchemaContent, null); //NOI18N
        try {
            sModel.sync();
        } catch (Exception ex) {
            //just catch
        }
        try {
            aModel.sync();
        } catch (Exception ex) {
            //just catch
        }
        assert(aModel.getState() == State.NOT_WELL_FORMED);
        assert(sModel.getState() == State.NOT_WELL_FORMED);
        document.remove(39, length-39);
        try {
            sModel.sync();
        } catch (Exception ex) {
            //just catch
        }
        try {
            aModel.sync();
        } catch (Exception ex) {
            //just catch
        }
        assert(sModel.getState() == State.VALID);
        assert(aModel.getState() == State.VALID);
        assert(sModel.getSchema().getChildren().size() == 2);
        assert(aModel.getRoot().getChildElements().size() == 2);
        assert(aModel.getRoot().getContentModels().size() == 0);
    }
    
    public void testHealthcareSchemaSyncPerformance() throws Exception {
        AXIModel aModel = getModel(HL7_XSD);
        doRun(aModel.getSchemaModel(), aModel, false);
        doRun(aModel.getSchemaModel(), aModel, true);
    }
    
    public void testOTASyncPerformance() throws Exception {
        AXIModel aModel = getModel(OTA_XSD);
        doRun(aModel.getSchemaModel(), aModel, false);
        doRun(aModel.getSchemaModel(), aModel, true);
    }
    
    private void doRun(SchemaModel sModel, AXIModel aModel, boolean worstCase) {
        if(worstCase) {
            DeepAXITreeVisitor visitor = new DeepAXITreeVisitor();
            visitor.visit(aModel.getRoot());
        }
        int schemaChildCount = sModel.getSchema().getChildren().size();
        int axiChildCount = aModel.getRoot().getChildren().size();
        try {
            getSchemaModel().startTransaction();
            GlobalElement ge = getSchemaModel().getFactory().createGlobalElement();
            ge.setName("NewGlobalElement");
            getSchemaModel().getSchema().addElement(ge);
            getSchemaModel().endTransaction();
            long startTime = System.currentTimeMillis();
            getAXIModel().sync();
            long endTime = System.currentTimeMillis();
            print("Time taken to sync: " +
                    (endTime - startTime));
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        assert(schemaChildCount+1 == sModel.getSchema().getChildren().size());
        assert(axiChildCount+1 == aModel.getRoot().getChildren().size());
    }
    
    private void updateDocument(javax.swing.text.Document document, String stringToInsert)
            throws BadLocationException {
        javax.swing.text.AbstractDocument doc = (javax.swing.text.AbstractDocument)document;
        String content = doc.getText(0,doc.getLength());
        int offset = content.indexOf("elementFormDefault"); //NOI18N
        //String tnsString = "targetNamespace=\"" + tns + "\"\n";
        document.insertString(offset, stringToInsert, null); //NOI18N        
        
        
        String newSchemaContent = "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n"+
                                    "xmlns:po=\"http://www.example.com/PO2\"\n"+
                                    "targetNamespace=\"http://www.example.com/PO2\">\n"+
                                    "<xsd:element name=\"AA\"/>\n"+
                                    "<xsd:element name=\"BB\"/>\n"+
                                    "</xsd:schema>";
        

    }
}

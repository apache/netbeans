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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.openide.util.WeakListeners;

        
/**
 * This unit test verifies various state change of the AXIModel.
 * It starts with a broken schema. Fixes it to change the state
 * from NOT_WELL_FORMED to VALID. Then it again makes the schema
 * invalid and the state changes from VALID to NOT_WELL_FORMED.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class StateChangeTest extends AbstractSyncTestCase {
                
    public static final String TEST_XSD  = "resources/brokenSchema.xsd";
    private Document document;
    private AXIModel.State expectedState = null;
    private boolean invalidToValid = false;
    private boolean validToInvalid = false;
    
    public StateChangeTest(String testName) {
        super(testName, TEST_XSD, null);
    }
    
    /**
     * Overwrites setUp.
     */
    protected void setUp() throws Exception {
        super.setUp();
        SchemaModel sm = getSchemaModel();
        document = ((AbstractDocumentModel)sm).getBaseDocument();
        AXIModel axiModel = getAXIModel();
        if (axiModel != null) {
            ModelStateChangeListener listener = new ModelStateChangeListener(axiModel);
            axiModel.addPropertyChangeListener(listener);
        }
    }
        
    public static Test suite() {
        TestSuite suite = new TestSuite();
//        Disabled as referenced XSD file were partly not donated by oracle to apache
//        suite.addTestSuite(StateChangeTest.class);
        return suite;
    }

    public void testStateChange() throws InterruptedException {
        AXIModel model = getAXIModel();
        //make the schema valid from invalid
        assert(model.getState() == AXIModel.State.NOT_WELL_FORMED);
        String replaceFrom = "<xsd:element name=\"address\" type=\"addr:USAddr";
        String replaceTo = "<xsd:element name=\"address\" type=\"addr:USAddress\"/>";
        replaceInDocument(replaceFrom, replaceTo);
        expectedState = AXIModel.State.VALID;
        Thread.sleep(10000);
        
        //make the schema invalid from valid
        replaceInDocument(replaceTo, replaceFrom);
        expectedState = AXIModel.State.NOT_WELL_FORMED;
        Thread.sleep(10000);
        
        //finally make sure both the tests have been carried out
        assert(invalidToValid);
        assert(validToInvalid);
    }
    
    private void replaceInDocument(String replaceFrom, String replaceTo) {
        javax.swing.text.AbstractDocument doc = (javax.swing.text.AbstractDocument)document;
        int len = replaceFrom.length();
        try {
            String content = doc.getText(0,doc.getLength());
            int index = content.lastIndexOf(replaceFrom);
            while (index>=0) {
                doc.replace(index,len,replaceTo,null);
                content=content.substring(0,index);
                index = content.lastIndexOf(replaceFrom);
            }
        } catch (javax.swing.text.BadLocationException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Model state change listener.
     */
    private class ModelStateChangeListener implements PropertyChangeListener {        
        private AXIModel model;
        ModelStateChangeListener(AXIModel model) {
            this.model = model;
        }
        public void propertyChange(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();
            if(!AXIModel.STATE_PROPERTY.equals(property)) {
                return;
            }
            
            State newState = (State)evt.getNewValue();
            State oldState = (State)evt.getOldValue();
            Object source = evt.getSource();
            assert(source == model);
            if(newState == AXIModel.State.VALID) {
                assert(oldState == AXIModel.State.NOT_WELL_FORMED);
                assert(expectedState == newState);
                AXIDocument doc = model.getRoot();
                assert(doc.getChildren().size() == 7);
                assert(doc.getContentModels().size() == 3);
                assert(doc.getChildElements().size() == 3);
                invalidToValid = true;
                return;
            }
            
            if(newState != AXIModel.State.VALID) {
                assert(oldState == AXIModel.State.VALID);
                assert(expectedState == newState);
                validToInvalid = true;
                return;
            }            
        }        
    }    
}

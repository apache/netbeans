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
package org.netbeans.modules.java.j2seproject.api;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.text.Document;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.project.Project;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Test of SPI class org.netbeans.modules.java.j2seproject.api.J2SECustomPropertySaver
 * 
 * @author Petr Somol
 */
public class J2SECustomPropertySaverTest {
    
    public J2SECustomPropertySaverTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        MockServices.setServices(MockProjectExtenderPropertiesSaver.class);
    }

    private static MockProjectExtenderProperties extProperties = MockProjectExtenderProperties.getInstance();    
    
    /**
     * Simulates property storage.
     */
    private static EditableProperties storedProperties = new EditableProperties(true);
    
    /**
     * Simulates reaction to clicking OK in Project Properties dialog.
     * Both standard and extended properties thus need to be saved.
     */
    public void invokeCentralizedSave() {
        // first save standard project properties
        System.out.println("invokeCentralizedSave():");
        // then invoke all registered CustomPropertySavers to save all extended properties
        Project thisProjectMockup = null;
        for (J2SECustomPropertySaver saver : Lookup.getDefault().lookupAll(J2SECustomPropertySaver.class)) {
            saver.save(thisProjectMockup);
        }
    }
    
    /**
     * Test of save method, of class J2SECustomPropertySaver.
     */
    @Test
    public void testCentralizedPropertiesSave() {
        MockupCategory1Panel panel1 = new MockupCategory1Panel();
        MockupCategory2Panel panel2 = new MockupCategory2Panel();
        System.out.println("testCentralizedPropertiesSave():");
        assertEquals(storedProperties.size(), 0);
        invokeCentralizedSave();
        assertEquals(storedProperties.size(), 2); // empty document is not saved
        assertEquals(storedProperties.get("mock.property1"), "false"); 
        assertEquals(storedProperties.get("mock.property2"), "false"); 
        assertNull(storedProperties.get("mock.property3")); 
        System.out.println("OK"); 
        // simulate user invoked property change in first Category panel followed by click on OK button
        panel1.simulateUserButtonAction();
        invokeCentralizedSave();
        assertEquals(storedProperties.size(), 2); // empty document is not saved
        assertEquals(storedProperties.get("mock.property1"), "true"); 
        assertEquals(storedProperties.get("mock.property2"), "false"); 
        assertNull(storedProperties.get("mock.property3")); 
        System.out.println("OK"); 
        // simulate user invoked property change in second Category panel followed by click on OK button
        panel2.simulateUserButtonAction();
        invokeCentralizedSave();
        assertEquals(storedProperties.size(), 2); // empty document is not saved
        assertEquals(storedProperties.get("mock.property1"), "true"); 
        assertEquals(storedProperties.get("mock.property2"), "true"); 
        assertNull(storedProperties.get("mock.property3")); 
        System.out.println("OK"); 
        // simulate user invoked property change in first and second Category panel followed by click on OK button
        panel1.simulateUserButtonAction();
        panel2.simulateUserTextFieldAction("USER WROTE THIS"); 
        invokeCentralizedSave();
        assertEquals(storedProperties.size(), 3);
        assertEquals(storedProperties.get("mock.property1"), "false"); 
        assertEquals(storedProperties.get("mock.property2"), "true"); 
        assertEquals(storedProperties.get("mock.property3"), "USER WROTE THIS"); 
        System.out.println("OK"); 
    }

    /**
     * Extended properties centralized saver service.
     */
    public static final class MockProjectExtenderPropertiesSaver implements J2SECustomPropertySaver {

        @Override
        public void save(Project p) {
            // in real p would be used to lookup the correct ProjectExtenderProperties instance
            MockProjectExtenderProperties prop = MockProjectExtenderProperties.getInstanceIfExists();
            if(prop != null) {
                try {
                    prop.store();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    /**
     * Class managing properties specific to the project extension module.
     */
    public static final class MockProjectExtenderProperties {

        private static MockProjectExtenderProperties extProperties = null;

        public static final String PROPERTY1 = "mock.property1"; 
        public static final String PROPERTY2 = "mock.property2"; 
        public static final String PROPERTY3 = "mock.property3"; 
        private JToggleButton.ToggleButtonModel model1;
        private JToggleButton.ToggleButtonModel model2;
        private Document model3;
        private StoreGroup mockPropGroup = new StoreGroup();

        public JToggleButton.ToggleButtonModel getModel1() {
            return model1;
        }
        public JToggleButton.ToggleButtonModel getModel2() {
            return model2;
        }
        public Document getModel3() {
            return model3;
        }

        MockProjectExtenderProperties() {
            EditableProperties mockCentralProperties = new EditableProperties(true);
            MockPropertyEvaluator pe = new MockPropertyEvaluator(mockCentralProperties);
            model1 = mockPropGroup.createToggleButtonModel(pe, PROPERTY1);
            model2 = mockPropGroup.createToggleButtonModel(pe, PROPERTY2);
            model3 = mockPropGroup.createStringDocument(pe, PROPERTY3);
        }
        
        public static MockProjectExtenderProperties getInstanceIfExists() {
            if(extProperties != null) {
                return extProperties;
            }
            return null;
        }
        
        public static MockProjectExtenderProperties getInstance() {
            if(extProperties == null) {
                extProperties = new MockProjectExtenderProperties();
            }
            return extProperties;
        }

        /**
         * Method implementing actual saving of extended properties.
         */
        public void store() throws IOException {
            try {
                mockPropGroup.store(storedProperties);
                // in reality cache or save to *.properties file
                if(false) throw new IOException();
                
            } catch (IOException ioex) {}
        }   
    }
    
    public static final class MockupCategory1Panel extends JPanel {
        JToggleButton button = new JToggleButton();
        MockupCategory1Panel() {
            button.setModel(extProperties.getModel1());
        }
        public void simulateUserButtonAction() {
            button.doClick();
        }
    }

    public static final class MockupCategory2Panel extends JPanel {
        JToggleButton button = new JToggleButton();
        JTextField textField  = new JTextField();
        MockupCategory2Panel() {
            button.setModel(extProperties.getModel2());
            textField.setDocument(extProperties.getModel3());
        }
        public void simulateUserButtonAction() {
            button.doClick();
        }
        public void simulateUserTextFieldAction(String str) {
            textField.setText(str);
        }
    }

    public static final class MockPropertyEvaluator implements PropertyEvaluator {

        private final EditableProperties ep;
                
        MockPropertyEvaluator(EditableProperties ep) {
            this.ep = ep;
        }
        
        @Override
        public String getProperty(String prop) {
            return ep.getProperty(prop);
        }

        @Override
        public String evaluate(String text) {
            return null;
        }

        @Override
        public Map<String, String> getProperties() {
            return null;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
        
    }
    
}

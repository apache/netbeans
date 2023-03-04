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

package org.netbeans.spi.project.support.ant.ui;

import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.text.Document;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

public class StoreGroupTest extends NbTestCase {

    public StoreGroupTest(String name) {
        super(name);
    }

    public void testString() throws Exception {

        // Test values
        String PROP_NAME = "testText";
        String ORIGINAL_VALUE = "originalValue";
        String NEW_VALUE = "newValue";
        
        // Needed objects
        EditableProperties ep = new EditableProperties(false);
        PropertyEvaluator evaluator = new PlainPropertyEvaluator( ep );
        StoreGroup sg = new StoreGroup();

        // Test correct value of the model
        ep.setProperty( PROP_NAME, ORIGINAL_VALUE );
        Document doc = sg.createStringDocument( evaluator, PROP_NAME );
        JTextField jtf = new JTextField();
        jtf.setDocument( doc );        
        assertEquals( "JTextField has to have correct value", ORIGINAL_VALUE, jtf.getText() );
        
        // Test value is stored
        jtf.setText( NEW_VALUE );        
        sg.store( ep );        
        assertEquals( "Value has to be set into the properties", NEW_VALUE, ep.getProperty( PROP_NAME ) );
        
    }
    
    public void testBooleans() throws Exception {
        
        // Test values
        
        String[][] TEST_PROPERTIES = new String[][] {
            new String[] { "propTrue", "true", "false", "true" },
            new String[] { "propFalse", "false", "true", "false" },
            new String[] { "propYes", "yes", "no", "true" },
            new String[] { "propNo", "no", "yes", "false"},
            new String[] { "propOn", "on", "off", "true" },
            new String[] { "propOff", "off", "on", "false" },
        };
        
        JToggleButton.ToggleButtonModel[] models = new JToggleButton.ToggleButtonModel[ TEST_PROPERTIES.length ];
        JToggleButton.ToggleButtonModel[] inverseModels = new JToggleButton.ToggleButtonModel[ TEST_PROPERTIES.length ];        
        JToggleButton buttons[] = new JToggleButton[ TEST_PROPERTIES.length ];
        JToggleButton inverseButtons[] = new JToggleButton[ TEST_PROPERTIES.length ];
        
        // Needed objects
        EditableProperties ep = new EditableProperties(false);
        PropertyEvaluator evaluator = new PlainPropertyEvaluator( ep );
        StoreGroup sg = new StoreGroup();
        StoreGroup inverseSg = new StoreGroup();
        
        // Test correct value of the model
        for( int i = 0; i < TEST_PROPERTIES.length; i++ ) {
            ep.setProperty( TEST_PROPERTIES[i][0], TEST_PROPERTIES[i][1] );
        }        
        for( int i = 0; i < TEST_PROPERTIES.length; i++ ) {
            models[i] = sg.createToggleButtonModel( evaluator, TEST_PROPERTIES[i][0] );
            inverseModels[i] = inverseSg.createInverseToggleButtonModel( evaluator, TEST_PROPERTIES[i][0] );
            buttons[i] = new JToggleButton();
            buttons[i].setModel( models[i] );
            inverseButtons[i] = new JToggleButton();
            inverseButtons[i].setModel( inverseModels[i] );
        }
        for( int i = 0; i < TEST_PROPERTIES.length; i++ ) {
            assertEquals( "Button [" + i + "] has to have correct value.", Boolean.valueOf( TEST_PROPERTIES[i][3] ).booleanValue(), buttons[i].isSelected() );
            assertEquals( "InverseButton [" + i + "] has to have correct value.", !Boolean.valueOf( TEST_PROPERTIES[i][3] ).booleanValue(), inverseButtons[i].isSelected() );
        }
        
        // Change value of all the buttons and test the correct property values
        for( int i = 0; i < TEST_PROPERTIES.length; i++ ) {
            buttons[i].setSelected( !buttons[i].isSelected() );
            inverseButtons[i].setSelected( !inverseButtons[i].isSelected() );            
        }
        
        EditableProperties inverseEp = new EditableProperties(false);
        sg.store( ep );
        inverseSg.store( inverseEp );
        for( int i = 0; i < TEST_PROPERTIES.length; i++ ) {
            assertEquals( "Property [" + i + "] has to have correct value.", TEST_PROPERTIES[i][2], ep.getProperty( TEST_PROPERTIES[i][0] ) );
            assertEquals( "Property [" + i + "] has to have correct value.", TEST_PROPERTIES[i][2], inverseEp.getProperty( TEST_PROPERTIES[i][0] ) );
        }        
        
    }
    
    public void testDuplicateModels() throws Exception {
        
        // Test values
        String PROP_BOOLEAN = "boolean";
        String PROP_STRING = "string";
        
        // Needed objects
        EditableProperties ep = new EditableProperties(false);
        PropertyEvaluator evaluator = new PlainPropertyEvaluator( ep );
        StoreGroup sg = new StoreGroup();

        ep.setProperty( PROP_BOOLEAN, "true" );
        ep.setProperty( PROP_STRING, "text" );
        
        sg.createToggleButtonModel( evaluator, PROP_BOOLEAN );
        sg.createStringDocument( evaluator, PROP_STRING );
        
        IllegalArgumentException e = null;
        
        try {
            sg.createToggleButtonModel( evaluator, PROP_BOOLEAN );        
        }
        catch ( IllegalArgumentException iea ) {
            e = iea;
        }
        assertNotNull( "Exception has to be throen", e);
        
        e = null;
        try {
            sg.createStringDocument( evaluator, PROP_STRING );        
        }
        catch ( IllegalArgumentException iea ) {
            e = iea;
        }
        assertNotNull( "Exception has to be throen", e );
        
        
    }
    
    /**
     *#57797:dist.jar changed to hardcode 'dist' rather than '${dist.dir}'
     */
    public void testIssue57797 () throws Exception {
        String PROP_NAME_A = "propertyA";
        String PROP_NAME_B = "propertyB";
        String ORIGINAL_A_VALUE = "original_A_Value";
        String ORIGINAL_B_VALUE = "original_B_Value";
        String NEW_A_VALUE = "new_A_Value";
        
        EditableProperties ep = new EditableProperties(false);
        PropertyEvaluator evaluator = new PlainPropertyEvaluator( ep );
        StoreGroup sg = new StoreGroup();

        ep.setProperty( PROP_NAME_A, ORIGINAL_A_VALUE );
        ep.setProperty( PROP_NAME_B, ORIGINAL_B_VALUE );
        Document doc1 = sg.createStringDocument( evaluator, PROP_NAME_A );
        Document doc2 = sg.createStringDocument( evaluator, PROP_NAME_B );
        JTextField jtf1 = new JTextField ();        
        jtf1.setDocument ( doc1 );        
        JTextField jtf2 = new JTextField ();
        jtf2.setDocument ( doc2 );               
        jtf1.setText( NEW_A_VALUE );     
        EditableProperties newEp = new EditableProperties(false);
        sg.store( newEp );        
        assertEquals( "Expected one new propery", 1, newEp.size());
        assertEquals( "Value has to be set into the properties", NEW_A_VALUE, newEp.getProperty( PROP_NAME_A ) );
    }
    
    
    // Innerclasses ------------------------------------------------------------

    
    private static class PlainPropertyEvaluator implements PropertyEvaluator {
        
        private EditableProperties properties;
        
        PlainPropertyEvaluator( EditableProperties properties ) {            
            this.properties = properties;            
        }
        
        
        public String getProperty(String prop) {            
            return properties.getProperty( prop );            
        }

        public String evaluate(String text) {
            return text;
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            // NOP
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            // NOP
        }

        public Map<String,String> getProperties() {
            return properties;
        }
        
    }
    
    
}

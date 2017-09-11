/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

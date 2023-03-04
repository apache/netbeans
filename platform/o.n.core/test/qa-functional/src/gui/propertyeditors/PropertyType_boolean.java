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

package gui.propertyeditors;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests of boolean Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_boolean extends PropertyEditorsTest {

    public String propertyName_L;
    public String propertyValue_L;

    /** Creates a new instance of PropertyType_boolean */
    public PropertyType_boolean(String testName) {
        super(testName);
    }


    public void setUp(){
        propertyName_L = "boolean";
        super.setUp();
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_boolean("testByComboFalse"));
        suite.addTest(new PropertyType_boolean("testByComboTrue"));
        return suite;
    }
    
    public void testByComboFalse(){
        propertyValue_L = Boolean.FALSE.toString();
        setByCombo(propertyName_L, propertyValue_L, true);
    }
    
    public void testByComboTrue(){
        propertyValue_L = Boolean.TRUE.toString();
        setByCombo(propertyName_L, propertyValue_L, true);
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValue_L, propertyValue_L, false);
    }
    
    public void setCustomizerValue(){}
    public void verifyCustomizerLayout(){}
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_boolean.class));
        junit.textui.TestRunner.run(suite());
    }
}

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

package org.netbeans.qa.form.beans;

import junit.framework.Test;
import org.netbeans.jellytools.actions.CompileJavaAction;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.junit.NbModuleSuite;

/**
 * Tests creating Bean Forms from visual and non-visual JavaBeans superclasses
 * and tests value and access rights of inherited properties
 *
 * @author Jiri Vagner
 * 
 * <b>Adam Senk</b>
 * 20 April 2011 WORKS
 */
public class AddBeanFormsTest extends AddAndRemoveBeansTest {
    
    /**
     * Constructor required by JUnit
     * @param testName
     */
    public AddBeanFormsTest(String testName) {
        super(testName);
        //this.DELETE_FILES = false;
    }
   
    /**
     * Creates suite from particular test cases.
     * @return nb test suite
     */
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(AddBeanFormsTest.class)
                .addTest("testCompileBeanClasses", "testAddingBeanFormWithVisualBeanSuperclass", "testAddingBeanFormWithNonVisualBeanSuperclass")
                .clusters(".*").enableModules(".*").gui(true));
        
    }

    /** Compiling beans components */
    public void testCompileBeanClasses() {
        Node beanNode = openFile(VISUAL_BEAN_NAME);
        CompileJavaAction action = new CompileJavaAction();
        action.perform(beanNode);

        beanNode = openFile(NONVISUAL_BEAN_NAME);
        action = new CompileJavaAction();
        action.perform(beanNode);
   }
    
    /** Test adding Bean Form with visual bean superclass */
    public void testAddingBeanFormWithVisualBeanSuperclass() {
        String name = createBeanFormFile(VISUAL_BEAN_NAME);

        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.selectComponent(VISUAL_BEAN_NAME);
        Property prop = new Property(inspector.properties(), "text"); // NOI18N
        assertEquals("Text property of component " + name + " was not set correctly.",
            prop.getValue(), TESTED_BEAN_TEXT); // NOI18N

        removeFile(name);
    }

    /** Test adding Bean Form with non-visual bean superclass */
    public void testAddingBeanFormWithNonVisualBeanSuperclass() {
        String name = createBeanFormFile(NONVISUAL_BEAN_NAME);


        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.selectComponent(NONVISUAL_BEAN_NAME);
        
        Property prop = new Property(inspector.properties(), "power"); // NOI18N
        assertEquals("Text property of component " + name + " was not set correctly.",
            prop.getValue(), this.TESTED_BEAN_POWER); // NOI18N
        assertEquals("Property of component " + name + " is read-only.",
            prop.isEnabled(), true); // NOI18N

        prop = new Property(inspector.properties(), "carName"); // NOI18N
        assertEquals("Text property of component " + name + " was not set correctly.",
            prop.getValue(), TESTED_BEAN_TEXT); // NOI18N
        assertEquals("Property of component " + name + " is not read-only.",
            prop.isEnabled(), false); // NOI18N

        removeFile(name);
    }
}

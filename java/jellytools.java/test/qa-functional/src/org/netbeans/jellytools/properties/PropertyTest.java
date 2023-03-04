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
package org.netbeans.jellytools.properties;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of org.netbeans.jellytools.properties.Property.
 *
 * @author Jiri Skrivanek
 */
public class PropertyTest extends JellyTestCase {

    public static final String[] tests = new String[]{
        "testGetName",
        "testGetValue",
        "testGetShortDescription",
        "testOpenEditor",
        "testSetDefaultValue",
        "testGetRendererName",
        "testCanEditAsText",
        "testIsEnabled",
        "testClose",
        "testSetValue"
    };

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static NbTestSuite suite() {
        return (NbTestSuite) createModuleTest(PropertyTest.class, tests);
    }
    private static Property property;
    private static Property propertyAllFiles;
    private static PropertySheetOperator pso;
    //"Name" property
    private static final String SAMPLE_PROPERTY_NAME = Bundle.getString(
            "org.openide.loaders.Bundle",
            "PROP_name");
    //  "All Files"
    private static final String ALL_FILES_LABEL = Bundle.getString("org.openide.loaders.Bundle",
            "PROP_files");
    private static final String SAMPLE_NODE_NAME = "SampleClass1.java";

    /** Open property sheet and find sample property. */
    @Override
    protected void setUp() throws Exception {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
        if (property == null) {
            // opens properties window
            Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
            Node sampleClass1 = new Node(sample1, SAMPLE_NODE_NAME);
            new PropertiesAction().performAPI(sampleClass1);
            pso = new PropertySheetOperator(PropertySheetOperator.MODE_PROPERTIES_OF_ONE_OBJECT,
                    SAMPLE_NODE_NAME);
            property = new Property(pso, SAMPLE_PROPERTY_NAME);
            propertyAllFiles = new Property(pso, ALL_FILES_LABEL);
        }
    }

    /** Clean up after each test case. */
    @Override
    protected void tearDown() {
    }

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public PropertyTest(String testName) {
        super(testName);
    }

    /** Test of getName method */
    public void testGetName() {
        assertEquals("Wrong property name.", SAMPLE_PROPERTY_NAME, property.getName());
    }

    /** Test of getValue method */
    public void testGetValue() {
        assertEquals("Wrong property value.", SAMPLE_NODE_NAME.replaceFirst("\\.java", ""), property.getValue());
    }

    /** Test of getShortDescription method */
    public void testGetShortDescription() {
        String desc = Bundle.getString("org.openide.loaders.Bundle", "HINT_name");
        assertEquals("Wrong property value.", desc, property.getShortDescription());
    }

    /** Test of openEditor method */
    public void testOpenEditor() {
        propertyAllFiles.openEditor();
        new JDialogOperator(ALL_FILES_LABEL).requestClose();
    }

    /** Test of supportsCustomEditor method */
    public void testSupportsCustomEditor() {
        assertTrue("Wrong value from supportCustomEditor", property.supportsCustomEditor());
    }

    /** Test of setDefaultValue method */
    public void testSetDefaultValue() {
        // is is still needed? In UI there is no handle for it.
        property.setDefaultValue();
    }

    /** Test of getRendererName method */
    public void testGetRendererName() {
        assertEquals("Renderer", Property.STRING_RENDERER, property.getRendererName());
        Node folderNode = new Node(FilesTabOperator.invoke().getProjectNode("SampleProject"), "src"); //NOI18N
        new PropertiesAction().performAPI(folderNode);
        PropertySheetOperator pso1 = new PropertySheetOperator(
                PropertySheetOperator.MODE_PROPERTIES_OF_ONE_OBJECT,
                "src"); // NOI18N
        // "Sort Mode"
        String sortModeLabel = Bundle.getString("org.openide.loaders.Bundle", "PROP_sort");
        Property p1 = new Property(pso1, sortModeLabel);
        assertEquals("Renderer", Property.COMBOBOX_RENDERER, p1.getRendererName());
        pso1.close();
    }

    /** Test of canEditAsText method */
    public void testCanEditAsText() {
        assertTrue("Property Encoding can be edited as text.", property.canEditAsText());
        assertFalse("Property All Files cannot be edited as text.", propertyAllFiles.canEditAsText());
    }

    /** Test of isEnabled method */
    public void testIsEnabled() {
        assertTrue("Property Encoding should be enabled.", property.isEnabled()); // NOI18N
        assertFalse("Property All Files should be disabled.", propertyAllFiles.isEnabled());
    }

    /** Close tested property sheet. */
    public void testClose() {
        pso.close();
    }

    /** Test of setValue method */
    //TODO write a new setValue test
    public void testSetValue() {
        TestNode testNode = new TestNode();
        testNode.showProperties();
        PropertySheetOperator psoTestNode = new PropertySheetOperator(TestNode.NODE_NAME);
        try {
            // test boolean property

            // find "boolean" property
            Property booleanProperty = new Property(psoTestNode, "boolean");
            String oldValue = booleanProperty.getValue();
            assertEquals("Wrong initial value.", true, Boolean.valueOf(oldValue).booleanValue());
            // set to false
            booleanProperty.setValue(1);
            assertTrue("Setting value by index failed.", booleanProperty.getValue().equalsIgnoreCase("false"));  //NOI18N
            // set to false
            booleanProperty.setValue("false");   // NOI18N
            assertTrue("Setting boolean value by string value failed.", booleanProperty.getValue().equalsIgnoreCase("false"));  //NOI18N

            // test text property

            Property portProperty = new Property(psoTestNode, "String");
            String expected = "test value"; // NOI18N
            portProperty.setValue(expected);
            assertEquals("Wrong property value was set.", expected, portProperty.getValue());
        } finally {
            psoTestNode.close();
        }
    }
}

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

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JLabelOperator;

/**
 * Test of org.netbeans.jellytools.properties.PropertySheetOperator.
 *
 * @author Jiri Skrivanek
 */
public class PropertySheetOperatorTest extends JellyTestCase {

    private static String SAMPLE_NODE_NAME = "SampleClass1.java";
    private static PropertySheetOperator pso;
    static final String[] tests = {
        "testInvoke",
        "testTblSheet",
        "testGetDescriptionHeader",
        "testGetDescription",
        "testSortByName",
        "testSortByCategory",
        "testShowDescriptionArea",
        "testVerify",
        // has to be the last
        "testClose"
    };

    public static Test suite() {
        return createModuleTest(PropertySheetOperatorTest.class, tests);
    }

    /** Open sample property sheet and create PropertySheetOperator */
    @Override
    protected void setUp() throws Exception {
        System.out.println("### " + getName() + " ###");  // NOI18N
        openDataProjects("SampleProject");
        if (pso == null && !getName().equals("testInvoke")) {    // NOI18N
            // opens properties window
            Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
            Node sampleClass1 = new Node(sample1, SAMPLE_NODE_NAME);
            new PropertiesAction().performAPI(sampleClass1);
            pso = new PropertySheetOperator(PropertySheetOperator.MODE_PROPERTIES_OF_ONE_OBJECT,
                    SAMPLE_NODE_NAME);
        }
    }

    /** Clean up after each test case. */
    @Override
    protected void tearDown() {
    }

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public PropertySheetOperatorTest(java.lang.String testName) {
        super(testName);
    }

    /** Test of invoke method. */
    public void testInvoke() {
        Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
        new Node(sample1, SAMPLE_NODE_NAME).select();
        PropertySheetOperator.invoke();
        new PropertySheetOperator(PropertySheetOperator.MODE_PROPERTIES_OF_ONE_OBJECT,
                SAMPLE_NODE_NAME).close();
    }

    /** Test of tblSheet method */
    public void testTblSheet() {
        pso.tblSheet();
    }

    /** Test of btHelp method */
    public void testBtHelp() {
        pso.btHelp();
    }

    /** Test of getDescriptionHeader method */
    public void testGetDescriptionHeader() {
        pso.tblSheet().selectCell(0, 0);
        String expected = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Properties");
        assertEquals("Wrong description header was found.", expected, pso.getDescriptionHeader());
    }

    /** Test of getDescription method */
    public void testGetDescription() {
        pso.tblSheet().selectCell(0, 0);
        String expected = Bundle.getString("org.openide.nodes.Bundle", "HINT_Properties");
        assertTrue("Wrong description was found. Should be '" + expected + "' but was '" + pso.getDescription() + "'.",
                pso.getDescription().indexOf(expected) > 0);
    }

    /** Test of sortByName method */
    public void testSortByName() {
        int oldCount = pso.tblSheet().getRowCount();
        pso.sortByName();
        assertTrue("Sort by name failed.", oldCount > pso.tblSheet().getRowCount());
    }

    /** Test of sortByCategory method */
    public void testSortByCategory() {
        int oldCount = pso.tblSheet().getRowCount();
        pso.sortByCategory();
        assertTrue("Sort by category failed.", oldCount < pso.tblSheet().getRowCount());
    }

    /** Test of showDescriptionArea method */
    public void testShowDescriptionArea() {
        pso.showDescriptionArea();
        // try to find description header label
        Object label = pso.findSubComponent(new JLabelOperator.JLabelFinder());
        assertNull("Description area was not hidden.", label);
        pso.showDescriptionArea();
        // try to find description header label
        label = pso.findSubComponent(new JLabelOperator.JLabelFinder());
        assertNotNull("Description area was not shown.", label);
    }

    /** Test of verify method */
    public void testVerify() {
        pso.verify();
    }

    /** Close property sheet. */
    public void testClose() {
        pso.close();
    }
}

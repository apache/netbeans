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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

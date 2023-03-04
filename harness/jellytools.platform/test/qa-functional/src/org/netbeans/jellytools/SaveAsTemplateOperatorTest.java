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
package org.netbeans.jellytools;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;

/** Test of org.netbeans.jellytools.SaveAsTemplateOperator.
 */
public class SaveAsTemplateOperatorTest extends JellyTestCase {

    public static final String[] tests = new String[]{"testInvoke",
        "testTree",
        "testLblSelectTheCategory",
        "testGetRootNode",
        "testSelectTemplate"};

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        return createModuleTest(SaveAsTemplateOperatorTest.class, tests);
    }

    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
    }

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public SaveAsTemplateOperatorTest(String testName) {
        super(testName);
    }

    /** Test of invoke method. */
    public void testInvoke() {
        Node sample1 = new Node(Utils.getSourcePackagesNode(), "sample1");  // NOI18N
        Node sampleClass1 = new Node(sample1, "SampleClass1.java");  // NOI18N
        SaveAsTemplateOperator.invoke(sampleClass1);
    }

    /** Test of tree method. */
    public void testTree() {
        SaveAsTemplateOperator sato = new SaveAsTemplateOperator();
        sato.tree();
    }

    /** Test of lblSelectTheCategory method. */
    public void testLblSelectTheCategory() {
        SaveAsTemplateOperator sato = new SaveAsTemplateOperator();
        String labelText = sato.lblSelectTheCategory().getText();
        String expectedText = Bundle.getStringTrimmed("org.openide.loaders.Bundle",
                "CTL_SaveAsTemplate");
        assertEquals("Wrong label found.", expectedText, labelText);
    }

    /** Test of getRootNode method. */
    public void testGetRootNode() {
        SaveAsTemplateOperator sato = new SaveAsTemplateOperator();
        String text = sato.getRootNode().getText();
        String expectedText = "Templates"; // NOI18N
        assertEquals("Wrong root node.", expectedText, text);
    }

    /** Test of selectTemplate method. */
    public void testSelectTemplate() {
        SaveAsTemplateOperator sato = new SaveAsTemplateOperator();
        String mainClass = "Java Main Class";
        // "Java Classes|Java Main Class"
        String templatePath = Bundle.getString("org.netbeans.modules.java.project.Bundle",
                "Templates/Classes")
                + "|" + mainClass;
        sato.setComparator(new DefaultStringComparator(true, true));
        sato.selectTemplate(templatePath);
        String selected = sato.tree().getSelectionPath().getLastPathComponent().toString();
        sato.close();
        assertEquals("Path \"" + templatePath + "\" not selected.", mainClass, selected);
    }
}

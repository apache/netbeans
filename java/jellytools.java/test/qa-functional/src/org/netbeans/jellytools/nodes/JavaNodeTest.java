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
package org.netbeans.jellytools.nodes;

import java.awt.Toolkit;
import java.io.IOException;
import junit.framework.Test;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.SaveAsTemplateOperator;
import org.netbeans.jellytools.testutils.JavaNodeUtils;

/**
 * Test of org.netbeans.jellytools.nodes.JavaNode
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class JavaNodeTest extends JellyTestCase {

    public static String[] tests = new String[]{
        "testVerifyPopup",
        "testOpen",
        "testCut",
        "testCopy",
        "testDelete",
        "testSaveAsTemplate",
        "testProperties"
    };

    /**
     * constructor required by JUnit
     *
     * @param testName method name to be used as testcase
     */
    public JavaNodeTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(JavaNodeTest.class, tests);
    }
    protected static JavaNode javaNode = null;

    /** Finds node before each test case. */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
        if (javaNode == null) {
            javaNode = new JavaNode(new FilesTabOperator().getProjectNode("SampleProject"),
                    "src|sample1|SampleClass1.java"); // NOI18N
        }
    }

    /** Test verifyPopup */
    public void testVerifyPopup() {
        javaNode.verifyPopup();
    }

    /** Test open */
    public void testOpen() {
        javaNode.open();
        new EditorOperator("SampleClass1.java").closeDiscard();  // NOI18N
    }

    /** Test cut  */
    public void testCut() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        javaNode.cut();
        JavaNodeUtils.testClipboard(clipboard1);
    }

    /** Test copy */
    public void testCopy() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        javaNode.copy();
        JavaNodeUtils.testClipboard(clipboard1);
    }

    /** Test delete */
    public void testDelete() {
        javaNode.delete();
        JavaNodeUtils.closeSafeDeleteDialog();
    }

    /** Test properties */
    public void testProperties() {
        javaNode.properties();
        JavaNodeUtils.closeProperties("SampleClass1.java"); // NOI18N
    }

    /** Test saveAsTemplate */
    public void testSaveAsTemplate() {
        javaNode.saveAsTemplate();
        new SaveAsTemplateOperator().close();
    }
}

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
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.SaveAsTemplateOperator;
import org.netbeans.jellytools.testutils.NodeUtils;

/** Test of org.netbeans.jellytools.nodes.URLNode
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class URLNodeTest extends JellyTestCase {

    public static final String[] tests = new String[]{
        "testVerifyPopup",
        "testCut",
        "testCopy",
        "testDelete",
        "testRename",
        "testSaveAsTemplate",
        "testProperties"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public URLNodeTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(URLNodeTest.class, tests);
    }

    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    protected static URLNode urlNode = null;

    /** Finds node before each test case. */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
        // find node
        if (urlNode == null) {
            urlNode = new URLNode(new SourcePackagesNode("SampleProject"), "sample1|url.url");  // NOI18N
        }
    }

    /** Test verifyPopup */
    public void testVerifyPopup() {
        urlNode.verifyPopup();
    }

    /** Test cut */
    public void testCut() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        urlNode.cut();
        NodeUtils.testClipboard(clipboard1);
    }

    /** Test copy  */
    public void testCopy() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        urlNode.copy();
        NodeUtils.testClipboard(clipboard1);
    }

    /** Test delete  */
    public void testDelete() {
        urlNode.delete();
        NodeUtils.closeConfirmDeleteDialog();
    }

    /** Test rename  */
    public void testRename() {
        urlNode.rename();
        NodeUtils.closeRenameDialog();
    }

    /** Test properties  */
    public void testProperties() {
        urlNode.properties();
        NodeUtils.closeProperties("url"); //NOI18N
    }

    /** Test saveAsTemplate  */
    public void testSaveAsTemplate() {
        urlNode.saveAsTemplate();
        new SaveAsTemplateOperator().close();
    }
}

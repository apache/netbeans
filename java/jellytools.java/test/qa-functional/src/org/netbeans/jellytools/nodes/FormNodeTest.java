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
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.SaveAsTemplateOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.testutils.JavaNodeUtils;

/**
 * Test of org.netbeans.jellytools.nodes.FormNode
 */
public class FormNodeTest extends JellyTestCase {

    public static String[] tests = new String[]{
        "testVerifyPopup",
        "testOpen",
        "testEdit",
        "testCompile",
        "testCut",
        "testCopy",
        "testDelete",
        "testSaveAsTemplate",
        "testProperties"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public FormNodeTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(FormNodeTest.class, tests);
    }
    private static FormNode formNode;

    /** Find node. */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
        if (formNode == null) {
            formNode = new FormNode(new FilesTabOperator().getProjectNode("SampleProject"),
                    "src|sample1|JFrameSample.java"); // NOI18N
        }
    }

    /** Test verifyPopup */
    public void testVerifyPopup() {
        formNode.verifyPopup(); // NOI18N
    }

    /** Test open */
    public void testOpen() {
        formNode.open();
        FormDesignerOperator formDesigner = new FormDesignerOperator("JFrameSample");  // NOI18N
        // for an unknown reason IDE thinks that opened form is modified and we need to save it
        new SaveAllAction().performAPI();
        formDesigner.closeDiscard();
    }

    /** Test edit  */
    public void testEdit() {
        formNode.edit();
        new EditorOperator("JFrameSample").closeDiscard();  //NOI18N
    }

    /** Test compile  */
    public void testCompile() {
        MainWindowOperator.StatusTextTracer statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
        statusTextTracer.start();
        formNode.compile();
        // wait status text "Building SampleProject (compile-single)"
        statusTextTracer.waitText("compile-single", true); // NOI18N
        // wait status text "Finished building SampleProject (compile-single).
        statusTextTracer.waitText("compile-single", true); // NOI18N
        statusTextTracer.stop();
    }

    /** Test cut */
    public void testCut() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        formNode.cut();
        JavaNodeUtils.testClipboard(clipboard1);
    }

    /** Test copy  */
    public void testCopy() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        formNode.copy();
        JavaNodeUtils.testClipboard(clipboard1);
    }

    /** Test delete */
    public void testDelete() {
        formNode.delete();
        JavaNodeUtils.closeSafeDeleteDialog();
    }

    /** Test saveAsTemplate. */
    public void testSaveAsTemplate() {
        formNode.saveAsTemplate();
        new SaveAsTemplateOperator().close();
    }

    /** Test properties */
    public void testProperties() {
        formNode.properties();
        JavaNodeUtils.closeProperties("JFrameSample");  // NOI18N
    }
}

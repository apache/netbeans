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
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.testutils.JavaNodeUtils;

/**
 * Test of org.netbeans.jellytools.nodes.FolderNode
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class FolderNodeTest extends JellyTestCase {

    public static String[] tests = {
        "testFind",
        "testCompile",
        "testCut",
        "testCopy",
        "testPaste",
        "testDelete",
        "testRename",
        "testProperties",
        "testNewFile"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public FolderNodeTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static junit.framework.Test suite() {
        return createModuleTest(FolderNodeTest.class, tests);
    }

    /** Test case setup. */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
    }

    /** Test find method. */
    public void testFind() {
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        folderNode.find();
        new FindInFilesOperator().close();
    }

    /** Test compile. */
    public void testCompile() {
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        MainWindowOperator.StatusTextTracer statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
        statusTextTracer.start();
        folderNode.compile();
        // wait status text "Building SampleProject (compile-single)"
        statusTextTracer.waitText("compile-single", true); // NOI18N
        // wait status text "Finished building SampleProject (compile-single).
        statusTextTracer.waitText("compile-single", true); // NOI18N
        statusTextTracer.stop();
    }

    /** Test paste. */
    public void testPaste() {
        FolderNode sample1Node = new FolderNode(new FilesTabOperator().getProjectNode("SampleProject"), "src|sample1"); // NOI18N
        FolderNode propertiesNode = new FolderNode(sample1Node, "properties.properties"); // NOI18N
        propertiesNode.copy();
        sample1Node.paste();
        FolderNode properties1Node = new FolderNode(sample1Node, "properties_1.properties");  // NOI18N
        JavaNodeUtils.performSafeDelete(properties1Node);
    }

    /** Test cut. */
    public void testCut() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        folderNode.cut();
        JavaNodeUtils.testClipboard(clipboard1);
    }

    /** Test copy. */
    public void testCopy() {
        final Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        folderNode.copy();
        JavaNodeUtils.testClipboard(clipboard1);
    }

    /** Test delete. */
    public void testDelete() {
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        folderNode.delete();
        JavaNodeUtils.closeSafeDeleteDialog();
    }

    /** Test rename */
    public void testRename() {
        FolderNode sample1Node = new FolderNode(new FilesTabOperator().getProjectNode("SampleProject"), "nbproject"); // NOI18N
        sample1Node.rename();
        JavaNodeUtils.closeRenameDialog();
    }

    /** Test properties */
    public void testProperties() {
        FolderNode sample1Node = new FolderNode(new FilesTabOperator().getProjectNode("SampleProject"), "src|sample1"); // NOI18N
        sample1Node.properties();
        JavaNodeUtils.closeProperties("sample1"); //NOI18N
    }

    /** Test newFile */
    public void testNewFile() {
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        folderNode.newFile();
        new NewFileWizardOperator().close();
    }
}

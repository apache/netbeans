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
/*
 * LocalHistoryViewTest.java
 *
 * Created on February 2, 2007, 1:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author peter
 */
package org.netbeans.test.localhistory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.localhistory.operators.ShowLocalHistoryOperator;
import org.netbeans.test.localhistory.utils.TestKit;

/**
 * @author pvcs
 */
public class LocalHistoryViewTest extends JellyTestCase {

    public static final String TMP_PATH = "/tmp";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    private static ShowLocalHistoryOperator slho;
    private static EditorOperator eo;
    private static Node node;
    private static String fileContent;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;

    /** Creates a new instance of LocalHistoryViewTest */
    public LocalHistoryViewTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        System.out.println("### " + getName() + " ###");

    }

    public static void main(String[] args) {
        // TODO code application logic here
        TestRunner.run(suite());
    }

    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(LocalHistoryViewTest.class).addTest(
                "testLocalHistoryInvoke",
                "testLocalHistoryRevertFromHistory",
//                "testLocalHistoryRevisionCountAfterModification",
                "testLocalHistoryNewFileInNewPackage",
                "testLocalHistoryRevertDeleted",
                "testLocalHistoryRevisionCountAfterModification2")
                .enableModules(".*")
                .clusters(".*"));
    }

    private void sleep(int timeInMs) {
        new EventTool().waitNoEvent(timeInMs);
    }

    public void testLocalHistoryInvoke() {
        try {
            openDataProjects(PROJECT_NAME);
        } catch (IOException ex) {
            ex.printStackTrace();
            fail("Unable to open project: " + PROJECT_NAME);
        }
        sleep(5000);
        node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
        node.performPopupAction("Open");
        eo = new EditorOperator("Main.java");
        eo.deleteLine(2);
        eo.saveDocument();
        slho = ShowLocalHistoryOperator.invoke(node);
        slho.verify();
    }

    public void testLocalHistoryRevertFromHistory() {
        slho.performPopupAction(1, "Revert from History");
        sleep(1500);
        int versions = slho.getVersionCount();
        assertEquals("1. Wrong number of versions!", 2, versions);
    }

    public void testLocalHistoryDeleteFromHistory() {
        slho.performPopupAction(2, "Delete from History");
        sleep(1500);
        //nodes are collapsed after deletion - new invocation has to called
        EditorOperator.closeDiscardAll();
        node.performPopupAction("Open");
        eo = new EditorOperator("Main.java");
        node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
        slho = ShowLocalHistoryOperator.invoke(node);
        //
        sleep(1500);
        int versions = slho.getVersionCount();
        assertEquals("2. Wrong number of versions!", 1, versions);
    }

    public void testLocalHistoryRevisionCountAfterModification() {
        sleep(1500);
        eo = new EditorOperator("Main.java");
        eo.insert("// modification //", 11, 1);
        eo.save();
        sleep(1500);
        slho = ShowLocalHistoryOperator.invoke(node);
        int versions = slho.getVersionCount();
        assertEquals("3. Wrong number of versions!", 3, versions);
        slho.close();
    }

    public void testLocalHistoryNewFileInNewPackage() {
        TestKit.createNewPackage(PROJECT_NAME, "NewPackage");
        TestKit.createNewElement(PROJECT_NAME, "NewPackage", "NewClass");
        node = new Node(new SourcePackagesNode(PROJECT_NAME), "NewPackage|NewClass.java");
        node.performPopupAction("Open");
        eo = new EditorOperator("NewClass.java");
        eo.deleteLine(5);
        eo.insert(TestKit.getOsName(), 12, 1);
        eo.saveDocument();
        fileContent = eo.getText();
        slho = ShowLocalHistoryOperator.invoke(node);
        sleep(1500);
        int versions = slho.getVersionCount();
        assertEquals("4. Wrong number of versions!", 1, versions);
        slho.close();
    }

    public void testLocalHistoryRevertDeleted() {
        node = new Node(new SourcePackagesNode(PROJECT_NAME), "NewPackage");
        sleep(3000);
        node.performPopupActionNoBlock("Delete");
//        NbDialogOperator dialog = new NbDialogOperator("Safe Delete");
        NbDialogOperator dialog = new NbDialogOperator("Delete");
        dialog.ok();
        node = new SourcePackagesNode(PROJECT_NAME);
        node.performPopupAction("Local History|Revert Deleted");
        sleep(1000);
        node = new Node(new SourcePackagesNode(PROJECT_NAME), "NewPackage|NewClass.java");
        slho = ShowLocalHistoryOperator.invoke(node);
        sleep(1500);
        int versions = slho.getVersionCount();
        assertEquals("5. Wrong number of versions!", 2, versions);
    }

    public void testLocalHistoryRevisionCountAfterModification2() {
        node.performPopupAction("Open");
        eo = new EditorOperator("NewClass.java");
        assertEquals("Content of file differs after revert!", fileContent, eo.getText());
        eo.deleteLine(5);
        eo.insert(TestKit.getOsName(), 12, 1);
        eo.save();
        sleep(1500);
        int versions = slho.getVersionCount();
        assertEquals("6. Wrong number of versions!", 3, versions);
    }
}

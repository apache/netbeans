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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

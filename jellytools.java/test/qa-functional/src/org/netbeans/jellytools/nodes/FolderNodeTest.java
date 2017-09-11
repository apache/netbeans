/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
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

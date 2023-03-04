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
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.SaveAsTemplateOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jellytools.testutils.NodeUtils;

/** Test of org.netbeans.jellytools.nodes.PropertiesNode
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class PropertiesNodeTest extends JellyTestCase {

    static final String[] tests = {
        "testVerifyPopup",
        "testOpen",
        "testEdit",
        "testCut",
        "testCopy",
        "testDelete",
        "testRename",
        "testAddLocale",
        "testSaveAsTemplate",
        "testProperties"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public PropertiesNodeTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(PropertiesNodeTest.class, tests);
    }
    protected static PropertiesNode propertiesNode = null;

    /** Finds node before each test case. */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        if (propertiesNode == null) {
            openDataProjects("SampleProject");
            propertiesNode = new PropertiesNode(new SourcePackagesNode("SampleProject"), "sample1|properties.properties");  // NOI18N
        }
    }

    /** Test verifyPopup */
    public void testVerifyPopup() {
        propertiesNode.verifyPopup();
    }

    /** Test open */
    public void testOpen() {
        propertiesNode.open();
        new TopComponentOperator("properties").close();
    }

    /** Test edit */
    public void testEdit() {
        propertiesNode.edit();
        new TopComponentOperator("properties").close();
    }

    /** Test addLocale  */
    public void testAddLocale() {
        propertiesNode.addLocale();
        // "New Locale"
        String newLocaleTitle = Bundle.getString("org.netbeans.modules.properties.Bundle", "CTL_NewLocaleTitle");
        new JDialogOperator(newLocaleTitle).requestClose();
    }

    /** Test cut */
    public void testCut() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        propertiesNode.cut();
        NodeUtils.testClipboard(clipboard1);
    }

    /** Test copy */
    public void testCopy() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        propertiesNode.copy();
        NodeUtils.testClipboard(clipboard1);
    }

    /** Test delete */
    public void testDelete() {
        propertiesNode.delete();
        NodeUtils.closeConfirmDeleteDialog();
    }

    /** Test rename */
    public void testRename() {
        propertiesNode.rename();
        NodeUtils.closeRenameDialog();
    }

    /** Test properties */
    public void testProperties() {
        propertiesNode.properties();
        NodeUtils.closeProperties("properties");
    }

    /** Test saveAsTemplate */
    public void testSaveAsTemplate() {
        propertiesNode.saveAsTemplate();
        new SaveAsTemplateOperator().close();
    }
}

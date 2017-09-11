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

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
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.SaveAsTemplateOperator;
import org.netbeans.jellytools.testutils.NodeUtils;

/** Test of org.netbeans.jellytools.nodes.HTMLNode
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class HTMLNodeTest extends JellyTestCase {

    public static final String[] tests = {
        "testVerifyPopup",
        "testOpen",
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
    public HTMLNodeTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(HTMLNodeTest.class, tests);
    }
    protected static HTMLNode htmlNode = null;

    /** Find node. */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
        if (htmlNode == null) {
            htmlNode = new HTMLNode(new SourcePackagesNode("SampleProject"),
                    "sample1|html.html"); // NOI18N
        }
    }

    /** Test verifyPopup  */
    public void testVerifyPopup() {
        htmlNode.verifyPopup();
    }

    /** Test open */
    public void testOpen() {
        htmlNode.open();
        new EditorOperator("html").closeDiscard();  // NOI18N
    }

    /** Test cut */
    public void testCut() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        htmlNode.cut();
        NodeUtils.testClipboard(clipboard1);
    }

    /** Test copy */
    public void testCopy() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        htmlNode.copy();
        NodeUtils.testClipboard(clipboard1);
    }

    /** Test delete */
    public void testDelete() {
        htmlNode.delete();
        NodeUtils.closeConfirmDeleteDialog();
    }

    /** Test rename */
    public void testRename() {
        htmlNode.rename();
        NodeUtils.closeRenameDialog();
    }

    /** Test properties */
    public void testProperties() {
        htmlNode.properties();
        NodeUtils.closeProperties("html"); // NOI18N
    }

    /** Test saveAsTemplate */
    public void testSaveAsTemplate() {
        htmlNode.saveAsTemplate();
        new SaveAsTemplateOperator().close();
    }
}

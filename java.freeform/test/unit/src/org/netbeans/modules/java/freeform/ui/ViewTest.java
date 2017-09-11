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

package org.netbeans.modules.java.freeform.ui;

import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.ant.freeform.TestBase;
import org.netbeans.modules.java.freeform.JavaProjectNature;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Test just style="packages".
 * @author Jesse Glick
 */
public class ViewTest extends TestBase {

    public ViewTest(String name) {
        super(name);
    }

    private LogicalViewProvider lvp;

    protected void setUp() throws Exception {
        super.setUp();
        lvp = extsrcroot.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("found a LogicalViewProvider", lvp);
    }
    
    public void testViewItemBasic() throws Exception {
        Node root = lvp.createLogicalView();
        Children ch = root.getChildren();
        Node[] kids = ch.getNodes(true);
        assertEquals("two child nodes", 2, kids.length);
        assertEquals("correct code name #1", "../src", kids[0].getName());
        assertEquals("correct display name #1", "External Sources", kids[0].getDisplayName());
        assertEquals("correct cookie #1",
            DataObject.find(egdirFO.getFileObject("extsrcroot/src")),
            kids[0].getLookup().lookup(DataObject.class));
        Node[] kids2 = kids[0].getChildren().getNodes(true);
        assertEquals("one child of ../src", 1, kids2.length);
        assertEquals("correct name of #1's kid", "org.foo", kids2[0].getName());
        // Do not test node #2; supplied by ant/freeform.
    }
    
    @RandomlyFails
    public void testFindPath() throws Exception {
        LogicalViewProvider lvp2 = simple.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull(lvp2);
        Node root = lvp2.createLogicalView();
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathPositive(lvp2, root, simple, "src/org/foo/myapp/MyApp.java");
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathPositive(lvp2, root, simple, "src/org/foo/myapp");
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathNegative(lvp2, root, simple, "src/org/foo");
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathNegative(lvp2, root, simple, "src/org");
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathPositive(lvp2, root, simple, "src");
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathPositive(lvp2, root, simple, "antsrc/org/foo/ant/SpecialTask.java");
    }

    @RandomlyFails
    public void testIncludesExcludes() throws Exception {
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestIncludesExcludes(this, JavaProjectNature.STYLE_PACKAGES,
                "prj{s{ignored{file} relevant.excluded{file} relevant.included{file}}}",
                "prj{s{relevant.included{file}}}",
                "prj{s{ignored{file} relevant.included{file}}}",
                "prj{s{relevant.included{file}}}");
    }

}

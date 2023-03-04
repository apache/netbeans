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

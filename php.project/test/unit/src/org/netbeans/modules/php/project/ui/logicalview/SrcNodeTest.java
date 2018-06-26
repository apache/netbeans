/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.logicalview;

import java.util.List;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.util.PhpTestCase;
import org.netbeans.modules.php.project.util.TestUtils;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

public class SrcNodeTest extends PhpTestCase {

    public SrcNodeTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.setLayersAndInstances();
    }

    // #202263, #200297
    public void testSubfolderLookup() throws Exception {
        PhpProject phpProject = TestUtils.createPhpProject(getWorkDir());
        FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(phpProject);
        final FileObject subfolder = sources.createFolder("emptyFolder");
        NodeList<SourceGroup> nodes = new SourcesNodeFactory().createNodes(phpProject);
        List<SourceGroup> sourceGroups = nodes.keys();
        assertFalse("Source groups should be found", sourceGroups.isEmpty());
        for (SourceGroup sourceGroup : sourceGroups) {
            Node node = nodes.node(sourceGroup);
            Lookup lookup = node.getLookup();
            assertNotNull("Fileobject should be found", lookup.lookup(FileObject.class));
            Children children = node.getChildren();
            assertTrue("Should have subnodes", children.getNodesCount(true) > 0);
            for (Node subnode : children.getNodes(true)) {
                Lookup sublookup = subnode.getLookup();
                FileObject fileObject = sublookup.lookup(FileObject.class);
                assertNotNull("Fileobject should be found", fileObject);
                assertEquals("Fileobjects should be same", subfolder, fileObject);
            }
        }
    }
}

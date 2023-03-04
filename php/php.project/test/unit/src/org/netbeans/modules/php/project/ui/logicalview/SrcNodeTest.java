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

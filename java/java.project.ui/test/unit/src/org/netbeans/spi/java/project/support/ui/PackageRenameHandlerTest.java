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

package org.netbeans.spi.java.project.support.ui;

import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.test.MockLookup;

/**
 * @author Jan Becicka
 */
public class PackageRenameHandlerTest extends NbTestCase {

    private Node n;
    private PackageRenameHandlerImpl frh = new PackageRenameHandlerImpl();
    
    
    public PackageRenameHandlerTest(String testName) {
        super(testName);
    }
    
    public @Override void setUp() throws Exception {
        final FileObject root = TestUtil.makeScratchDir(this);
        root.createFolder("testproject");
        MockLookup.setInstances(TestUtil.testProjectFactory());
        
        SourceGroup group = GenericSources.group(ProjectManager.getDefault().findProject(root), root.createFolder("src"), "testGroup", "Test Group", null, null);
        Children ch = PackageView.createPackageView(group).getChildren();
        
        // Create folder
        FileUtil.createFolder(root, "src/foo");
        n = ch.findChild("foo");
        
        assertNotNull(n);
    }
    
    public void testRenameHandlerNotCalled() throws Exception {
        MockLookup.setInstances();
        frh.called = false;
        
        n.setName("blabla");
        assertFalse(frh.called);
    }
    
    public void testRenameHandlerCalled() throws Exception {
        MockLookup.setInstances(frh);
        frh.called = false;
        
        n.setName("foo");// NOI18N
        assertTrue(frh.called);
    }
    
    private static final class PackageRenameHandlerImpl implements PackageRenameHandler {
        boolean called = false;
        public @Override void handleRename(Node n, String newName) throws IllegalArgumentException {
            called = true;
        }
    }
    
}

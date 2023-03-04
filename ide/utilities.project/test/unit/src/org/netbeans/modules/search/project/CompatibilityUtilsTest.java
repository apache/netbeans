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
package org.netbeans.modules.search.project;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.project.Project;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.netbeans.spi.search.SubTreeSearchOptions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author jhavlin
 */
public class CompatibilityUtilsTest extends NbTestCase {

    private FileObject projectDir;

    public CompatibilityUtilsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        projectDir = FileUtil.createMemoryFileSystem().getRoot();
        projectDir.createData("valid.file");
        projectDir.createData("filtered.file");
        FileObject inner = projectDir.createFolder("inner");
        inner.createData("filteredInner.file");
        inner.createData("validInner.file");
    }

    public void testUseSubTreeSearchOptions() {
        Node n = new MockProjectNode(projectDir, Lookups.singleton(new MockProject()));
        assertEquals(3, n.getChildren().getNodes(true).length);
        traverseNodes(n);
    }

    private class MockProjectNode extends AbstractNode {

        FileObject fileObject;

        public MockProjectNode(FileObject fileObject, Lookup lookup) {
            super(new MockChildren(fileObject), new ProxyLookup(lookup));
            this.fileObject = fileObject;
        }

        @Override
        public String getDisplayName() {
            return fileObject.getName() + " (" + fileObject.getPath() + ")";
        }
    }

    private class MockChildren extends Children.Keys<FileObject> {

        private FileObject root;

        public MockChildren(FileObject root) {
            this.root = root;
        }

        @Override
        protected Node[] createNodes(FileObject key) {
            try {
                return new Node[]{new MockProjectNode(key, Lookups.singleton(DataObject.find(key)))};
            } catch (DataObjectNotFoundException ex) {
                fail("Cannot find data object " + key.getPath());
                return new Node[0];
            }
        }

        @Override
        protected void addNotify() {
            setKeys(root.getChildren());
        }
    }

    private class MockProject implements Project {

        @Override
        public FileObject getProjectDirectory() {
            return projectDir;
        }

        @Override
        public Lookup getLookup() {
            InstanceContent ic = new InstanceContent();
            ic.add(new MockSearchOptions());
            return new AbstractLookup(ic);
        }
    }

    private class MockSearchOptions extends SubTreeSearchOptions {

        @Override
        public List<SearchFilterDefinition> getFilters() {
            return Collections.<SearchFilterDefinition>singletonList(
                    new MockFilter());
        }
    }

    private class MockFilter extends SearchFilterDefinition {

        @Override
        public boolean searchFile(FileObject file) throws IllegalArgumentException {
            return !file.getName().startsWith("filtered");
        }

        @Override
        public FolderResult traverseFolder(FileObject folder) throws IllegalArgumentException {
            return folder.getName().startsWith("filtered")
                    ? FolderResult.DO_NOT_TRAVERSE
                    : FolderResult.TRAVERSE;
        }
    }

    private void traverseNodes(Node node) {
        System.out.println("Finding under " + node.getDisplayName());
        testFilters(node);
        for (Node n : node.getChildren().getNodes()) {
            traverseNodes(n);
        }
    }

    private void testFilters(Node n) {
        SearchInfo si = SearchInfoHelper.getSearchInfoForNode(n);
        for (FileObject fo : si.getFilesToSearch(
                SearchScopeOptions.create("*", false),
                new SearchListener() {
                }, new AtomicBoolean(false))) {
            if (fo.getName().startsWith("filtered")
                    && !n.getDisplayName().startsWith("filtered")) {
                fail(fo.getPath() + " should be filtered when searching under "
                        + n.getDisplayName());
            }
        }
    }
}

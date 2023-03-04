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
import javax.swing.Action;
import org.netbeans.modules.php.project.PhpProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

public final class Nodes {

    private Nodes() {
    }

    public abstract static class FileChildFactory extends ChildFactory<Node> {
        protected final PhpProject project;

        public FileChildFactory(PhpProject project) {
            assert project != null;
            this.project = project;
        }

        protected abstract List<Node> getNodes();

        @Override
        protected boolean createKeys(List<Node> toPopulate) {
            toPopulate.addAll(getNodes());
            return true;
        }

        @Override
        protected Node createNodeForKey(Node key) {
            return key;
        }

    }

    public static class FileNode extends DummyNode {

        public FileNode(DataObject dobj, PhpProject project) {
            super(dobj.getNodeDelegate(), getChildren(dobj, project));
        }

        @Override
        public String getDisplayName() {
            FileObject fo = getOriginal().getLookup().lookup(FileObject.class);
            return fo != null ? FileUtil.getFileDisplayName(fo) : super.getDisplayName();
        }

        private static org.openide.nodes.Children getChildren(DataObject dobj, PhpProject project) {
            if (dobj instanceof DataFolder) {
                return new DummyChildren(new DummyNode(dobj.getNodeDelegate()), new PhpSourcesFilter(project));
            }
            return Children.LEAF;
        }
    }

    public static class DummyNode extends FilterNode {

        public DummyNode(Node original) {
            super(original);
        }

        public DummyNode(Node original, org.openide.nodes.Children children) {
            super(original, children);
        }

        @Override
        public boolean canCopy() {
            return true;
        }

        @Override
        public boolean canCut() {
            return false;
        }

        @Override
        public boolean canDestroy() {
            return false;
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{};
        }

        @Override
        public boolean hasCustomizer() {
            return false;
        }
    }

    public static class DummyChildren extends FilterNode.Children {
        private final PhpSourcesFilter filter;

        DummyChildren(final Node originalNode, PhpSourcesFilter filter) {
            super(originalNode);
            this.filter = filter;
        }

        @Override
        protected Node[] createNodes(Node key) {
            FileObject file = key.getLookup().lookup(FileObject.class);
            return file != null && filter.acceptFileObject(file) ? super.createNodes(key) : new Node[0];
        }

        @Override
        protected Node copyNode(final Node originalNode) {
            FileObject file = originalNode.getLookup().lookup(FileObject.class);
            if (file.isFolder()) {
                return new DummyNode(originalNode, new DummyChildren(originalNode, filter));
            }
            return new DummyNode(originalNode);
        }
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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

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

package org.netbeans.modules.apisupport.project.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Displays data directories.
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-apisupport-project", position=150)
public class TestDataDirsNodeFactory implements NodeFactory {

    /** public for layer */
    public TestDataDirsNodeFactory() {}

    public NodeList<?> createNodes(Project p) {
        NbModuleProject prj = p.getLookup().lookup(NbModuleProject.class);
        return new TestDataDirsNL(prj);
    }

    private static class TestDataDirsNL implements NodeList<SourceGroup> {

        private final NbModuleProject project;

        public TestDataDirsNL(NbModuleProject project) {
            this.project = project;
        }

        public List<SourceGroup> keys() {
            List<SourceGroup> keys = new ArrayList<SourceGroup>();
            for (String testType : project.supportedTestTypes()) {
                String dataDir = project.evaluator().getProperty("test." + testType + ".data.dir");
                if (dataDir != null) {
                    FileObject root = project.getHelper().resolveFileObject(dataDir);
                    if (root != null) {
                        String displayName = NbBundle.getMessage(TestDataDirsNodeFactory.class, "TestDataDirsNodeFactory." + testType + "_test_data");
                        keys.add(GenericSources.group(project, root, testType,displayName, null, null));
                    }
                }
            }
            return keys;
        }

        public Node node(final SourceGroup key) {
            try {
                Node nodeDelegate = DataObject.find(key.getRootFolder()).getNodeDelegate();
                return new FilterNode(nodeDelegate,
                        null, new ProxyLookup(nodeDelegate.getLookup(), Lookups.singleton(new PathFinder(key)))) {
                    @Override
                    public String getName() {
                        return key.getName();
                    }
                    @Override
                    public String getDisplayName() {
                        return key.getDisplayName();
                    }
                };
            } catch (DataObjectNotFoundException ex) {
                throw new AssertionError(ex);
            }
        }

        public void addChangeListener(ChangeListener l) {}

        public void removeChangeListener(ChangeListener l) {}

        public void addNotify() {}

        public void removeNotify() {}

    }
    
    /** Copied from PhysicalView, PackageRootNode and TreeRootNode. */
    public static final class PathFinder implements org.netbeans.spi.project.ui.PathFinder {
        
        private final SourceGroup g;
        
        PathFinder(SourceGroup g) {
            this.g = g;
        }
        
        @Override
        public Node findPath(Node rootNode, Object o) {
            FileObject fo;
            if (o instanceof FileObject) {
                fo = (FileObject) o;
            } else if (o instanceof DataObject) {
                fo = ((DataObject) o).getPrimaryFile();
            } else {
                return null;
            }
            FileObject groupRoot = g.getRootFolder();
            if (FileUtil.isParentOf(groupRoot, fo) /* && group.contains(fo) */) {
                return findPathReduced(fo, rootNode);
            } else if (groupRoot.equals(fo)) {
                return rootNode;
            } else {
                return null;
            }
        }

        private Node findPathPlain(FileObject fo, FileObject groupRoot, Node rootNode) {
            FileObject folder = fo.isFolder() ? fo : fo.getParent();
            String relPath = FileUtil.getRelativePath(groupRoot, folder);
            List<String> path = new ArrayList<String>();
            StringTokenizer strtok = new StringTokenizer(relPath, "/"); // NOI18N
            while (strtok.hasMoreTokens()) {
                String token = strtok.nextToken();
               path.add(token);
            }
            try {
                Node folderNode =  folder.equals(groupRoot) ? rootNode : NodeOp.findPath(rootNode, Collections.enumeration(path));
                if (fo.isFolder()) {
                    return folderNode;
                } else {
                    Node[] childs = folderNode.getChildren().getNodes(true);
                    for (int i = 0; i < childs.length; i++) {
                       DataObject dobj = childs[i].getLookup().lookup(DataObject.class);
                       if (dobj != null && dobj.getPrimaryFile().getNameExt().equals(fo.getNameExt())) {
                           return childs[i];
                       }
                    }
                }
            } catch (NodeNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        private Node findPathReduced(FileObject fo, Node n) {
            FileObject f = n.getLookup().lookup(FileObject.class);
            if (f == fo) {
                return n;
            } else if (f != null && FileUtil.isParentOf(f, fo)) {
                for (Node child : n.getChildren().getNodes(true)) {
                    Node found = findPathReduced(fo, child);
                    if (found != null) {
                        return found;
                    }
                }
            }
            return null;
        }

    }

}

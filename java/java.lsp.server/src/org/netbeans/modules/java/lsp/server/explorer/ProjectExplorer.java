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
package org.netbeans.modules.java.lsp.server.explorer;

import java.net.MalformedURLException;
import java.net.URI;
import org.netbeans.modules.java.lsp.server.explorer.api.ExplorerManagerFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeDataListener;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeDataProvider;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeItemData;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author sdedic
 */
@ServiceProviders({
    @ServiceProvider(path = "Explorers/" + ProjectExplorer.ID_PROJECT_LOGICAL_VIEW, service = ExplorerManagerFactory.class),
    @ServiceProvider(path = "Explorers/" + ProjectExplorer.ID_PROJECT_LOGICAL_VIEW, service = PathFinder.class),
    @ServiceProvider(service = TreeDataProvider.Factory.class, path = "Explorers/" + ProjectExplorer.ID_PROJECT_LOGICAL_VIEW)
})
public class ProjectExplorer implements ExplorerManagerFactory, PathFinder, TreeDataProvider.Factory {
    static final String ID_PROJECT_LOGICAL_VIEW = "foundProjects"; // NOI18N
    
    private static final RequestProcessor PROJECT_INIT_RP = new RequestProcessor(ProjectExplorer.class.getName());
    
    @Override
    public CompletionStage<ExplorerManager> createManager(String id, Lookup context) {
        if (!ID_PROJECT_LOGICAL_VIEW.equals(id)) {
            return null;
        }
        return CompletableFuture.supplyAsync(() -> OpenProjects.getDefault().createLogicalView(), PROJECT_INIT_RP);
    }

    @Override
    public Node findPath(Node root, Object target) {
        FileObject file = null;
        
        if (target instanceof FileObject) {
            file = (FileObject)target;
        } else {
            URI uri = null;
            if (target instanceof String) {
                uri = URI.create(target.toString());
            } else if (target instanceof URI) {
                uri = (URI)target;
            }
            if (uri == null) {
                return null;
            }
            try {
                file = URLMapper.findFileObject(uri.toURL());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }
        if (file == null) {
            return null;
        }
        Project p = FileOwnerQuery.getOwner(file); 
        if (p == null) {
            return null;
        }
        Node[] projectChildren = root.getChildren().getNodes(true);
        if (projectChildren.length == 0) {
            // this is mega-ugly, but the project's node initializes lazily somehow and
            // sometimes does not return proper children on first query.
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            projectChildren = root.getChildren().getNodes(true);
        }
        for (Node n : projectChildren) {
            Project nodeProject = n.getLookup().lookup(Project.class);
            if (nodeProject != p) {
                continue;
            }
            org.netbeans.spi.project.ui.PathFinder ppf = p.getLookup().lookup(org.netbeans.spi.project.ui.PathFinder.class);
            if (ppf == null) {
                continue;
            }
            return ppf.findPath(n, file);
        }
        return null;
    }

    @Override
    public TreeDataProvider createProvider(String treeId) {
        return new ProjectDecorator();
    }
    
    static class ProjectDecorator implements TreeDataProvider {

        @Override
        public TreeItemData createDecorations(Node n, boolean expanded) {
            TreeItemData tid = new TreeItemData();
            FileObject f = n.getLookup().lookup(FileObject.class);
            if (f != null && f.isData()) {
                // set leaf status for all files in the projects view.
                tid.makeLeaf();
            }
            return tid;
        }

        @Override
        public void addTreeItemDataListener(TreeDataListener l) {
        }

        @Override
        public void removeTreeItemDataListener(TreeDataListener l) {
        }

        @Override
        public void nodeReleased(Node n) {
            
        }
    }
}

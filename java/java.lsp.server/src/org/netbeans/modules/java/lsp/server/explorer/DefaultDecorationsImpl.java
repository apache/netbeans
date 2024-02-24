/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.lsp.server.explorer;

import org.netbeans.modules.java.lsp.server.explorer.api.TreeDataListener;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeItemData;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeDataProvider;
import java.awt.Image;
import java.beans.BeanInfo;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.lsp.server.Utils;
import static org.netbeans.modules.java.lsp.server.explorer.NodeLookupContextValues.nodeLookup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provides default decorations for the node.
 *
 * @author sdedic
 */
@ServiceProvider(service = TreeDataProvider.Factory.class, path = "Explorers/_all")
public class DefaultDecorationsImpl implements TreeDataProvider.Factory {

    public static final String EXPLORER_ROOT = "Explorers"; // NOI18N
    public static final String COOKIES_EXT = "contextValues"; // NOI18N
    
    public static final String CTXVALUE_FILE = "is:file"; // NOI18N
    public static final String CTXVALUE_FOLDER = "is:folder"; // NOI18N
    public static final String CTXVALUE_PROJECT = "is:project"; // NOI18N
    public static final String CTXVALUE_PROJECT_ROOT = "is:projectRoot"; // NOI18N
    public static final String CTXVALUE_PROJECT_SUBPROJECT = "is:subproject"; // NOI18N
    public static final String CTXVALUE_CAP_RENAME = "cap:rename"; // NOI18N
    public static final String CTXVALUE_CAP_DELETE = "cap:delete"; // NOI18N

    private static final Logger LOG = Logger.getLogger(DefaultDecorationsImpl.class.getName());

    private static final Node DUMMY_NODE = new AbstractNode(Children.LEAF);
    private static final Image DEFAULT_IMAGE = DUMMY_NODE.getIcon(BeanInfo.ICON_COLOR_16x16);

    void readFiles(FileObject parent, List<String> lines) {
        if (parent == null) {
            return;
        }
        for (FileObject f : parent.getChildren()) {
            if (f.isData() && COOKIES_EXT.equals(f.getExt())) {
                try {
                    f.asLines().stream().filter(s -> !s.trim().isEmpty() && !s.startsWith("#")).forEach(lines::add); // NOI18N
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Unable to read lookup items from {0}", f);
                }
            }
        }
    }

    @Override
    public synchronized TreeDataProvider createProvider(String treeId) {
        LOG.log(Level.FINE, "Creating default DecorationImpl for {0}", treeId);
        List<String> lines = new ArrayList<>();
        FileObject explorerRoot = FileUtil.getConfigFile(EXPLORER_ROOT);
        readFiles(explorerRoot, lines);
        readFiles(explorerRoot.getFileObject(treeId), lines);
        readFiles(explorerRoot.getFileObject("_all"), lines);

        NodeLookupContextValues p = nodeLookup(lines.toArray(new String[0]));
        return new ProviderImpl(p);
    }
    
    static class ProviderImpl implements TreeDataProvider {

        private final NodeLookupContextValues lookupValues;

        public ProviderImpl(NodeLookupContextValues lookupValues) {
            this.lookupValues = lookupValues;
        }

        @Override
        public TreeItemData createDecorations(Node n, boolean expanded) {
            TreeItemData d = new TreeItemData();
            boolean set = false;
            NodeLookupContextValues v = lookupValues;
            if (v != null) {
                String[] vals = v.findContextValues(n);
                if (vals != null && vals.length > 0) {
                    set = true;
                    d.setContextValues(vals);
                }
            }
            Image i = n.getIcon(BeanInfo.ICON_COLOR_16x16);
            if (!i.equals(DEFAULT_IMAGE)) {
                d.setIconImage(i);
                set = true;
            }
            
            FileObject f = n.getLookup().lookup(FileObject.class);
            boolean nodeChecked = false;
            if (f == null) {
                DataFolder df = n.getLookup().lookup(DataFolder.class);
                if (df != null) {
                    f = df.getPrimaryFile();
                    // Workaround for possible bug in data folder: DataFolder itself and the Fileobject for the folder do not
                    // have a Node in their Lookups.
                    nodeChecked = true;
                }
            } else if (f.isFolder()) {
                // Workaround for possible bug in data folder
                nodeChecked = true;
            }
            
            boolean folder = false;
            File physFile = null;
            Project p = n.getLookup().lookup(Project.class);
            
            if (f != null) {
                // reverse check, if the file's node is proxied to by the node we got:
                Node fn = f.getLookup().lookup(Node.class);
                // DataObjects that have MultiDataObject.associateLookup() == 0 do not report Node in their FileObject, hack it for files only:
                if (fn == null && !f.isFolder()) {
                    try {
                        DataObject obj = DataObject.find(f);
                        fn = obj.getNodeDelegate();
                    } catch (DataObjectNotFoundException ex) {
                        // ignore
                    }
                }
                if (nodeChecked || fn != null) {
                    if (nodeChecked || n.getLookup().lookup(fn.getClass()) == fn) {
                        try {
                            // Workaround for prevailing folder usage in LSP clients: filter out
                            // virtual or archive-based = readonly folders
                            physFile = FileUtil.toFile(f);
                            F: if (f.isFolder() && physFile != null) {
                                // workaround^2: if the node represents the project directory, it may be some computed collection like
                                // project files / buildscripts. Check the parent folder and if it yields the same project, then this node is
                                // just a collection not a real project root -> not a "folder" for LSP.
                                Project owner = FileOwnerQuery.getOwner(f);
                                if (owner != null && owner.getProjectDirectory().equals(f)) {
                                    Node parent = n.getParentNode();
                                    if (parent != null) {
                                        Project parentP = parent.getLookup().lookup(Project.class);
                                        if (parentP == owner) {
                                            break F;
                                        }   
                                    }
                                }
                                folder = true;
                                d.addContextValues(CTXVALUE_FOLDER);
                            } else if ((f.isData() || f.isValid()) && !f.isVirtual()) {
                                d.addContextValues(CTXVALUE_FILE);
                                // PENDING: this could be moved to the VSNetbeans module ?
                                d.setCommand("vscode.open"); // NOI18N
                            }
                            // set the URI:
                            d.setResourceURI(new URI(Utils.toUri(f)));
                            set = true;
                        } catch (URISyntaxException ex) {
                            LOG.log(Level.WARNING, "Could not convert file to URI: {0}", f);
                        }
                    }
                }
            }
            
            // special handling for project - just presence of ProjectCookie is not sufficient. 
            // The Node must also expose a folder that is the project root folder itself:
            if (p != null & folder && p.getProjectDirectory().equals(f)) {
                d.addContextValues(CTXVALUE_PROJECT);
                Project root = ProjectUtils.rootOf(p);
                if (root == p) {
                    Set<Project> contained = ProjectUtils.getContainedProjects(root, false);
                    if (contained != null && !contained.isEmpty()) {
                        d.addContextValues(CTXVALUE_PROJECT_ROOT);
                    }
                } else {
                    d.addContextValues(CTXVALUE_PROJECT_SUBPROJECT);
                }
            } else if (n.canDestroy()) {
                // TODO Hack: exclude projects from delete capability. The TreeItemData probably needs to support
                // exclusion... Project delete UI is not suitable for LSP at the moment
                d.addContextValues(CTXVALUE_CAP_DELETE);
                set = true;
            }
            if (n.canRename()) {
                d.addContextValues(CTXVALUE_CAP_RENAME);
                set = true;
            }

            return set ? d : null;
        }

        @Override
        public void addTreeItemDataListener(TreeDataListener l) {
            lookupValues.addTreeItemDataListener(l);
        }

        @Override
        public void removeTreeItemDataListener(TreeDataListener l) {
            lookupValues.removeTreeItemDataListener(l);
        }

        @Override
        public void nodeReleased(Node n) {
            lookupValues.nodeReleased(n);
        }
    }
}

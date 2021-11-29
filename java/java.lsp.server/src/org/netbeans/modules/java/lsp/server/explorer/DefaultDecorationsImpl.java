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
import java.awt.image.ImageObserver;
import java.beans.BeanInfo;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.java.lsp.server.Utils;
import static org.netbeans.modules.java.lsp.server.explorer.NodeLookupContextValues.nodeLookup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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

        NodeLookupContextValues p = nodeLookup(lines.toArray(new String[lines.size()]));
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
            if (f != null) {
                // reverse check, if the file's node is proxied to by the node we got:
                Node fn = f.getLookup().lookup(Node.class);
                if (fn != null) {
                    if (n.getLookup().lookup(fn.getClass()) == fn) {
                        try {
                            // set the URI:
                            d.setResourceURI(new URI(Utils.toUri(f)));
                            // PENDING: this could be moved to the VSNetbeans module ?
                            d.setCommand("vscode.open"); // NOI18N
                            set = true;
                        } catch (URISyntaxException ex) {
                            LOG.log(Level.WARNING, "Could not convert file to URI: {0}", f);
                        }
                    }
                }

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

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
package org.netbeans.modules.nbcode.integration;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cloud.oracle.assets.CloudAssets;
import org.netbeans.modules.cloud.oracle.bucket.BucketItem;
import org.netbeans.modules.cloud.oracle.database.DatabaseItem;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import static org.netbeans.modules.java.lsp.server.explorer.DefaultDecorationsImpl.COOKIES_EXT;
import org.netbeans.modules.java.lsp.server.explorer.NodeLookupContextValues;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeDataListener;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeDataProvider;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeItemData;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author Jan Horvath
 */
@ServiceProvider(service = TreeDataProvider.Factory.class, path = "Explorers/cloud.assets")
public class LspAssetsDecorationProvider implements TreeDataProvider.Factory {
    private static final Logger LOG = Logger.getLogger(LspAssetsDecorationProvider.class.getName());
    
    public static final String CTXVALUE_CAP_REFERENCE_NAME = "cap:refName"; // NOI18N
    public static final String CTXVALUE_PREFIX_REFERENCE_NAME = "cloudAssetsReferenceName:"; // NOI18N

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
        return new ProviderImpl(null);
    }
    
    static class ProviderImpl implements TreeDataProvider {
        public ProviderImpl(NodeLookupContextValues lookupValues) {
        }

        @Override
        public TreeItemData createDecorations(Node n, boolean expanded) {
            TreeItemData d = new TreeItemData();
            String refName = null;
            boolean set = false;
            
            OCIItem item = n.getLookup().lookup(OCIItem.class);
            if (item != null) {
                refName = CloudAssets.getDefault().getReferenceName(item);
            }
            if (refName != null) {
                d.addContextValues(CTXVALUE_PREFIX_REFERENCE_NAME + refName);
                set = true;
            }
            if (item instanceof BucketItem 
                    || item instanceof DatabaseItem) {
                d.addContextValues(CTXVALUE_CAP_REFERENCE_NAME);
                set = true;
            }
            return set ? d : null;
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

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

import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.java.lsp.server.explorer.NodeLookupContextValues;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeDataListener;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeDataProvider;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeItemData;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Horvath
 */
@ServiceProvider(service = TreeDataProvider.Factory.class, path = "Explorers/_all")
public class LspOcidDecorationProvider implements TreeDataProvider.Factory {
    public static final String CTXVALUE_PREFIX_OCID = "ocid:"; // NOI18N

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
            
            OCIItem item = n.getLookup().lookup(OCIItem.class);
            if (item == null) {
                return null;
            }
            
            d.addContextValues(CTXVALUE_PREFIX_OCID + item.getKey().getValue());
            return d;
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

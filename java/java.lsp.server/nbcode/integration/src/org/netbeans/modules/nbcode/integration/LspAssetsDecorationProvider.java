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

import java.util.Optional;
import java.util.logging.Logger;
import org.netbeans.modules.cloud.oracle.assets.CloudAssets;
import org.netbeans.modules.cloud.oracle.bucket.BucketItem;
import org.netbeans.modules.cloud.oracle.compute.ClusterItem;
import org.netbeans.modules.cloud.oracle.compute.ComputeInstanceItem;
import org.netbeans.modules.cloud.oracle.database.DatabaseItem;
import org.netbeans.modules.cloud.oracle.developer.ContainerRepositoryItem;
import org.netbeans.modules.cloud.oracle.developer.ContainerTagItem;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.vault.SecretItem;
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
@ServiceProvider(service = TreeDataProvider.Factory.class, path = "Explorers/cloud.assets")
public class LspAssetsDecorationProvider implements TreeDataProvider.Factory {
    private static final Logger LOG = Logger.getLogger(LspAssetsDecorationProvider.class.getName());
    
    public static final String CTXVALUE_CAP_REFERENCE_NAME = "cap:refName"; // NOI18N
    public static final String CTXVALUE_PREFIX_REFERENCE_NAME = "cloudAssetsReferenceName:"; // NOI18N
    public static final String CTXVALUE_PREFIX_PUBLIC_IP = "publicIp:"; // NOI18N
    public static final String CTXVALUE_PREFIX_CLUSTER_NAME = "clusterName:"; // NOI18N
    public static final String CTXVALUE_PREFIX_IMAGE_URL = "imageUrl:"; // NOI18N
    public static final String CTXVALUE_PREFIX_IMAGE_COUNT = "imageCount:"; // NOI18N
    public static final String CTXVALUE_PREFIX_REPOSITORY_PUBLIC = "repositoryPublic:"; // NOI18N
    public static final String CTXVALUE_PREFIX_SECRET_LIFECYCLE_STATE = "lifecycleState:"; // NOI18N

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
            String refName;
            boolean set = false;
            
            OCIItem item = n.getLookup().lookup(OCIItem.class);
            if (item == null) {
                return null;
            }
            refName = CloudAssets.getDefault().getReferenceName(item);
            if (refName != null) {
                d.addContextValues(CTXVALUE_PREFIX_REFERENCE_NAME + refName);
                set = true;
            }
            if (item instanceof ComputeInstanceItem) {
                String publicIp = ((ComputeInstanceItem) item).getPublicIp();
                if (publicIp != null) {
                    d.addContextValues(CTXVALUE_PREFIX_PUBLIC_IP + publicIp);
                    set = true;
                }
            }
            if (item instanceof ContainerRepositoryItem) {
                ContainerRepositoryItem repo = (ContainerRepositoryItem) item;
                d.addContextValues(CTXVALUE_PREFIX_IMAGE_COUNT + repo.getImageCount());
                d.addContextValues(CTXVALUE_PREFIX_REPOSITORY_PUBLIC + repo.getIsPublic());
                set = true;
            }
            if (item instanceof ContainerTagItem) {
                String imageUrl = ((ContainerTagItem) item).getUrl();
                Optional<OCIItem> instance = CloudAssets.getDefault().getAssignedItems().stream().filter(i -> i.getClass().equals(ComputeInstanceItem.class)).findFirst();
                if (instance.isPresent()) {
                    d.addContextValues(CTXVALUE_PREFIX_PUBLIC_IP + ((ComputeInstanceItem) instance.get()).getPublicIp());
                } else {
                    ClusterItem cluster = CloudAssets.getDefault().getItem(ClusterItem.class);
                    if (cluster != null) {
                         d.addContextValues(CTXVALUE_PREFIX_CLUSTER_NAME + cluster.getName());
                    }
                }
                d.addContextValues(CTXVALUE_PREFIX_IMAGE_URL + imageUrl);
                set = true;
            }
            if (item instanceof BucketItem 
                    || item instanceof DatabaseItem) {
                d.addContextValues(CTXVALUE_CAP_REFERENCE_NAME);
                set = true;
            }
            
            if (item instanceof SecretItem) {
                d.addContextValues(CTXVALUE_PREFIX_SECRET_LIFECYCLE_STATE + ((SecretItem)item).getLifecycleState());
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

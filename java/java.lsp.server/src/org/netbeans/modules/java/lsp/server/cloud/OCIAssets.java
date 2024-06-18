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
package org.netbeans.modules.java.lsp.server.cloud;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.netbeans.modules.java.lsp.server.explorer.api.ExplorerManagerFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 * Explorer manager for Cloud Services.
 * 
 * @author Jan Horvath
 */
@ServiceProvider(path = "Explorers/" + OCIAssets.ID, service = ExplorerManagerFactory.class)
public class OCIAssets implements ExplorerManagerFactory {
    public static final String ID = "cloud.assets"; // NOI18N
    public static final String CLOUD_PATH = "CloudAssets"; // NOI18N
    private static String CLOUD_NODE_NAME = "cloudAssets";

    @Override
    public CompletionStage<ExplorerManager> createManager(String id, Lookup context) {
        if (!ID.equals(id)) {
            throw new IllegalArgumentException("Unsupported view"); //NOI18N
        }
        ExplorerManager em = new ExplorerManager();
        Lookup.Result<Node> nodes = Lookups.forPath("Cloud/Oracle/Assets").lookupResult(Node.class);
        nodes.allInstances().stream()
//            .filter(n -> CLOUD_NODE_NAME.equals(n.getName()))
                .findFirst()
                .ifPresent(n -> em.setRootContext(n));
        return CompletableFuture.completedFuture(em);
    }
    
}
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
package org.netbeans.modules.nbcode.integration.commands;

import com.google.gson.JsonPrimitive;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.java.lsp.server.explorer.TreeNodeRegistry;
import org.netbeans.modules.java.lsp.server.explorer.TreeViewProvider;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Hurka
 */
@ServiceProvider(service = CommandProvider.class)
public class OCIDCommand implements CommandProvider {

    private static final Logger LOG = Logger.getLogger(OCIDCommand.class.getName());

    private static final String COMMAND_CLOUD_OCID_GET = "nbls.cloud.ocid.get"; // NOI18N

    public OCIDCommand() {
    }

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(COMMAND_CLOUD_OCID_GET);
    }

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        if (arguments.size() < 1) {
            throw new IllegalArgumentException("Expected nodeid");      // NOI18N
        }
        TreeNodeRegistry r = Lookup.getDefault().lookup(TreeNodeRegistry.class);
        if (r == null) {
            return CompletableFuture.completedFuture(null);
        }
        int nodeId = ((JsonPrimitive) arguments.get(0)).getAsInt();
        LOG.log(Level.FINE, "Running OCID command with context: {0}", nodeId);  // NOI18N

        TreeViewProvider nodeProvider = r.providerOf(nodeId);
        Node node = null;
        if (nodeProvider != null) {
            node = nodeProvider.findNode(nodeId);
        }
        if (node == null) {
            return CompletableFuture.completedFuture(null);
        }
        OCIItem item = node.getLookup().lookup(OCIItem.class);
        if (item != null) {
            return CompletableFuture.completedFuture(item.getKey().getValue());
        }
        return CompletableFuture.completedFuture(null);
    }
}

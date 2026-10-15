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
package org.netbeans.modules.cloud.oracle.assets;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.netbeans.modules.cloud.oracle.developer.ContainerRepositoryItem;
import org.netbeans.modules.cloud.oracle.developer.ContainerTagItem;
import org.netbeans.modules.cloud.oracle.developer.ContainerTagNode;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Horvath
 */
@ServiceProvider(service = CommandProvider.class)
public class IntegrationCommands implements CommandProvider {

    private static final String COMMAND_ASSETS_GET = "nbls.cloud.assets.get"; //NOI18N
    private static final String COMMAND_ASSETS_GET_IMAGE_VERSIONS = "nbls.cloud.assets.getImageVersions"; //NOI18N

    private static final Set COMMANDS = new HashSet<>(Arrays.asList(
            COMMAND_ASSETS_GET,
            COMMAND_ASSETS_GET_IMAGE_VERSIONS
    ));

    private final Gson gson = new Gson();

    @Override
    public Set<String> getCommands() {
        return Collections.unmodifiableSet(COMMANDS);
    }

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        CompletableFuture<Object> result = new CompletableFuture<>();
        if (COMMAND_ASSETS_GET.equals(command)) {
            String path = parseStringArgument(arguments); //NOI18N
            if (path != null) {
                List<OCIItem> items = CloudAssets.getDefault().getItems(path);
                result.complete(items);
            } else {
                result.cancel(true);
            }
        } else if (COMMAND_ASSETS_GET_IMAGE_VERSIONS.equals(command)) {
            List<OCIItem> repos = CloudAssets.getDefault().getItems("ContainerRepository"); //NOI18N
            if (repos != null && repos.size() == 1) {
                ContainerRepositoryItem repo = (ContainerRepositoryItem) repos.get(0);
                List<ContainerTagItem> tags = ContainerTagNode.getContainerTags().apply(repo);
                Map<String, String> tagUrls = new HashMap<> ();
                for (ContainerTagItem tag : tags) {
                    tagUrls.put(tag.getName(), tag.getUrl());
                }
                result.complete(tagUrls);
            } else {
                result.cancel(true);
            }
        }
        return result;
    }

    private String parseStringArgument(List<Object> arguments) {
        if (!arguments.isEmpty()) {
            JsonPrimitive item = gson.fromJson(gson.toJson(arguments.get(0)), JsonPrimitive.class);
            return item.getAsString(); // NOI18N
        }
        return null;
    }

}

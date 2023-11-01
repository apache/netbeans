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
package org.netbeans.modules.java.lsp.server.protocol;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.eclipse.lsp4j.CompletionItem;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;

/**
 * Provider of launch configurations completion. Run/Debug launch and Debugger
 * attach configurations can be provided.
 *
 * @author Martin Entlicher
 */
public interface LaunchConfigurationCompletion {

    /**
     * Provide configurations of Run/Debug actions.
     *
     * @param projectSupplier Supplier of the relevant project
     * @return a list of completion items, the list must not be <code>null</code>.
     */
    @NonNull
    CompletableFuture<List<CompletionItem>> configurations(Supplier<CompletableFuture<Project>> projectSupplier);

    /**
     * Provide attributes to a specific configuration.
     *
     * @param projectSupplier Supplier of the relevant project
     * @param attributes all attributes currently specified for the configuration
     * @return a list of completion items, the list must not be <code>null</code>.
     */
    @NonNull
    CompletableFuture<List<CompletionItem>> attributes(Supplier<CompletableFuture<Project>> projectSupplier, Map<String, Object> attributes);

    /**
     * Provide values of an attribute of a configuration.
     *
     * @param projectSupplier Supplier of the relevant project
     * @param attributes all attributes currently specified for the configuration
     * @param attributeName name of the attribute which values are to be provided
     * @return a list of completion items, the list must not be <code>null</code>.
     */
    @NonNull
    CompletableFuture<List<CompletionItem>> attributeValues(Supplier<CompletableFuture<Project>> projectSupplier, Map<String, Object> attributes, String attributeName);

    public interface Factory {
        public LaunchConfigurationCompletion createLaunchConfigurationCompletion(NbCodeClientCapabilities capa);
    }
}

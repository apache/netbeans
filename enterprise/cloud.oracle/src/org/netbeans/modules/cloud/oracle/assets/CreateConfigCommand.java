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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.netbeans.modules.cloud.oracle.actions.ConfigMapUploader;
import org.netbeans.modules.cloud.oracle.actions.DevOpsProjectConfigMapUploader;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Petrovic
 */
@ServiceProvider(service = CommandProvider.class)
public class CreateConfigCommand implements CommandProvider {

    private static final String COMMAND_CREATE_CONFIG = "nbls.cloud.assets.config.create.local"; //NOI18N
    private static final String COMMAND_UPLOAD_TO_CONFIGMAP_WITHIN_DEVOPS = "nbls.cloud.assets.configmap.devops.upload"; //NOI18N
    private static final String COMMAND_UPLOAD_TO_CONFIGMAP = "nbls.cloud.assets.configmap.upload"; //NOI18N

    private static final Set COMMANDS = new HashSet<>(Arrays.asList(
            COMMAND_CREATE_CONFIG,
            COMMAND_UPLOAD_TO_CONFIGMAP_WITHIN_DEVOPS,
            COMMAND_UPLOAD_TO_CONFIGMAP
    ));
    
    @Override
    public Set<String> getCommands() {
        return Collections.unmodifiableSet(COMMANDS);
    }

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        CompletableFuture future = new CompletableFuture();
        if (COMMAND_CREATE_CONFIG.equals(command)) {
            PropertiesGenerator propGen = new PropertiesGenerator(false);
            ApplicationPropertiesGenerator appPropGen = new ApplicationPropertiesGenerator(propGen);
            String toWrite = appPropGen.getApplicationPropertiesString();
            future.complete(toWrite);
        } else if (COMMAND_UPLOAD_TO_CONFIGMAP_WITHIN_DEVOPS.equals(command)) {
            DevOpsProjectConfigMapUploader.uploadConfigMap(future);
        } else if (COMMAND_UPLOAD_TO_CONFIGMAP.equals(command)) {
            ConfigMapUploader.uploadConfigMap(future);
        }
        return future;
    }
    
}

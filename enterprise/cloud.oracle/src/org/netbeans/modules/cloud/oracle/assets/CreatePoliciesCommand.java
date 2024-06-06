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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.netbeans.modules.cloud.oracle.policy.PolicyGenerator;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Horvath
 */
@ServiceProvider(service = CommandProvider.class)
public class CreatePoliciesCommand implements CommandProvider {
    private static final Logger LOG = Logger.getLogger(CreatePoliciesCommand.class.getName());
    
    

    private static final String COMMAND_CREATE_POLICIES = "nbls.cloud.assets.policy.create.local"; //NOI18N
    private static final String COMMAND_CREATE_CONFIG = "nbls.cloud.assets.config.create.local"; //NOI18N
    private static final String COMMAND_CLOUD_ASSETS_REFRESH = "nbls.cloud.assets.refresh"; //NOI18N
    
    private static final RequestProcessor RP = new RequestProcessor("PoliciesCommand"); //NOI18N

    private static final Set COMMANDS = new HashSet<>(Arrays.asList(
            COMMAND_CREATE_POLICIES,
            COMMAND_CREATE_CONFIG,
            COMMAND_CLOUD_ASSETS_REFRESH
    ));

    @Override
    public Set<String> getCommands() {
        return Collections.unmodifiableSet(COMMANDS);
    }

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        CompletableFuture future = new CompletableFuture();
        if (COMMAND_CREATE_POLICIES.equals(command)) {
                RP.post(() -> {
                    try {
                        String policies = PolicyGenerator.createPolicies(CloudAssets.getDefault().getItems());
                        future.complete(policies);
                    } catch (IllegalStateException e) {
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(e.getMessage());
                        DialogDisplayer.getDefault().notifyLater(msg);
                        future.completeExceptionally(e);
                    }
                });
            return future;
        } else if (COMMAND_CREATE_CONFIG.equals(command)) {
            StringWriter writer = new StringWriter();
            Properties dbProps = new Properties();
            dbProps.putAll(PropertiesGenerator.getProperties(null));
            try {
                dbProps.store(writer, "Generated application.properties\n" //NOI18N
                    + "Uncomment following line when running inside Oracle Cloud\n" //NOI18N
                    + "oci.config.instance-principal.enabled=true"); //NOI18N
                future.complete(writer.toString());
            } catch (IOException ex) {
                future.completeExceptionally(ex);
            }
        } else if (COMMAND_CLOUD_ASSETS_REFRESH.equals(command)) {
            CloudAssets.getDefault().update();
        }
        return future;
    }

}

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
package org.netbeans.modules.cloud.oracle.actions;

import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.oracle.bmc.identity.Identity;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.Region;
import com.oracle.bmc.identity.requests.ListRegionsRequest;
import com.oracle.bmc.identity.responses.ListRegionsResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.CompartmentNodes;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCIProfile;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * This command lets user select an Oracle Cloud region. This region will be
 * used for all subsequent requests.
 *
 * @author Jan Horvath
 */
@ServiceProvider(service = CommandProvider.class)
public class SetRegionCommand implements CommandProvider {

    private static final String SET_REGION_COMMAND = "nbls.oci.setRegion"; //NOI18N
    private static final String GET_REGION_COMMAND = "nbls.oci.getRegion"; //NOI18N
    private static final String PROFILE_NAME = "profileName"; //NOI18N
    private static final RequestProcessor RP = new RequestProcessor(SetRegionCommand.class);

    private static Set<String> commands = new HashSet<String>() {{
        add(SET_REGION_COMMAND);
        add(GET_REGION_COMMAND);
    }};

    private final Gson gson = new Gson();

    @Override
    public Set<String> getCommands() {
        return commands;
    }

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        CompletableFuture result = new CompletableFuture();
        if (GET_REGION_COMMAND.equals(command)) {
            com.oracle.bmc.Region region = OCIManager.getDefault().getActiveSession().getRegion();
            result.complete(region.getRegionId());
            return result;
        }
        if (SET_REGION_COMMAND.equals(command)) {
            String profileName = null;
            if (arguments != null && !arguments.isEmpty()) {
                final Map m = arguments.get(0) instanceof JsonNull ? Collections.emptyMap() : gson.fromJson((JsonObject) arguments.get(0), Map.class);
                profileName = m != null ? (String) m.get(PROFILE_NAME) : null;
            }
            OCIProfile session = (OCIProfile) OCIManager.forProfile(profileName);
            RP.post(() -> {
                List<com.oracle.bmc.Region> regions = Arrays.asList(com.oracle.bmc.Region.values());
                List<NotifyDescriptor.QuickPick.Item> items = regions.stream()
                        .filter(r -> !r.getRegionId().contains("gov"))
                        .map(region -> new NotifyDescriptor.QuickPick.Item(region.getRegionId(), region.getRegionCode()))
                        .sorted((r1, r2) -> r1.getLabel().compareTo(r2.getLabel()))
                        .collect(Collectors.toList());

                DialogDisplayer.getDefault().notifyFuture(new NotifyDescriptor.QuickPick(Bundle.SelectRegion(), Bundle.SelectRegion(), items, false))
                        .thenAccept(input -> {
                            Optional<NotifyDescriptor.QuickPick.Item> selected = input.getItems().stream().filter(i -> i.isSelected()).findFirst();
                            if (selected.isPresent()) {
                                String selectedRegionCode = selected.get().getLabel();
                                session.setRegionCode(selectedRegionCode);
                                result.complete(selectedRegionCode);
                                CompartmentNodes.getDefault().refresh();
                            } else {
                                result.complete(null);
                            }
                        });
            });

        }
        return result;
    }

}

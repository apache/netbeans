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
package org.netbeans.modules.java.lsp.server.debugging.attach;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;

import org.netbeans.modules.java.lsp.server.debugging.utils.ErrorUtilities;
import org.netbeans.modules.java.lsp.server.protocol.DebugConnector;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.ShowQuickPickParams;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeClientCapabilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 * Java Debugger attach configurations provider.
 *
 * @author Martin Entlicher
 */
public final class AttachConfigurations {

    static final String CONFIG_TYPE = "java+";     // NOI18N
    static final String CONFIG_REQUEST = "attach";  // NOI18N

    static final RequestProcessor RP = new RequestProcessor(AttachConfigurations.class);

    private final List<ConfigurationAttributes> configurations;

    private AttachConfigurations(NbCodeClientCapabilities capa, List<Connector> attachingConnectors) {
        List<ConfigurationAttributes> configs = new ArrayList<>(5);
        for (Connector ac : attachingConnectors) {
            configs.add(new ConfigurationAttributes(capa, ac));
        }
        this.configurations = Collections.unmodifiableList(configs);
    }

    public static AttachConfigurations get(NbCodeClientCapabilities capa) {
        List<AttachingConnector> attachingConnectors = Bootstrap.virtualMachineManager().attachingConnectors();
        List<ListeningConnector> listeningConnectors = Bootstrap.virtualMachineManager().listeningConnectors();
        List<Connector> connectors = new ArrayList<>(attachingConnectors.size() + listeningConnectors.size());
        connectors.addAll(attachingConnectors);
        connectors.addAll(listeningConnectors);
        return new AttachConfigurations(capa, connectors);
    }

    public static CompletableFuture<Object> findConnectors(NbCodeClientCapabilities capa) {
        return CompletableFuture.supplyAsync(() -> {
            return get(capa).listAttachingConnectors();
        }, RP);
    }

    List<ConfigurationAttributes> getConfigurations() {
        return configurations;
    }

    private List<DebugConnector> listAttachingConnectors() {
        List<DebugConnector> connectors = new ArrayList<>(configurations.size());
        for (ConfigurationAttributes configAttributes : configurations) {
            Map<String, ConfigurationAttribute> attributesMap = configAttributes.getAttributes();
            List<String> names = new ArrayList<>(2);
            List<String> values = new ArrayList<>(2);
            List<String> descriptions = new ArrayList<>(2);
            for (Map.Entry<String, ConfigurationAttribute> entry : attributesMap.entrySet()) {
                ConfigurationAttribute ca = entry.getValue();
                if (ca.isMustSpecify()) {
                    names.add(entry.getKey());
                    values.add(ca.getDefaultValue());
                    descriptions.add(ca.getDescription());
                }
            }
            DebugConnector connector = new DebugConnector(configAttributes.getId(), configAttributes.getName(), CONFIG_TYPE,
                            names, values, descriptions);
            connectors.add(connector);
        }
        connectors.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
        return connectors;
    }

    ConfigurationAttributes findConfiguration(Map<String, Object> attributes) {
        if (!CONFIG_TYPE.equals(attributes.get("type")) ||              // NOI18N
            !CONFIG_REQUEST.equals(attributes.get("request"))) {        // NOI18N

            return null;
        }
        Set<String> names = attributes.keySet();
        Object listenValue = attributes.get("listen");
        boolean listen = listenValue != null && ("true".equals(listenValue) || Boolean.TRUE.equals(listenValue));
        for (ConfigurationAttributes config : configurations) {
            if (listen != (config.getConnector() instanceof ListeningConnector)) {
                continue;
            }
            if (config.areMandatoryAttributesIn(names)) {
                return config;
            }
        }
        return null;
    }

    public static CompletableFuture<Object> findProcessAttachTo(NbCodeLanguageClient client) {
        return CompletableFuture.supplyAsync(() -> {
            return listProcessesToAttachTo(client);
        }, RP).thenCompose(params -> client.showQuickPick(params)).thenApply(itemsList -> {
            if (itemsList == null || itemsList.isEmpty()) {
                return null;
            } else {
                return itemsList.get(0).getUserData();
            }
        });
    }

    @Messages("MSG_NoDebuggableProcess=No debuggable JVM process found. Please be sure to use `-agentlib:jdwp=transport=dt_socket,server=y` option.")
    private static void notifyNoProcessesError(NbCodeLanguageClient client) {
        MessageParams params = new MessageParams();
        params.setMessage(Bundle.MSG_NoDebuggableProcess());
        params.setType(MessageType.Error);
        client.showMessage(params);
    }

    @Messages("LBL_PickProcessAttach=Pick JVM process to attach to:")
    private static ShowQuickPickParams listProcessesToAttachTo(NbCodeLanguageClient client) {
        List<QuickPickItem> attachables = new ArrayList<>();
        List<VirtualMachineDescriptor> descriptors = VirtualMachine.list();
        for (VirtualMachineDescriptor descriptor : descriptors) {
            try {
                VirtualMachine vm = VirtualMachine.attach(descriptor);
                Properties agentProperties = vm.getAgentProperties();
                boolean hasJDWP = false;
                for (Object key : agentProperties.keySet()) {
                    if (key instanceof String && ((String) key).contains("jdwp")) { // NOI18N
                        hasJDWP = true;
                        break;
                    }
                }
                if (hasJDWP) {
                    attachables.add(createQuickPickItem(descriptor, vm));
                }
            } catch (AttachNotSupportedException | IOException ex) {
                continue;
            }
        }
        if (attachables.isEmpty()) {
            notifyNoProcessesError(client);
            throw ErrorUtilities.createResponseErrorException("No debuggable JVM process found.", ResponseErrorCode.RequestCancelled);  // NOI18N
        }
        return new ShowQuickPickParams(Bundle.LBL_PickProcessAttach(), attachables);
    }

    private static QuickPickItem createQuickPickItem(VirtualMachineDescriptor descriptor, VirtualMachine vm) {
        String label = descriptor.id();
        String detail = descriptor.displayName();
        if ("Unknown".equals(detail)) {     // NOI18N
            String command = null;
            try {
                command = vm.getSystemProperties().getProperty("sun.java.command");     // NOI18N
            } catch (IOException ex) {}
            if (command == null) {
                try {
                    command = vm.getAgentProperties().getProperty("sun.java.command");  // NOI18N
                } catch (IOException ex) {}
            }
            if (command != null) {
                detail = command;
            }
        }
        int index = detail.indexOf(' ');
        String description = index > 0 ? detail.substring(0, index) : detail;
        if (index <= 0) {
            // Do not duplicate the description.
            detail = null;
        }
        Object userData = descriptor.id(); // Process Id
        return new QuickPickItem(label, description, detail, false, userData);
    }

}

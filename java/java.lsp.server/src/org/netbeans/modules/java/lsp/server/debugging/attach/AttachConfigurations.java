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
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;

import org.netbeans.modules.java.lsp.server.debugging.utils.ErrorUtilities;
import org.netbeans.modules.java.lsp.server.protocol.DebugConnector;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.QuickPickItem;
import org.netbeans.modules.java.lsp.server.protocol.Server;
import org.netbeans.modules.java.lsp.server.protocol.ShowQuickPickParams;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 * Debugger attach configurations provider.
 *
 * @author Martin Entlicher
 */
public final class AttachConfigurations {

    static final String NAME_ATTACH_PROCESS = "Attach to Process";          // NOI18N
    static final String NAME_ATTACH_SOCKET = "Attach to Port";              // NOI18N
    static final String NAME_ATTACH_SHMEM = "Attach to Shared Memory";      // NOI18N
    static final String NAME_ATTACH_BY = "Attach by ";                      // NOI18N

    static final String CONNECTOR_PROCESS = "com.sun.jdi.ProcessAttach";    // NOI18N
    static final String CONNECTOR_SOCKET = "com.sun.jdi.SocketAttach";      // NOI18N
    static final String CONNECTOR_SHMEM = "com.sun.jdi.SharedMemoryAttach"; // NOI18N

    static final String PROCESS_ARG_PID = "processId";          // NOI18N
    static final String SOCKET_ARG_HOST = "hostName";           // NOI18N
    static final String SOCKET_ARG_PORT = "port";               // NOI18N
    static final String SHMEM_ARG_NAME = "sharedMemoryName";    // NOI18N

    private static final RequestProcessor RP = new RequestProcessor(AttachConfigurations.class);

    private AttachConfigurations() {}

    public static CompletableFuture<Object> findConnectors() {
        return CompletableFuture.supplyAsync(() -> {
            return listAttachingConnectors();
        }, RP);
    }

    @Messages({"DESC_HostName=Name of host machine to connect to", "DESC_Port=Port number to connect to",
               "DESC_ShMem=Shared memory transport address at which the target VM is listening"})
    private static List<DebugConnector> listAttachingConnectors() {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager ();
        List<AttachingConnector> attachingConnectors = vmm.attachingConnectors();
        List<DebugConnector> connectors = new ArrayList<>(5);
        String type = "java8+";             // NOI18N
        for (AttachingConnector ac : attachingConnectors) {
            String connectorName = ac.name();
            Map<String, Connector.Argument> defaultArguments = ac.defaultArguments();
            DebugConnector connector;
            switch (connectorName) {
                case CONNECTOR_PROCESS:
                    connector = new DebugConnector(connectorName, NAME_ATTACH_PROCESS, type,
                            Collections.singletonList(PROCESS_ARG_PID),
                            Collections.singletonList("${command:" + Server.JAVA_FIND_DEBUG_PROCESS_TO_ATTACH + "}"),   // NOI18N
                            Collections.singletonList(""));
                    break;
                case CONNECTOR_SOCKET: {
                    String hostName = getArgumentOrDefault(defaultArguments.get("hostname"), "localhost");          // NOI18N
                    String port = getArgumentOrDefault(defaultArguments.get("port"), "8000"); // NOI18N
                    connector = new DebugConnector(connectorName, NAME_ATTACH_SOCKET, type,
                            Arrays.asList(SOCKET_ARG_HOST, SOCKET_ARG_PORT),
                            Arrays.asList(hostName, port),
                            Arrays.asList(Bundle.DESC_HostName(), Bundle.DESC_Port()));
                    break;
                }
                case CONNECTOR_SHMEM: {
                    String name = getArgumentOrDefault(defaultArguments.get("name"), "");       // NOI18N
                    connector = new DebugConnector(connectorName, NAME_ATTACH_SHMEM, type,
                            Collections.singletonList(SHMEM_ARG_NAME),
                            Collections.singletonList(name),
                            Collections.singletonList(Bundle.DESC_ShMem()));
                    break;
                }
                default: {
                    List<String> names = new ArrayList<>();
                    List<String> values = new ArrayList<>();
                    List<String> descriptions = new ArrayList<>();
                    for (Connector.Argument arg : defaultArguments.values()) {
                        if (arg.mustSpecify()) {
                            names.add(arg.name());
                            String value = arg.value();
                            values.add(value);
                            descriptions.add(arg.description());
                        }
                    }
                    connector = new DebugConnector(connectorName, NAME_ATTACH_BY + connectorName, type,
                            names, values, descriptions);
                }
            }
            connectors.add(connector);
        }
        connectors.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
        return connectors;
    }

    private static String getArgumentOrDefault(Connector.Argument arg, String def) {
        if (arg != null) {
            String value = arg.value();
            if (!value.isEmpty()) {
                return value;
            }
        }
        return def;
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

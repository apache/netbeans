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

import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.java.lsp.server.protocol.Server;
import org.openide.util.NbBundle;

/**
 * Attributes of an attach configuration. Based on {@link AttachingConnector},
 * we translate names and attributes of well known connectors for usability reasons.
 *
 * @author Martin Entlicher
 */
final class ConfigurationAttributes {

    private static final String CONNECTOR_PROCESS = "com.sun.jdi.ProcessAttach";    // NOI18N
    private static final String CONNECTOR_SOCKET = "com.sun.jdi.SocketAttach";      // NOI18N
    private static final String CONNECTOR_SHMEM = "com.sun.jdi.SharedMemoryAttach"; // NOI18N

    static final String PROCESS_ARG_PID = "processId";          // NOI18N
    static final String SOCKET_ARG_HOST = "hostName";           // NOI18N
    static final String SOCKET_ARG_PORT = "port";               // NOI18N
    static final String SHMEM_ARG_NAME = "sharedMemoryName";    // NOI18N

    private final AttachingConnector ac;
    private final String id;
    private final String name;
    private final String description;
    private final Map<String, ConfigurationAttribute> attributes = new LinkedHashMap<>();

    @NbBundle.Messages({"LBL_AttachToProcess=Attach to Process",
                        "LBL_AttachToPort=Attach to Port",
                        "LBL_AttachToShmem=Attach to Shared Memory",
                        "# {0} - connector name", "LBL_AttachBy=Attach by {0}",
                        "DESC_Process=Process Id of the debuggee",
                        "DESC_HostName=Name or IP address of the host machine to connect to",
                        "DESC_Port=Port number to connect to",
                        "DESC_ShMem=Shared memory transport address at which the target VM is listening"})
    ConfigurationAttributes(AttachingConnector ac) {
        this.ac = ac;
        String connectorName = ac.name();
        this.id = connectorName;
        this.description = ac.description();
        Map<String, Connector.Argument> defaultArguments = ac.defaultArguments();
        switch (connectorName) {
            case CONNECTOR_PROCESS:
                this.name = Bundle.LBL_AttachToProcess();
                attributes.put(PROCESS_ARG_PID, new ConfigurationAttribute("${command:" + Server.JAVA_FIND_DEBUG_PROCESS_TO_ATTACH + "}", "", true)); // NOI18N
                break;
            case CONNECTOR_SOCKET:
                this.name = Bundle.LBL_AttachToPort();
                String hostName = getArgumentOrDefault(defaultArguments.get("hostname"), "localhost"); // NOI18N
                String port = getArgumentOrDefault(defaultArguments.get("port"), "8000"); // NOI18N
                attributes.put(SOCKET_ARG_HOST, new ConfigurationAttribute(hostName, Bundle.DESC_HostName(), true));
                attributes.put(SOCKET_ARG_PORT, new ConfigurationAttribute(port, Bundle.DESC_Port(), true));
                break;
            case CONNECTOR_SHMEM:
                this.name = Bundle.LBL_AttachToShmem();
                String shmName = getArgumentOrDefault(defaultArguments.get("name"), ""); // NOI18N
                attributes.put(SHMEM_ARG_NAME, new ConfigurationAttribute(shmName, Bundle.DESC_ShMem(), true));
                break;
            default:
                this.name = Bundle.LBL_AttachBy(connectorName);
                for (Connector.Argument arg : defaultArguments.values()) {
                    if (arg.mustSpecify()) {
                        attributes.put(arg.name(), new ConfigurationAttribute(arg.value(), arg.description(), true));
                    }
                }
        }
        for (Connector.Argument arg : defaultArguments.values()) {
            if (!arg.mustSpecify()) {
                attributes.put(arg.name(), new ConfigurationAttribute(arg.value(), arg.description(), false));
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public AttachingConnector getConnector() {
        return ac;
    }

    public Map<String, ConfigurationAttribute> getAttributes() {
        return attributes;
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

    boolean areMandatoryAttributesIn(Set<String> names) {
        for (Map.Entry<String, ConfigurationAttribute> entry : attributes.entrySet()) {
            if (entry.getValue().isMustSpecify()) {
                if (!names.contains(entry.getKey())) {
                    return false;
                }
            }
        }
        return true;
    }

}

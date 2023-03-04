/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.project.connections.api;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.php.project.PhpPreferences;
import org.openide.util.Parameters;

/**
 * Remote preferences, for the remote type and with import enabled or disabled.
 * @see #getPreferences()
 */
public final class RemotePreferences {

    private static final Logger LOGGER = Logger.getLogger(RemotePreferences.class.getName());
    // do not change arbitrary - consult with layer's folder OptionsExport
    private static final String REMOTE_CONNECTIONS = "RemoteConnections"; // NOI18N
    private static final String GENERAL_PREFERENCES = "general"; // NOI18N

    private final String type;
    private final boolean importEnabled;


    private RemotePreferences(String type, boolean importEnabled) {
        Parameters.notNull("type", type);

        this.type = type;
        this.importEnabled = importEnabled;
    }

    /**
     * Create remote preferences for the given type and with import enabled or disabled.
     * @param type connection type, e.g. FTP or SFTP
     * @param importEnabled {@code true} if the import is enabled
     * @return remote preferences for the given type and with import enabled or disabled
     */
    public static RemotePreferences forType(String type, boolean importEnabled) {
        return new RemotePreferences(type, importEnabled);
    }

    /**
     * Get all server configurations of all types.
     * @return all server configurations of all types
     */
    public static Map<String, Map<String, String>> getServerConfigs() {
        Map<String, Map<String, String>> serverConfigs = new HashMap<>();
        Preferences remoteConnections = getServerConfigsPreferences();
        try {
            for (String name : remoteConnections.childrenNames()) {
                if (name.equals(RemotePreferences.GENERAL_PREFERENCES)) {
                    // ignore "general" node
                    continue;
                }
                Preferences node = remoteConnections.node(name);
                Map<String, String> value = new TreeMap<>();
                for (String key : node.keys()) {
                    value.put(key, node.get(key, null));
                }
                serverConfigs.put(name, value);
            }
        } catch (BackingStoreException bse) {
            LOGGER.log(Level.INFO, "Error while reading existing remote connections", bse);
        }
        return serverConfigs;
    }

    /**
     * Get preferences themselves.
     * @return the preferences themselves
     */
    public Preferences getPreferences() {
        return PhpPreferences.getPreferences(importEnabled).node(REMOTE_CONNECTIONS).node(GENERAL_PREFERENCES).node(type);
    }

    // XXX could be moved into "servers" subnode
    /**
     * Get root node of server configurations.
     * @return root node of server configurations
     */
    public static Preferences getServerConfigsPreferences() {
        return PhpPreferences.getPreferences(true).node(REMOTE_CONNECTIONS);
    }

}

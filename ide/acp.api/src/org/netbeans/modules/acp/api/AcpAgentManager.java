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
package org.netbeans.modules.acp.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * Manages agent configurations (CRUD), persisted via {@link NbPreferences}.
 * Agents are stored as indexed preferences entries and fire
 * {@link java.beans.PropertyChangeEvent}s when the list changes.
 * <p>
 * Obtain the singleton instance via {@link #getDefault()}.
 *
 * @since 1.0
 */
@ServiceProvider(service = AcpAgentManager.class)
public final class AcpAgentManager {

    private static final Logger LOG = Logger.getLogger(AcpAgentManager.class.getName());

    /** Property name fired when the agent list changes. */
    public static final String PROP_AGENTS = "agents"; // NOI18N

    private static final String PREFIX_AGENT = "agent."; // NOI18N
    private static final String KEY_NAME = ".name"; // NOI18N
    private static final String KEY_TRANSPORT_TYPE = ".transportType"; // NOI18N
    private static final String KEY_COMMAND = ".command"; // NOI18N
    private static final String KEY_URL = ".url"; // NOI18N
    private static final String KEY_WORKING_DIRECTORY = ".workingDirectory"; // NOI18N
    private static final String KEY_ENV_COUNT = ".envCount"; // NOI18N
    private static final String KEY_ENV_KEY = ".envKey."; // NOI18N
    private static final String KEY_ENV_VALUE = ".envValue."; // NOI18N
    private static final String KEY_AGENT_COUNT = "agentCount"; // NOI18N

    private static final String COMMAND_DELIMITER = "\u001F"; // unit separator // NOI18N

    private final Object lock = new Object();
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     * Returns the default instance of the agent manager.
     *
     * @return the agent manager singleton
     */
    public static AcpAgentManager getDefault() {
        AcpAgentManager mgr = Lookup.getDefault().lookup(AcpAgentManager.class);
        if (mgr == null) {
            mgr = new AcpAgentManager();
        }
        return mgr;
    }

    /**
     * Returns all configured agents.
     *
     * @return an unmodifiable list of agent descriptors
     */
    public List<AcpAgentDescriptor> getAgents() {
        synchronized (lock) {
            return loadAgents();
        }
    }

    private List<AcpAgentDescriptor> loadAgents() {
        Preferences prefs = getPreferences();
        int count = prefs.getInt(KEY_AGENT_COUNT, 0);
        List<AcpAgentDescriptor> result = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            AcpAgentDescriptor agent = loadAgent(prefs, i);
            if (agent != null) {
                result.add(agent);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Adds a new agent configuration. If an agent with the same name
     * already exists, this method does nothing.
     *
     * @param agent the agent descriptor to add
     */
    public void addAgent(AcpAgentDescriptor agent) {
        Objects.requireNonNull(agent, "agent must not be null");

        List<AcpAgentDescriptor> oldAgents;
        List<AcpAgentDescriptor> newAgents;
        synchronized (lock) {
            oldAgents = loadAgents();
            for (AcpAgentDescriptor existing : oldAgents) {
                if (existing.getName().equals(agent.getName())) {
                    LOG.log(Level.WARNING, "Agent already exists: {0}", agent.getName());
                    return;
                }
            }

            newAgents = new ArrayList<>(oldAgents);
            newAgents.add(agent);
            saveAllAgents(newAgents);
        }

        changeSupport.firePropertyChange(PROP_AGENTS, oldAgents, Collections.unmodifiableList(newAgents));
    }

    /**
     * Removes the agent configuration with the given name.
     *
     * @param name the name of the agent to remove
     */
    public void removeAgent(String name) {
        Objects.requireNonNull(name, "name must not be null");

        List<AcpAgentDescriptor> oldAgents;
        List<AcpAgentDescriptor> newAgents = new ArrayList<>();
        boolean found = false;
        synchronized (lock) {
            oldAgents = loadAgents();
            for (AcpAgentDescriptor agent : oldAgents) {
                if (agent.getName().equals(name)) {
                    found = true;
                } else {
                    newAgents.add(agent);
                }
            }
            if (found) {
                saveAllAgents(newAgents);
            }
        }

        if (found) {
            changeSupport.firePropertyChange(PROP_AGENTS, oldAgents, Collections.unmodifiableList(newAgents));
        }
    }

    /**
     * Updates an existing agent configuration. The agent is identified by
     * its name. If no agent with the given name exists, the agent is added.
     *
     * @param agent the updated agent descriptor
     */
    public void updateAgent(AcpAgentDescriptor agent) {
        Objects.requireNonNull(agent, "agent must not be null");

        List<AcpAgentDescriptor> oldAgents;
        List<AcpAgentDescriptor> newAgents = new ArrayList<>();
        synchronized (lock) {
            oldAgents = loadAgents();
            boolean found = false;
            for (AcpAgentDescriptor existing : oldAgents) {
                if (existing.getName().equals(agent.getName())) {
                    newAgents.add(agent);
                    found = true;
                } else {
                    newAgents.add(existing);
                }
            }

            if (!found) {
                newAgents.add(agent);
            }

            saveAllAgents(newAgents);
        }
        changeSupport.firePropertyChange(PROP_AGENTS, oldAgents, Collections.unmodifiableList(newAgents));
    }

    /**
     * Adds a property change listener.
     *
     * @param listener the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener.
     *
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    private AcpAgentDescriptor loadAgent(Preferences prefs, int index) {
        String prefix = PREFIX_AGENT + index;
        String name = prefs.get(prefix + KEY_NAME, null);
        if (name == null) {
            return null;
        }

        String transportStr = prefs.get(prefix + KEY_TRANSPORT_TYPE, AcpAgentDescriptor.TransportType.STDIO.name());
        AcpAgentDescriptor.TransportType transportType;
        try {
            transportType = AcpAgentDescriptor.TransportType.valueOf(transportStr);
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.WARNING, "Unknown transport type: {0}, defaulting to STDIO", transportStr);
            transportType = AcpAgentDescriptor.TransportType.STDIO;
        }

        AcpAgentDescriptor.Builder builder = new AcpAgentDescriptor.Builder(name, transportType);

        String commandStr = prefs.get(prefix + KEY_COMMAND, null);
        if (commandStr != null && !commandStr.isEmpty()) {
            builder.command(Arrays.asList(commandStr.split(COMMAND_DELIMITER)));
        }

        String url = prefs.get(prefix + KEY_URL, null);
        builder.url(url);

        String workingDir = prefs.get(prefix + KEY_WORKING_DIRECTORY, null);
        builder.workingDirectory(workingDir);

        int envCount = prefs.getInt(prefix + KEY_ENV_COUNT, 0);
        if (envCount > 0) {
            Map<String, String> envVars = new LinkedHashMap<>();
            for (int j = 0; j < envCount; j++) {
                String key = prefs.get(prefix + KEY_ENV_KEY + j, null);
                String value = prefs.get(prefix + KEY_ENV_VALUE + j, ""); // NOI18N
                if (key != null) {
                    envVars.put(key, value);
                }
            }
            builder.environmentVariables(envVars);
        }

        try {
            return builder.build();
        } catch (IllegalStateException ex) {
            LOG.log(Level.WARNING, "Invalid agent configuration at index {0}: {1}", new Object[]{index, ex.getMessage()});
            return null;
        }
    }

    private void saveAllAgents(List<AcpAgentDescriptor> agents) {
        Preferences prefs = getPreferences();

        // Clear existing entries
        try {
            String[] keys = prefs.keys();
            for (String key : keys) {
                if (key.startsWith(PREFIX_AGENT) || key.equals(KEY_AGENT_COUNT)) {
                    prefs.remove(key);
                }
            }
        } catch (BackingStoreException ex) {
            LOG.log(Level.WARNING, "Error clearing agent preferences", ex);
        }

        prefs.putInt(KEY_AGENT_COUNT, agents.size());

        for (int i = 0; i < agents.size(); i++) {
            saveAgent(prefs, i, agents.get(i));
        }

        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            LOG.log(Level.WARNING, "Error flushing agent preferences", ex);
        }
    }

    private void saveAgent(Preferences prefs, int index, AcpAgentDescriptor agent) {
        String prefix = PREFIX_AGENT + index;

        prefs.put(prefix + KEY_NAME, agent.getName());
        prefs.put(prefix + KEY_TRANSPORT_TYPE, agent.getTransportType().name());

        if (!agent.getCommand().isEmpty()) {
            prefs.put(prefix + KEY_COMMAND, String.join(COMMAND_DELIMITER, agent.getCommand()));
        }

        if (agent.getUrl() != null) {
            prefs.put(prefix + KEY_URL, agent.getUrl());
        }

        if (agent.getWorkingDirectory() != null) {
            prefs.put(prefix + KEY_WORKING_DIRECTORY, agent.getWorkingDirectory());
        }

        Map<String, String> envVars = agent.getEnvironmentVariables();
        prefs.putInt(prefix + KEY_ENV_COUNT, envVars.size());

        int j = 0;
        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            prefs.put(prefix + KEY_ENV_KEY + j, entry.getKey());
            prefs.put(prefix + KEY_ENV_VALUE + j, entry.getValue());
            j++;
        }
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(AcpAgentManager.class);
    }
}

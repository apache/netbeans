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

package org.netbeans.modules.j2ee.deployment.impl;

import javax.enterprise.deploy.spi.Target;

public class ServerString implements java.io.Serializable {

    private final String plugin;
    private final String instance;
    /** <i>NonNull</i> */
    private final String[] targets;
    private final transient ServerInstance serverInstance;
    private transient String[] theTargets;
    private static final long serialVersionUID = 923457209372L;

    protected ServerString(String plugin, String instance, String[] targets, ServerInstance serverInstance) {
        if (targets == null) {
            this.targets = new String[0];
        } else {
            this.targets = targets.clone();
        }
        this.plugin = plugin;
        this.instance = instance;
        this.serverInstance = serverInstance;
    }

    public ServerString(String plugin, String instance, String[] targets) {
        this(plugin, instance, targets, null);
    }

    public ServerString(ServerInstance instance) {
        this(instance.getServer().getShortName(), instance.getUrl(), null, instance);
    }

    public ServerString(ServerTarget target) {
        this(target.getInstance().getServer().getShortName(),
                target.getInstance().getUrl(), new String[] {target.getName()}, null);
    }

    public ServerString(ServerInstance instance, String targetName) {
        this(instance.getServer().getShortName(),
                instance.getUrl(),
                (targetName != null && ! "".equals(targetName.trim())) ? new String[] {targetName} : null,
                instance);
    }

    public String getPlugin() {
        return plugin;
    }

    public String getUrl() {
        return instance;
    }

    public String[] getTargets() {
        return getTargets(false);
    }

    /**
     * <i>This method can have ugly side effect of starting the server.</i>
     * 
     * @param concrete
     * @return
     */
    public String[] getTargets(boolean concrete) {
        if (!concrete || targets.length > 0) {
            return targets.clone();
        }

        if (theTargets != null) {
            return theTargets.clone();
        }

        ServerTarget[] serverTargets = getServerInstance().getTargets();
        theTargets = new String[serverTargets.length];
        for (int i = 0; i < theTargets.length; i++) {
            theTargets[i] = serverTargets[i].getName();
        }
        return theTargets.clone();
    }

    public Server getServer() {
        return ServerRegistry.getInstance().getServer(plugin);
    }

    public ServerInstance getServerInstance() {
        if (serverInstance != null) {
            return serverInstance;
        }
        return ServerRegistry.getInstance().getServerInstance(instance);
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Server ").append(plugin); // NOI18N
        buffer.append(" Instance ").append(instance); // NOI18N
        if (/*targets == null || */targets.length == 0) {
            buffer.append(" Targets none"); // NOI18N
        } else {
            buffer.append(" Targets ").append(targets.length); // NOI18N
        }
        return buffer.toString();
    }

    /**
     * <i>This method can have ugly side effect of starting the server.</i>
     * 
     * @return
     */
    public Target[] toTargets() {
        String[] targetNames = getTargets(true);
        Target[] ret = new Target[targetNames.length];
        for (int i = 0; i < targetNames.length; i++) {
            ret[i] = getServerInstance().getServerTarget(targetNames[i]).getTarget();
        }
        return ret;
    }
}

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
package org.netbeans.libs.jsch.agentproxy;

import com.jcraft.jsch.AgentConnector;
import com.jcraft.jsch.PageantConnector;
import com.jcraft.jsch.SSHAgentConnector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ondrej Vrabec
 */
public class ConnectorFactory {
    
    private static ConnectorFactory instance;
    private static final Logger LOG = Logger.getLogger(ConnectorFactory.class.getName());
    
    public enum ConnectorKind {
        ANY,
        PAGEANT,
        SSH_AGENT
    }
    
    private ConnectorFactory () {
        
    }
    
    public static synchronized ConnectorFactory getInstance ()  {
        if (instance == null) {
            instance = new ConnectorFactory();
        }
        return instance;
    }
    
    public AgentConnector createConnector(ConnectorKind preferredKind) {
        AgentConnector agentConnector = null;
        if (agentConnector == null) {
            try {
                if (preferredKind == ConnectorKind.ANY || preferredKind == ConnectorKind.SSH_AGENT) {
                    agentConnector = new SSHAgentConnector();
                    if (!agentConnector.isAvailable()) {
                        agentConnector = null;
                    }
                }
            } catch (Throwable ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }
        if (agentConnector == null) {
            try {
                if (preferredKind == ConnectorKind.ANY || preferredKind == ConnectorKind.PAGEANT) {
                    agentConnector = new PageantConnector();
                    if (!agentConnector.isAvailable()) {
                        agentConnector = null;
                    }
                }
            } catch (Throwable ex) {
                LOG.log(Level.FINE, null, ex);

            }
        }
        if (agentConnector != null) {
            LOG.log(Level.FINE, "AgentConnector: {0} / {1}", new Object[]{agentConnector.getName(), agentConnector.getClass().getName()});
        } else {
            LOG.log(Level.FINE, "AgentConnector: none found");
        }
        return agentConnector;
    }

}

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
package org.netbeans.libs.jsch.agentproxy;

import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.USocketFactory;
import com.jcraft.jsch.agentproxy.connector.PageantConnector;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;
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
    
    public Connector createConnector (ConnectorKind preferredKind) {
        Connector con = null;
        try {
            if ((preferredKind == ConnectorKind.ANY || preferredKind == ConnectorKind.SSH_AGENT)
                    && SSHAgentConnector.isConnectorAvailable()) {
                USocketFactory usf = new JNAUSocketFactory();
                con = new SSHAgentConnector(usf);
            }
        } catch (Throwable ex) {
            LOG.log(Level.FINE, null, ex);
        }
        try {
            if ((preferredKind == ConnectorKind.ANY || preferredKind == ConnectorKind.PAGEANT)
                    && PageantConnector.isConnectorAvailable()) {
                con = new PageantConnector();
            }
        } catch (Throwable ex) {
            LOG.log(Level.FINE, null, ex);
        }
        return con;
    }
    
}

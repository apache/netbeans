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

/*
 * ServerDebugInfo.java
 *
 */

package org.netbeans.modules.j2ee.deployment.plugins.api;

/**
 * Class to communicate the debugging information between plugin, server api and IDE.
 * @author Martin Grebac
 * @version 0.1
 */

public class ServerDebugInfo {

    public static final String TRANSPORT_SOCKET = "dt_socket";  //NOI18N
    public static final String TRANSPORT_SHMEM = "dt_shmem";    //NOI18N

    /**
     * Holds value of property transport - socket or shared memory.
     */
    private String transport = TRANSPORT_SOCKET;

    /**
     * Holds value of property host - where the target vm is running.
     */
    private String host = "localhost";                          //NOI18N
    
    /**
     * Holds value of property shmemName - shared memory name of the target vm.
     */
    private String shmemName = "";
    
    /**
     * Holds value of property port - port number of the target vm..
     */
    private int port;
    
    public ServerDebugInfo(String host, String shmemName) {
        setTransport(TRANSPORT_SHMEM);
        setHost(host);
        setShmemName(shmemName);
    }
    
    public ServerDebugInfo(String host, int port) {
        setTransport(TRANSPORT_SOCKET);
        setHost(host);
        setPort(port);
    }
    
    /**
     * Getter for property transport.
     * @return Value of property transport.
     */
    public String getTransport() {
        return this.transport;
    }
    
    /**
     * Setter for property transport.
     * @param transport New value of property transport.
     */
    public void setTransport(String transport) {
        this.transport = transport;
    }
    
    /**
     * Getter for property host.
     * @return Value of property host.
     */
    public String getHost() {
        return this.host;
    }
    
    /**
     * Setter for property host.
     * @param host New value of property host.
     */
    public void setHost(String host) {
        this.host = host;
    }
    
    /**
     * Getter for property shmemName.
     * @return Value of property shmemName.
     */
    public String getShmemName() {
        return this.shmemName;
    }
    
    /**
     * Setter for property shmemName.
     * @param shmemName New value of property shmemName.
     */
    public void setShmemName(String shmemName) {
        this.shmemName = shmemName;
    }
    
    /**
     * Getter for property port.
     * @return Value of property port.
     */
    public int getPort() {
        return this.port;
    }
    
    /**
     * Setter for property port.
     * @param port New value of property port.
     */
    public void setPort(int port) {
        this.port = port;
    }
    
}

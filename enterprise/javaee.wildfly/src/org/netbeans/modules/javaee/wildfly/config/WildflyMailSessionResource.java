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
package org.netbeans.modules.javaee.wildfly.config;

import java.util.Collections;
import java.util.Map;

/**
 *
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class WildflyMailSessionResource {

    private final String name;
    private String jndiName;
    private String userName;
    private String fromAddr;
    private String isDebug;
    private WildflySocket socket;
    private final Map<String, String> configuration;




    /**
     * Creates a new instance of MailSessionResource
     */
    public WildflyMailSessionResource(Map<String, String> configuration, String name) {
        this.name = name;
        this.configuration = configuration;
        this.socket = new WildflySocket();
    }

    public String getName() {
        return name;
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String value) {
        this.jndiName = value;
    }

    public String getHostName() {
        return socket.getHost();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String value) {
        this.userName = value;
    }

    public String getFromAddr() {
        return fromAddr;
    }

    public void setFromAddr(String value) {
        this.fromAddr = value;
    }

    public String getIsDebug() {
        return isDebug;
    }

    public void setIsDebug(String value) {
        this.isDebug = value;
    }

    public void setSocket(WildflySocket socket) {
        this.socket = socket;
    }

    public String getPort() {
        return String.valueOf(this.socket.getPort());
    }

    public Map<String, String> getConfiguration() {
        return Collections.unmodifiableMap(configuration);
    }
}

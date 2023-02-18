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

package org.netbeans.modules.httpserver;

import java.util.EventObject;
import java.net.InetAddress;

/**
 * This event is sent to access listeners to
 * ask them, whether the access to specified resource is
 * allowed.
 *
 * @author Jaroslav Tulach
 */
public class GrantAccessEvent extends EventObject {
    /** is access granted */
    private boolean granted = false;
    private final InetAddress clientAddress;
    private final String resource;

    /**
     * Creates new AccessEvent. Used only in this package by
     * the HttpServer to create new access event when a resource
     * is requested.
     *
     * @param httpServer the server
     */
    GrantAccessEvent(Object source, InetAddress clientAddress, String resource) {
        super (source);
        this.clientAddress = clientAddress;
        this.resource = resource;
    }

    /**
     * The Inet address that initiated the connection.
     *
     * @return the inet address
     */
    public InetAddress getClientAddress () {
        return clientAddress;
    }

    /**
     * The resource to which access is requested
     */
    public String getResource() {
        return resource;
    }

    /**
     * Allows access. The listener can use this method to grant
     * access the client and resource.
     */
    public void grantAccess () {
        granted = true;
    }

    /**
     * Getter to test whether the access has been granted.
     *
     * @return true if a listener granted the access
     */
    boolean isGranted () {
        return granted;
    }
}

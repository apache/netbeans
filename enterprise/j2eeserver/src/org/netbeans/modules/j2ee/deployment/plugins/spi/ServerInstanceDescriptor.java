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

package org.netbeans.modules.j2ee.deployment.plugins.spi;

/**
 * Descriptor providing extra (and optional) information about the server instance.
 *
 * @author Petr Hejl
 * @since 1.46
 */
public interface ServerInstanceDescriptor {

    /**
     * Returns the HTTP port of the server.
     *
     * @return the HTTP port of the server
     */
    int getHttpPort();

    /**
     * Returns the hostname of the server. Returned name is usable to reach
     * the server from the computer where IDE runs.
     *
     * @return the hostname of the server
     */
    String getHostname();

    /**
     * Returns <code>true</code> if the server is installed locally,
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if the server is installed locally
     */
    boolean isLocal();

}

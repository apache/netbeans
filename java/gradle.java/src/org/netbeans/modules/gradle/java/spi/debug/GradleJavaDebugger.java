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
package org.netbeans.modules.gradle.java.spi.debug;

/**
 * Instance would be found in the project's lookup allow to attach debugger
 * to the project. This is essentially the same as MavenDebugger for Maven
 * projects.
 * 
 * @since 1.2
 */
public interface GradleJavaDebugger {

    /**
     * Attaches a Java Debugger session to the project.
     * 
     * @param name the name of the session.
     * @param transport the transport to attach to the JVM
     * @param host the host where the debugee process is running
     * @param address the address (usually port number) where the debugee process is listening.
     * @throws Exception if the debug session cannot be started.
     */
    void attachDebugger(String name,
            final String transport,
            final String host,
            final String address) throws Exception;

}

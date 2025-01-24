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
package org.netbeans.modules.docker.api;

import java.io.IOException;
import java.util.List;
import org.netbeans.modules.docker.DockerConfig;

/**
 *
 * @author Petr Hejl
 */
public final class CredentialsManager {

    private static final CredentialsManager INSTANCE = new CredentialsManager();

    private CredentialsManager() {
        super();
    }

    public static CredentialsManager getDefault() {
        return INSTANCE;
    }

    public List<Credentials> getAllCredentials() throws IOException {
        //assert !SwingUtilities.isEventDispatchThread();
        return DockerConfig.getDefault().getAllCredentials();
    }

    public Credentials getCredentials(String registry) throws IOException {
        //assert !SwingUtilities.isEventDispatchThread();
        return DockerConfig.getDefault().getCredentials(registry);
    }

    public void setCredentials(Credentials credentials) throws IOException {
        //assert !SwingUtilities.isEventDispatchThread();
        DockerConfig.getDefault().setCredentials(credentials);
    }

    public void removeCredentials(Credentials credentials) throws IOException {
        //assert !SwingUtilities.isEventDispatchThread();
        DockerConfig.getDefault().removeCredentials(credentials);
    }
}

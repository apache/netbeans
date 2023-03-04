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
package org.netbeans.modules.maven.embedder.impl;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnector;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.transfer.NoRepositoryConnectorException;

/**
 *
 * @author mkleint
 */
public final class OfflineConnector implements RepositoryConnectorFactory {

    @Override
    public RepositoryConnector newInstance(RepositorySystemSession session, RemoteRepository repository) throws NoRepositoryConnectorException {
        // Throwing NoRepositoryConnectorException is ineffective because DefaultRemoteRepositoryManager will just skip to WagonRepositoryConnectorFactory.
        // (No apparent way to suppress WRCF from the Plexus container; using "wagon" as the role hint does not work.)
        // Could also return a no-op RepositoryConnector which would perform no downloads.
        // But we anyway want to ensure that related code is consistently setting the offline flag on all Maven structures that require it.
        // Throwing NoRepositoryConnectorException is ineffective because DefaultRemoteRepositoryManager will just skip to WagonRepositoryConnectorFactory.
        // (No apparent way to suppress WRCF from the Plexus container; using "wagon" as the role hint does not work.)
        // Could also return a no-op RepositoryConnector which would perform no downloads.
        // But we anyway want to ensure that related code is consistently setting the offline flag on all Maven structures that require it.
        throw new OfflineOperationError();
    }
    
    @Override
    public float getPriority() {
        return Float.MAX_VALUE;
    }
    
}

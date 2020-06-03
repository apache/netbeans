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

package org.netbeans.modules.git.remote.cli.jgit;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRepositoryState;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public final class JGitRepository {
    private final VCSFileProxy location;
    private final AtomicInteger counter = new AtomicInteger();
    private final JGitConfig config;
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    public JGitRepository (VCSFileProxy location) {
        this.location = location;
        config = new JGitConfig(location);
    }

    public synchronized void increaseClientUsage () throws GitException {
        counter.incrementAndGet();
    }

    public synchronized void decreaseClientUsage () {
        counter.decrementAndGet();
    }

    public VCSFileProxy getLocation() {
        return location;
    }

    public VCSFileProxy getMetadataLocation() {
        return VCSFileProxy.createFileProxy(location, ".git");
    }

    public GitRepositoryState getRepositoryState() {
        return null;
    }
    
    public JGitConfig getConfig(){
        synchronized(loaded) {
            if (!loaded.get()) {
                config.load();
                loaded.set(true);
            }
            return config;
        }
    }

}

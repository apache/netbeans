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

package org.netbeans.modules.git.ui.repository.remote;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;

/**
 *
 * @author ondra
 */
public class RemoteConfig {
    private final String remoteName;
    private final LinkedList<String> pushRefSpecs;
    private final LinkedList<String> pushUris;
    private final LinkedList<String> fetchRefSpecs;
    private final LinkedList<String> fetchUris;

    public RemoteConfig (String remoteName) {
        this(remoteName, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList());
    }

    private RemoteConfig (String remoteName, List<String> fetchUris, List<String> pushUris, List<String> fetchRefSpecs, List<String> pushRefSpecs) {
        this.remoteName = remoteName;
        this.fetchUris = new LinkedList<String>(fetchUris);
        this.fetchRefSpecs = new LinkedList<String>(fetchRefSpecs);
        this.pushUris = new LinkedList<String>(pushUris);
        this.pushRefSpecs = new LinkedList<String>(pushRefSpecs);
    }

    public RemoteConfig (GitRemoteConfig originalConfig) {
        this(originalConfig.getRemoteName(), originalConfig.getUris(), originalConfig.getPushUris(), originalConfig.getFetchRefSpecs(), originalConfig.getPushRefSpecs());
    }

    public String getRemoteName () {
        return remoteName;
    }

    public List<String> getUris () {
        return Collections.unmodifiableList(fetchUris);
    }

    public List<String> getPushUris () {
        return Collections.unmodifiableList(pushUris);
    }

    public List<String> getFetchRefSpecs () {
        return Collections.unmodifiableList(fetchRefSpecs);
    }

    public List<String> getPushRefSpecs () {
        return Collections.unmodifiableList(pushRefSpecs);
    }

    public static RemoteConfig createUpdatableRemote (File repository, String remoteName) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        Map<String, GitRemoteConfig> remotes = info.getRemotes();
        RemoteConfig config;
        if (remotes.containsKey(remoteName)) {
            config = new RemoteConfig(remotes.get(remoteName));
        } else {
            config = new RemoteConfig(remoteName);
        }
        return config;
    }

    public void setFetchUris (List<String> uris) {
        update(fetchUris, uris);            
    }

    public void setFetchRefSpecs (List<String> refs) {
        update(fetchRefSpecs, refs);
    }

    private void update (LinkedList<String> toUpdate, List<String> newValues) {
        toUpdate.clear();
        toUpdate.addAll(newValues);
    }

    GitRemoteConfig toGitRemote () {
        return new GitRemoteConfig(getRemoteName(),
                getUris(),
                getPushUris(),
                getFetchRefSpecs(),
                getPushRefSpecs());
    }
}

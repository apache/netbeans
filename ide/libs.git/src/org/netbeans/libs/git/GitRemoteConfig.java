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

package org.netbeans.libs.git;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jgit.transport.RemoteConfig;

/**
 * Represents the <code>[remote]</code> area of a gitconfig file.
 * Contains and provides information about a remote git repository, its push and fetch URIs
 * and fetch and push reference specifications. See the help pages for git config for more information.
 * 
 * @author Ondra Vrabec
 */
public final class GitRemoteConfig {
    
    private final List<String> uris;
    private final List<String> pushUris;
    private final List<String> fetchSpecs;
    private final List<String> pushSpecs;
    private final String remoteName;

    /**
     * @param remoteName alias for the remote repository
     * @param uris list of URIs the remote repository is represented by
     * @param pushUris list of URIs that should be used when pushing to the repository
     * @param fetchSpecs list of fetch reference specifications that come in question when fetching from the repository
     * @param pushSpecs list of push reference specifications that come in question when pushing to the repository
     */
    public GitRemoteConfig (String remoteName, List<String> uris, List<String> pushUris, List<String> fetchSpecs, List<String> pushSpecs) {
        this.remoteName = remoteName;
        this.uris = uris;
        this.pushUris = pushUris;
        this.fetchSpecs = fetchSpecs;
        this.pushSpecs = pushSpecs;
    }

    /**
     * @return remote's name
     */
    public String getRemoteName () {
        return remoteName;
    }

    /**
     * @return list of URIs known to this remote.
     */
    public List<String> getUris () {
        return Collections.unmodifiableList(uris);
    }

    /**
     * @return list of push-only URIs known to this remote.
     */
    public List<String> getPushUris () {
        return Collections.unmodifiableList(pushUris);
    }

    /**
     * @return list of specs used when fetching.
     */
    public List<String> getFetchRefSpecs () {
        return Collections.unmodifiableList(fetchSpecs);
    }

    /**
     * @return list of specs used when pushing.
     */
    public List<String> getPushRefSpecs () {
        return Collections.unmodifiableList(pushSpecs);
    }

    private static List<String> getAsStrings (List<? extends Object> list) {
        Set<String> set = new LinkedHashSet<String>();
        for (Object elem : list) {
            set.add(elem.toString());
        }
        return new ArrayList<String>(set);
    }
    
    static GitRemoteConfig fromRemoteConfig (RemoteConfig config) {
        return new GitRemoteConfig(config.getName(),
                getAsStrings(config.getURIs()),
                getAsStrings(config.getPushURIs()),
                getAsStrings(config.getFetchRefSpecs()),
                getAsStrings(config.getPushRefSpecs()));
    }

}

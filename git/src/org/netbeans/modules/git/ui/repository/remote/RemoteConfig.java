/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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

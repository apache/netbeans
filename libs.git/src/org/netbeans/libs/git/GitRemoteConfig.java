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

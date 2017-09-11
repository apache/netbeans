/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.libs.git.jgit.commands;

import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class SetRemoteCommand extends GitCommand {
    private final GitRemoteConfig remote;
    
    private static final String KEY_URL = "url"; //NOI18N
    private static final String KEY_PUSHURL = "pushurl"; //NOI18N
    private static final String KEY_FETCH = "fetch"; //NOI18N
    private static final String KEY_PUSH = "push"; //NOI18N
    
    public SetRemoteCommand (Repository repository, GitClassFactory gitFactory, GitRemoteConfig remoteConfig, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.remote = remoteConfig;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        StoredConfig config = repository.getConfig();
        boolean finished = false;
        try {
            config.unset(ConfigConstants.CONFIG_REMOTE_SECTION, remote.getRemoteName(), KEY_URL);
            config.unset(ConfigConstants.CONFIG_REMOTE_SECTION, remote.getRemoteName(), KEY_PUSHURL);
            config.unset(ConfigConstants.CONFIG_REMOTE_SECTION, remote.getRemoteName(), KEY_FETCH);
            config.unset(ConfigConstants.CONFIG_REMOTE_SECTION, remote.getRemoteName(), KEY_PUSH);
            RemoteConfig cfg = new RemoteConfig(config, remote.getRemoteName());
            for (String uri : remote.getUris()) {
                cfg.addURI(new URIish(uri));
            }
            for (String uri : remote.getPushUris()) {
                cfg.addPushURI(new URIish(uri));
            }
            for (String spec : remote.getFetchRefSpecs()) {
                cfg.addFetchRefSpec(new RefSpec(spec));
            }
            for (String spec : remote.getPushRefSpecs()) {
                cfg.addPushRefSpec(new RefSpec(spec));
            }
            cfg.update(config);
            config.save();
            finished = true;
        } catch (Exception ex) {
            throw new GitException(ex);
        } finally {
            if (!finished) {
                try {
                    if (config instanceof FileBasedConfig) {
                        FileBasedConfig fileConfig = (FileBasedConfig) config;
                        fileConfig.clear();
                    }
                    config.load();
                } catch (Exception e) {

                }
            }
        }
    }

    @Override
    protected String getCommandDescription () {
        return new StringBuilder("setting up remote: ").append(remote.getRemoteName()).toString(); //NOI18N
    }
}

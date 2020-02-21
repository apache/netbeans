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
package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRemoteConfig;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class GetRemotesCommand extends GitCommand {
    public static final boolean KIT = false;
    private final ProgressMonitor monitor;
    private Map<String, GitRemoteConfig> remotes;
    
    public GetRemotesCommand (JGitRepository repository, GitClassFactory gitFactory, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
    }
    
    public Map<String, GitRemoteConfig> getRemotes () {
        return remotes;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "remote"); //NOI18N
        addArgument(0, "-v"); //NOI18N
    }

    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            remotes = new LinkedHashMap<>();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseRemoteOutput(output);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    parseAddError(error);
                }
                
            }.runCLI();
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    
    private void parseRemoteOutput(String output) {
        //$ git remote -v
        //origin	https://github.com/git/git (fetch)
        //origin	https://github.com/git/git (push)
        Map<String, RemoteContainer> list = new LinkedHashMap<>();
        for (String line : output.split("\n")) { //NOI18N
            if (!line.isEmpty()) {
                line = line.replace('\t', ' ').trim();
                String[] s = line.split(" ");
                String remoteName = s[0];
                RemoteContainer conf = list.get(remoteName);
                if (conf == null) {
                    conf = new RemoteContainer();
                    list.put(remoteName, conf);
                }
                if (s.length == 3) {
                    if ("(fetch)".equals(s[2])) {
                        conf.uris.add(s[1]);
                    } else if ("(push)".equals(s[2])) {
                        conf.pushUris.add(s[1]);
                    }  
                }
            }
        }
        for(Map.Entry<String, RemoteContainer> e : list.entrySet()) {
            GitRemoteConfig fromConfig = new GitRemoteConfig(getRepository().getConfig(), e.getKey());
            final RemoteContainer value = e.getValue();
            value.fetchSpecs =  fromConfig.getFetchRefSpecs();
            value.pushSpecs = fromConfig.getPushRefSpecs();
            if (value.uris.isEmpty()) {
                value.uris = fromConfig.getUris();
            }
            if (value.pushUris.isEmpty()) {
                value.uris = fromConfig.getPushUris();
            }
            GitRemoteConfig conf = new GitRemoteConfig(e.getKey(), value.uris, value.pushUris, value.fetchSpecs, value.pushSpecs);
            remotes.put(e.getKey(), conf);
        }
    }
    
    private void parseAddError(String error) {
        processMessages(error);
    }
    
    private static final class RemoteContainer {
        List<String> uris = new ArrayList<>();
        List<String> pushUris = new ArrayList<>();
        List<String> fetchSpecs = new ArrayList<>();
        List<String> pushSpecs = new ArrayList<>();
    }

}

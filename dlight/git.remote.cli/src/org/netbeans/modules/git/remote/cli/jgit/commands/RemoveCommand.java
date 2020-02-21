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

package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.jgit.Utils;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class RemoveCommand extends GitCommand {
    public static final boolean KIT = false;
    private final VCSFileProxy[] roots;
    private final FileListener listener;
    private final ProgressMonitor monitor;
    private final boolean cached;

    public RemoveCommand (JGitRepository repository, GitClassFactory gitFactory, VCSFileProxy[] roots, boolean cached, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.listener = listener;
        this.monitor = monitor;
        this.cached = cached;
    }

    @Override
    protected boolean prepareCommand() throws GitException {
        boolean retval = super.prepareCommand();
        if (retval && roots.length == 0) {
            retval = false;
            monitor.notifyWarning(EMPTY_ROOTS);
        }
        return retval;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "rm"); //NOI18N
        addArgument(0, "--ignore-unmatch");
        addArgument(0, "-r"); //NOI18N
        if (cached) {
            addArgument(0, "--cached"); //NOI18N
        } else {
            addArgument(0, "--force"); //NOI18N
        }
        addArgument(0, "--"); //NOI18N
        addFiles(0, roots);
        //addExistingFiles(0,roots);
    }
    
    
    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseRemoveOutput(output);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    super.errorParser(error); //To change body of generated methods, choose Tools | Templates.
                }
                
            }.runCLI();
            if (!cached) {
                for(VCSFileProxy root : roots) {
                    VCSFileProxySupport.delete(root);
                }
            }
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    
    private void parseRemoveOutput(String output) {
        //rm 'folder1/file1'
        //rm 'folder1/file2'
        //rm 'folder1/folder2/file3'
        Set<VCSFileProxy> parents = new HashSet<VCSFileProxy>();
        for (String line : output.split("\n")) { //NOI18N
            line = line.trim();
            if (line.startsWith("rm '") && line.endsWith("'")) {
                String file = line.substring(4, line.length()-1);
                VCSFileProxy path = VCSFileProxy.createFileProxy(getRepository().getLocation(), file);
                if (file.indexOf('/') > 0) {
                    parents.add(path.getParentFile());
                }
                listener.notifyFile(path, file);
            }
        }
        for(VCSFileProxy parent : parents) {
            if (!parent.exists()) {
                listener.notifyFile(parent, Utils.getRelativePath(getRepository().getLocation(), parent));
            }
        }
    }
}

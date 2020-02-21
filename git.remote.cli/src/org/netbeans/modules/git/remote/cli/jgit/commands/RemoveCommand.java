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

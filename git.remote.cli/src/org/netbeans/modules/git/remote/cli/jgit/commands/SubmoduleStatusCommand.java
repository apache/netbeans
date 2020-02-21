/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitSubmoduleStatus;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class SubmoduleStatusCommand extends GitCommand {
    
    private final VCSFileProxy[] roots;
    private final LinkedHashMap<VCSFileProxy, GitSubmoduleStatus> statuses;
    private final ProgressMonitor monitor;

    public SubmoduleStatusCommand (JGitRepository repository, GitClassFactory classFactory,
            VCSFileProxy[] roots, ProgressMonitor monitor) {
        super(repository, classFactory, monitor);
        this.roots = roots;
        this.monitor = monitor;
        statuses = new LinkedHashMap<>();
    }

    public Map<VCSFileProxy, GitSubmoduleStatus> getStatuses () {
        return statuses;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "submodule"); //NOI18N
        addArgument(0, "status"); //NOI18N
        addFiles(0, roots);
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
                    parseStatusOutput(output);
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
    
    private void parseStatusOutput(String output) {
        //"-636aae5b92efef302398bd5e84219dba80e8fe06 submodules/submodule1"
        for (String line : output.split("\n")) { //NOI18N
            if(line.indexOf(' ') >=0) {
                char c = line.charAt(0);
                line= line.substring(1);
                String[] s = line.split(" ");
                if (s.length == 2) {
                    VCSFileProxy file = VCSFileProxy.createFileProxy(getRepository().getLocation(), s[1]);
                    GitSubmoduleStatus.StatusType st = GitSubmoduleStatus.StatusType.INITIALIZED;
                    switch(c) {
                        case '-'://submodule is not initialized
                            st = GitSubmoduleStatus.StatusType.UNINITIALIZED;
                            break;
                        case '+'://the currently checked out submodule commit does not match the SHA-1 found in the index of the containing repository
                            st = GitSubmoduleStatus.StatusType.REV_CHECKED_OUT;
                            break;
                        case 'U'://submodule has merge conflicts
                            st = GitSubmoduleStatus.StatusType.MISSING;
                            break;
                    }
                    statuses.put(file, getClassFactory().createSubmoduleStatus(st, file));
                }
            }
        }
    }
    
}

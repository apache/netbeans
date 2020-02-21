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

import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class CompareCommand extends GitCommand {
    private final LinkedHashMap<VCSFileProxy, GitRevisionInfo.GitFileInfo> statuses;
    private final VCSFileProxy[] roots;
    private final String revisionFirst;
    private final String revisionSecond;
    private final ProgressMonitor monitor;

    public CompareCommand (JGitRepository repository, String revisionFirst, String revisionSecond, VCSFileProxy[] roots,
            GitClassFactory gitFactory, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.revisionFirst = revisionFirst;
        this.revisionSecond = revisionSecond;
        statuses = new LinkedHashMap<VCSFileProxy, GitRevisionInfo.GitFileInfo>();
        this.monitor = monitor;
    }
    
    @Override
    protected boolean prepareCommand () throws GitException {
        final boolean exists = getRepository().getMetadataLocation().exists();
        if (exists) {
            prepare();
        }
        return exists;
    }
    
    public Map<VCSFileProxy, GitRevisionInfo.GitFileInfo> getFileDifferences () {
        return statuses;
    }

    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(2);
        super.prepare();
        addArgument(0, "diff"); //NOI18N
        addArgument(0, "--raw"); //NOI18N
        addArgument(0, "--no-renames"); //NOI18N
        addArgument(0, revisionFirst);
        addArgument(0, revisionSecond);
        addArgument(0, "--"); //NOI18N
        addFiles(0, roots);
        addArgument(1, "diff"); //NOI18N
        addArgument(1, "--raw"); //NOI18N
        addArgument(1, "--find-renames"); //NOI18N
        addArgument(1, revisionFirst);
        addArgument(1, revisionSecond);
        addArgument(1, "--"); //NOI18N
        addFiles(1, roots);
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
                    parseDiffOutput(output, statuses);
                }

            }.runCLI();
            boolean hasDeleted = false;
            for(GitRevisionInfo.GitFileInfo info : statuses.values()) {
                if (info.getStatus() == GitRevisionInfo.GitFileInfo.Status.REMOVED) {
                    hasDeleted = true;
                    break;
                }
            }
            if (hasDeleted) {
                new Runner(canceled, 1){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseDiffOutput(output, statuses);
                    }

                }.runCLI();
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
    
    private void parseDiffOutput(String output, LinkedHashMap<VCSFileProxy, GitRevisionInfo.GitFileInfo> statuses) {
        //:100644 100644 ee73c61... b1b7161... M	file
        //
        //:100644 000000 7a65a3d... 0000000... D	file
        //:000000 100644 0000000... 7a65a3d... A	file2
        //
        //:100644 100644 7a65a3d... 7a65a3d... R100	file	file2
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith(":")) {
                String[] s = line.split("\\s");
                if (s.length == 6) {
                    String file = s[s.length-1];
                    String st = s[s.length-2].substring(0, 1);
                    GitRevisionInfo.GitFileInfo.Status gitSt =  GitRevisionInfo.GitFileInfo.Status.UNKNOWN;
                    if ("A".equals(st)) {
                        gitSt =  GitRevisionInfo.GitFileInfo.Status.ADDED;
                    } else if ("M".equals(st)) {
                        gitSt =  GitRevisionInfo.GitFileInfo.Status.MODIFIED;
                    } else if ("R".equals(st)) {
                        gitSt =  GitRevisionInfo.GitFileInfo.Status.RENAMED;
                    } else if ("C".equals(st)) {
                        gitSt =  GitRevisionInfo.GitFileInfo.Status.COPIED;
                    } else if ("D".equals(st)) {
                        gitSt =  GitRevisionInfo.GitFileInfo.Status.REMOVED;
                    }
                    VCSFileProxy f = VCSFileProxy.createFileProxy(getRepository().getLocation(), file);
                    statuses.put(f, getClassFactory().createFileInfo(f, file, gitSt, null, null));
                } else if (s.length == 7) {
                    String fileTo = s[s.length-1];
                    String fileFrom = s[s.length-2];
                    GitRevisionInfo.GitFileInfo.Status gitSt =  GitRevisionInfo.GitFileInfo.Status.RENAMED;
                    VCSFileProxy f = VCSFileProxy.createFileProxy(getRepository().getLocation(), fileTo);
                    VCSFileProxy fOld = VCSFileProxy.createFileProxy(getRepository().getLocation(), fileFrom);
                    statuses.put(f, getClassFactory().createFileInfo(f, fileTo, gitSt, fOld, fileFrom));
                }
                continue;
            }
        }
    }
    
}

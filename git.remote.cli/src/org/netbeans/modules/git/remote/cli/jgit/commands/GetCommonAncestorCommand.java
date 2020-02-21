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

import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo.GitRevCommit;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class GetCommonAncestorCommand extends GitCommand {
    public static final boolean KIT = false;
    private final String[] revisions;
    private GitRevisionInfo revision;
    private final Revision revisionPlaseHolder;
    private final ProgressMonitor monitor;

    public GetCommonAncestorCommand (JGitRepository repository, GitClassFactory gitFactory, String[] revisions, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.revisions = revisions;
        this.monitor = monitor;
        revisionPlaseHolder = new Revision();
    }
    
    public GitRevisionInfo getRevision () {
        return revision;
    }
    
    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(2);
        super.prepare();
        addArgument(0, "merge-base"); //NOI18N
        for (String s : revisions) {
            addArgument(0, s);
        }
        addArgument(1, "log"); //NOI18N
        addArgument(1, "--raw"); //NOI18N
        addArgument(1, "--pretty=raw"); //NOI18N
        addArgument(1, "-1"); //NOI18N
        addArgument(1, revisionPlaseHolder); //NOI18N
    }
    
    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            final GitRevCommit status = new GitRevCommit();
            if (revisions.length != 1) {
                new Runner(canceled, 0){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseCommit(output, status);
                    }
                }.runCLI();
            } else {
                status.revisionCode = revisions[0];
            }
            
            if (status.revisionCode != null) {
                revisionPlaseHolder.setContent(status.revisionCode);
                new Runner(canceled, 1){

                    @Override
                    public void outputParser(String output) throws GitException {
                        CommitCommand.parseLog(output, status);
                    }
                }.runCLI();
            }
            if (canceled.canceled()) {
                return;
            }
            revision = getClassFactory().createRevisionInfo(status, getRepository());
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    
    private void parseCommit(String output, GitRevCommit status) {
        for (String line : output.split("\n")) { //NOI18N
            line = line.trim();
            if (!line.isEmpty()) {
                status.revisionCode = line;
            }
        }
    }
}

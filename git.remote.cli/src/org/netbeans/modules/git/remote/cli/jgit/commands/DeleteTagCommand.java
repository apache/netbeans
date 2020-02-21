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
package org.netbeans.modules.git.remote.cli.jgit.commands;

import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRefUpdateResult;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class DeleteTagCommand extends GitCommand {
    private final String tagName;
    private GitRefUpdateResult result;
    private final ProgressMonitor monitor;

    public DeleteTagCommand (JGitRepository repository, GitClassFactory gitFactory, String tagName, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.tagName = tagName;
        this.monitor = monitor;
    }

    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "tag"); //NOI18N
        addArgument(0, "-d"); //NOI18N
        addArgument(0, tagName);
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
                    parseTagOutput(output);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    //TODO:
                    //switch (deleteResult) {
                    //    case IO_FAILURE:
                    //    case LOCK_FAILURE:
                    //    case REJECTED:
                    //        throw new GitException.RefUpdateException("Cannot delete tag " + tagName, GitRefUpdateResult.valueOf(deleteResult.name()));
                    //}
                    super.errorParser(error);
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
    
    private void parseTagOutput(String output) {
        //Deleted tag 'tag-name' (was 6e5965e)
        //for (String line : output.split("\n")) { //NOI18N
        //    if (line.startsWith("Deleted tag")) {
        //        String s = line.substring(11).trim();
        //        if (s.startsWith("'")) {
        //            int i = s.indexOf('\'',1);
        //            if (i > 0) {
        //                String name = s.substring(1,i);
        //                String[] a = s.split("\\s");
        //                String rev = a[a.length-1];
        //                if (rev.endsWith(")")) {
        //                    rev = rev.substring(0, rev.length()-1);
        //                }
        //                TagContainer tagContainer = new TagContainer();
        //                tagContainer.name = name;
        //                tagContainer.objectId = rev;
        //            }
        //        }
        //        continue;
        //    }
        //}
    }
}

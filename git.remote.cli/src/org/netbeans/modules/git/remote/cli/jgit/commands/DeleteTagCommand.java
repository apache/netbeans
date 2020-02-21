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

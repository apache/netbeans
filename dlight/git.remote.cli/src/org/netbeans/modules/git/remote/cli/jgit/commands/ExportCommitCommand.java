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

import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class ExportCommitCommand extends GitCommand {
    private final ProgressMonitor monitor;
    private final OutputStream out;
    private final FileListener listener;
    private final String revisionStr;
    
    private static final char NL = '\n';

    public ExportCommitCommand (JGitRepository repository, GitClassFactory gitFactory, String revisionStr, OutputStream out, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
        this.listener = listener;
        this.out = out;
        this.revisionStr = revisionStr;
    }

    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "format-patch"); //NOI18N
        addArgument(0, "--no-stat"); //NOI18N
        addArgument(0, "--stdout");
        addArgument(0, "--keep-subject");
        addArgument(0, "-1"); //NOI18N
        addArgument(0, revisionStr);
    }

    @Override
    protected void run() throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    try {
                        for(int i = 0; i < output.length(); i++)  {
                            out.write(output.charAt(i));
                        }
                    } catch (Exception e) {
                        throw new GitException(e);
                    }
                }
            }.runCLI();
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}

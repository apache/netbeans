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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitObjectType;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.jgit.Utils;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.remotefs.versioning.api.RemoteVcsSupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.Exceptions;

/**
 *
 */
public class CatCommand extends GitCommand {
    public static final boolean KIT = false;
    private final String revision;
    private final VCSFileProxy file;
    private final OutputStream os;
    private final ProgressMonitor monitor;
    private String relativePath;
    private boolean found;
    private final boolean fromRevision;
    private final int stage;

    public CatCommand (JGitRepository repository, GitClassFactory gitFactory, VCSFileProxy file, String revision, OutputStream out, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.file = file;
        this.revision = revision;
        this.os = out;
        this.monitor = monitor;
        this.fromRevision = true;
        this.stage = 0;
    }

    public CatCommand (JGitRepository repository, GitClassFactory gitFactory, VCSFileProxy file, int stage, OutputStream out, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.file = file;
        this.revision = null;
        this.os = out;
        this.monitor = monitor;
        this.fromRevision = false;
        this.stage = stage;
    }

    @Override
    protected boolean prepareCommand() throws GitException {
        boolean retval = super.prepareCommand();
        if (retval) {
            relativePath = Utils.getRelativePath(getRepository().getLocation(), file);
            if (relativePath.isEmpty()) {
                String message = MessageFormat.format(Utils.getBundle(CatCommand.class).getString("MSG_Error_CannotCatRoot"), file); //NOI18N
                monitor.preparationsFailed(message);
                throw new GitException(message);
            }
        }
        return retval;
    }
    
    public boolean foundInRevision () {
        return found;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        if (fromRevision) {
            addArgument(0, "show"); //NOI18N
            String relPath = Utils.getRelativePath(getRepository().getLocation(), file);
            addArgument(0, revision+":"+relPath); //NOI18N
        } else {
            addArgument(0, "show"); //NOI18N
            String relPath = Utils.getRelativePath(getRepository().getLocation(), file);
            if (stage == 0) {
                addArgument(0, ":"+relPath); //NOI18N
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
    
    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        found = false;
        try {
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    BufferedWriter bw = null;
                    try {
                        found = true;
                        Charset encoding = RemoteVcsSupport.getEncoding(file);
                        bw = new BufferedWriter(new OutputStreamWriter(os, encoding));
                        bw.write(output);
                        bw.flush();
                    } catch (Exception e) {
                        throw new GitException(e);
                    } finally {
                        if (bw != null) {
                            try {
                                bw.close();
                            } catch (IOException ex) {
                            }
                        }
                    }
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    //fatal: Invalid object name 'HEAD'.
                    //fatal: Path 'removed' exists on disk, but not in 'HEAD'.
                    for (String msg : error.split("\n")) { //NOI18N
                        if (msg.startsWith("fatal: Invalid object")) {
                            throw new GitException.MissingObjectException(GitConstants.HEAD ,GitObjectType.COMMIT);
                        }
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
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}

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

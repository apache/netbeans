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

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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
public class RenameCommand extends GitCommand {
    public static final boolean KIT = false;

    private final VCSFileProxy source;
    private final VCSFileProxy target;
    private final boolean after;
    private final ProgressMonitor monitor;
    private final FileListener listener;

    public RenameCommand (JGitRepository repository, GitClassFactory gitFactory, VCSFileProxy source, VCSFileProxy target, boolean after, ProgressMonitor monitor, FileListener listener){
        super(repository, gitFactory, monitor);
        this.source = source;
        this.target = target;
        this.after = after;
        this.monitor = monitor;
        this.listener = listener;
    }

    @Override
    protected boolean prepareCommand() throws GitException {
        boolean retval = super.prepareCommand();
        if (retval) {
            VCSFileProxy workTree = getRepository().getLocation();
            String relPathToSource = Utils.getRelativePath(workTree, source);
            String relPathToTarget = Utils.getRelativePath(workTree, target);
            if (relPathToSource.startsWith(relPathToTarget + "/")) { //NOI18N
                monitor.preparationsFailed(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Error_SourceFolderUnderTarget"), new Object[] { relPathToSource, relPathToTarget } )); //NOI18N
                throw new GitException(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Error_SourceFolderUnderTarget"), new Object[] { relPathToSource, relPathToTarget } )); //NOI18N
            } else if (relPathToTarget.startsWith(relPathToSource + "/")) { //NOI18N
                monitor.preparationsFailed(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Error_TargetFolderUnderSource"), new Object[] { relPathToTarget, relPathToSource } )); //NOI18N
                throw new GitException(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Error_TargetFolderUnderSource"), new Object[] { relPathToTarget, relPathToSource } )); //NOI18N
            }
            if (source.equals(getRepository().getLocation())) {
                throw new GitException(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Exception_CannotMoveWT"), source.getPath())); //NOI18N
            }
            if (!source.exists() && !after) {
                throw new GitException(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Exception_SourceDoesNotExist"), source.getPath())); //NOI18N
            }
            if (target.exists()) {
                if (!after) {
                    throw new GitException(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Exception_TargetExists"), target.getPath())); //NOI18N
                }
            } else if (after) {
                throw new GitException(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Exception_TargetDoesNotExist"), target.getPath())); //NOI18N
            }
        }
        return retval;
    }
    
    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(3);
        super.prepare();
        addArgument(0, "mv"); //NOI18N
        addArgument(0, "--verbose"); //NOI18N
        addArgument(0, "-f"); //NOI18N
        addArgument(0, Utils.getRelativePath(getRepository().getLocation(), source));
        addArgument(0, Utils.getRelativePath(getRepository().getLocation(), target));

        addArgument(1, "rm"); //NOI18N
        addArgument(1, "--ignore-unmatch");
        addArgument(1, "-r"); //NOI18N
        addArgument(1, "--"); //NOI18N
        addArgument(1, Utils.getRelativePath(getRepository().getLocation(), source));

        addArgument(2, "add"); //NOI18N
        addArgument(2, "-v"); //NOI18N
        addArgument(2, "--"); //NOI18N
        addArgument(2, Utils.getRelativePath(getRepository().getLocation(), target));
    }
    
    @Override
    protected void run() throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            if (!after) {
                rename();
                final AtomicBoolean isError = new AtomicBoolean(false);
                new Runner(canceled, 0){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseMoveOutput(output);
                    }

                    @Override
                    protected void errorParser(String error) throws GitException {
                        isError.set(true);
                    }

                }.runCLI();
                if (isError.get()) {
                    VCSFileProxySupport.renameTo(source, target);
                    listener.notifyFile(target, Utils.getRelativePath(getRepository().getLocation(), target));
                }
            } else {
                new Runner(canceled, 1){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseRemoveOutput(output);
                    }

                }.runCLI();
                new Runner(canceled, 2){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseAddVerboseOutput(output);
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
    
     private void rename () throws GitException {
        VCSFileProxy parentFile = target.getParentFile();
        if (!parentFile.exists() && !VCSFileProxySupport.mkdirs(parentFile)) {
            throw new GitException(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Exception_CannotCreateFolder"), parentFile.getPath())); //NOI18N
        }
    }

    private void parseMoveOutput(String output) {
        //Renaming file to folder/file
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("Renaming")) {
                String[] s = line.split(" ");
                String file = s[s.length-1];
                listener.notifyFile(VCSFileProxy.createFileProxy(getRepository().getLocation(), file), file);
            }
        }
    }

    private void parseAddVerboseOutput(String output) {
        //add 'folder1/subfolder/file1'
        //add 'folder1/subfolder/file2'
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("add")) {
                String s = line.substring(3).trim();
                if (s.startsWith("'") && s.endsWith("'")) {
                    String file = s.substring(1,s.length()-1);
                    listener.notifyFile(VCSFileProxy.createFileProxy(getRepository().getLocation(), file), file);
                }
                continue;
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

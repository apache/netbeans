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

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.extexecution.ProcessBuilder;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.jgit.Utils;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;

/**
 *
 */
public abstract class GitCommand {
    private final JGitRepository repository;
    private final ProgressMonitor monitor;
    protected static final String EMPTY_ROOTS = Utils.getBundle(GitCommand.class).getString("MSG_Error_NoFiles"); //NOI18N
    private final GitClassFactory gitFactory;
    private final List<List<CharSequence>> args = new ArrayList<>(2);
    private static String gitPath = "git"; //NOI18N
    
    // for testing
    public static void setGitCommand(String git) {
        gitPath = git;
    }

    protected GitCommand (JGitRepository repository, GitClassFactory gitFactory, ProgressMonitor monitor) {
        this.repository = repository;
        this.gitFactory = gitFactory;
        this.monitor = monitor;
        args.add(new ArrayList<CharSequence>(5));
    }

    public final void execute () throws GitException {
        if (prepareCommand()) {
            try {
                monitor.started(getCommandLine(0));
                try {
                    AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                        @Override
                        public Void run () throws GitException {
                            GitCommand.this.run();
                            return null;
                        }
                    });
                } catch (PrivilegedActionException e) {
                    throw (GitException) e.getException();
                }
            } catch (RuntimeException ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("Unknown repository format")) { //NOI18N
                    throw new GitException("It seems the config file for repository at [" + repository.getLocation() + "] is corrupted.\nEnsure it's valid.", ex); //NOI18N
                } else {
                    throw ex;
                }
            } finally {
                monitor.finished();
            }
        }
    }

    protected abstract void run () throws GitException;

    protected final void setCommandsNumber(int commandNumber) {
        for (int i = 1; i < commandNumber; i++) {
            args.add(new ArrayList<CharSequence>(5));
        }
    }
    
    protected void prepare () throws GitException {
        for (List<CharSequence> arg : args) {
            arg.add("--no-pager");
        }
    }

    protected boolean prepareCommand () throws GitException {
        boolean repositoryExists = repository.getMetadataLocation().exists();
        if (!repositoryExists) {
            String message = MessageFormat.format(Utils.getBundle(GitCommand.class).getString("MSG_Error_RepositoryDoesNotExist"), repository.getLocation().getPath()); //NOI18N
            monitor.preparationsFailed(message);
            throw new GitException(message);
        }
        prepare();
        return repositoryExists;
    }

    protected JGitRepository getRepository () {
        return repository;
    }

    protected final GitClassFactory getClassFactory () {
        return gitFactory;
    }

    public final void addArgument(int command, CharSequence argument) {
        args.get(command).add(argument);
    }

    public final void addFiles(int command, VCSFileProxy... files) {
         for(String s : Utils.getRelativePaths(getRepository().getLocation(), files)) {
            addArgument(command, s);
         }
    }

    public final void addExistingFiles(int command, VCSFileProxy... files) {
        for (VCSFileProxy root : files) {
            if (!root.exists()) {
                //skip unexisting file
                continue;
            }
            String relativePath = Utils.getRelativePath(getRepository().getLocation(), root);
            if (relativePath.isEmpty()) {
                addArgument(0, ".");
            } else {
                addArgument(0, relativePath);
            }
        }
    }
    
    public final String getExecutable() {
        return gitPath;
    }
        
    protected Map<String, String> getEnvVar() {
        Map<String,String> ret = new HashMap<>();
        ret.put("LC_ALL", "");                // NOI18N    
        ret.put("LC_MESSAGES", "C");          // NOI18N    
        ret.put("LC_TIME", "C");              // NOI18N    
        return ret;
    }	    

    public final String[] getCliArguments(int command) {
        final List<CharSequence> commandArgs = args.get(command);
        final String[] res = new String[args.get(command).size()];
        for(int i = 0; i < commandArgs.size(); i++) {
            res[i] = commandArgs.get(i).toString();
        }
        return res;
    }
    
    protected final String getCommandLine(int command) {
        StringBuilder sb = new StringBuilder(getExecutable()); //NOI18N
        for(CharSequence s : args.get(command)) {
            sb.append(" ").append(s); //NOI18N
        }
        return sb.toString();
    }

    protected final void processMessages (String messages) {
        for (String msg : messages.split("\n")) { //NOI18N
            if (msg.startsWith(MSG_FATAL)) { //NOI18N
                monitor.notifyError(msg.substring(MSG_ERROR.length()).trim());
            } else if (msg.startsWith(MSG_ERROR)) { //NOI18N
                monitor.notifyError(msg.substring(MSG_ERROR.length()).trim());
            } else if (msg.startsWith(MSG_WARNING)) { //NOI18N
                monitor.notifyWarning(msg.substring(MSG_WARNING.length()).trim());
            } else if (!msg.isEmpty()) {
                // these are not warnings, i guess, just plain informational messages
                monitor.notifyMessage(msg);
            }
        }
    }
    private static final String MSG_WARNING = "warning:"; //NOI18N
    private static final String MSG_ERROR = "error:"; //NOI18N
    private static final String MSG_FATAL = "fatal:"; //NOI18N
    
    protected abstract class Runner {
        private final ProcessUtils.Canceler canceled;
        private final int command;
        private final String cmd;
        
        protected Runner(ProcessUtils.Canceler canceled, int command) {
            this.canceled = canceled;
            this.command = command;
            cmd = getCommandLine(command);
        }
        
        protected void runCLI() throws GitException {
            if(canceled.canceled()) {
                return;
            }
            String executable = getExecutable();
            String[] args = getCliArguments(command);
            ProcessUtils.ExitStatus exitStatus = ProcessUtils.executeInDir(getRepository().getLocation().getPath(), getEnvVar(), false, canceled, getRepository().getLocation(), executable, args); //NOI18N
            if(canceled.canceled()) {
                return;
            }
            if (exitStatus.output!= null && exitStatus.isOK()) {
                if (exitStatus.error != null && !exitStatus.error.isEmpty()) {
                    if (exitStatus.output.isEmpty()) {
                        outputParser(exitStatus.error);
                    } else {
                        outputParser(exitStatus.output+"\n"+exitStatus.error);
                    }
                } else {
                    outputParser(exitStatus.output);
                }
            } else {
                outputErrorParser(exitStatus.output, exitStatus.error, exitStatus.exitCode);
            }
            if (exitStatus.error != null && !exitStatus.isOK()) {
                errorParser(exitStatus.error);
            }
        }
        
        protected abstract void outputParser(String output) throws GitException;
        
        protected void errorParser(String error) throws GitException {
            throw new GitException("#"+cmd+"\n"+error);
        }

        protected void outputErrorParser(String output, String error, int exitCode) throws GitException {
        }
    }
    
    protected static final class Revision implements CharSequence {
        private String currentRevision = "place-holder";
        
        protected Revision() {
            
        }

        protected void setContent(String revision) {
            currentRevision = revision;
        }
        
        @Override
        public int length() {
            return currentRevision.length();
        }

        @Override
        public char charAt(int index) {
            return currentRevision.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return currentRevision.subSequence(end, end);
        }

        @Override
        public String toString() {
            return currentRevision;
        }
    }    
}

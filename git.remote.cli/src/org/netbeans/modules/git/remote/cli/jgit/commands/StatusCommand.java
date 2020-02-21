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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.cli.GitConflictDescriptor;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitStatus;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.git.remote.cli.progress.StatusListener;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class StatusCommand extends StatusCommandBase {
    public static final boolean KIT = false;
    private final VCSFileProxy[] roots;
    private final ProgressMonitor monitor;
    private final String revision;
    private static final Logger LOG = Logger.getLogger(StatusCommand.class.getName());
    private static final Set<VCSFileProxy> logged = new HashSet<>();
    private final boolean isRevision;

    public StatusCommand (JGitRepository repository, String revision, VCSFileProxy[] roots, GitClassFactory gitFactory,
            ProgressMonitor monitor, StatusListener listener) {
        super(repository, revision, roots, gitFactory, monitor, listener);
        this.roots = roots;
        this.monitor = monitor;
        this.revision = revision;
        isRevision = !GitConstants.HEAD.equals(revision);
    }
    
    @Override
    protected boolean prepareCommand () throws GitException {
        final boolean exists = getRepository().getMetadataLocation().exists();
        if (exists) {
            prepare();
        }
        return exists;
    }

    @Override
    protected void prepare() throws GitException {
        if (isRevision) {
            setCommandsNumber(4);
        } else {
            setCommandsNumber(3);
        }
        super.prepare();
        if (isRevision) {
            addArgument(0, "diff"); //NOI18N
            addArgument(0, "--cached"); //NOI18N
            addArgument(0, "--raw"); //NOI18N
            addArgument(0, "--name-status"); //NOI18N
            addArgument(0, revision);
            addArgument(0, "--"); //NOI18N
            addFiles(0, roots);
            addArgument(1, "diff"); //NOI18N
            addArgument(1, "--raw"); //NOI18N
            addArgument(1, "--name-status"); //NOI18N
            addArgument(1, revision);
            addArgument(1, "--"); //NOI18N
            addFiles(1, roots);
            addArgument(2, "status"); //NOI18N
            addArgument(2, "--short"); //NOI18N
            addArgument(2, "--ignored"); //NOI18N
            addArgument(2, "--untracked-files=all"); //NOI18N
            addArgument(2, "--"); //NOI18N
            addFiles(2, roots);
            addArgument(3, "ls-files"); //NOI18N
            addArgument(3, "--cached"); //NOI18N
            addArgument(3, "--others"); //NOI18N
            addArgument(3, "-t"); //NOI18N
            addArgument(3, "--"); //NOI18N
            addFiles(3, roots);
        } else {
            addArgument(0, "status"); //NOI18N
            addArgument(0, "--short"); //NOI18N
            addArgument(0, "--ignored"); //NOI18N
            addArgument(0, "--untracked-files=all"); //NOI18N
            addArgument(0, "--"); //NOI18N
            addFiles(0, roots);
            addArgument(1, "diff"); //NOI18N
            addArgument(1, "--raw"); //NOI18N
            addArgument(1, "--name-status"); //NOI18N
            addArgument(1, GitConstants.HEAD);
            addArgument(1, "--"); //NOI18N
            addFiles(1, roots);
            addArgument(2, "ls-files"); //NOI18N
            addArgument(2, "--cached"); //NOI18N
            addArgument(2, "--others"); //NOI18N
            addArgument(2, "-t"); //NOI18N
            addArgument(2, "--"); //NOI18N
            addFiles(2, roots);
        }
    }

    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            if (isRevision) {
                final LinkedHashMap<String, StatusLine> list = new LinkedHashMap<>();
                new Runner(canceled, 0){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseDiffOutput(output, 1, list);
                    }
                }.runCLI();
                
                new Runner(canceled, 1){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseDiffOutput(output, 3, list);
                    }
                }.runCLI();

                new Runner(canceled, 2){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseStatusOutput(output, list, true);
                    }
                }.runCLI();

                new Runner(canceled, 3){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseLsOutput(output, list);
                    }
                }.runCLI();
                if (canceled.canceled()) {
                    return;
                }
                processOutput(list, canceled);
            } else {
                final LinkedHashMap<String, StatusLine> list = new LinkedHashMap<>();
                new Runner(canceled, 0){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseStatusOutput(output, list, false);
                    }
                }.runCLI();
                new Runner(canceled, 1){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseDiffOutput(output, 3, list);
                    }

                    @Override
                    protected void errorParser(String error) throws GitException {
                        if (error.contains("fatal: bad revision 'HEAD'")) {
                            for (Map.Entry<String, StatusLine> e : list.entrySet()) {
                                final char first = e.getValue().first;
                                if (first != '?' && first != '!') {
                                    e.getValue().third = first;
                                }
                            }
                        }
                    }

                }.runCLI();

                new Runner(canceled, 2){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseLsOutput(output, list);
                    }
                }.runCLI();
                if (canceled.canceled()) {
                    return;
                }
                processOutput(list, canceled);
            }
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if(canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }        
    }

    static void parseStatusOutput(String output, Map<String, StatusLine> list, boolean onlyIndexWC) {
        for (String line : output.split("\n")) { //NOI18N
            if (line.length() > 3) {
                char first = line.charAt(0);
                char second = line.charAt(1);
                String file;
                String renamed = null;
                int i = line.indexOf("->");
                if (i > 0) {
                    file = line.substring(2, i).trim();
                    renamed = line.substring(i + 2).trim();
                } else {
                    file = line.substring(2).trim();
                }
                StatusLine status = list.get(file);
                if (status == null) {
                    status = new StatusLine();
                    if (onlyIndexWC) {
                        if (first == '?' || first == '!') {
                            status.first = first;
                            status.second = second;
                        } else {
                            status.second = second;
                        }
                    } else {
                        status.first = first;
                        status.second = second;
                        status.to = renamed;
                    }
                    list.put(file, status);
                } else {
                    if (onlyIndexWC) {
                        if (first == '?' || first == '!') {
                            status.untracked = first;
                        } else {
                            status.second = second;
                        }
                    } else {
                        status.untracked = first;
                    }
                }
                if (renamed != null) {
                    StatusLine renamedToStatus = list.get(renamed);
                    if (renamedToStatus == null) {
                        renamedToStatus = new StatusLine();
                        if (onlyIndexWC) {
                            if (first == '?' || first == '!') {
                                renamedToStatus.first = 'A';
                                renamedToStatus.second = second;
                            } else {
                                renamedToStatus.second = second;
                            }
                        } else {
                            renamedToStatus.first = 'A';
                            renamedToStatus.second = second;
                            renamedToStatus.to = null;
                        }
                        list.put(renamed, renamedToStatus);
                    }                
                }
            }
        }
    }

    private void parseDiffOutput(String output, int n, Map<String, StatusLine> list) {
        for (String line : output.split("\n")) { //NOI18N
            if (line.length() > 2) {
                char c = line.charAt(0);
                String file = line.substring(2).trim();
                StatusLine status = list.get(file);
                if (status == null) {
                    status = new StatusLine();
                    if (n == 1) {
                        status.first = c;
                    }
                    if (n == 2) {
                        status.second = c;
                    }
                    if (n == 3) {
                        status.third = c;
                    }
                    list.put(file, status);
                } else {
                    if (n == 1) {
                        status.first = c;
                    }
                    if (n == 2) {
                        status.second = c;
                    }
                    if (n == 3) {
                        status.third = c;
                    }
                }
            }
        }
    }

    private void parseLsOutput(String output, Map<String, StatusLine> list) {
        for (String line : output.split("\n")) { //NOI18N
            if (line.length() > 0) {
                String file = line.trim();
                boolean cached = true;
                if (file.startsWith("H ")) {
                    file = file.substring(2);
                    cached = true;
                } else if (file.startsWith("? ")) {
                    file = file.substring(2);
                    cached = false;
                }
                StatusLine status = list.get(file);
                if (status == null) {
                    if (cached) {
                        status = new StatusLine();
                    } else {
                        // other (ignored)
                        status = new StatusLine();
                        status.first = '!';
                        status.second = '!';
                        status.third = '!';
                    }
                    list.put(file, status);
                }
            }
        }
    }
    
    private void processOutput(LinkedHashMap<String, StatusLine> parseOutput, ProcessUtils.Canceler canceled) {
        HashMap<String, GitStatus.GitDiffEntry> renamedEntry = new HashMap<>();
        for(Map.Entry<String, StatusLine> entry : parseOutput.entrySet()) {
            String file = entry.getKey();
            StatusLine v = entry.getValue();
            String renamed = v.to;
            if (renamed != null) {
                GitStatus.GitDiffEntry renamedDiff = new GitStatus.GitDiffEntry(GitStatus.GitChangeType.RENAME, file);
                renamedEntry.put(renamed, renamedDiff);
            }
        }
        for(Map.Entry<String, StatusLine> entry : parseOutput.entrySet()) {
            String file = entry.getKey();
            StatusLine v = entry.getValue();
            char first = v.first;
            char second = v.second;
            char third = v.third;
            char untracked = v.untracked;
            String renamed = v.to;

            boolean tracked = !(first == '?' || first == '!' );
            GitStatus.Status statusHeadIndex = GitStatus.Status.STATUS_IGNORED;
            switch (first) {
                case 'A':
                case 'C':
                    statusHeadIndex = GitStatus.Status.STATUS_ADDED;
                    break;
                case 'R':
                case 'D':
                    statusHeadIndex = GitStatus.Status.STATUS_REMOVED;
                    break;
                case 'M':
                case 'U':
                    statusHeadIndex = GitStatus.Status.STATUS_MODIFIED;
                    break;
                case ' ':
                    statusHeadIndex = GitStatus.Status.STATUS_NORMAL;
                    break;
                case '?':
                case '!':
                    statusHeadIndex = GitStatus.Status.STATUS_NORMAL;
                    break;
            }
            GitStatus.Status statusIndexWC = GitStatus.Status.STATUS_IGNORED;
            switch (second) {
                case 'A':
                    statusIndexWC = GitStatus.Status.STATUS_ADDED;
                    break;
                case 'D':
                    statusIndexWC = GitStatus.Status.STATUS_REMOVED;
                    break;
                case 'M':
                case 'U':
                    statusIndexWC = GitStatus.Status.STATUS_MODIFIED;
                    break;
                case ' ':
                    if (untracked == '?') {
                        statusIndexWC = GitStatus.Status.STATUS_ADDED;
                    } else {
                        statusIndexWC = GitStatus.Status.STATUS_NORMAL;
                    }
                    break;
                case '?':
                    statusIndexWC = GitStatus.Status.STATUS_ADDED;
                    break;
                case '!':
                    statusIndexWC = GitStatus.Status.STATUS_IGNORED;
                    break;
            }
            GitStatus.Status statusHeadWC = GitStatus.Status.STATUS_IGNORED;
            switch (third) {
                case 'A':
                    statusHeadWC = GitStatus.Status.STATUS_ADDED;
                    break;
                case 'D':
                    if (untracked == '?') {
                        statusHeadWC = GitStatus.Status.STATUS_MODIFIED;
                    } else {
                        statusHeadWC = GitStatus.Status.STATUS_REMOVED;
                    }
                    break;
                case 'M':
                case 'U':
                    statusHeadWC = GitStatus.Status.STATUS_MODIFIED;
                    break;
                case ' ':
                    if (first == '?' || first == '!') {
                        statusHeadWC = GitStatus.Status.STATUS_ADDED;
                    } else {
                        statusHeadWC = GitStatus.Status.STATUS_NORMAL;
                    }
                    break;
                case '?':
                    statusHeadWC = GitStatus.Status.STATUS_ADDED;
                    break;
                case '!':
                    statusHeadWC = GitStatus.Status.STATUS_IGNORED;
                    break;
            }
            boolean isFolder = false;
            if (file.endsWith("/")) {
                file = file.substring(0, file.length()-1);
                isFolder = true;
            }
            if (!tracked) {
                if (statusIndexWC == GitStatus.Status.STATUS_IGNORED && isFolder) {
                    statusHeadWC = statusIndexWC;
                } else {
                    statusHeadWC = GitStatus.Status.STATUS_ADDED;
                }
            }
            VCSFileProxy vcsFile = VCSFileProxy.createFileProxy(getRepository().getLocation(), file);
            long indexTimestamp = -1;
            GitConflictDescriptor conflict = null;
            if (first == 'U' && second == 'U') { //unmerged, both modified
                conflict = getClassFactory().createConflictDescriptor(GitConflictDescriptor.Type.BOTH_MODIFIED);
            } else if (first == 'D' && second == 'U') { //unmerged, deleted by us
                conflict = getClassFactory().createConflictDescriptor(GitConflictDescriptor.Type.DELETED_BY_US);
            } else if (first == 'A' && second == 'U') { //unmerged, added by us
                conflict = getClassFactory().createConflictDescriptor(GitConflictDescriptor.Type.ADDED_BY_US);
            } else if (first == 'U' && second == 'D') { //unmerged, deleted by them
                conflict = getClassFactory().createConflictDescriptor(GitConflictDescriptor.Type.DELETED_BY_THEM);
            } else if (first == 'U' && second == 'A') { //unmerged, added by them
                conflict = getClassFactory().createConflictDescriptor(GitConflictDescriptor.Type.ADDED_BY_THEM);
            } else if (first == 'D' && second == 'D') { //unmerged, both deleted
                conflict = getClassFactory().createConflictDescriptor(GitConflictDescriptor.Type.BOTH_DELETED);
            } else if (first == 'A' && second == 'A') { //unmerged, both added
                conflict = getClassFactory().createConflictDescriptor(GitConflictDescriptor.Type.BOTH_ADDED);
            }
            GitStatus status = getClassFactory().createStatus(tracked, file, getRepository().getLocation().getPath(), vcsFile,
                    statusHeadIndex, statusIndexWC, statusHeadWC,
                    conflict, isFolder, renamedEntry.get(file), indexTimestamp);
            addStatus(vcsFile, status);
            //command.outputText(line);
        }
    }

    static final class StatusLine {
        char first = ' ';
        char second = ' ';
        char third = ' ';
        char untracked = ' ';
        String to;

        public StatusLine() {
        }

        @Override
        public String toString() {
            return ""+first+second+third+untracked;
        }
    }
}

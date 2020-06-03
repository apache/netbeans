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

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.git.remote.cli.GitBlameResult;
import org.netbeans.modules.git.remote.cli.GitBlameResult.GitBlameContent;
import org.netbeans.modules.git.remote.cli.GitBlameResult.LineInfo;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class BlameCommand extends GitCommand {
    public static final boolean KIT = false;
    private final String revision;
    private final VCSFileProxy file;
    private final ProgressMonitor monitor;
    private GitBlameResult result;

    public BlameCommand (JGitRepository repository, GitClassFactory gitFactory, VCSFileProxy file, String revision, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.file = file;
        this.revision = revision;
        this.monitor = monitor;
    }

    public GitBlameResult getResult () {
        return result;
    }

    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "blame"); //NOI18N
        addArgument(0, "--porcelain"); //NOI18N
        if (revision != null) {
            addArgument(0, revision);
        }
        addArgument(0, "--"); //NOI18N
        addFiles(0, new VCSFileProxy[]{file});
    }
    
    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            final LinkedHashMap<String, GitBlameContent> content = new LinkedHashMap<String, GitBlameContent>();
            final AtomicBoolean failed = new AtomicBoolean(false);
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseBlameOutput(output, content);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    failed.set(true);
                }
                
            }.runCLI();
            if (!failed.get()) {
                result = getClassFactory().createBlameResult(file, content, getRepository());
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
    
    private void parseBlameOutput(String output, LinkedHashMap<String, GitBlameContent> content) {
        //48695c22e752fbed64a9a6ba73a91185c01c1542 1 1 1
        //author user1
        //author-mail <user1@company.com>
        //author-time 1424260119
        //author-tz +0300
        //committer
        //committer-mail
        //committer-time 1424260119
        //committer-tz +0300
        //summary initial commit
        //boundary
        //filename f
        //	aaa
        //0000000000000000000000000000000000000000 2 2 1
        //author Not Committed Yet
        //author-mail <not.committed.yet>
        //author-time 1424260119
        //author-tz +0300
        //committer Not Committed Yet
        //committer-mail <not.committed.yet>
        //committer-time 1424260119
        //committer-tz +0300
        //summary Version of f from f
        //previous 48695c22e752fbed64a9a6ba73a91185c01c1542 f
        //filename f
        //	ccc
        State state = State.revision;
        GitBlameContent current = null;
        int currLine = -1;
        LineInfo currLineInfo = null;
        for (String line : output.split("\n")) { //NOI18N
            if (state == State.revision) {
                String[] s = line.split(" ");
                String rev = s[0];
                current = content.get(rev);
                if (current == null) {
                    current = new GitBlameContent();
                    current.revision = rev;
                    content.put(rev, current);
                    state = State.header;
                } else {
                    state = State.line;
                }
                currLine = Integer.parseInt(s[2]);
                currLineInfo = new LineInfo();
                currLineInfo.line = Integer.parseInt(s[1]);
                continue;
            }
            if (state == State.header) {
                if (line.startsWith("author ")) {
                    current.author = line.substring(7).trim();
                    continue;
                }
                if (line.startsWith("author-mail ")) {
                    current.author_mail = line.substring(12).trim();
                    continue;
                }
                if (line.startsWith("author-time ")) {
                    current.author_time = line.substring(12).trim();
                    continue;
                }
                if (line.startsWith("author-tz ")) {
                    current.author_tz = line.substring(10).trim();
                    continue;
                }
                if (line.startsWith("committer ")) {
                    current.committer = line.substring(10).trim();
                    continue;
                }
                if (line.startsWith("committer-mail ")) {
                    current.committer_mail = line.substring(15).trim();
                    continue;
                }
                if (line.startsWith("committer-time ")) {
                    current.committer_time = line.substring(15).trim();
                    continue;
                }
                if (line.startsWith("committer-tz ")) {
                    current.committer_tz = line.substring(13).trim();
                    continue;
                }
                if (line.startsWith("summary ")) {
                    current.summary = line.substring(8).trim();
                    continue;
                }
                if (line.startsWith("previous ")) {
                    current.previous = line.substring(9).trim();
                    continue;
                }
                if (line.startsWith("boundary")) {
                    continue;
                }
                if (line.startsWith("filename ")) {
                    current.filename = line.substring(9).trim();
                    state = State.line;
                    continue;
                }
            }
            if (state == State.line) {
                currLineInfo.lineContent = line.substring(1);
                current.lines.put(currLine, currLineInfo);
                state = State.revision;
                continue;
            }
        }
    }

    private enum State {revision, header, line};
    
}

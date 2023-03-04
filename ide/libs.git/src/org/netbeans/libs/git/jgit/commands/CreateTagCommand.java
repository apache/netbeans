/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.libs.git.jgit.commands;

import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevObject;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitTag;
import org.netbeans.libs.git.jgit.DelegatingGitProgressMonitor;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class CreateTagCommand extends GitCommand {
    private final boolean forceUpdate;
    private final String tagName;
    private final String taggedObject;
    private final String message;
    private final boolean signed;
    private GitTag tag;
    private final ProgressMonitor monitor;

    public CreateTagCommand (Repository repository, GitClassFactory gitFactory, String tagName, String taggedObject, String message, boolean signed, boolean forceUpdate, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
        this.tagName = tagName;
        this.taggedObject = taggedObject;
        this.message = message;
        this.signed = signed;
        this.forceUpdate = forceUpdate;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        try {
            RevObject obj = Utils.findObject(repository, taggedObject);
            TagCommand cmd = new Git(repository).tag();
            cmd.setName(tagName);
            cmd.setForceUpdate(forceUpdate);
            cmd.setObjectId(obj);
            cmd.setAnnotated(message != null && !message.isEmpty() || signed);
            if (cmd.isAnnotated()) {
                cmd.setMessage(message);
                cmd.setSigned(signed);
            }
            cmd.call();
            ListTagCommand tagCmd = new ListTagCommand(repository, getClassFactory(), false, new DelegatingGitProgressMonitor(monitor));
            tagCmd.run();
            Map<String, GitTag> tags = tagCmd.getTags();
            tag = tags.get(tagName);
        } catch (JGitInternalException | GitAPIException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git tag");
        if (signed) {
            sb.append(" -s");
        }
        if (forceUpdate) {
            sb.append(" -f");
        }
        if (message != null && !message.isEmpty()) {
            sb.append(" -m ").append(message.replace("\n", "\\n"));
        }
        sb.append(' ').append(tagName);
        if (taggedObject != null) {
            sb.append(' ').append(taggedObject);
        }
        return sb.toString();
    }

    public GitTag getTag () {
        return tag;
    }
}

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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class ExportCommitCommand extends GitCommand {
    private final ProgressMonitor monitor;
    private final OutputStream out;
    private final FileListener listener;
    private final String revisionStr;
    
    private static final char NL = '\n';

    public ExportCommitCommand (Repository repository, GitClassFactory gitFactory, String revisionStr, OutputStream out, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
        this.listener = listener;
        this.out = out;
        this.revisionStr = revisionStr;
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository();
        String workTreePath = repository.getWorkTree().getAbsolutePath();
        RevCommit commit = Utils.findCommit(repository, revisionStr);
        if (commit.getParentCount() > 1) {
            throw new GitException("Unable to export a merge commit");
        }
        try (DiffFormatter formatter = new DiffFormatter(out)) {
            out.write(Constants.encode(formatCommitInfo(commit)));
            formatter.setRepository(repository);
            List<DiffEntry> diffEntries;
            if (commit.getParentCount() > 0) {
                formatter.setDetectRenames(true);
                diffEntries = formatter.scan(commit.getParent(0), commit);
            } else {
                TreeWalk walk = new TreeWalk(repository);
                walk.reset();
                walk.setRecursive(true);
                walk.addTree(new EmptyTreeIterator());
                walk.addTree(commit.getTree());
                walk.setFilter(AndTreeFilter.create(TreeFilter.ANY_DIFF, PathFilter.ANY_DIFF));
                diffEntries = DiffEntry.scan(walk);
            }
            for (DiffEntry ent : diffEntries) {
                if (monitor.isCanceled()) {
                    break;
                }
                listener.notifyFile(new File(workTreePath + File.separator + ent.getNewPath()), ent.getNewPath());
                formatter.format(ent);
            }
            formatter.flush();
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        return "git format-patch --no-stat -1 " + revisionStr;
    }

    private String formatCommitInfo (RevCommit commit) {
        GitRevisionInfo info = getClassFactory().createRevisionInfo(commit, getRepository());
        StringBuilder sb = new StringBuilder();
        sb.append("From ").append(info.getRevision()).append(" ").append("Mon Sep 17 00:00:00 2001").append(NL);
        if (info.getAuthor() != null) {
            sb.append("From: ").append(info.getAuthor().toString()).append(NL);
        } else if (info.getCommitter() != null) {
            sb.append("From: ").append(info.getAuthor().toString()).append(NL);
        }
        sb.append("Date: ").append(DateFormat.getDateTimeInstance().format(new Date(info.getCommitTime()))).append(NL);
        sb.append(NL).append(info.getFullMessage()).append(NL).append(NL);
        return sb.toString();
    }
}

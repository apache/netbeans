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
import java.util.Collection;
import java.util.List;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.diff.ContentSource;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.CoreConfig;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.jgit.utils.AutoCRLFComparator;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class ExportDiffCommand extends GitCommand {
    private final File[] roots;
    private final ProgressMonitor monitor;
    private final OutputStream out;
    private final FileListener listener;
    private final String firstCommit;
    private final String secondCommit;

    public ExportDiffCommand (Repository repository, GitClassFactory gitFactory,
            File[] roots, String firstCommit, String secondCommit,
            OutputStream out, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.monitor = monitor;
        this.listener = listener;
        this.firstCommit = firstCommit;
        this.secondCommit = secondCommit;
        this.out = out;
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository();
        String workTreePath = repository.getWorkTree().getAbsolutePath();
        try (DiffFormatter formatter = new DiffFormatter(out);
            ObjectReader or = repository.newObjectReader()) {
            formatter.setRepository(repository);
            Collection<PathFilter> pathFilters = Utils.getPathFilters(repository.getWorkTree(), roots);
            if (!pathFilters.isEmpty()) {
                formatter.setPathFilter(PathFilterGroup.create(pathFilters));
            }
            if (repository.getConfig().get(WorkingTreeOptions.KEY).getAutoCRLF() != CoreConfig.AutoCRLF.FALSE) {
                // work-around for autocrlf
                formatter.setDiffComparator(new AutoCRLFComparator());
            }
            AbstractTreeIterator firstTree = getIterator(firstCommit, or);
            AbstractTreeIterator secondTree = getIterator(secondCommit, or);
            List<DiffEntry> diffEntries;
            if (secondTree instanceof WorkingTreeIterator) {
                // remote when fixed in JGit, see ExportDiffTest.testDiffRenameDetectionProblem
                formatter.setDetectRenames(false);
                diffEntries = formatter.scan(firstTree, secondTree);
                formatter.setDetectRenames(true);
                RenameDetector detector = formatter.getRenameDetector();
                detector.reset();
                detector.addAll(diffEntries);
		diffEntries = detector.compute(new ContentSource.Pair(ContentSource.create(or), ContentSource.create((WorkingTreeIterator) secondTree)), NullProgressMonitor.INSTANCE);
            } else {
                formatter.setDetectRenames(true);
                diffEntries = formatter.scan(firstTree, secondTree);
            }
            for (DiffEntry ent : diffEntries) {
                if (monitor.isCanceled()) {
                    break;
                }
                listener.notifyFile(new File(workTreePath + File.separator + ent.getNewPath()), ent.getNewPath());
                formatter.format(ent);
            }
            formatter.flush();
        } catch (IOException | CanceledException ex) {
            throw new GitException(ex);
        }
    }

    private AbstractTreeIterator getIterator (String commit, ObjectReader or) throws IOException, GitException {
        Repository repository = getRepository();
        switch (commit) {
            case Constants.HEAD:
                return getHeadIterator(or);
            case GitClient.INDEX:
                return new DirCacheIterator(repository.readDirCache());
            case GitClient.WORKING_TREE:
                return new FileTreeIterator(repository);
            default:
                CanonicalTreeParser p = new CanonicalTreeParser();
                p.reset(or, Utils.findCommit(repository, commit).getTree());
                return p;
        }
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git diff"); //NOI18N
        for (File root : roots) {
            sb.append(" ").append(root); //NOI18N
        }
        return sb.toString();
    }

    private AbstractTreeIterator getHeadIterator (ObjectReader or) throws IOException {
        Repository repository = getRepository();
        AbstractTreeIterator headIterator;
        ObjectId headId = repository.resolve(Constants.HEAD);
        if (headId != null) {
            headIterator = new CanonicalTreeParser(null, or, new RevWalk(repository).parseTree(headId).getId());
        } else {
            headIterator = new EmptyTreeIterator();
        }
        return headIterator;
    }

}

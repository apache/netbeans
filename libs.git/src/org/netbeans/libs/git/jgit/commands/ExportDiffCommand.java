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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
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
        DiffFormatter formatter = new DiffFormatter(out);
        formatter.setRepository(repository);
        ObjectReader or = null;
        String workTreePath = repository.getWorkTree().getAbsolutePath();
        try {
            Collection<PathFilter> pathFilters = Utils.getPathFilters(repository.getWorkTree(), roots);
            if (!pathFilters.isEmpty()) {
                formatter.setPathFilter(PathFilterGroup.create(pathFilters));
            }
            if (repository.getConfig().get(WorkingTreeOptions.KEY).getAutoCRLF() != CoreConfig.AutoCRLF.FALSE) {
                // work-around for autocrlf
                formatter.setDiffComparator(new AutoCRLFComparator());
            }
            or = repository.newObjectReader();
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
        } catch (IOException ex) {
            throw new GitException(ex);
        } finally {
            if (or != null) {
                or.release();
            }
            formatter.release();
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

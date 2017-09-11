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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.*;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class CompareCommand extends GitCommand {
    private final LinkedHashMap<File, GitRevisionInfo.GitFileInfo> statuses;
    private final File[] roots;
    private final String revisionFirst;
    private final String revisionSecond;

    public CompareCommand (Repository repository, String revisionFirst, String revisionSecond, File[] roots,
            GitClassFactory gitFactory, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.revisionFirst = revisionFirst;
        this.revisionSecond = revisionSecond;
        statuses = new LinkedHashMap<File, GitRevisionInfo.GitFileInfo>();
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git diff --raw"); //NOI18N
        sb.append(revisionFirst).append(' ').append(revisionSecond);                
        for (File root : roots) {
            sb.append(" ").append(root.getAbsolutePath());
        }
        return sb.toString();
    }

    @Override
    protected boolean prepareCommand () throws GitException {
        return getRepository().getDirectory().exists();
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        TreeWalk walk = new TreeWalk(repository);
        try {
            walk.reset();
            walk.setRecursive(true);
            walk.addTree(Utils.findCommit(repository, revisionFirst).getTree());
            walk.addTree(Utils.findCommit(repository, revisionSecond).getTree());
            Collection<PathFilter> pathFilters = Utils.getPathFilters(repository.getWorkTree(), roots);
            if (pathFilters.isEmpty()) {
                walk.setFilter(AndTreeFilter.create(TreeFilter.ANY_DIFF, PathFilter.ANY_DIFF));
            } else {
                walk.setFilter(AndTreeFilter.create(new TreeFilter[] { 
                    TreeFilter.ANY_DIFF,
                    PathFilter.ANY_DIFF,
                    PathFilterGroup.create(pathFilters)
                }));
            }
            List<GitRevisionInfo.GitFileInfo> infos = Utils.getDiffEntries(repository, walk, getClassFactory());
            for (GitRevisionInfo.GitFileInfo info : infos) {
                statuses.put(info.getFile(), info);
            }
        } catch (IOException ex) {
            throw new GitException(ex);
        } finally {
            walk.release();
        }
    }

    public Map<File, GitRevisionInfo.GitFileInfo> getFileDifferences () {
        return statuses;
    }
}

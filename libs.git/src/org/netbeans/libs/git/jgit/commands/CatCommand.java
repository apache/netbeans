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
import java.text.MessageFormat;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.CoreConfig;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.AutoCRLFOutputStream;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class CatCommand extends GitCommand {
    private final String revision;
    private final File file;
    private final OutputStream os;
    private final ProgressMonitor monitor;
    private String relativePath;
    private boolean found;
    private final boolean fromRevision;
    private final int stage;

    public CatCommand (Repository repository, GitClassFactory gitFactory, File file, String revision, OutputStream out, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.file = file;
        this.revision = revision;
        this.os = out;
        this.monitor = monitor;
        this.fromRevision = true;
        this.stage = 0;
    }

    public CatCommand (Repository repository, GitClassFactory gitFactory, File file, int stage, OutputStream out, ProgressMonitor monitor) {
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
            relativePath = Utils.getRelativePath(getRepository().getWorkTree(), file);
            if (relativePath.isEmpty()) {
                String message = MessageFormat.format(Utils.getBundle(CatCommand.class).getString("MSG_Error_CannotCatRoot"), file); //NOI18N
                monitor.preparationsFailed(message);
                throw new GitException(message);
            }
        }
        return retval;
    }

    @Override
    protected void run () throws GitException {
        if (fromRevision) {
            catFromRevision();
        } else {
            catIndexEntry();
        }
    }

    private void catFromRevision () throws GitException.MissingObjectException, GitException {
        Repository repository = getRepository();
        OutputStream out = null;
        try {
            RevCommit commit = Utils.findCommit(repository, revision);
            TreeWalk walk = new TreeWalk(repository);
            walk.reset();
            walk.addTree(commit.getTree());
            walk.setFilter(PathFilter.create(relativePath));
            walk.setRecursive(true);
            found = false;
            while (!found && walk.next() && !monitor.isCanceled()) {
                if (relativePath.equals(walk.getPathString())) {
                    WorkingTreeOptions opt = repository.getConfig().get(WorkingTreeOptions.KEY);
                    ObjectLoader loader = repository.getObjectDatabase().open(walk.getObjectId(0));
                    if (opt.getAutoCRLF() != CoreConfig.AutoCRLF.FALSE) {
                        out = new AutoCRLFOutputStream(os);
                    } else {
                        out = os;
                    }
                    loader.copyTo(os);
                    found = true;
                }
            }
        } catch (MissingObjectException ex) {
            throw new GitException(ex);
        } catch (IOException ex) {
            throw new GitException(ex);
        } finally {
            try {
                if (out == null) {
                    os.close();
                } else {
                    out.close();
                }
            } catch (IOException ex) {
                //
            }
        }
    }

    private void catIndexEntry () throws GitException {
        Repository repository = getRepository();
        OutputStream out = null;
        try {
            DirCache cache = repository.readDirCache();
            int pos = cache.findEntry(relativePath);
            DirCacheEntry entry = null;
            if (pos >= 0) {
                DirCacheEntry e = cache.getEntry(pos);
                do {
                    if (stage == e.getStage()) {
                        entry = e;
                    }
                } while (entry == null && ++pos < cache.getEntryCount() && relativePath.equals((e = cache.getEntry(pos)).getPathString()));
            }
            found = false;
            if (entry != null) {
                found = true;
                WorkingTreeOptions opt = repository.getConfig().get(WorkingTreeOptions.KEY);
                ObjectLoader loader = repository.getObjectDatabase().open(entry.getObjectId());
		if (opt.getAutoCRLF() != CoreConfig.AutoCRLF.FALSE) {
                    out = new AutoCRLFOutputStream(os);
                } else {
                    out = os;
                }
                loader.copyTo(os);
                found = true;
            }
        } catch (NoWorkTreeException ex) {
            throw new GitException(ex);
        } catch (IOException ex) {
            throw new GitException(ex);
        } finally {
            try {
                if (out == null) {
                    os.close();
                } else {
                    out.close();
                }
            } catch (IOException ex) {
                //
            }
        }
    }

    @Override
    protected String getCommandDescription () {
        return new StringBuilder("git show ").append(revision).append(" ").append(file).toString(); //NOI18N
    }

    public boolean foundInRevision () {
        return found;
    }

}

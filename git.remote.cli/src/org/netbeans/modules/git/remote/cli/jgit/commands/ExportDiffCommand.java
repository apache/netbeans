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

package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class ExportDiffCommand extends GitCommand {
    private final VCSFileProxy[] roots;
    private final ProgressMonitor monitor;
    private final OutputStream out;
    private final FileListener listener;
    private final String firstCommit;
    private final String secondCommit;

    public ExportDiffCommand (JGitRepository repository, GitClassFactory gitFactory,
            VCSFileProxy[] roots, String firstCommit, String secondCommit,
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
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "diff"); //NOI18N
        if (GitConstants.HEAD.equals(firstCommit) && GitClient.INDEX.equals(secondCommit)) {
            //HEAD_VS_INDEX
            addArgument(0, "--cached"); //NOI18N
        } else if (GitConstants.HEAD.equals(firstCommit) && GitClient.WORKING_TREE.equals(secondCommit)) {
            //HEAD_VS_WORKINGTREE
            addArgument(0, "HEAD"); //NOI18N
        } else if (GitClient.INDEX.equals(firstCommit) && GitClient.WORKING_TREE.equals(secondCommit)) {
            //INDEX_VS_WORKINGTREE
        } else {
            addArgument(0, firstCommit); //NOI18N
            addArgument(0, secondCommit); //NOI18N
        }
        addArgument(0, "--"); //NOI18N
        addFiles(0, roots);
    }

    @Override
    protected void run() throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    try {
                        for(int i = 0; i < output.length(); i++)  {
                            out.write(output.charAt(i));
                        }
                    } catch (Exception e) {
                        throw new GitException(e);
                    }
                }

            }.runCLI();
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                }
            }
        }
//        Repository repository = getRepository().getRepository();
//        DiffFormatter formatter = new DiffFormatter(out);
//        formatter.setRepository(repository);
//        ObjectReader or = null;
//        String workTreePath = repository.getWorkTree().getAbsolutePath();
//        try {
//            Collection<PathFilter> pathFilters = Utils.getPathFilters(getRepository().getLocation(), roots);
//            if (!pathFilters.isEmpty()) {
//                formatter.setPathFilter(PathFilterGroup.create(pathFilters));
//            }
//            if (repository.getConfig().get(WorkingTreeOptions.KEY).getAutoCRLF() != CoreConfig.AutoCRLF.FALSE) {
//                // work-around for autocrlf
//                formatter.setDiffComparator(new AutoCRLFComparator());
//            }
//            or = repository.newObjectReader();
//            AbstractTreeIterator firstTree = getIterator(firstCommit, or);
//            AbstractTreeIterator secondTree = getIterator(secondCommit, or);
//            List<DiffEntry> diffEntries;
//            if (secondTree instanceof WorkingTreeIterator) {
//                // remote when fixed in JGit, see ExportDiffTest.testDiffRenameDetectionProblem
//                formatter.setDetectRenames(false);
//                diffEntries = formatter.scan(firstTree, secondTree);
//                formatter.setDetectRenames(true);
//                RenameDetector detector = formatter.getRenameDetector();
//                detector.reset();
//                detector.addAll(diffEntries);
//		diffEntries = detector.compute(new ContentSource.Pair(ContentSource.create(or), ContentSource.create((WorkingTreeIterator) secondTree)), NullProgressMonitor.INSTANCE);
//            } else {
//                formatter.setDetectRenames(true);
//                diffEntries = formatter.scan(firstTree, secondTree);
//            }
//            for (DiffEntry ent : diffEntries) {
//                if (monitor.isCanceled()) {
//                    break;
//                }
//                listener.notifyFile(VCSFileProxy.createFileProxy(getRepository().getLocation(), ent.getNewPath()), ent.getNewPath());
//                formatter.format(ent);
//            }
//            formatter.flush();
//        } catch (IOException ex) {
//            throw new GitException(ex);
//        } finally {
//            if (or != null) {
//                or.release();
//            }
//            formatter.release();
//        }
    }

}

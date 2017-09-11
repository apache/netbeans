/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.git;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitRevisionInfo.GitFileInfo;
import org.netbeans.libs.git.SearchCriteria;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class HistoryRegistry {
    private static HistoryRegistry instance;

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.mercurial.HistoryRegistry"); // NOI18N
    
    private Map<File, List<GitRevisionInfo>> logs = Collections.synchronizedMap(new HashMap<File, List<GitRevisionInfo>>());
    private Map<File, Map<String, List<GitFileInfo>>> changesets = new HashMap<File, Map<String, List<GitFileInfo>>>();
    
    private HistoryRegistry() {}
    
    public synchronized static HistoryRegistry getInstance() {
        if(instance == null) {
            instance = new HistoryRegistry();
        }
        return instance;
    }
    
    public GitRevisionInfo[] getLogs (File repository, File[] files, Date from, Date to, ProgressMonitor pm) throws GitException {
        GitClient client = Git.getInstance().getClient(repository);
        SearchCriteria crit = new SearchCriteria();
        crit.setFrom(from);
        crit.setTo(to);
        crit.setRevisionTo(GitUtils.HEAD);
        crit.setFiles(files);
        crit.setFollowRenames(true);
        crit.setIncludeMerges(false);
        try {
            GitRevisionInfo[] history = client.log(crit, false, pm);
            if (!pm.isCanceled() && history.length > 0) {
                for (File f : files) {
                    logs.put(f, Arrays.asList(history));
                }
            }
            return history;
        } finally {
            if (client != null) {
                client.release();
            }
        }
    }
    
    public File getHistoryFile(final File repository, final File originalFile, final String revision, final boolean dryTry) {
        long t = System.currentTimeMillis();
        String originalPath = GitUtils.getRelativePath(repository, originalFile);
        try {
            final List<GitRevisionInfo> history = logs.get(originalFile);
            final String path = originalPath;
            Map<String, List<GitFileInfo>> fileChangesets = changesets.get(originalFile);
            if(fileChangesets == null) {
                fileChangesets = new HashMap<String, List<GitFileInfo>>();
                changesets.put(originalFile, fileChangesets);
            }
            final Map<String, List<GitFileInfo>> fcs = fileChangesets;
            final String[] ret = new String[] {null};
            if(history != null) {
                GitProgressSupport support = new GitProgressSupport() {
                    @Override
                    protected void perform() {
                        ret[0] = getRepositoryPathIntern(history, revision, fcs, repository, originalFile, path, dryTry, getProgressHandle(), getProgressMonitor());
                    }
                };
                support.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(HistoryRegistry.class, "LBL_LookingUp")).waitFinished(); //NOI18N
            }
            if(ret[0] != null && !ret[0].equals(originalPath)) {
                return new File(repository, ret[0]);
            }
            return null;

        } finally { 
            if(LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, " resolving historyFile for {0} took {1}", new Object[]{originalPath, System.currentTimeMillis() - t}); //NOI18N
            }
        }
    }

    private String getRepositoryPathIntern (List<GitRevisionInfo> history, String revision, Map<String, List<GitFileInfo>> fileChangesets, 
            File repository, File originalFile, final String path, boolean dryTry, ProgressHandle progressHandle, ProgressMonitor pm) {
        int count = 0;
        String historyPath = path;
        Iterator<GitRevisionInfo> it = history.iterator();
        while(it.hasNext() && !revision.equals(it.next().getRevision())) {
            count++;
        }
        progressHandle.switchToDeterminate(count);
        
        // XXX try dry first, might be it will lead to the in in the revision
        for (int i = 0; i < history.size() && !pm.isCanceled(); i ++) {
            GitRevisionInfo lm = history.get(i);
            String historyRevision = lm.getRevision();
            if(historyRevision.equals(revision)) {
                break;
            }
            progressHandle.progress(NbBundle.getMessage(HistoryRegistry.class, "LBL_LookingUpAtRevision", originalFile.getName(), historyRevision), i); //NOI18N
            List<GitFileInfo> changePaths = fileChangesets.get(historyRevision);
            if(changePaths == null && !dryTry) {
                long t1 = System.currentTimeMillis();
                Map<File, GitFileInfo> cps = null;
                GitClient client = null;
                try {
                    client = Git.getInstance().getClient(repository);
                    GitRevisionInfo lms = client.log(historyRevision, pm);
                    assert lms != null;
                    cps = lms.getModifiedFiles();
                } catch (GitException ex) {
                    LOG.log(Level.INFO, null, ex);
                } finally {
                    if (client != null) {
                        client.release();
                    }
                }
                if (cps == null) {
                    changePaths = Collections.<GitFileInfo>emptyList();
                } else {
                    changePaths = new ArrayList<GitFileInfo>(cps.values());
                }
                fileChangesets.put(historyRevision, changePaths);
                if(LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, " loading changePaths for {0} took {1}", new Object[]{historyRevision, System.currentTimeMillis() - t1}); // NOI18N
                }
            }
            if(changePaths != null) {
                for (GitFileInfo cp : changePaths) {
                    String copy = cp.getOriginalPath();
                    if (copy != null && historyPath.equals(cp.getRelativePath())) {
                        historyPath = copy;
                        break;
                    }
                }
            }
        }
        // XXX check if found path exists in the revision we search for ...
        return pm.isCanceled() ? path : historyPath;
    }
}

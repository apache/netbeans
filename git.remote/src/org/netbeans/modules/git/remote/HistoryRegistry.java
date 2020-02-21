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
package org.netbeans.modules.git.remote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo.GitFileInfo;
import org.netbeans.modules.git.remote.cli.SearchCriteria;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.NbBundle;

/**
 *
 */
public class HistoryRegistry {
    private static HistoryRegistry instance;

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.mercurial.HistoryRegistry"); // NOI18N

    public static synchronized HistoryRegistry getInstance() {
        if(instance == null) {
            instance = new HistoryRegistry();
        }
        return instance;
    }
    
    private final Map<VCSFileProxy, List<GitRevisionInfo>> logs = Collections.synchronizedMap(new HashMap<VCSFileProxy, List<GitRevisionInfo>>());
    private final Map<VCSFileProxy, Map<String, List<GitFileInfo>>> changesets = new HashMap<>();
    
    private HistoryRegistry() {}
    
    public GitRevisionInfo[] getLogs(VCSFileProxy repository, VCSFileProxy[] files, Date from, Date to, ProgressMonitor pm) throws GitException {
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
                for (VCSFileProxy f : files) {
                    logs.put(f, Arrays.asList(history));
                }
            }
            return history;
        } finally {
            client.release();
        }
    }
    
    public VCSFileProxy getHistoryFile (final VCSFileProxy repository, final VCSFileProxy originalFile, final String revision, final boolean dryTry) {
        long t = System.currentTimeMillis();
        String originalPath = GitUtils.getRelativePath(repository, originalFile);
        try {
            final List<GitRevisionInfo> history = logs.get(originalFile);
            final String path = originalPath;
            Map<String, List<GitFileInfo>> fileChangesets = changesets.get(originalFile);
            if(fileChangesets == null) {
                fileChangesets = new HashMap<>();
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
                return VCSFileProxy.createFileProxy(repository, ret[0]);
            }
            return null;
            
        } finally { 
            if(LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, " resolving historyFile for {0} took {1}", new Object[]{originalPath, System.currentTimeMillis() - t}); //NOI18N
            }
        }
    }
    
    private String getRepositoryPathIntern(List<GitRevisionInfo> history, String revision, Map<String, List<GitFileInfo>> fileChangesets, VCSFileProxy repository, VCSFileProxy originalFile, final String path, boolean dryTry, ProgressHandle progressHandle, ProgressMonitor pm) {
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
                Map<VCSFileProxy, GitFileInfo> cps = null;
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
                    changePaths = new ArrayList<>(cps.values());
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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.subversion.ui.history;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.client.SvnClient;
import org.tigris.subversion.svnclientadapter.*;
import javax.swing.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.*;
import java.io.File;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.tigris.subversion.svnclientadapter.utils.SVNUrlUtils;

/**
 * Executes searches in Search History panel.
 *
 * @author Maros Sandor
 */
class SearchExecutor extends SvnProgressSupport {

    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");  // NOI18N

    static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");  // NOI18N
    static final DateFormat [] dateFormats = new DateFormat[] {
        fullDateFormat,
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),  // NOI18N
        simpleDateFormat,
        new SimpleDateFormat("yyyy-MM-dd"), // NOI18N
    };

    private static final Logger LOG = Logger.getLogger(SearchExecutor.class.getName());
    private final SearchHistoryPanel    master;
    private Map<SVNUrl, Set<File>>      workFiles;
    private Map<String,File>            pathToRoot;
    private final SearchCriteriaPanel   criteria;

    private int                         completedSearches;
    private boolean                     searchCanceled;
    private List<RepositoryRevision> results = new ArrayList<RepositoryRevision>();
    static final int DEFAULT_LIMIT = 10;
    private final SVNRevision fromRevision;
    private final SVNRevision toRevision;
    private final int limit;
    private SvnProgressSupport currentSearch;

    public SearchExecutor(SearchHistoryPanel master) {
        this.master = master;
        criteria = master.getCriteria();
        fromRevision = criteria.getFrom();
        toRevision = criteria.getTo();
        limit = searchingUrl() || master.getRoots().length == 1 ? DEFAULT_LIMIT : 0;
    }

    private void populatePathToRoot() {
        pathToRoot = new HashMap<String, File>();
        try {
            if (searchingUrl()) {
                String rootPath = SvnUtils.getRepositoryPath(master.getRoots()[0]);
                pathToRoot.put(rootPath, master.getRoots()[0]);
            } else {
                workFiles = new HashMap<SVNUrl, Set<File>>();
                for (File file : master.getRoots()) {
                    SVNUrl rootUrl = SvnUtils.getRepositoryRootUrl(file);
                    populatePathToRoot(file, rootUrl);
                    Set<File> set = workFiles.get(rootUrl);
                    if (set == null) {
                        set = new HashSet<File>(2);
                        workFiles.put(rootUrl, set);
                    }
                    set.add(file);
                }
            }
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
        }
    }

    private void populatePathToRoot(File file, SVNUrl rootUrl) throws SVNClientException {
        Map<File, SVNUrl> m = SvnUtils.getRepositoryUrls(file);
        for (Entry<File, SVNUrl> e : m.entrySet()) {
            SVNUrl url = e.getValue();
            if(url != null) {
                String rootPath = SvnUtils.decodeToString(SVNUrlUtils.getRelativePath(rootUrl, url, true));
                if (rootPath == null) {
                    LOG.log(Level.FINE, "populatePathToRoot: rootUrl: {0}, url: {1}, probably svn:externals", new String[] {rootUrl.toString(), url.toString()});
                    continue;
                }
                String fileAbsPath = e.getKey().getAbsolutePath().replace(File.separatorChar, '/');
                int commonPathLength = getCommonPostfixLength(rootPath, fileAbsPath);
                pathToRoot.put(rootPath.substring(0, rootPath.length() - commonPathLength),
                               new File(fileAbsPath.substring(0, fileAbsPath.length() - commonPathLength)));

            }
        }
    }

    private int getCommonPostfixLength(String a, String b) {
        int ai = a.length() - 1;
        int bi = b.length() - 1;
        int slash = -1;
        for (;;) {
            if (ai < 0 || bi < 0) break;
            char ca = a.charAt(ai);
            char cb = b.charAt(bi);
            if(ca == '/') slash = ai;
            if ( ca != cb ) {
                if(slash > -1) {
                    return a.length() - slash;
                }
                break;
            }
            ai--; bi--;
        }
        return a.length() - ai - 1;
    }

    @Override
    public void perform () {
        populatePathToRoot();

        if (fromRevision == null || toRevision == null) {
            // guess a sync problem, search criteria changed while populatePathToRoot was running
            LOG.log(Level.WARNING, "wrong revision: [{0}:{1}] - [{2}:{3}]", new Object[] { fromRevision, criteria.tfFrom.getText(), toRevision, criteria.tfTo.getText() }); //NOI18N
            return;
        }
        completedSearches = 0;
        if (searchingUrl()) {
            RequestProcessor rp = Subversion.getInstance().getRequestProcessor(master.getRepositoryUrl());
            currentSearch = new SvnProgressSupport() {
                @Override
                public void perform() {
                    search(master.getRepositoryUrl(), null, fromRevision, toRevision, this, false, limit);
                    checkFinished();
                }
            };
            currentSearch.start(rp, master.getRepositoryUrl(), NbBundle.getMessage(SearchExecutor.class, "MSG_Search_Progress")).waitFinished(); // NOI18N
        } else {
            for (Iterator i = workFiles.keySet().iterator(); i.hasNext();) {
                final SVNUrl rootUrl = (SVNUrl) i.next();
                final Set<File> files = workFiles.get(rootUrl);
                RequestProcessor rp = Subversion.getInstance().getRequestProcessor(rootUrl);
                currentSearch = new SvnProgressSupport() {
                    @Override
                    public void perform() {
                        search(rootUrl, files, fromRevision, toRevision, this, false, limit);
                        checkFinished();
                    }
                };
                currentSearch.start(rp, rootUrl, NbBundle.getMessage(SearchExecutor.class, "MSG_Search_Progress")).waitFinished(); // NOI18N
                if (isCanceled() || currentSearch.isCanceled()) {
                    cancel();
                    break;
                }
            }
        }
    }

    private void search(SVNUrl rootUrl, Set<File> files, SVNRevision fromRevision, SVNRevision toRevision, SvnProgressSupport progressSupport, boolean fetchDetailsPaths, int limit) {
        SvnClient client;
        try {
            client = Subversion.getInstance().getClient(rootUrl, progressSupport);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }
        if (progressSupport.isCanceled()) {
            searchCanceled = true;
            return;
        }
        if (searchingUrl()) {
            try {
                ISVNLogMessage [] messages = client.getLogMessages(rootUrl, null, toRevision, fromRevision, false, fetchDetailsPaths, limit);
                appendResults(rootUrl, messages, null);
            } catch (SVNClientException e) {
                if(!SvnClientExceptionHandler.handleLogException(rootUrl, toRevision, e)) {
                    progressSupport.annotate(e);
                }
            }
        } else {
            String [] paths = new String[files.size()];
            int idx = 0;
            Map<String, SVNRevision> revisions = new HashMap<>();
            try {
                for (File file : files) {
                    ISVNInfo info = client.getInfoFromWorkingCopy(file);
                    String p = SvnUtils.getRelativePath(file);
                    if(p != null && p.startsWith("/")) {
                        p = p.substring(1, p.length());
                    }
                    paths[idx++] = p;
                    if (info != null && info.getRevision() != null) {
                        revisions.put(p, info.getRevision());
                    }
                }
                ISVNLogMessage [] messages = SvnUtils.getLogMessages(client, rootUrl, paths, revisions, toRevision, fromRevision, false, fetchDetailsPaths, limit);
                appendResults(rootUrl, messages, revisions);
            } catch (SVNClientException e) {
                try {
                    // WORKAROUND issue #110034
                    // the client.getLogMessages(rootUrl, paths[] ... seems to touch also the repository root even if it's not
                    // listed in paths[]. This causes problems when the given user has restricted access only to a specific folder.
                    if(SvnClientExceptionHandler.isHTTP403(e.getMessage())) { // 403 forbidden
                        for(String path : paths) {
                            ISVNLogMessage [] messages = client.getLogMessages(rootUrl.appendPath(path), null, toRevision, fromRevision, false, fetchDetailsPaths, limit);
                            appendResults(rootUrl, messages, revisions);
                        }
                        return;
                    }
                } catch (SVNClientException ex) {
                    if(!SvnClientExceptionHandler.handleLogException(rootUrl, toRevision, e)) {
                        progressSupport.annotate(ex);
                    }
                }
                if(!SvnClientExceptionHandler.handleLogException(rootUrl, toRevision, e)) {
                    progressSupport.annotate(e);
                }
            }
        }
    }



    /**
     * Processes search results from a single repository.
     *
     * @param rootUrl repository root URL
     * @param logMessages events in chronological order
     */
    private synchronized void appendResults (SVNUrl rootUrl, ISVNLogMessage[] logMessages,
            Map<String, SVNRevision> pegRevisions) {
        // traverse in reverse chronological order
        for (int i = logMessages.length - 1; i >= 0; i--) {
            ISVNLogMessage logMessage = logMessages[i];
            if(logMessage == null) continue;
            RepositoryRevision rev = new RepositoryRevision(logMessage, rootUrl, master.getRoots(), pathToRoot, pegRevisions);
            results.add(rev);
        }
    }

    private boolean searchingUrl() {
        return master.getRepositoryUrl() != null;
    }

    private void checkFinished() {
        completedSearches++;
        if (searchingUrl() && completedSearches >= 1 || workFiles.size() == completedSearches) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    master.setResults(results, SearchHistoryPanel.createKenaiUsersMap(results), limit);
                }
            });
        }
    }

    void start() {
        start(Subversion.getInstance().getParallelRequestProcessor(), null, null);
    }

    @Override
    public synchronized boolean cancel() {
        if (currentSearch != null) {
            currentSearch.cancel();
        }
        return super.cancel();
    }

    @Override
    protected void finnishProgress () {

    }

    @Override
    protected void startProgress () {

    }

    @Override
    protected ProgressHandle getProgressHandle () {
        return null;
    }

    List<RepositoryRevision> search(SVNUrl repositoryUrl, int count, SvnProgressSupport supp) {
        results.clear();
        search(repositoryUrl, searchingUrl() ? null : new HashSet<File>(Arrays.asList(master.getRoots())),
                fromRevision, toRevision, supp, false, count);
        return new ArrayList<RepositoryRevision>(results);
    }
}

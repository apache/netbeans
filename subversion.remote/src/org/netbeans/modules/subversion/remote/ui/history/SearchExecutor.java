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
package org.netbeans.modules.subversion.remote.ui.history;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import javax.swing.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.ISVNInfo;
import org.netbeans.modules.subversion.remote.api.ISVNLogMessage;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.api.SVNUrlUtils;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.client.SvnProgressSupport;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;


/**
 * Executes searches in Search History panel.
 *
 * 
 */
class SearchExecutor extends SvnProgressSupport {

    static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");  // NOI18N

    static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");  // NOI18N
    static final DateFormat [] dateFormats = new DateFormat[] {
        fullDateFormat,
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),  // NOI18N
        simpleDateFormat,
        new SimpleDateFormat("yyyy-MM-dd"), // NOI18N
    };

    private static final Logger LOG = Logger.getLogger(SearchExecutor.class.getName());
    private final SearchHistoryPanel    master;
    private Map<SVNUrl, Set<VCSFileProxy>>      workFiles;
    private Map<String,VCSFileProxy>            pathToRoot;
    private final SearchCriteriaPanel   criteria;

    private int                         completedSearches;
    private boolean                     searchCanceled;
    private final List<RepositoryRevision> results = new ArrayList<>();
    static final int DEFAULT_LIMIT = 10;
    private final SVNRevision fromRevision;
    private final SVNRevision toRevision;
    private final int limit;
    private SvnProgressSupport currentSearch;
    private final FileSystem fileSystem;

    public SearchExecutor(FileSystem fileSystem, SearchHistoryPanel master) {
        super(fileSystem);
        this.master = master;
        this.fileSystem = fileSystem;
        criteria = master.getCriteria();
        fromRevision = criteria.getFrom();
        toRevision = criteria.getTo();
        limit = searchingUrl() || master.getRoots().length == 1 ? DEFAULT_LIMIT : 0;
    }

    private void populatePathToRoot() {
        pathToRoot = new HashMap<>();
        try {
            if (searchingUrl()) {
                String rootPath = SvnUtils.getRepositoryPath(master.getRoots()[0]);
                pathToRoot.put(rootPath, master.getRoots()[0]);
            } else {
                workFiles = new HashMap<>();
                for (VCSFileProxy file : master.getRoots()) {
                    SVNUrl rootUrl = SvnUtils.getRepositoryRootUrl(file);
                    populatePathToRoot(file, rootUrl);
                    Set<VCSFileProxy> set = workFiles.get(rootUrl);
                    if (set == null) {
                        set = new HashSet<>(2);
                        workFiles.put(rootUrl, set);
                    }
                    set.add(file);
                }
            }
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(new Context(master.getRoots()), ex, true, true);
        }
    }

    private void populatePathToRoot(VCSFileProxy file, SVNUrl rootUrl) throws SVNClientException {
        Map<VCSFileProxy, SVNUrl> m = SvnUtils.getRepositoryUrls(file);
        for (Entry<VCSFileProxy, SVNUrl> e : m.entrySet()) {
            SVNUrl url = e.getValue();
            if(url != null) {
                String rootPath = SvnUtils.decodeToString(SVNUrlUtils.getRelativePath(rootUrl, url, true));
                if (rootPath == null) {
                    LOG.log(Level.FINE, "populatePathToRoot: rootUrl: {0}, url: {1}, probably svn:externals", new String[] {rootUrl.toString(), url.toString()});
                    continue;
                }
                String fileAbsPath = e.getKey().getPath();
                int commonPathLength = getCommonPostfixLength(rootPath, fileAbsPath);
                pathToRoot.put(rootPath.substring(0, rootPath.length() - commonPathLength),
                               VCSFileProxySupport.getResource(file, fileAbsPath.substring(0, fileAbsPath.length() - commonPathLength)));

            }
        }
    }

    private int getCommonPostfixLength(String a, String b) {
        int ai = a.length() - 1;
        int bi = b.length() - 1;
        int slash = -1;
        for (;;) {
            if (ai < 0 || bi < 0) {
                break;
            }
            char ca = a.charAt(ai);
            char cb = b.charAt(bi);
            if(ca == '/') {
                slash = ai;
            }
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
            currentSearch = new SvnProgressSupport(fileSystem) {
                @Override
                public void perform() {
                    search(master.getRepositoryUrl(), null, fromRevision, toRevision, this, false, limit);
                    checkFinished();
                }
            };
            currentSearch.start(rp, master.getRepositoryUrl(), NbBundle.getMessage(SearchExecutor.class, "MSG_Search_Progress")).waitFinished(); // NOI18N
        } else {
            for (Iterator<SVNUrl> i = workFiles.keySet().iterator(); i.hasNext();) {
                final SVNUrl rootUrl = i.next();
                final Set<VCSFileProxy> files = workFiles.get(rootUrl);
                RequestProcessor rp = Subversion.getInstance().getRequestProcessor(rootUrl);
                currentSearch = new SvnProgressSupport(fileSystem) {
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

    private void search(SVNUrl rootUrl, Set<VCSFileProxy> files, SVNRevision fromRevision, SVNRevision toRevision, SvnProgressSupport progressSupport, boolean fetchDetailsPaths, int limit) {
        SvnClient client;
        Context context;
        if (files != null) {
            context = new Context(files.toArray(new VCSFileProxy[files.size()]));
        } else {
            context = new Context(VCSFileProxy.createFileProxy(fileSystem.getRoot()));
        }
        try {
            client = Subversion.getInstance().getClient(context, rootUrl, progressSupport);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(context, ex, true, true);
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
                for (VCSFileProxy file : files) {
                    ISVNInfo info = client.getInfoFromWorkingCopy(file);
                    String p = SvnUtils.getRelativePath(file);
                    if(p != null && p.startsWith("/")) { //NOI18N
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
            if(logMessage == null) {
                continue;
            }
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
                    master.setResults(results,  limit);
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
        search(repositoryUrl, searchingUrl() ? null : new HashSet<>(Arrays.asList(master.getRoots())),
                fromRevision, toRevision, supp, false, count);
        return new ArrayList<>(results);
    }
}

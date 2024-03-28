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
package org.netbeans.modules.mercurial.ui.log;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.branch.HgBranch;
import org.netbeans.modules.mercurial.ui.log.RepositoryRevision.Kind;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Executes searches in Search History panel.
 * 
 * @author Maros Sandor
 */
class SearchExecutor extends HgProgressSupport {

    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");  // NOI18N
    
    static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");  // NOI18N
    static final DateFormat [] dateFormats = new DateFormat[] {
        fullDateFormat,
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),  // NOI18N
        simpleDateFormat,
        new SimpleDateFormat("yyyy-MM-dd"), // NOI18N
    };
    
    private final SearchHistoryPanel    master;
    private final File                  root;
    private final Set<File>             files;
    private Map<String,File>            pathToRoot;
    
    private final String fromRevision;
    private final String toRevision;
    private int limitRevisions;
    private final String branchName;
    static final int DEFAULT_LIMIT = 10;
    static final int UNLIMITTED = -1;
    private final boolean includeMerges;
    private HgBranch[] branches;

    public SearchExecutor(SearchHistoryPanel master) {
        this.master = master;
        SearchCriteriaPanel criteria = master.getCriteria();
        fromRevision = criteria.getFrom();
        toRevision = criteria.getTo();
        includeMerges = criteria.isIncludeMerges();
        limitRevisions = criteria.getLimit();
        if (limitRevisions <= 0) {
            limitRevisions = UNLIMITTED;
        }
        branchName = criteria.getBranch();
        
        pathToRoot = new HashMap<String, File>(); 
        File rootFile = Mercurial.getInstance().getRepositoryRoot(master.getRoots()[0]);
        if (rootFile == null) {
            rootFile = master.getRoots()[0];
        }
        root = rootFile;
        files = new HashSet<File>(Arrays.asList(master.getRoots()));

    }    
        
    @Override
    public void perform() {
        OutputLogger logger = getLogger();
        try {
            this.branches = HgCommand.getBranches(root, OutputLogger.getLogger(null));
        } catch (HgException ex) {
            this.branches = new HgBranch[0];
            Mercurial.LOG.log(ex instanceof HgException.HgCommandCanceledException
                    ? Level.FINE
                    : Level.INFO, null, ex);
        }
        List<RepositoryRevision> results = search(fromRevision, toRevision, limitRevisions, branchName, this, logger);
        if (!isCanceled()) {
            checkFinished(results);
        }
    }

    public void start () {
        if (!HgBranch.DEFAULT_NAME.equals(branchName)) {
            // only for branches other than default
            HgModuleConfig.getDefault().setSearchOnBranchEnabled(master.getCurrentBranch(), !branchName.isEmpty());
        }

        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        start(rp, root, NbBundle.getMessage(SearchExecutor.class, "MSG_Search_Progress")); //NOI18N
    }

    private List<RepositoryRevision> search (String fromRevision, String toRevision, int limitRevisions, String branchName, HgProgressSupport progressSupport, OutputLogger logger) {
        if (progressSupport.isCanceled()) {
            return Collections.<RepositoryRevision>emptyList();
        }
        
        HgLogMessage[] messages = new HgLogMessage[0];
        try {
            if (master.isIncomingSearch()) {
                messages = HgCommand.getIncomingMessages(root, toRevision, branchName.isEmpty() ? null : branchName,
                        includeMerges, false, includeMerges, limitRevisions, logger);
            } else if (master.isOutSearch()) {
                messages = HgCommand.getOutMessages(root, toRevision, branchName.isEmpty() ? null : branchName,
                        includeMerges, includeMerges, limitRevisions, logger);
            } else {
                List<String> branchNames = branchName.isEmpty() ? Collections.<String>emptyList() : Collections.singletonList(branchName);
                messages = HgCommand.getLogMessages(root, files, fromRevision, toRevision, includeMerges, false, includeMerges, limitRevisions, branchNames, logger, true);
            }
        } catch (HgException.HgCommandCanceledException ex) {
            // do not take any action
        } catch (HgException ex) {
            HgUtils.notifyException(ex);
        }
        return appendResults(root, messages);
    }
  
    
    /**
     * Processes search results from a single repository. 
     * 
     * @param root repository root
     * @param logMessages events in chronological order
     */ 
    private List<RepositoryRevision> appendResults(File root, HgLogMessage[] logMessages) {
        List<RepositoryRevision> results = new ArrayList<RepositoryRevision>();
        // traverse in reverse chronological order
        for (int i = logMessages.length - 1; i >= 0; i--) {
            HgLogMessage logMessage = logMessages[i];
            RepositoryRevision rev = new RepositoryRevision(logMessage, root, getCurrentRevisionKind(), master.getRoots(), getBranches(logMessage));
            results.add(rev);
        }
        return results;
    }
    
    private Kind getCurrentRevisionKind () {
        if (master.isIncomingSearch()) {
            return Kind.INCOMING;
        } else if (master.isOutSearch()) {
            return Kind.OUTGOING;
        } else {
            return Kind.LOCAL;
        }
    }

    private void checkFinished(final List<RepositoryRevision> results) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if(results.isEmpty()) {
                    master.setResults(null, -1);
                } else {
                    master.setResults(results, limitRevisions);
                }

            }
        });
    }

    List<RepositoryRevision> search (int count, HgProgressSupport supp) {
        return search(fromRevision, toRevision, count, branchName, supp, supp.getLogger());
    }

    /**
     * Returns set of branches the given log message is head of
     * @param logMessage
     * @return 
     */
    private Set<String> getBranches (HgLogMessage logMessage) {
        Set<String> headOfBranches = new HashSet<String>(2);
        for (HgBranch b : branches) {
            if (b.getRevisionInfo().getCSetShortID().equals(logMessage.getCSetShortID())) {
                headOfBranches.add(b.getName());
            }
        }
        return headOfBranches;
    }
  
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.bugtracking.ui.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.AbstractListModel;
import javax.swing.Timer;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.openide.util.Mutex;

/**
 * Model of search results. Works as ListModel for JList which is displaying
 * results. Actual results data are stored in List of CategoryResult objects.
 *
 * As model changes can come very frequently, firing of changes is coalesced.
 * Coalescing of changes helps UI to reduce flicker and unnecessary updates.
 *
 * @author Jan Becicka
 */
public final class ResultsModel extends AbstractListModel implements ActionListener {

    private static ResultsModel instance;
    private List<PopupItem> results;

    /* Timer for coalescing fast coming changes of model */
    private Timer fireTimer;

    /** Amount of time during which model has to be unchanged in order to fire
     * changes to listeners. */
    static final int COALESCE_TIME = 200;

    private Map<RepositoryImpl, Set<IssueImpl>> issuesCached = new HashMap<RepositoryImpl, Set<IssueImpl>>();

    /** Singleton */
    private ResultsModel () {
    }

    public static ResultsModel getInstance () {
        if (instance == null) {
            instance = new ResultsModel();
        }
        return instance;
    }

    void setContent (final List<PopupItem> results) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                ResultsModel.this.results = results;
                maybeFireChanges();
            }
        });
    }

    synchronized void cacheIssues(RepositoryImpl repo, Collection<IssueImpl> issues) {
        HashSet<IssueImpl> s = new HashSet<IssueImpl>();
        for (IssueImpl issue : issues) {
            assert issue != null;
            s.add(issue);
        }
        issuesCached.put(repo, s);
    }

    synchronized Collection<IssueImpl> getCachedIssues(RepositoryImpl repo) {
        if(issuesCached != null) {
            Set<IssueImpl> s = issuesCached.get(repo);
            if(s != null) {
                return s;
            }
        }
        return Collections.emptyList();
    }
    /******* AbstractListModel impl ********/

    @Override
    public int getSize() {
        if (results == null) {
            return 0;
        }
        return results.size();
    }

    @Override
    public Object getElementAt (int index) {
        if (results == null) {
            return null;
        }
        return results.get(index);
    }

    private void maybeFireChanges () {
        if (fireTimer == null) {
            fireTimer = new Timer(COALESCE_TIME, this);
        }
        if (!fireTimer.isRunning()) {
            // first change in possible flurry, start timer
            fireTimer.start();
        } else {
            // model change came too fast, let's wait until providers calm down :)
            fireTimer.restart();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        fireTimer.stop();
        fireContentsChanged(this, 0, getSize());
    }
}

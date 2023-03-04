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

package org.netbeans.modules.bugtracking.issuetable;

import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public abstract class Filter {

    private static AllFilter allFilter;
    private static NotSeenFilter notSeenFilter;
    private static NewFilter newFilter;

    public abstract String getDisplayName();
    public abstract boolean accept(IssueNode issue);

    static synchronized Filter getAllFilter() {
        if(allFilter == null) {
            allFilter = new AllFilter();
        }
        return allFilter;
    }
    
    static Filter getNotSeenFilter() {
        if(notSeenFilter == null) {
            notSeenFilter = new NotSeenFilter();
        }
        return notSeenFilter;
    }
    
    static Filter getNewFilter() {
        if(newFilter == null) {
            newFilter = new NewFilter();
        }
        return newFilter;
    }

    private static class AllFilter extends Filter {
        AllFilter() { }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Filter.class, "LBL_AllIssuesFilter");     // NOI18N
        }
        @Override
        public boolean accept(IssueNode node) {
            return true;
        }
    }
    private static class NotSeenFilter extends Filter {
        NotSeenFilter() { }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Filter.class, "LBL_UnseenIssuesFilter");  // NOI18N
        }
        @Override
        public boolean accept(IssueNode node) {
            return node.getStatus() != IssueStatusProvider.Status.SEEN;
        }
    }
    private static class NewFilter extends Filter {
        NewFilter() { }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Filter.class, "LBL_NewIssuesFilter");     // NOI18N
        }
        @Override
        public boolean accept(IssueNode node) {
            return node.getStatus() == IssueStatusProvider.Status.INCOMING_NEW;
        }
    }
    
}

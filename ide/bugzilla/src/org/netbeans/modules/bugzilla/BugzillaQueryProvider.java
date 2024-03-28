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
package org.netbeans.modules.bugzilla;

import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaQueryProvider implements QueryProvider<BugzillaQuery, BugzillaIssue> {

    @Override
    public String getDisplayName(BugzillaQuery query) {
        return query.getDisplayName();
    }

    @Override
    public String getTooltip(BugzillaQuery query) {
        return query.getTooltip();
    }

    @Override
    public QueryController getController(BugzillaQuery query) {
        return query.getController();
    }

    @Override
    public boolean canRemove(BugzillaQuery q) {
        return q.canRemove();
    }
    
    @Override
    public void remove(BugzillaQuery q) {
        q.remove();
    }
    
    @Override
    public boolean canRename(BugzillaQuery q) {
        return true;
    }

    @Override
    public void rename(BugzillaQuery q, String displayName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void setIssueContainer(BugzillaQuery query, IssueContainer<BugzillaIssue> c) {
        query.getController().setContainer(c);
    }
    
    @Override
    public void refresh(BugzillaQuery query) {
        query.getController().refresh(true);
    }

}

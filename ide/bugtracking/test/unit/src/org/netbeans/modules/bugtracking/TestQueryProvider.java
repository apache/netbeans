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
package org.netbeans.modules.bugtracking;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.netbeans.modules.bugtracking.api.APITestIssue;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.openide.nodes.Node;

/**
 *
 * @author tomas
 */
public class TestQueryProvider implements QueryProvider<TestQuery, TestIssue> {

    @Override
    public String getDisplayName(TestQuery q) {
        return q.getDisplayName();
    }

    @Override
    public String getTooltip(TestQuery q) {
        return q.getTooltip();
    }

    @Override
    public QueryController getController(TestQuery q) {
        return q.getController();
    }

    @Override
    public void remove(TestQuery q) {
        q.remove();
    }
    
    @Override
    public void refresh(TestQuery q) {
        q.refresh();
    }

    @Override
    public boolean canRename(TestQuery q) {
        return q.canRename();
    }

    @Override
    public void rename(TestQuery q, String displayName) {
        q.rename(displayName);
    }

    @Override
    public boolean canRemove(TestQuery q) {
        return q.canRemove();
    }

    @Override
    public void setIssueContainer(TestQuery q, IssueContainer<TestIssue> c) {
        q.setIssueContainer(c);
    }

}

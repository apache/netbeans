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

package org.netbeans.modules.localtasks;

import org.netbeans.modules.localtasks.task.LocalTask;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;

/**
 *
 * @author Ondrej Vrabec
 */
public class QueryProviderImpl implements QueryProvider<LocalQuery, LocalTask> {

    @Override
    public String getDisplayName (LocalQuery q) {
        return q.getDisplayName();
    }

    @Override
    public String getTooltip (LocalQuery q) {
        return q.getTooltip();
    }

    @Override
    public QueryController getController (LocalQuery q) {
        return null;
    }

    @Override
    public boolean canRemove(LocalQuery q) {
        return false;
    }
    
    @Override
    public void remove (LocalQuery q) {
        // NO OP
    }

    @Override
    public boolean canRename(LocalQuery q) {
        return false;
    }

    @Override
    public void rename(LocalQuery q, String displayName) {
        // NO OP
    }
    
    @Override
    public void refresh (LocalQuery query) {
        query.refresh();
    }

    @Override
    public void setIssueContainer(LocalQuery query, IssueContainer<LocalTask> c) {
        query.setIssueContainer(c);
    }

}

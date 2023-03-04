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
package org.netbeans.modules.bugtracking.api;

import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;

/**
 *
 * @author Tomas Stupka
 */
class APIAccessorImpl extends APIAccessor {

    static void createAccesor() {
        if (IMPL == null) {
            IMPL = new APIAccessorImpl();
        }
    }

    @Override
    public Repository createRepository(RepositoryImpl impl) {
        return impl != null ? new Repository(impl) : null;
    }

    @Override
    public Query createQuery(QueryImpl impl) {
        return impl != null ? new Query(impl) : null;
    }

    @Override
    public Issue createIssue(IssueImpl impl) {
        return impl != null ? new Issue(impl) : null;
    }

    @Override
    public RepositoryImpl getImpl(Repository repository) {
        return repository != null ? repository.getImpl() : null;
    }

    @Override
    public QueryImpl getImpl(Query query) {
        return query != null ? query.getImpl() : null;
    }

    @Override
    public IssueImpl getImpl(Issue issue) {
        return issue != null ? issue.getImpl() : null;
    }
}

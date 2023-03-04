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

import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;

/**
 *
 * @author tomas
 */
public class TestKit {
    public static RepositoryImpl getRepository(String repoId) {
        return new RepositoryImpl(
                new DefaultTestRepository(repoId), 
                new TestRepositoryProvider(), 
                new TestQueryProvider(),
                new TestIssueProvider(),
                new TestStatusProvider(),
                null, null, null);
    }
    
    public static RepositoryImpl getRepository(TestRepository repo) {
        return new RepositoryImpl(
                repo, 
                new TestRepositoryProvider(), 
                new TestQueryProvider(),
                new TestIssueProvider(),
                new TestStatusProvider(),
                null, null, null);
    }
    
    public static IssueImpl getIssue(RepositoryImpl repo, TestIssue issue) {
        return repo.getIssue(issue);
    }
    
    public static IssueImpl getIssue(Repository repo, TestIssue issue) {
        return APIAccessor.IMPL.getImpl(repo).getIssue(issue);
    }

    public static QueryImpl getQuery(RepositoryImpl repo, TestQuery query) {
        return repo.getQuery(query);
    }

    public static void openQuery(Query query) {
        QueryAction.openQuery(APIAccessor.IMPL.getImpl(query), null, QueryController.QueryMode.EDIT);
    }
    
    private static class DefaultTestRepository extends TestRepository {
        private RepositoryInfo info;

        public DefaultTestRepository(RepositoryInfo info) {
            this.info = info;
        }

        public DefaultTestRepository(String id) {
            this(id, "dafaultestconnector");
        }
        
        public DefaultTestRepository(String id, String cid) {
            this.info = new RepositoryInfo(id, cid, "http://test", null, null, null, null, null, null);
        }
        
        @Override
        public RepositoryInfo getInfo() {
            return info;
        }
    }
}

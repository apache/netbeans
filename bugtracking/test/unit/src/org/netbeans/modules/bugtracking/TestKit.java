/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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

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

package org.netbeans.modules.maven.indexer.api;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.indexer.api.AbstractTestQueryProvider.TestIndexer1;
import org.netbeans.modules.maven.indexer.api.AbstractTestQueryProvider.TestIndexer2;
import org.netbeans.modules.maven.indexer.spi.ArchetypeQueries;
import org.openide.util.Exceptions;

public class RepositoryQueriesTest extends NbTestCase {

    public RepositoryQueriesTest(String name) {
        super(name);
    }
    
    @Override 
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testAlternativeResult() throws URISyntaxException {
        MockServices.setServices(TestIndexer1.class);
        
        RepositoryQueries.Result<NBVersionInfo> result = RepositoryQueries.findArchetypesResult(Arrays.asList(TestIndexer1.REPO));
        assertEquals(TestIndexer1.ID, result.getResults().get(0).getArtifactId());
        assertEquals(1, result.getTotalResultCount());
        assertEquals(1, result.getReturnedResultCount());
    }
    
    public void testTwoReposTwoQueryProviders() throws URISyntaxException {
        MockServices.setServices(TestIndexer1.class, TestIndexer2.class);

        RepositoryQueries.Result<NBVersionInfo> result = RepositoryQueries.findArchetypesResult(List.of(TestIndexer1.REPO, TestIndexer2.REPO));
        assertEquals(2, result.getTotalResultCount());
        assertEquals(2, result.getReturnedResultCount());
        assertArtefactIds(result.getResults(), new String[] {TestIndexer1.ID, TestIndexer2.ID});
    }
    
    public void testTwoReposOneAccepted() throws URISyntaxException {
        MockServices.setServices(TestIndexer1.class);
        
        RepositoryQueries.Result<NBVersionInfo> result = RepositoryQueries.findArchetypesResult(List.of(TestIndexer1.REPO, TestIndexer2.REPO));
        assertEquals(1, result.getTotalResultCount());
        assertEquals(1, result.getReturnedResultCount());
        assertArtefactIds(result.getResults(), new String[] {TestIndexer1.ID});
    }
    
    public void testNullResult() throws URISyntaxException {
        MockServices.setServices(NullQueryProvider.class);
        
        List<RepositoryInfo> repos = Arrays.asList(NullQueryProvider.REPO);
        assertEquals(0, RepositoryQueries.findArchetypesResult(repos).getTotalResultCount());
        assertEquals(0, RepositoryQueries.findBySHA1Result(new File(""), Arrays.asList(NullQueryProvider.REPO)).getTotalResultCount());
        assertEquals(0, RepositoryQueries.findClassUsagesResult("", repos).getTotalResultCount());
        assertEquals(0, RepositoryQueries.findDependencyUsageResult("","","", repos).getTotalResultCount());
        assertEquals(0, RepositoryQueries.findResult(Collections.emptyList(), repos).getTotalResultCount());
        assertEquals(0, RepositoryQueries.findVersionsByClassResult("", repos).getTotalResultCount());
        
        RepositoryPreferences.getInstance().addOrModifyRepositoryInfo(NullQueryProvider.REPO);
        assertEquals(0, RepositoryQueries.getLoadedContexts().size());
    }

    private void assertArtefactIds(List<NBVersionInfo> infos, String[] ids) {
        assertEquals(ids.length, infos.size());
        List<String> returnedIds = new ArrayList<>(infos.size());
        infos.forEach((info) -> returnedIds.add(info.getArtifactId()));
        for (String id : ids) {
            assertTrue(returnedIds.contains(id));
        }
    }

    public static class NullQueryProvider extends AbstractTestQueryProvider {
        static final String ID = "nullrepo";
        static RepositoryInfo REPO;
        static {
            try {
                REPO = new RepositoryInfo(ID, ID, null, "http://test1");
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public NullQueryProvider() {
            this.repos = new RepositoryInfo[] {REPO};
        }
        @Override
        protected String getID() {
            return ID;
        }
        @Override
        public ArchetypeQueries getArchetypeQueries() {
            return null;
        }        
    }

}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
    
    public void testAlternativeQueryProvider() throws URISyntaxException {
        
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

        RepositoryQueries.Result<NBVersionInfo> result = RepositoryQueries.findArchetypesResult(Arrays.asList(TestIndexer1.REPO, TestIndexer2.REPO));
        assertEquals(2, result.getTotalResultCount());
        assertEquals(2, result.getReturnedResultCount());
        assertArtefactIds(result.getResults(), new String[] {TestIndexer1.ID, TestIndexer2.ID});
    }
    
    public void testTwoReposOneAccepted() throws URISyntaxException {
        MockServices.setServices(TestIndexer1.class);
        
        RepositoryQueries.Result<NBVersionInfo> result = RepositoryQueries.findArchetypesResult(Arrays.asList(TestIndexer1.REPO, TestIndexer2.REPO));
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
        assertEquals(0, RepositoryQueries.findResult(Collections.EMPTY_LIST, repos).getTotalResultCount());
        assertEquals(0, RepositoryQueries.findVersionsByClassResult("", repos).getTotalResultCount());
        
        RepositoryPreferences.getInstance().addOrModifyRepositoryInfo(NullQueryProvider.REPO);
        assertEquals(0, RepositoryQueries.getLoadedContexts().size());
    }

    private void assertArtefactIds(List<NBVersionInfo> infos, String[] ids) {
        assertEquals(ids.length, infos.size());
        List<String> returnedIds = new ArrayList<>(infos.size());
        infos.stream().forEach((info) -> returnedIds.add(info.getArtifactId()));
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
        protected String getID() {
            return ID;
        }
        @Override
        public ArchetypeQueries getArchetypeQueries() {
            return null;
        }        
    }

}
